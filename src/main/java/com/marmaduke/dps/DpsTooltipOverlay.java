package com.marmaduke.dps;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.ItemComposition;
import net.runelite.api.MenuEntry;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;

/**
 * Hover tooltip for the prayer book, quick prayers, and combat potions.
 * The pinned DPS number is left unchanged.
 */
class DpsTooltipOverlay extends Overlay
{
	private static final Color GAIN = new Color(0, 180, 70);
	private static final Color LOSS = new Color(220, 60, 60);
	private static final Map<String, Prayer> PRAYERS = prayerNames();

	private final Client client;
	private final ItemManager itemManager;
	private final TooltipManager tooltipManager;
	private final DpsPlugin plugin;

	@Inject
	DpsTooltipOverlay(Client client, ItemManager itemManager, TooltipManager tooltipManager, DpsPlugin plugin)
	{
		this.client = client;
		this.itemManager = itemManager;
		this.tooltipManager = tooltipManager;
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (client.isMenuOpen())
		{
			return null;
		}
		CombatInput current = plugin.getCurrentInput();
		if (current == null)
		{
			return null;
		}
		MenuEntry[] menu = client.getMenuEntries();
		if (menu.length == 0)
		{
			return null;
		}
		Widget widget = menu[menu.length - 1].getWidget();
		if (widget == null)
		{
			return null;
		}

		String text = null;
		Prayer prayer = prayerAt(widget);
		if (prayer != null)
		{
			if (prayer.name().startsWith("RP_"))
			{
				text = "Not included in this estimate";
			}
			else
			{
				Double delta = DpsDelta.prayer(current, plugin.getCurrentPrayers(), prayer);
				if (delta != null)
				{
					text = DpsDelta.format(delta.doubleValue());
					text = ColorUtil.wrapWithColorTag(text, color(delta.doubleValue()));
				}
			}
		}
		else
		{
			String itemName = potionAt(widget);
			if (itemName != null)
			{
				Double delta = DpsDelta.potion(
					current,
					client.getRealSkillLevel(Skill.ATTACK),
					client.getRealSkillLevel(Skill.STRENGTH),
					client.getRealSkillLevel(Skill.RANGED),
					client.getRealSkillLevel(Skill.MAGIC),
					itemName);
				if (delta != null)
				{
					text = DpsDelta.format(delta.doubleValue());
					text = ColorUtil.wrapWithColorTag(text, color(delta.doubleValue()));
				}
			}
		}
		if (text != null)
		{
			tooltipManager.add(new Tooltip(text));
		}
		return null;
	}

	private Prayer prayerAt(Widget widget)
	{
		int book = client.getVarbitValue(VarbitID.PRAYERBOOK);
		EnumComposition prayers = client.getEnum(book == 1 ? EnumID.PRAYERS_RUINOUS : EnumID.PRAYERS_NORMAL);
		if (prayers == null)
		{
			return null;
		}
		int[] keys = prayers.getKeys();
		for (int i = 0; i < keys.length; i++)
		{
			ItemComposition definition = client.getItemDefinition(prayers.getIntValue(keys[i]));
			if (definition == null)
			{
				continue;
			}
			int component = definition.getIntValue(net.runelite.api.ParamID.OC_PRAYER_COMPONENT);
			if (component > 0 && widgetChain(widget, component))
			{
				return PRAYERS.get(definition.getName().toLowerCase(Locale.US));
			}
		}
		if (WidgetUtil.componentToInterface(widget.getId()) != InterfaceID.QUICKPRAYER)
		{
			return null;
		}
		Widget buttons = client.getWidget(InterfaceID.Quickprayer.BUTTONS);
		if (buttons == null)
		{
			return null;
		}
		Widget[] children = buttons.getDynamicChildren();
		if (children == null)
		{
			return null;
		}
		for (int i = 0; i < keys.length; i++)
		{
			int key = keys[i];
			if (key < 0 || key >= children.length || !widgetChain(widget, children[key].getId()))
			{
				continue;
			}
			ItemComposition definition = client.getItemDefinition(prayers.getIntValue(key));
			if (definition == null || definition.getName() == null)
			{
				return null;
			}
			return PRAYERS.get(definition.getName().toLowerCase(Locale.US));
		}
		return null;
	}

	private String potionAt(Widget widget)
	{
		int itemId = itemId(widget);
		if (itemId <= 0)
		{
			return null;
		}
		String name = itemManager.getItemComposition(itemId).getName();
		if (!PotionBoosts.affectsCombat(name))
		{
			return null;
		}
		return name;
	}

	private int itemId(Widget widget)
	{
		int group = WidgetUtil.componentToInterface(widget.getId());
		if (widget.getId() == InterfaceID.Inventory.ITEMS
			|| widget.getId() == InterfaceID.Bankmain.ITEMS
			|| widget.getId() == InterfaceID.SharedBank.ITEMS
			|| group == InterfaceID.BANKSIDE
			|| group == InterfaceID.SHARED_BANK_SIDE)
		{
			return widget.getItemId();
		}
		return -1;
	}

