package com.marmaduke.dps;

/**
 * Player-versus-NPC accuracy and max hit, using the integer formulas published on the
 * OSRS wiki and matched by the maintained Weird Gloop DPS calculator.
 * A successful hit then rolls uniformly from 0 through the max hit. Flat armour is
 * subtracted from each of those rolls.
 */
final class CombatMath
{
	private static final double SECONDS_PER_TICK = 0.6;

	private CombatMath()
	{
	}

	static DpsEstimate estimate(CombatInput in, String targetName, String defenceLabel)
	{
		if (in.uncharged)
		{
			return DpsEstimate.message(targetName, "Weapon is uncharged");
		}
		if (in.combatStyle == CombatStyle.MAGIC && !in.poweredStaff && in.spellMaxHit <= 0
			&& !"Magic Dart".equals(in.spellName))
		{
			return DpsEstimate.message(targetName, "Choose an autocast spell");
		}

		int maxHit = maxHit(in);
		int attackRoll = attackRoll(in);
		int defenceRoll = defenceRoll(in);
		double accuracy = accuracy(in, attackRoll, defenceRoll);
		int speed = attackSpeed(in);
		int flat = in.combatStyle == CombatStyle.MAGIC ? 0 : Math.max(0, in.npcFlatArmour);
		double expected = expectedDamage(accuracy, maxHit, flat);
		double dps = expected / (speed * SECONDS_PER_TICK);

		String note = "";
		String weapon = in.weaponName == null ? "" : in.weaponName.toLowerCase();
		if (weapon.contains("blowpipe"))
		{
			note = "Dart inside the blowpipe is not added";
		}
		else if (weapon.contains("chinchompa"))
		{
			note = "Single-target only; fuse distance is not applied";
		}

		return new DpsEstimate(
			targetName,
			dps,
			maxHit,
			accuracy,
			speed,
			styleLabel(in),
			defenceLabel,
			note);
	}

	static int maxHit(CombatInput in)
	{
		if (in.combatStyle == CombatStyle.MAGIC)
		{
			return magicMaxHit(in);
		}
		if (in.combatStyle == CombatStyle.RANGED)
		{
			return rangedMaxHit(in);
		}
		return meleeMaxHit(in);
	}

	static int attackRoll(CombatInput in)
	{
		if (in.combatStyle == CombatStyle.MAGIC)
		{
			return magicAttackRoll(in);
		}
		if (in.combatStyle == CombatStyle.RANGED)
		{
			return rangedAttackRoll(in);
		}
		return meleeAttackRoll(in);
	}

	static int defenceRoll(CombatInput in)
	{
		if (in.combatStyle == CombatStyle.MAGIC)
		{
			int level = in.magicUsesDefenceLevel ? in.npcDefence : in.npcMagic;
			return (level + 9) * (in.npcMagicDefence + 64);
		}
		int bonus = in.combatStyle == CombatStyle.RANGED ? in.npcRangedDefence() : in.npcMeleeDefence();
		return (in.npcDefence + 9) * (bonus + 64);
	}

	static double accuracy(CombatInput in, int attackRoll, int defenceRoll)
	{
		double hit = normalAccuracy(attackRoll, defenceRoll);
		if (in.fang && in.meleeStyle == MeleeStyle.STAB && in.combatStyle == CombatStyle.MELEE)
		{
			if (in.toa)
			{
				return 1.0 - (1.0 - hit) * (1.0 - hit);
			}
			return fangAccuracy(attackRoll, defenceRoll);
		}
		return hit;
	}

	static double normalAccuracy(int atk, int def)
	{
		if (atk < 0)
		{
			atk = Math.min(0, atk + 2);
		}
		if (def < 0)
		{
			def = Math.min(0, def + 2);
		}
		if (atk >= 0 && def >= 0)
		{
			return atk > def
				? 1.0 - (def + 2.0) / (2.0 * (atk + 1))
				: atk / (2.0 * (def + 1));
		}
		if (atk >= 0)
		{
			return 1.0 - 1.0 / (-def + 1) / (atk + 1);
		}
		return 0;
	}

	static double fangAccuracy(int atk, int def)
	{
		if (atk < 0)
		{
			atk = Math.min(0, atk + 2);
		}
		if (def < 0)
		{
			def = Math.min(0, def + 2);
		}
		if (atk < 0 || def < 0)
		{
			return normalAccuracy(atk, def);
		}
		if (atk > def)
		{
			return 1.0 - (def + 2.0) * (2.0 * def + 3.0) / (atk + 1) / (atk + 1) / 6.0;
		}
		return atk * (4.0 * atk + 5.0) / 6.0 / (atk + 1) / (def + 1);
	}

