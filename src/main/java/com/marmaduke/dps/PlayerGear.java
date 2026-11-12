package com.marmaduke.dps;

import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ParamID;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.StructComposition;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;

/**
 * Reads worn gear, prayers, boosted stats, and the selected attack style from the client.
 */
final class PlayerGear
{
	private PlayerGear()
	{
	}

	static CombatInput build(Client client, ItemManager itemManager, boolean onSlayerTask, NpcCombatStats npc, DefenceModel defence)
	{
		CombatInput in = new CombatInput();
		in.attackLevel = client.getBoostedSkillLevel(Skill.ATTACK);
		in.strengthLevel = client.getBoostedSkillLevel(Skill.STRENGTH);
		in.rangedLevel = client.getBoostedSkillLevel(Skill.RANGED);
		in.magicLevel = client.getBoostedSkillLevel(Skill.MAGIC);
		in.onSlayerTask = onSlayerTask;

		String weaponName = "";
		String headName = "";
		String bodyName = "";
		String legsName = "";
		String glovesName = "";
		ItemEquipmentStats weaponEquipment = null;
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment != null)
		{
			for (EquipmentInventorySlot slot : EquipmentInventorySlot.values())
			{
				Item item = equipment.getItem(slot.getSlotIdx());
				if (item == null || item.getId() < 0)
				{
					continue;
				}
				String name = itemName(itemManager, item.getId());
				if (slot == EquipmentInventorySlot.HEAD)
				{
					headName = name;
				}
				else if (slot == EquipmentInventorySlot.BODY)
				{
					bodyName = name;
				}
				else if (slot == EquipmentInventorySlot.LEGS)
				{
					legsName = name;
				}
				else if (slot == EquipmentInventorySlot.GLOVES)
				{
					glovesName = name;
				}
				ItemStats stats = itemManager.getItemStats(item.getId());
				if (stats != null && stats.getEquipment() != null)
				{
					addEquipment(in, stats.getEquipment());
				}
				if (slot == EquipmentInventorySlot.WEAPON)
				{
					weaponName = name;
					if (stats != null)
					{
						weaponEquipment = stats.getEquipment();
					}
				}
				applyNamedGear(in, slot, name);
			}
		}
		finishVoid(in, headName, bodyName, legsName, glovesName);

		in.weaponName = weaponName;
		if (weaponEquipment != null && weaponEquipment.getAspeed() > 0)
		{
			in.weaponSpeed = weaponEquipment.getAspeed();
		}
		in.poweredStaff = isPoweredStaff(weaponName);
		in.uncharged = isUncharged(weaponName);
		in.meleeStyle = meleeStyle(weaponEquipment, client);

		String styleName = readStyleName(client);
		in.stance = stanceFrom(styleName, in.poweredStaff);
		if (in.poweredStaff)
		{
			in.combatStyle = CombatStyle.MAGIC;
			if (in.stance == AttackStance.RANGED_LONG
				|| in.stance == AttackStance.MELEE_DEFENSIVE
				|| in.stance == AttackStance.MAGIC_DEFENSIVE)
			{
				in.stance = AttackStance.MAGIC_DEFENSIVE;
			}
			else
			{
				in.stance = AttackStance.MAGIC_ACCURATE;
			}
		}
		else if (in.stance == AttackStance.RANGED_ACCURATE
			|| in.stance == AttackStance.RANGED_RAPID
			|| in.stance == AttackStance.RANGED_LONG)
		{
			in.combatStyle = CombatStyle.RANGED;
		}
		else if (in.stance == AttackStance.MAGIC_ACCURATE || in.stance == AttackStance.MAGIC_DEFENSIVE)
		{
			in.combatStyle = CombatStyle.MAGIC;
		}
		else
		{
			in.combatStyle = CombatStyle.MELEE;
		}

		in.rangedStyle = rangedStyle(weaponName);
		applyPrayers(client, in);

