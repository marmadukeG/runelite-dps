package com.marmaduke.dps;

import java.util.Locale;

/**
 * Combat-potion boosts using the same integer percent rules as RuneLite's item stats:
 * a boost is added to the current level and capped at base + boost. A drain is taken off
 * the current level. Drinking is hypothetical; the caller keeps the live levels unchanged.
 */
final class PotionBoosts
{
	private PotionBoosts()
	{
	}

	static void apply(CombatInput in, int baseAttack, int baseStrength, int baseRanged, int baseMagic, String itemName)
	{
		String name = canonical(itemName);
		if (!affectsCombat(itemName) || name == null)
		{
			return;
		}
		if (name.startsWith("overload (+)"))
		{
			boostAll(in, baseAttack, baseStrength, baseRanged, baseMagic, 0.16, 6);
		}
		else if (name.startsWith("overload (-)"))
		{
			boostAll(in, baseAttack, baseStrength, baseRanged, baseMagic, 0.10, 4);
		}
		else if (name.startsWith("overload"))
		{
			boost(in, SkillStat.ATTACK, baseAttack, 0.15, 5);
			boost(in, SkillStat.STRENGTH, baseStrength, 0.15, 5);
			boost(in, SkillStat.RANGED, baseRanged, 0.15, 5);
			boost(in, SkillStat.MAGIC, baseMagic, 0.15, 5);
		}
		else if (name.startsWith("smelling salt"))
		{
			boostAll(in, baseAttack, baseStrength, baseRanged, baseMagic, 0.16, 11);
		}
		else if (name.startsWith("elder (+)"))
		{
			boost(in, SkillStat.ATTACK, baseAttack, 0.16, 6);
			boost(in, SkillStat.STRENGTH, baseStrength, 0.16, 6);
		}
		else if (name.startsWith("elder (-)"))
		{
			boost(in, SkillStat.ATTACK, baseAttack, 0.10, 4);
			boost(in, SkillStat.STRENGTH, baseStrength, 0.10, 4);
		}
		else if (name.startsWith("elder"))
		{
			boost(in, SkillStat.ATTACK, baseAttack, 0.13, 5);
			boost(in, SkillStat.STRENGTH, baseStrength, 0.13, 5);
		}
		else if (name.startsWith("twisted (+)"))
		{
			boost(in, SkillStat.RANGED, baseRanged, 0.16, 6);
		}
		else if (name.startsWith("twisted (-)"))
		{
			boost(in, SkillStat.RANGED, baseRanged, 0.10, 4);
		}
		else if (name.startsWith("twisted potion"))
		{
			boost(in, SkillStat.RANGED, baseRanged, 0.13, 5);
		}
		else if (name.startsWith("kodai (+)"))
		{
			boost(in, SkillStat.MAGIC, baseMagic, 0.16, 6);
		}
		else if (name.startsWith("kodai (-)"))
		{
			boost(in, SkillStat.MAGIC, baseMagic, 0.10, 4);
		}
		else if (name.startsWith("kodai"))
		{
			boost(in, SkillStat.MAGIC, baseMagic, 0.13, 5);
		}
		else if (name.startsWith("super combat"))
		{
			boost(in, SkillStat.ATTACK, baseAttack, 0.15, 5);
			boost(in, SkillStat.STRENGTH, baseStrength, 0.15, 5);
		}
		else if (name.startsWith("bastion"))
		{
			boost(in, SkillStat.RANGED, baseRanged, 0.10, 4);
		}
		else if (name.startsWith("battlemage"))
		{
			boost(in, SkillStat.MAGIC, baseMagic, 0, 4);
		}
		else if (name.startsWith("zamorak brew"))
		{
			boost(in, SkillStat.ATTACK, baseAttack, 0.20, 2);
			boost(in, SkillStat.STRENGTH, baseStrength, 0.12, 2);
		}
		else if (name.startsWith("saradomin brew"))
		{
			drain(in, SkillStat.ATTACK, 0.10, 2);
			drain(in, SkillStat.STRENGTH, 0.10, 2);
			drain(in, SkillStat.RANGED, 0.10, 2);
			drain(in, SkillStat.MAGIC, 0.10, 2);
		}
		else if (name.startsWith("forgotten brew"))
		{
			boost(in, SkillStat.MAGIC, baseMagic, 0.08, 3);
			drain(in, SkillStat.ATTACK, 0.10, 2);
			drain(in, SkillStat.STRENGTH, 0.10, 2);
		}
		else if (name.startsWith("ancient brew"))
		{
			boost(in, SkillStat.MAGIC, baseMagic, 0.05, 2);
			drain(in, SkillStat.ATTACK, 0.10, 2);
			drain(in, SkillStat.STRENGTH, 0.10, 2);
		}
		else if (name.startsWith("armadyl brew"))
		{
			boost(in, SkillStat.RANGED, baseRanged, 0.10, 4);
			drain(in, SkillStat.ATTACK, 0.10, 2);
			drain(in, SkillStat.STRENGTH, 0.10, 2);
			drain(in, SkillStat.MAGIC, 0.10, 2);
		}
		else if (name.startsWith("super ranging") || name.startsWith("ranging"))
		{
			if (name.startsWith("super ranging"))
			{
				boost(in, SkillStat.RANGED, baseRanged, 0.15, 5);
			}
			else
			{
				boost(in, SkillStat.RANGED, baseRanged, 0.10, 4);
			}
		}
		else if (name.startsWith("super magic"))
		{
			boost(in, SkillStat.MAGIC, baseMagic, 0.15, 5);
		}
		else if (name.startsWith("magic essence"))
		{
			boost(in, SkillStat.MAGIC, baseMagic, 0, 3);
		}
		else if (name.startsWith("magic potion") || name.startsWith("magic mix"))
		{
			boost(in, SkillStat.MAGIC, baseMagic, 0, 4);
		}
		else if (name.startsWith("super attack"))
		{
			boost(in, SkillStat.ATTACK, baseAttack, 0.15, 5);
		}
		else if (name.startsWith("super strength"))
		{
			boost(in, SkillStat.STRENGTH, baseStrength, 0.15, 5);
		}
		else if (name.startsWith("combat"))
		{
			boost(in, SkillStat.ATTACK, baseAttack, 0.10, 3);
			boost(in, SkillStat.STRENGTH, baseStrength, 0.10, 3);
		}
		else if (name.startsWith("attack"))
		{
			boost(in, SkillStat.ATTACK, baseAttack, 0.10, 3);
		}
		else if (name.startsWith("strength"))
		{
			boost(in, SkillStat.STRENGTH, baseStrength, 0.10, 3);
		}
	}