	static double expectedDamage(double accuracy, int maxHit, int flatReduction)
	{
		if (maxHit < 0)
		{
			maxHit = 0;
		}
		double sum = 0;
		int outcomes = maxHit + 1;
		for (int roll = 0; roll <= maxHit; roll++)
		{
			sum += Math.max(0, roll - flatReduction);
		}
		return accuracy * (sum / outcomes);
	}

	static int attackSpeed(CombatInput in)
	{
		int speed = in.weaponSpeed <= 0 ? 4 : in.weaponSpeed;
		if (in.combatStyle == CombatStyle.RANGED && in.stance == AttackStance.RANGED_RAPID)
		{
			speed -= 1;
		}
		else if (in.combatStyle == CombatStyle.MAGIC && !in.poweredStaff)
		{
			if (in.harmonisedStaff && "standard".equals(in.spellbook))
			{
				speed = 4;
			}
			else if (in.twinflameStaff)
			{
				speed = 6;
			}
			else
			{
				speed = 5;
			}
		}
		return Math.max(1, speed);
	}

	static int tbowScale(int current, int magicLevel, boolean accuracy)
	{
		int factor = accuracy ? 10 : 14;
		int base = accuracy ? 140 : 250;
		int clamp = accuracy ? 140 : 250;
		int t2 = (3 * magicLevel - factor) / 100;
		int inner = (3 * magicLevel / 10) - (10 * factor);
		int t3 = (inner * inner) / 100;
		int bonus = Math.max(0, Math.min(clamp, base + t2 - t3));
		return current * bonus / 100;
	}

	private static int meleeMaxHit(CombatInput in)
	{
		int effective = in.strengthLevel * in.meleeStrengthPrayer / 100;
		if (in.stance == AttackStance.MELEE_AGGRESSIVE)
		{
			effective += 3;
		}
		else if (in.stance == AttackStance.MELEE_CONTROLLED)
		{
			effective += 1;
		}
		effective += 8;
		if (in.voidMelee)
		{
			effective = effective * 11 / 10;
		}
		int max = (effective * (in.strengthBonus + 64) + 320) / 640;
		max = applyMeleeGearBonus(in, max);
		if ((in.arclight || in.emberlight) && in.demon)
		{
			max += max * 70 / 100;
		}
		else if (in.darklight && in.demon)
		{
			max += max * 60 / 100;
		}
		if (in.dragon && in.dragonHunterLance)
		{
			max = max * 6 / 5;
		}
		else if (in.dragon && in.dragonHunterWand)
		{
			max = max * 7 / 5;
		}
		return Math.max(0, max);
	}

	private static int rangedMaxHit(CombatInput in)
	{
		int effective = in.rangedLevel * in.rangedStrengthPrayer / 100;
		if (in.stance == AttackStance.RANGED_ACCURATE)
		{
			effective += 3;
		}
		effective += 8;
		if (in.voidRangedElite)
		{
			effective = effective * 9 / 8;
		}
		else if (in.voidRanged)
		{
			effective = effective * 11 / 10;
		}
		int max = (effective * (in.rangedStrengthBonus + 64) + 320) / 640;
		if (in.crystalBow && in.crystalPieces > 0)
		{
			max = max * (40 + in.crystalPieces) / 40;
		}
		max = applyRangedGearBonus(in, max);
		if (in.twistedBow)
		{
			max = tbowScale(max, tbowMagic(in), false);
		}
		if (in.dragon && in.dragonHunterCrossbow)
		{
			max = max * 5 / 4;
		}
		return Math.max(0, max);
	}

