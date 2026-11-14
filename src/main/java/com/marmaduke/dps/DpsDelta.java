package com.marmaduke.dps;

import java.util.EnumSet;
import java.util.Locale;
import net.runelite.api.Prayer;

/**
 * Hypothetical DPS if one prayer or one potion were toggled. The inputs passed in are not modified.
 */
final class DpsDelta
{
	private DpsDelta()
	{
	}

	static Double prayer(CombatInput current, EnumSet<Prayer> active, Prayer prayer)
	{
		if (current == null || prayer == null || prayer.name().startsWith("RP_"))
		{
			return null;
		}
		CombatInput next = current.copy();
		PrayerBonus.apply(next, PrayerBonus.toggle(active, prayer));
		return difference(current, next);
	}

	static Double potion(CombatInput current, int baseAttack, int baseStrength, int baseRanged, int baseMagic, String itemName)
	{
		if (current == null || !PotionBoosts.affectsCombat(itemName))
		{
			return null;
		}
		CombatInput next = current.copy();
		PotionBoosts.apply(next, baseAttack, baseStrength, baseRanged, baseMagic, itemName);
		return difference(current, next);
	}

	static String format(double delta)
	{
		return String.format(Locale.US, "%+.2f DPS", delta);
	}

	private static Double difference(CombatInput current, CombatInput next)
	{
		DpsEstimate now = CombatMath.estimate(current, "", "");
		DpsEstimate then = CombatMath.estimate(next, "", "");
		if (now.attackSpeedTicks <= 0 || then.attackSpeedTicks <= 0)
		{
			return null;
		}
		return Double.valueOf(then.dps - now.dps);
	}
}