	static boolean affectsCombat(String itemName)
	{
		String name = canonical(itemName);
		if (name == null || !isDrink(name))
		{
			return false;
		}
		return name.startsWith("overload")
			|| name.startsWith("smelling salt")
			|| name.startsWith("elder")
			|| name.startsWith("twisted (+)")
			|| name.startsWith("twisted (-)")
			|| name.startsWith("twisted potion")
			|| name.startsWith("kodai")
			|| name.startsWith("super combat")
			|| name.startsWith("bastion")
			|| name.startsWith("battlemage")
			|| name.startsWith("zamorak brew")
			|| name.startsWith("saradomin brew")
			|| name.startsWith("forgotten brew")
			|| name.startsWith("ancient brew")
			|| name.startsWith("armadyl brew")
			|| name.startsWith("super ranging")
			|| name.startsWith("ranging")
			|| name.startsWith("super magic")
			|| name.startsWith("magic essence")
			|| name.startsWith("magic potion")
			|| name.startsWith("magic mix")
			|| name.startsWith("super attack")
			|| name.startsWith("super strength")
			|| name.startsWith("combat")
			|| name.startsWith("attack")
			|| name.startsWith("strength");
	}

	private static boolean isDrink(String name)
	{
		return name.contains("potion")
			|| name.contains("brew")
			|| name.contains("overload")
			|| name.contains("smelling salt")
			|| name.contains("essence")
			|| name.endsWith("(+) ")
			|| name.endsWith("(+)")
			|| name.endsWith("(-)")
			|| "super attack".equals(name)
			|| "super strength".equals(name);
	}

	private static String canonical(String itemName)
	{
		if (itemName == null)
		{
			return null;
		}
		String name = itemName.toLowerCase(Locale.US);
		name = name.replace("blighted ", "");
		name = name.replace("divine ", "");
		name = name.replaceAll("\\(\\d+\\)", "");
		name = name.replace(" mix", "");
		name = name.trim();
		if (name.isEmpty())
		{
			return null;
		}
		return name;
	}

	private static void boostAll(CombatInput in, int baseAttack, int baseStrength, int baseRanged, int baseMagic, double percent, int flat)
	{
		boost(in, SkillStat.ATTACK, baseAttack, percent, flat);
		boost(in, SkillStat.STRENGTH, baseStrength, percent, flat);
		boost(in, SkillStat.RANGED, baseRanged, percent, flat);
		boost(in, SkillStat.MAGIC, baseMagic, percent, flat);
	}

	private static void boost(CombatInput in, SkillStat stat, int base, double percent, int flat)
	{
		int current = stat.get(in);
		int delta = (int) (base * percent) + flat;
		if (delta <= 0)
		{
			stat.set(in, Math.max(current, base + delta));
			return;
		}
		int cap = base + delta;
		if (current > cap)
		{
			return;
		}
		int raised = current + delta;
		if (raised > cap)
		{
			raised = cap;
		}
		stat.set(in, raised);
	}

	private static void drain(CombatInput in, SkillStat stat, double percent, int flat)
	{
		int current = stat.get(in);
		int next = current - (int) (current * percent) - flat;
		if (next < 0)
		{
			next = 0;
		}
		stat.set(in, next);
	}

	private enum SkillStat
	{
		ATTACK,
		STRENGTH,
		RANGED,
		MAGIC;

		int get(CombatInput in)
		{
			switch (this)
			{
				case ATTACK:
					return in.attackLevel;
				case STRENGTH:
					return in.strengthLevel;
				case RANGED:
					return in.rangedLevel;
				case MAGIC:
				default:
					return in.magicLevel;
			}
		}

		void set(CombatInput in, int value)
		{
			switch (this)
			{
				case ATTACK:
					in.attackLevel = value;
					break;
				case STRENGTH:
					in.strengthLevel = value;
					break;
				case RANGED:
					in.rangedLevel = value;
					break;
				case MAGIC:
				default:
					in.magicLevel = value;
					break;
			}
		}
	}
}