	private static int magicMaxHit(CombatInput in)
	{
		int base = poweredStaffBase(in);
		if (base < 0)
		{
			if ("Magic Dart".equals(in.spellName))
			{
				base = in.blackMaskImbued && in.onSlayerTask
					? 13 + in.magicLevel / 6
					: 10 + in.magicLevel / 10;
			}
			else
			{
				base = Math.max(0, in.spellMaxHit);
			}
			if (in.chaosGauntlets && in.spellName != null && in.spellName.toLowerCase().contains("bolt"))
			{
				base += 3;
			}
		}
		if (base <= 0)
		{
			return 0;
		}

		int bonus = in.magicDamagePermille;
		if (in.shadow)
		{
			int factor = in.toa ? 4 : 3;
			bonus = Math.min(1000, bonus * factor);
		}
		if ("ancient".equals(in.spellbook) && !in.poweredStaff)
		{
			bonus += 30 * in.virtusPieces;
		}
		if (in.voidMagicElite)
		{
			bonus += 50;
		}
		if (in.avarice && in.revenant)
		{
			bonus += 200;
		}
		else if (in.undead && in.salveEnchantedImbued)
		{
			bonus += 200;
		}
		else if (in.undead && in.salveImbued)
		{
			bonus += 150;
		}
		bonus += in.magicDamagePrayerPermille;

		int max = base + base * bonus / 1000;
		boolean salve = in.undead && (in.salveImbued || in.salveEnchantedImbued);
		if (!salve && !(in.avarice && in.revenant) && in.blackMaskImbued && in.onSlayerTask)
		{
			max = max * 23 / 20;
		}
		if (in.dragon)
		{
			if (in.dragonHunterLance)
			{
				max = max * 6 / 5;
			}
			else if (in.dragonHunterWand)
			{
				max = max * 7 / 5;
			}
			else if (in.dragonHunterCrossbow)
			{
				max = max * 5 / 4;
			}
		}
		if (in.weaknessElement != null && in.weaknessElement.equals(in.spellElement) && in.weaknessPercent > 0)
		{
			max += base * in.weaknessPercent / 100;
		}
		if (tomeMatches(in))
		{
			max = max * 11 / 10;
		}
		return Math.max(0, max);
	}

	private static int poweredStaffBase(CombatInput in)
	{
		if (!in.poweredStaff)
		{
			return -1;
		}
		String name = in.weaponName == null ? "" : in.weaponName.toLowerCase();
		int magic = in.magicLevel;
		if (name.contains("trident of the seas"))
		{
			return Math.max(1, magic / 3 - 5);
		}
		if (name.contains("trident of the swamp"))
		{
			return Math.max(1, magic / 3 - 2);
		}
		if (name.contains("sanguinesti"))
		{
			return Math.max(1, magic / 3);
		}
		if (name.contains("tumeken"))
		{
			return Math.max(1, magic / 3 + 1);
		}
		if (name.contains("thammaron"))
		{
			return Math.max(1, magic / 3 - 8);
		}
		if (name.contains("accursed"))
		{
			return Math.max(1, magic / 3 - 6);
		}
		if (name.contains("eye of ayak"))
		{
			return Math.max(1, magic / 3 - 6);
		}
		if (name.contains("warped sceptre"))
		{
			return Math.max(1, (8 * magic + 96) / 37);
		}
		if (name.contains("bone staff"))
		{
			return Math.max(1, magic / 3 - 5) + 10;
		}
		if (name.contains("dawnbringer"))
		{
			return Math.max(1, magic / 3 - 2);
		}
		if (name.contains("starter staff"))
		{
			return 8;
		}
		return -1;
	}

	private static int meleeAttackRoll(CombatInput in)
	{
		int effective = in.attackLevel * in.meleeAttackPrayer / 100;
		if (in.stance == AttackStance.MELEE_ACCURATE)
		{
			effective += 3;
		}
		else if (in.stance == AttackStance.MELEE_CONTROLLED)
		{
			effective += 1;
		}
		effective += 8;
		if (in.voidMelee)
		{
			effective = effective * 11 / 10;
		}
		int roll = effective * (in.meleeAttackBonus() + 64);
		roll = applyMeleeGearBonus(in, roll);
		if ((in.arclight || in.emberlight) && in.demon)
		{
			roll += roll * 70 / 100;
		}
		else if (in.darklight && in.demon)
		{
			roll += roll * 60 / 100;
		}
		if (in.dragon && in.dragonHunterLance)
		{
			roll = roll * 6 / 5;
		}
		else if (in.dragon && in.dragonHunterWand)
		{
			roll = roll * 7 / 4;
		}
		return roll;
	}