	private static boolean widgetChain(Widget widget, int componentId)
	{
		Widget current = widget;
		for (int depth = 0; current != null && depth < 4; depth++)
		{
			if (current.getId() == componentId)
			{
				return true;
			}
			int parentId = current.getParentId();
			if (parentId == -1)
			{
				return false;
			}
			current = current.getParent();
		}
		return false;
	}

	private static Color color(double delta)
	{
		if (delta > 0.0005)
		{
			return GAIN;
		}
		if (delta < -0.0005)
		{
			return LOSS;
		}
		return Color.WHITE;
	}

	private static Map<String, Prayer> prayerNames()
	{
		Map<String, Prayer> names = new HashMap<String, Prayer>();
		put(names, "Thick Skin", Prayer.THICK_SKIN);
		put(names, "Burst of Strength", Prayer.BURST_OF_STRENGTH);
		put(names, "Clarity of Thought", Prayer.CLARITY_OF_THOUGHT);
		put(names, "Sharp Eye", Prayer.SHARP_EYE);
		put(names, "Mystic Will", Prayer.MYSTIC_WILL);
		put(names, "Rock Skin", Prayer.ROCK_SKIN);
		put(names, "Superhuman Strength", Prayer.SUPERHUMAN_STRENGTH);
		put(names, "Improved Reflexes", Prayer.IMPROVED_REFLEXES);
		put(names, "Rapid Restore", Prayer.RAPID_RESTORE);
		put(names, "Rapid Heal", Prayer.RAPID_HEAL);
		put(names, "Protect Item", Prayer.PROTECT_ITEM);
		put(names, "Hawk Eye", Prayer.HAWK_EYE);
		put(names, "Mystic Lore", Prayer.MYSTIC_LORE);
		put(names, "Steel Skin", Prayer.STEEL_SKIN);
		put(names, "Ultimate Strength", Prayer.ULTIMATE_STRENGTH);
		put(names, "Incredible Reflexes", Prayer.INCREDIBLE_REFLEXES);
		put(names, "Protect from Magic", Prayer.PROTECT_FROM_MAGIC);
		put(names, "Protect from Missiles", Prayer.PROTECT_FROM_MISSILES);
		put(names, "Protect from Melee", Prayer.PROTECT_FROM_MELEE);
		put(names, "Eagle Eye", Prayer.EAGLE_EYE);
		put(names, "Mystic Might", Prayer.MYSTIC_MIGHT);
		put(names, "Retribution", Prayer.RETRIBUTION);
		put(names, "Redemption", Prayer.REDEMPTION);
		put(names, "Smite", Prayer.SMITE);
		put(names, "Preserve", Prayer.PRESERVE);
		put(names, "Chivalry", Prayer.CHIVALRY);
		put(names, "Deadeye", Prayer.DEADEYE);
		put(names, "Mystic Vigour", Prayer.MYSTIC_VIGOUR);
		put(names, "Piety", Prayer.PIETY);
		put(names, "Zeal", Prayer.ZEAL);
		put(names, "Rigour", Prayer.RIGOUR);
		put(names, "Augury", Prayer.AUGURY);
		put(names, "Rejuvenation", Prayer.RP_REJUVENATION);
		put(names, "Ancient Strength", Prayer.RP_ANCIENT_STRENGTH);
		put(names, "Ancient Sight", Prayer.RP_ANCIENT_SIGHT);
		put(names, "Ancient Will", Prayer.RP_ANCIENT_WILL);
		put(names, "Ruinous Grace", Prayer.RP_RUINOUS_GRACE);
		put(names, "Dampen Magic", Prayer.RP_DAMPEN_MAGIC);
		put(names, "Dampen Ranged", Prayer.RP_DAMPEN_RANGED);
		put(names, "Dampen Melee", Prayer.RP_DAMPEN_MELEE);
		put(names, "Trinitas", Prayer.RP_TRINITAS);
		put(names, "Berserker", Prayer.RP_BERSERKER);
		put(names, "Purge", Prayer.RP_PURGE);
		put(names, "Metabolise", Prayer.RP_METABOLISE);
		put(names, "Rebuke", Prayer.RP_REBUKE);
		put(names, "Vindication", Prayer.RP_VINDICATION);
		put(names, "Decimate", Prayer.RP_DECIMATE);
		put(names, "Annihilate", Prayer.RP_ANNIHILATE);
		put(names, "Vaporise", Prayer.RP_VAPORISE);
		put(names, "Fumus' Vow", Prayer.RP_FUMUS_VOW);
		put(names, "Umbra's Vow", Prayer.RP_UMBRA_VOW);
		put(names, "Cruor's Vow", Prayer.RP_CRUORS_VOW);
		put(names, "Glacies' Vow", Prayer.RP_GLACIES_VOW);
		put(names, "Wrath", Prayer.RP_WRATH);
		put(names, "Intensify", Prayer.RP_INTENSIFY);
		return names;
	}

	private static void put(Map<String, Prayer> names, String label, Prayer prayer)
	{
		names.put(label.toLowerCase(Locale.US), prayer);
	}
}
