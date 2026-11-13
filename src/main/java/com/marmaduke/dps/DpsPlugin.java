package com.marmaduke.dps;

import com.google.gson.Gson;
import com.google.inject.Provides;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Hitsplat;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.SpotanimID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.OverlayMenuClicked;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.specialcounter.SpecialWeapon;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
	name = "Check DPS",
	description = "Pin a live DPS estimate on an NPC you choose or are attacking",
	tags = {"dps", "combat", "npc"},
	enabledByDefault = true
)
public class DpsPlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(DpsPlugin.class);

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ItemManager itemManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DpsOverlay overlay;

	@Inject
	private DpsTooltipOverlay tooltipOverlay;

	@Inject
	private DpsConfig config;

	@Inject
	private Gson gson;

	private NpcStatsIndex npcStats = NpcStatsIndex.empty();
	private final Map<Integer, DefenceModel> defenceByIndex = new HashMap<Integer, DefenceModel>();
	static final int REACTIVE_HOLD_TICKS = 100;

	private Integer targetIndex;
	private int reactiveIdleTicks;
	private DpsEstimate estimate;
	private CombatInput currentInput;
	private EnumSet<Prayer> currentPrayers = EnumSet.noneOf(Prayer.class);
	private PendingSpec pendingSpec;
	private int specialPercentage = -1;
	private int lastHpChangeCycle;
	private int lastHitpointsExperience = -1;

	@Provides
	DpsConfig provideConfig(net.runelite.client.config.ConfigManager configManager)
	{
		return configManager.getConfig(DpsConfig.class);
	}

	@Override
	protected void startUp()
	{
		npcStats = NpcStatsIndex.load(gson, DpsPlugin.class.getResourceAsStream("/npc-stats.json"));
		log.debug("Loaded {} NPC combat stat rows", npcStats.size());
		overlay.setPosition(config.position());
		overlayManager.add(overlay);
		overlayManager.add(tooltipOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlayManager.remove(tooltipOverlay);
		clearTarget();
		defenceByIndex.clear();
		pendingSpec = null;
		specialPercentage = -1;
	}

	DpsEstimate getEstimate()
	{
		return estimate;
	}

	CombatInput getCurrentInput()
	{
		return currentInput;
	}

	EnumSet<Prayer> getCurrentPrayers()
	{
		return currentPrayers;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!"checkdps".equals(event.getGroup()))
		{
			return;
		}
		overlay.setPosition(config.position());
		if ("reactiveMode".equals(event.getKey()))
		{
			if (config.reactiveMode())
			{
				followAttackTarget(false);
			}
			else
			{
				clearTarget();
			}
		}
		recompute();
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (config.reactiveMode() || !"Attack".equals(event.getOption()))
		{
			return;
		}
		NPC npc = event.getMenuEntry().getNpc();
		if (npc == null)
		{
			return;
		}
		final int index = npc.getIndex();
		client.createMenuEntry(-1)
			.setOption("Check DPS")
			.setTarget(event.getTarget())
			.setType(MenuAction.RUNELITE)
			.onClick(entry -> selectTarget(index));
		if (targetIndex != null && targetIndex == index)
		{
			client.createMenuEntry(-1)
				.setOption("Clear DPS")
				.setTarget(event.getTarget())
				.setType(MenuAction.RUNELITE)
				.onClick(entry -> clearTarget());
		}
	}

	@Subscribe
	public void onOverlayMenuClicked(OverlayMenuClicked event)
	{
		if (!config.reactiveMode() && event.getOverlay() == overlay && "Clear".equals(event.getEntry().getOption()))
		{
			clearTarget();
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		if (!config.reactiveMode() || event.getSource() != client.getLocalPlayer())
		{
			return;
		}
		followAttackTarget(false);
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		for (DefenceModel model : defenceByIndex.values())
		{
			model.tick();
		}
		boolean tome = wearingChargedTomeOfWater();
		for (NPC npc : client.getNpcs())
		{
			if (npc.hasSpotAnim(SpotanimID.VULNERABILITY_IMPACT))
			{
				DefenceModel model = modelFor(npc);
				if (model != null)
				{
					model.applyVulnerability(tome ? 15 : 10);
				}
			}
		}
		resolvePendingSpec(client.getTickCount());
		if (config.reactiveMode())
		{
			followAttackTarget(true);
		}
		if (targetIndex != null)
		{
			NPC target = findNpc(targetIndex);
			if (target == null || target.isDead() || (target.getHealthScale() > 0 && target.getHealthRatio() == 0))
			{
				clearTarget();
				return;
			}
		}
		recompute();
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		int index = event.getNpc().getIndex();
		defenceByIndex.remove(index);
		if (targetIndex != null && targetIndex == index)
		{
			clearTarget();
		}
		if (pendingSpec != null && pendingSpec.npcIndex == index)
		{
			pendingSpec = null;
		}
	}

	@Subscribe
	public void onNpcChanged(NpcChanged event)
	{
		int index = event.getNpc().getIndex();
		defenceByIndex.remove(index);
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() != Skill.HITPOINTS)
		{
			return;
		}
		if (lastHitpointsExperience >= 0 && event.getXp() > lastHitpointsExperience)
		{
			lastHpChangeCycle = client.getGameCycle();
		}
		lastHitpointsExperience = event.getXp();
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarpId() != VarPlayerID.SA_ENERGY)
		{
			return;
		}
		int special = event.getValue();
		if (specialPercentage == -1 || special >= specialPercentage)
		{
			specialPercentage = special;
			return;
		}
		specialPercentage = special;
		final int serverTicks = client.getTickCount();
		clientThread.invokeLater(() -> armSpec(serverTicks));
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		if (pendingSpec == null || client.getTickCount() != pendingSpec.hitsplatTick)
		{
			return;
		}
		Hitsplat hitsplat = event.getHitsplat();
		Actor actor = event.getActor();
		if (!hitsplat.isMine() || !(actor instanceof NPC))
		{
			return;
		}
		if (((NPC) actor).getIndex() != pendingSpec.npcIndex)
		{
			return;
		}
		pendingSpec.hits.add(hitsplat.getAmount());
	}

	private void followAttackTarget(boolean advanceIdle)
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}
		Actor interacting = player.getInteracting();
		if (interacting instanceof NPC && isAttackTarget((NPC) interacting))
		{
			reactiveIdleTicks = 0;
			NPC npc = (NPC) interacting;
			if (targetIndex == null || targetIndex.intValue() != npc.getIndex())
			{
				selectTarget(npc.getIndex());
			}
			return;
		}
		if (!advanceIdle || targetIndex == null)
		{
			return;
		}
		reactiveIdleTicks++;
		if (reactiveHoldExpired(reactiveIdleTicks))
		{
			clearTarget();
		}
	}

	static boolean reactiveHoldExpired(int idleTicks)
	{
		return idleTicks >= REACTIVE_HOLD_TICKS;
	}

	private static boolean isAttackTarget(NPC npc)
	{
		if (npc.isDead() || (npc.getHealthScale() > 0 && npc.getHealthRatio() == 0))
		{
			return false;
		}
		NPCComposition composition = npc.getTransformedComposition();
		if (composition == null)
		{
			composition = npc.getComposition();
		}
		return composition != null && hasAttackAction(composition.getActions());
	}

	static boolean hasAttackAction(String[] actions)
	{
		if (actions == null)
		{
			return false;
		}
		for (int i = 0; i < actions.length; i++)
		{
			if ("Attack".equals(actions[i]))
			{
				return true;
			}
		}
		return false;
	}

	private void selectTarget(int index)
	{
		targetIndex = index;
		NPC npc = findNpc(index);
		if (npc != null)
		{
			modelFor(npc);
		}
		recompute();
	}

	private void clearTarget()
	{
		targetIndex = null;
		estimate = null;
		currentInput = null;
		currentPrayers = EnumSet.noneOf(Prayer.class);
		reactiveIdleTicks = 0;
	}

	private void recompute()
	{
		if (targetIndex == null || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		NPC npc = findNpc(targetIndex);
		if (npc == null)
		{
			return;
		}
		NpcCombatStats stats = npcStats.get(npc.getId());
		if (stats == null)
		{
			currentInput = null;
			currentPrayers = EnumSet.noneOf(Prayer.class);
			estimate = DpsEstimate.message(npc.getName(), "No combat stats for this NPC");
			return;
		}
		DefenceModel model = defenceByIndex.get(npc.getIndex());
		CombatInput input = PlayerGear.build(client, itemManager, config.onSlayerTask(), stats, model);
		String defenceLabel = model == null ? Integer.toString(stats.def) : model.overlayDefence();
		currentInput = input;
		currentPrayers = PrayerBonus.active(client);
		estimate = CombatMath.estimate(input, stats.displayName(), defenceLabel);
	}

	private void armSpec(int serverTicks)
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}
		SpecialWeapon weapon = equippedSpecWeapon();
		if (weapon == null || !tracks(weapon))
		{
			return;
		}
		Actor interacting = player.getInteracting();
		if (!(interacting instanceof NPC))
		{
			return;
		}
		NPC npc = (NPC) interacting;
		int distance = 1;
		if (player.getWorldLocation() != null && npc.getWorldLocation() != null)
		{
			distance = Math.max(1, player.getWorldLocation().distanceTo(npc.getWorldLocation()));
		}
		PendingSpec spec = new PendingSpec();
		spec.weapon = weapon;
		spec.npcIndex = npc.getIndex();
		spec.hitsplatTick = serverTicks + weapon.getHitDelay(distance);
		spec.hpChanged = lastHpChangeCycle == client.getGameCycle();
		pendingSpec = spec;
	}

	private void resolvePendingSpec(int tick)
	{
		if (pendingSpec == null)
		{
			return;
		}
		if (pendingSpec.weapon == SpecialWeapon.ELDER_MAUL)
		{
			applyArmedSpec(pendingSpec.hpChanged ? 1 : 0);
			return;
		}
		if (tick < pendingSpec.hitsplatTick)
		{
			return;
		}
		if (tick > pendingSpec.hitsplatTick)
		{
			pendingSpec = null;
			return;
		}
		if (pendingSpec.weapon == SpecialWeapon.TONALZTICS_OF_RALOS && pendingSpec.hits.size() < 2)
		{
			return;
		}
		if (pendingSpec.hits.isEmpty())
		{
			return;
		}
		applyArmedSpec(-1);
	}

	private void applyArmedSpec(int elderMaulHit)
	{
		PendingSpec spec = pendingSpec;
		pendingSpec = null;
		if (spec == null)
		{
			return;
		}
		NPC npc = findNpc(spec.npcIndex);
		if (npc == null)
		{
			return;
		}
		DefenceModel model = modelFor(npc);
		if (model == null)
		{
			return;
		}
		if (elderMaulHit >= 0)
		{
			spec.hits.clear();
			spec.hits.add(elderMaulHit);
		}
		applySpec(model, npc, spec);
	}

	private void applySpec(DefenceModel model, NPC npc, PendingSpec spec)
	{
		boolean tekton = npc.getName() != null && npc.getName().contains("Tekton");
		int hit = spec.hits.get(spec.hits.size() - 1);
		boolean delayed = spec.weapon.getHitDelay(1) > 1;
		if (delayed && !spec.hits.isEmpty())
		{
			hit = spec.hits.get(0);
		}
		switch (spec.weapon)
		{
			case DRAGON_WARHAMMER:
				if (hit > 0)
				{
					model.applyDragonWarhammer(false);
				}
				else if (tekton)
				{
					model.applyDragonWarhammer(true);
				}
				break;
			case ELDER_MAUL:
				if (hit > 0)
				{
					model.applyElderMaul(false);
				}
				else if (tekton)
				{
					model.applyElderMaul(true);
				}
				break;
			case BANDOS_GODSWORD:
				model.applyBandosGodsword(hit);
				break;
			case ACCURSED_SCEPTRE:
				if (hit > 0)
				{
					model.applyAccursedSceptre();
				}
				break;
			case ARCLIGHT:
				if (hit > 0)
				{
					model.applyArclight();
				}
				break;
			case DARKLIGHT:
				if (hit > 0)
				{
					model.applyDarklight();
				}
				break;
			case EMBERLIGHT:
				if (hit > 0)
				{
					model.applyEmberlight();
				}
				break;
			case TONALZTICS_OF_RALOS:
				int damaging = 0;
				for (int amount : spec.hits)
				{
					if (amount > 0)
					{
						damaging++;
					}
				}
				model.applyTonalztics(damaging);
				break;
			default:
				break;
		}
	}

	private boolean tracks(SpecialWeapon weapon)
	{
		switch (weapon)
		{
			case DRAGON_WARHAMMER:
			case ELDER_MAUL:
			case BANDOS_GODSWORD:
			case ACCURSED_SCEPTRE:
			case ARCLIGHT:
			case DARKLIGHT:
			case EMBERLIGHT:
			case TONALZTICS_OF_RALOS:
				return true;
			default:
				return false;
		}
	}

	private SpecialWeapon equippedSpecWeapon()
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null)
		{
			return null;
		}
		Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		if (weapon == null)
		{
			return null;
		}
		int raw = weapon.getId();
		int canonical = itemManager.canonicalize(raw);
		for (SpecialWeapon special : SpecialWeapon.values())
		{
			for (int id : special.getItemID())
			{
				if (id == raw || id == canonical)
				{
					return special;
				}
			}
		}
		return null;
	}

	private DefenceModel modelFor(NPC npc)
	{
		DefenceModel existing = defenceByIndex.get(npc.getIndex());
		if (existing != null)
		{
			return existing;
		}
		NpcCombatStats stats = npcStats.get(npc.getId());
		if (stats == null)
		{
			return null;
		}
		DefenceModel created = new DefenceModel(stats);
		defenceByIndex.put(npc.getIndex(), created);
		return created;
	}

	private NPC findNpc(int index)
	{
		for (NPC npc : client.getNpcs())
		{
			if (npc.getIndex() == index)
			{
				return npc;
			}
		}
		return null;
	}

	private boolean wearingChargedTomeOfWater()
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null)
		{
			return false;
		}
		Item shield = equipment.getItem(EquipmentInventorySlot.SHIELD.getSlotIdx());
		if (shield == null)
		{
			return false;
		}
		String name = itemManager.getItemComposition(shield.getId()).getName();
		if (name == null)
		{
			return false;
		}
		String lower = name.toLowerCase();
		return lower.contains("tome of water") && !lower.contains("uncharged") && !lower.contains("empty");
	}

	private static final class PendingSpec
	{
		private SpecialWeapon weapon;
		private int npcIndex;
		private int hitsplatTick;
		private boolean hpChanged;
		private final java.util.List<Integer> hits = new java.util.ArrayList<Integer>();
	}
}