	private static int rangedAttackRoll(CombatInput in)
	{
		int effective = in.rangedLevel * in.rangedAttackPrayer / 100;
		if (in.stance == AttackStance.RANGED_ACCURATE)
		{
			effective += 3;
		}
		effective += 8;
		if (in.voidRangedElite)
		{
			effective = effective * 9 / 8;
		}
		else if (in.voidRanged)
		{
			effective = effective * 11 / 10;
		}
		int roll = effective * (in.rangedBonus + 64);
		roll = applyRangedGearBonus(in, roll);
		if (in.twistedBow)
		{
			roll = tbowScale(roll, tbowMagic(in), true);
		}
		if (in.dragon && in.dragonHunterCrossbow)
		{
			roll = roll * 13 / 10;
		}
		return roll;
	}

	private static int magicAttackRoll(CombatInput in)
	{
		int effective = in.magicLevel * in.magicAttackPrayer / 100;
		if (in.stance == AttackStance.MAGIC_ACCURATE)
		{
			effective += 2;
		}
		effective += 9;
		if (in.voidMagic || in.voidMagicElite)
		{
			effective = effective * 29 / 20;
		}
		int magicBonus = in.magicBonus;
		if (in.shadow)
		{
			magicBonus *= in.toa ? 4 : 3;
		}
		int base = effective * (magicBonus + 64);
		int roll = base;
		if (in.avarice && in.revenant)
		{
			roll = roll * 120 / 100;
		}
		else if (in.undead && in.salveEnchantedImbued)
		{
			roll = roll * 120 / 100;
		}
		else if (in.undead && in.salveImbued)
		{
			roll = roll * 115 / 100;
		}
		else if (in.blackMaskImbued && in.onSlayerTask)
		{
			roll = roll * 23 / 20;
		}
		if (in.dragon)
		{
			if (in.dragonHunterCrossbow)
			{
				roll = roll * 13 / 10;
			}
			else if (in.dragonHunterLance)
			{
				roll = roll * 6 / 5;
			}
			else if (in.dragonHunterWand)
			{
				roll = roll * 7 / 4;
			}
		}
		if (in.weaknessElement != null && in.weaknessElement.equals(in.spellElement) && in.weaknessPercent > 0)
		{
			roll += base * in.weaknessPercent / 100;
		}
		if (in.tomeWater && "water".equals(in.spellElement))
		{
			roll = roll * 6 / 5;
		}
		return roll;
	}

	private static int applyMeleeGearBonus(CombatInput in, int value)
	{
		if (in.avarice && in.revenant)
		{
			return value * 6 / 5;
		}
		if (in.undead && (in.salveEnchanted || in.salveEnchantedImbued))
		{
			return value * 6 / 5;
		}
		if (in.undead && (in.salve || in.salveImbued))
		{
			return value * 7 / 6;
		}
		if (in.onSlayerTask && (in.blackMask || in.blackMaskImbued))
		{
			return value * 7 / 6;
		}
		return value;
	}

	private static int applyRangedGearBonus(CombatInput in, int value)
	{
		if (in.avarice && in.revenant)
		{
			return value * 6 / 5;
		}
		if (in.undead && in.salveEnchantedImbued)
		{
			return value * 6 / 5;
		}
		if (in.undead && in.salveImbued)
		{
			return value * 7 / 6;
		}
		if (in.onSlayerTask && in.blackMaskImbued)
		{
			return value * 23 / 20;
		}
		return value;
	}

	private static int tbowMagic(CombatInput in)
	{
		int cap = in.xerician ? 350 : 250;
		return Math.min(cap, Math.max(in.npcMagic, in.npcOffensiveMagic));
	}

	private static boolean tomeMatches(CombatInput in)
	{
		return (in.tomeFire && "fire".equals(in.spellElement))
			|| (in.tomeWater && "water".equals(in.spellElement))
			|| (in.tomeEarth && "earth".equals(in.spellElement));
	}

	private static String styleLabel(CombatInput in)
	{
		if (in.combatStyle == CombatStyle.MAGIC)
		{
			if (in.spellName != null && !in.spellName.isEmpty())
			{
				return in.spellName;
			}
			return "Magic";
		}
		if (in.combatStyle == CombatStyle.RANGED)
		{
			switch (in.rangedStyle)
			{
				case LIGHT:
					return "Ranged (light)";
				case HEAVY:
					return "Ranged (heavy)";
				case STANDARD:
				default:
					return "Ranged";
			}
		}
		switch (in.meleeStyle)
		{
			case STAB:
				return "Stab";
			case CRUSH:
				return "Crush";
			case SLASH:
			default:
				return "Slash";
		}
	}
}