		if (in.combatStyle == CombatStyle.MAGIC && !in.poweredStaff)
		{
			SpellTable spell = SpellTable.fromAutocast(client.getVarbitValue(VarbitID.AUTOCAST_SPELL));
			if (spell != null)
			{
				in.spellName = spell.name;
				in.spellMaxHit = spell.maxHit;
				in.spellElement = spell.element;
				in.spellbook = spell.spellbook;
			}
		}

		if (npc != null)
		{
			in.npcDefence = defence != null ? defence.defence : npc.def;
			in.npcMagic = defence != null ? defence.magic : npc.magic;
			in.npcOffensiveMagic = npc.omagic;
			in.npcStab = npc.stab;
			in.npcSlash = npc.slash;
			in.npcCrush = npc.crush;
			in.npcMagicDefence = npc.mage;
			in.npcLight = npc.light;
			in.npcStandard = npc.std;
			in.npcHeavy = npc.heavy;
			in.npcFlatArmour = npc.flat;
			in.undead = npc.has("undead") || npc.has("vampyre1") || npc.has("vampyre2") || npc.has("vampyre3");
			in.demon = npc.has("demon");
			in.dragon = npc.has("dragon");
			in.xerician = npc.has("xerician");
			in.revenant = npc.name != null && npc.name.startsWith("Revenant");
			in.toa = npc.toa;
			in.magicUsesDefenceLevel = npc.mageUsesDef;
			in.weaknessElement = npc.weak == null ? "" : npc.weak;
			in.weaknessPercent = npc.weakPct;
		}
		return in;
	}

	private static void addEquipment(CombatInput in, ItemEquipmentStats equipment)
	{
		in.stabBonus += equipment.getAstab();
		in.slashBonus += equipment.getAslash();
		in.crushBonus += equipment.getAcrush();
		in.magicBonus += equipment.getAmagic();
		in.rangedBonus += equipment.getArange();
		in.strengthBonus += equipment.getStr();
		in.rangedStrengthBonus += equipment.getRstr();
		in.magicDamagePermille += Math.round(equipment.getMdmg() * 10f);
	}

	private static void applyNamedGear(CombatInput in, EquipmentInventorySlot slot, String name)
	{
		String lower = name.toLowerCase();
		if (slot == EquipmentInventorySlot.HEAD)
		{
			if (lower.contains("crystal helm"))
			{
				in.crystalPieces += 1;
			}
			if (lower.contains("virtus"))
			{
				in.virtusPieces++;
			}
		}
		else if (slot == EquipmentInventorySlot.BODY)
		{
			if (lower.contains("crystal body"))
			{
				in.crystalPieces += 3;
			}
			if (lower.contains("virtus"))
			{
				in.virtusPieces++;
			}
		}
		else if (slot == EquipmentInventorySlot.LEGS)
		{
			if (lower.contains("crystal legs"))
			{
				in.crystalPieces += 2;
			}
			if (lower.contains("virtus"))
			{
				in.virtusPieces++;
			}
		}
		else if (slot == EquipmentInventorySlot.GLOVES)
		{
			if (lower.contains("chaos gauntlets"))
			{
				in.chaosGauntlets = true;
			}
		}
		else if (slot == EquipmentInventorySlot.AMULET)
		{
			if (lower.contains("amulet of avarice"))
			{
				in.avarice = true;
			}
			if (lower.contains("salve amulet(ei)") || lower.contains("salve amulet (ei)"))
			{
				in.salveEnchantedImbued = true;
			}
			else if (lower.contains("salve amulet(e)") || lower.contains("salve amulet (e)"))
			{
				in.salveEnchanted = true;
			}
			else if (lower.contains("salve amulet(i)") || lower.contains("salve amulet (i)"))
			{
				in.salveImbued = true;
			}
			else if (lower.contains("salve amulet"))
			{
				in.salve = true;
			}
		}
		else if (slot == EquipmentInventorySlot.WEAPON)
		{
			in.shadow = lower.contains("tumeken's shadow");
			in.twistedBow = lower.contains("twisted bow");
			in.fang = lower.contains("osmumten's fang");
			in.crystalBow = lower.contains("bow of faerdhinen") || lower.contains("crystal bow");
			in.arclight = lower.contains("arclight") && !lower.contains("inactive");
			in.emberlight = lower.contains("emberlight");
			in.darklight = lower.contains("darklight") || lower.contains("silverlight");
			in.dragonHunterLance = lower.contains("dragon hunter lance");
			in.dragonHunterCrossbow = lower.contains("dragon hunter crossbow");
			in.dragonHunterWand = lower.contains("dragon hunter wand");
			in.harmonisedStaff = lower.contains("harmonised");
			in.twinflameStaff = lower.contains("twinflame");
		}
		else if (slot == EquipmentInventorySlot.SHIELD)
		{
			if (lower.equals("tome of fire") || lower.startsWith("tome of fire ") && !lower.contains("uncharged") && !lower.contains("empty"))
			{
				in.tomeFire = !lower.contains("uncharged") && !lower.contains("empty");
			}
			if (lower.contains("tome of water") && !lower.contains("uncharged") && !lower.contains("empty"))
			{
				in.tomeWater = true;
			}
			if (lower.contains("tome of earth") && !lower.contains("uncharged") && !lower.contains("empty"))
			{
				in.tomeEarth = true;
			}
		}
		if (lower.contains("slayer helmet") || lower.contains("black mask (i)") || lower.contains("black mask(i)"))
		{
			in.blackMaskImbued = true;
		}
		else if (lower.contains("black mask"))
		{
			in.blackMask = true;
		}
	}

	private static void finishVoid(CombatInput in, String head, String body, String legs, String gloves)
	{
		String headName = head.toLowerCase();
		String bodyName = body.toLowerCase();
		String legsName = legs.toLowerCase();
		String glovesName = gloves.toLowerCase();
		boolean glovesOk = glovesName.contains("void knight gloves") || glovesName.contains("void gloves");
		boolean eliteTop = bodyName.contains("elite void top");
		boolean eliteLegs = legsName.contains("elite void robe");
		boolean topOk = bodyName.contains("void knight top") || eliteTop;
		boolean legsOk = legsName.contains("void knight robe") || eliteLegs;
		boolean elite = eliteTop && eliteLegs;
		if (!glovesOk || !topOk || !legsOk)
		{
			return;
		}
		if (headName.contains("void melee helm"))
		{
			in.voidMelee = true;
		}
		else if (headName.contains("void ranger helm"))
		{
			in.voidRanged = true;
			in.voidRangedElite = elite;
		}
		else if (headName.contains("void mage helm"))
		{
			in.voidMagic = true;
			in.voidMagicElite = elite;
		}
	}

	private static boolean isPoweredStaff(String name)
	{
		String lower = name.toLowerCase();
		if (lower.contains("(a)") || isUncharged(lower))
		{
			return false;
		}
		return lower.contains("trident of the seas")
			|| lower.contains("trident of the swamp")
			|| lower.contains("sanguinesti")
			|| lower.contains("tumeken's shadow")
			|| lower.contains("warped sceptre")
			|| lower.contains("accursed sceptre")
			|| lower.contains("thammaron's sceptre")
			|| lower.contains("eye of ayak")
			|| lower.contains("bone staff")
			|| lower.contains("dawnbringer")
			|| lower.contains("starter staff");
	}

	private static boolean isUncharged(String name)
	{
		String lower = name.toLowerCase();
		return lower.contains("uncharged") || lower.contains("(empty)") || lower.contains("inactive");
	}

	private static MeleeStyle meleeStyle(ItemEquipmentStats weapon, Client client)
	{
		if (weapon == null)
		{
			return MeleeStyle.SLASH;
		}
		int stab = weapon.getAstab();
		int slash = weapon.getAslash();
		int crush = weapon.getAcrush();
		if (stab == slash && slash == crush)
		{
			int index = client.getVarpValue(VarPlayerID.COM_MODE);
			if (index == 1)
			{
				return MeleeStyle.SLASH;
			}
			if (index == 2)
			{
				return MeleeStyle.CRUSH;
			}
			return MeleeStyle.STAB;
		}
		if (crush >= stab && crush >= slash)
		{
			return MeleeStyle.CRUSH;
		}
		if (slash >= stab)
		{
			return MeleeStyle.SLASH;
		}
		return MeleeStyle.STAB;
	}

	private static RangedStyle rangedStyle(String weaponName)
	{
		String lower = weaponName.toLowerCase();
		if (lower.contains("blowpipe") || lower.contains("dart") || lower.contains("knife")
			|| lower.contains("thrownaxe") || lower.contains("toktz-xil-ul") || lower.contains("tonalztics"))
		{
			return RangedStyle.LIGHT;
		}
		if (lower.contains("crossbow") || lower.contains("ballista") || lower.contains("chinchompa")
			|| lower.contains("javelin"))
		{
			return RangedStyle.HEAVY;
		}
		return RangedStyle.STANDARD;
	}

	private static void applyPrayers(Client client, CombatInput in)
	{
		if (client.isPrayerActive(Prayer.ZEAL))
		{
			in.meleeAttackPrayer = 125;
			in.meleeStrengthPrayer = 128;
		}
		else if (client.isPrayerActive(Prayer.PIETY))
		{
			in.meleeAttackPrayer = 120;
			in.meleeStrengthPrayer = 123;
		}
		else if (client.isPrayerActive(Prayer.CHIVALRY))
		{
			in.meleeAttackPrayer = 115;
			in.meleeStrengthPrayer = 118;
		}
		else
		{
			if (client.isPrayerActive(Prayer.ULTIMATE_STRENGTH))
			{
				in.meleeStrengthPrayer = 115;
			}
			else if (client.isPrayerActive(Prayer.SUPERHUMAN_STRENGTH))
			{
				in.meleeStrengthPrayer = 110;
			}
			else if (client.isPrayerActive(Prayer.BURST_OF_STRENGTH))
			{
				in.meleeStrengthPrayer = 105;
			}
			if (client.isPrayerActive(Prayer.INCREDIBLE_REFLEXES))
			{
				in.meleeAttackPrayer = 115;
			}
			else if (client.isPrayerActive(Prayer.IMPROVED_REFLEXES))
			{
				in.meleeAttackPrayer = 110;
			}
			else if (client.isPrayerActive(Prayer.CLARITY_OF_THOUGHT))
			{
				in.meleeAttackPrayer = 105;
			}
		}

		if (client.isPrayerActive(Prayer.RIGOUR))
		{
			in.rangedAttackPrayer = 120;
			in.rangedStrengthPrayer = 123;
		}
		else if (client.isPrayerActive(Prayer.DEADEYE))
		{
			in.rangedAttackPrayer = 118;
			in.rangedStrengthPrayer = 118;
		}
		else if (client.isPrayerActive(Prayer.EAGLE_EYE))
		{
			in.rangedAttackPrayer = 115;
			in.rangedStrengthPrayer = 115;
		}
		else if (client.isPrayerActive(Prayer.HAWK_EYE))
		{
			in.rangedAttackPrayer = 110;
			in.rangedStrengthPrayer = 110;
		}
		else if (client.isPrayerActive(Prayer.SHARP_EYE))
		{
			in.rangedAttackPrayer = 105;
			in.rangedStrengthPrayer = 105;
		}

		if (client.isPrayerActive(Prayer.AUGURY))
		{
			in.magicAttackPrayer = 125;
			in.magicDamagePrayerPermille = 40;
		}
		else if (client.isPrayerActive(Prayer.MYSTIC_VIGOUR))
		{
			in.magicAttackPrayer = 118;
			in.magicDamagePrayerPermille = 30;
		}
		else if (client.isPrayerActive(Prayer.MYSTIC_MIGHT))
		{
			in.magicAttackPrayer = 115;
			in.magicDamagePrayerPermille = 20;
		}
		else if (client.isPrayerActive(Prayer.MYSTIC_LORE))
		{
			in.magicAttackPrayer = 110;
			in.magicDamagePrayerPermille = 10;
		}
		else if (client.isPrayerActive(Prayer.MYSTIC_WILL))
		{
			in.magicAttackPrayer = 105;
		}
	}

	private static String readStyleName(Client client)
	{
		try
		{
			int weaponType = client.getVarbitValue(VarbitID.COMBAT_WEAPON_CATEGORY);
			int index = client.getVarpValue(VarPlayerID.COM_MODE);
			int castingMode = client.getVarbitValue(VarbitID.AUTOCAST_DEFMODE);
			if (index == 4)
			{
				index += castingMode;
			}
			if (weaponType == 22)
			{
				return pick(new String[]{"Accurate", "Aggressive", null, "Defensive", "Casting", "Defensive Casting"}, index);
			}
			if (weaponType == 30)
			{
				return pick(new String[]{"Accurate", "Aggressive", "Aggressive", "Defensive"}, index);
			}
			int weaponStyleEnum = client.getEnum(EnumID.WEAPON_STYLES).getIntValue(weaponType);
			if (weaponStyleEnum == -1)
			{
				return "";
			}
			EnumComposition styles = client.getEnum(weaponStyleEnum);
			int[] structs = styles.getIntVals();
			String[] names = new String[structs.length];
			int cursor = 0;
			for (int style : structs)
			{
				StructComposition composition = client.getStructComposition(style);
				String name = composition.getStringValue(ParamID.ATTACK_STYLE_NAME);
				if (name != null && "Other".equalsIgnoreCase(name))
				{
					cursor++;
					continue;
				}
				if (cursor == 5 && name != null && "Defensive".equalsIgnoreCase(name))
				{
					name = "Defensive Casting";
				}
				if (cursor < names.length)
				{
					names[cursor] = name;
				}
				cursor++;
			}
			return pick(names, index);
		}
		catch (RuntimeException ex)
		{
			return "";
		}
	}

	private static String pick(String[] names, int index)
	{
		if (index < 0 || index >= names.length || names[index] == null)
		{
			return "";
		}
		return names[index];
	}

	static AttackStance stanceFrom(String name, boolean poweredStaff)
	{
		if (name == null)
		{
			return poweredStaff ? AttackStance.MAGIC_ACCURATE : AttackStance.MELEE_AGGRESSIVE;
		}
		String lower = name.toLowerCase();
		if (lower.contains("rapid"))
		{
			return AttackStance.RANGED_RAPID;
		}
		if (lower.contains("longrange"))
		{
			return AttackStance.RANGED_LONG;
		}
		if (lower.contains("ranging"))
		{
			return AttackStance.RANGED_ACCURATE;
		}
		if (lower.contains("defensive casting"))
		{
			return AttackStance.MAGIC_DEFENSIVE;
		}
		if (lower.contains("casting"))
		{
			return AttackStance.MAGIC_ACCURATE;
		}
		if (lower.contains("controlled"))
		{
			return AttackStance.MELEE_CONTROLLED;
		}
		if (lower.contains("aggressive"))
		{
			return AttackStance.MELEE_AGGRESSIVE;
		}
		if (lower.contains("defensive"))
		{
			return AttackStance.MELEE_DEFENSIVE;
		}
		if (lower.contains("accurate"))
		{
			return poweredStaff ? AttackStance.MAGIC_ACCURATE : AttackStance.MELEE_ACCURATE;
		}
		return poweredStaff ? AttackStance.MAGIC_ACCURATE : AttackStance.MELEE_AGGRESSIVE;
	}

	private static String itemName(ItemManager itemManager, int itemId)
	{
		String name = itemManager.getItemComposition(itemId).getName();
		if (name == null)
		{
			return "";
		}
		return name.replace('\u00A0', ' ').trim();
	}
}
