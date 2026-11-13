package com.marmaduke.dps;

import java.util.EnumSet;
import net.runelite.api.Client;
import net.runelite.api.Prayer;

/**
 * Offensive prayer multipliers. Only one prayer in each style group applies, matching the
 * client: turning Rigour on turns Eagle Eye off, and the same for the melee and magic groups.
 */
final class PrayerBonus
{
	private PrayerBonus()
	{
	}

	static EnumSet<Prayer> active(Client client)
	{
		EnumSet<Prayer> prayers = EnumSet.noneOf(Prayer.class);
		Prayer[] all = Prayer.values();
		for (int i = 0; i < all.length; i++)
		{
			if (client.isPrayerActive(all[i]))
			{
				prayers.add(all[i]);
			}
		}
		return prayers;
	}

	static EnumSet<Prayer> toggle(EnumSet<Prayer> active, Prayer prayer)
	{
		EnumSet<Prayer> next = EnumSet.noneOf(Prayer.class);
		if (active != null)
		{
			next.addAll(active);
		}
		if (next.contains(prayer))
		{
			next.remove(prayer);
			return next;
		}
		next.add(prayer);
		clearGroup(next, prayer);
		return next;
	}

	static void apply(CombatInput in, EnumSet<Prayer> active)
	{
		in.meleeAttackPrayer = 100;
		in.meleeStrengthPrayer = 100;
		in.rangedAttackPrayer = 100;
		in.rangedStrengthPrayer = 100;
		in.magicAttackPrayer = 100;
		in.magicDamagePrayerPermille = 0;
		if (active == null || active.isEmpty())
		{
			return;
		}

		if (active.contains(Prayer.ZEAL))
		{
			in.meleeAttackPrayer = 125;
			in.meleeStrengthPrayer = 128;
		}
		else if (active.contains(Prayer.PIETY))
		{
			in.meleeAttackPrayer = 120;
			in.meleeStrengthPrayer = 123;
		}
		else if (active.contains(Prayer.CHIVALRY))
		{
			in.meleeAttackPrayer = 115;
			in.meleeStrengthPrayer = 118;
		}
		else
		{
			if (active.contains(Prayer.ULTIMATE_STRENGTH))
			{
				in.meleeStrengthPrayer = 115;
			}
			else if (active.contains(Prayer.SUPERHUMAN_STRENGTH))
			{
				in.meleeStrengthPrayer = 110;
			}
			else if (active.contains(Prayer.BURST_OF_STRENGTH))
			{
				in.meleeStrengthPrayer = 105;
			}
			if (active.contains(Prayer.INCREDIBLE_REFLEXES))
			{
				in.meleeAttackPrayer = 115;
			}
			else if (active.contains(Prayer.IMPROVED_REFLEXES))
			{
				in.meleeAttackPrayer = 110;
			}
			else if (active.contains(Prayer.CLARITY_OF_THOUGHT))
			{
				in.meleeAttackPrayer = 105;
			}
		}

		if (active.contains(Prayer.RIGOUR))
		{
			in.rangedAttackPrayer = 120;
			in.rangedStrengthPrayer = 123;
		}
		else if (active.contains(Prayer.DEADEYE))
		{
			in.rangedAttackPrayer = 118;
			in.rangedStrengthPrayer = 118;
		}
		else if (active.contains(Prayer.EAGLE_EYE))
		{
			in.rangedAttackPrayer = 115;
			in.rangedStrengthPrayer = 115;
		}
		else if (active.contains(Prayer.HAWK_EYE))
		{
			in.rangedAttackPrayer = 110;
			in.rangedStrengthPrayer = 110;
		}
		else if (active.contains(Prayer.SHARP_EYE))
		{
			in.rangedAttackPrayer = 105;
			in.rangedStrengthPrayer = 105;
		}

		if (active.contains(Prayer.AUGURY))
		{
			in.magicAttackPrayer = 125;
			in.magicDamagePrayerPermille = 40;
		}
		else if (active.contains(Prayer.MYSTIC_VIGOUR))
		{
			in.magicAttackPrayer = 118;
			in.magicDamagePrayerPermille = 30;
		}
		else if (active.contains(Prayer.MYSTIC_MIGHT))
		{
			in.magicAttackPrayer = 115;
			in.magicDamagePrayerPermille = 20;
		}
		else if (active.contains(Prayer.MYSTIC_LORE))
		{
			in.magicAttackPrayer = 110;
			in.magicDamagePrayerPermille = 10;
		}
		else if (active.contains(Prayer.MYSTIC_WILL))
		{
			in.magicAttackPrayer = 105;
		}
	}

	private static void clearGroup(EnumSet<Prayer> next, Prayer prayer)
	{
		if (prayer == Prayer.ZEAL || prayer == Prayer.PIETY || prayer == Prayer.CHIVALRY)
		{
			remove(next, prayer, Prayer.ZEAL, Prayer.PIETY, Prayer.CHIVALRY,
				Prayer.ULTIMATE_STRENGTH, Prayer.SUPERHUMAN_STRENGTH, Prayer.BURST_OF_STRENGTH,
				Prayer.INCREDIBLE_REFLEXES, Prayer.IMPROVED_REFLEXES, Prayer.CLARITY_OF_THOUGHT);
		}
		else if (prayer == Prayer.ULTIMATE_STRENGTH
			|| prayer == Prayer.SUPERHUMAN_STRENGTH
			|| prayer == Prayer.BURST_OF_STRENGTH)
		{
			remove(next, prayer, Prayer.ZEAL, Prayer.PIETY, Prayer.CHIVALRY,
				Prayer.ULTIMATE_STRENGTH, Prayer.SUPERHUMAN_STRENGTH, Prayer.BURST_OF_STRENGTH);
		}
		else if (prayer == Prayer.INCREDIBLE_REFLEXES
			|| prayer == Prayer.IMPROVED_REFLEXES
			|| prayer == Prayer.CLARITY_OF_THOUGHT)
		{
			remove(next, prayer, Prayer.ZEAL, Prayer.PIETY, Prayer.CHIVALRY,
				Prayer.INCREDIBLE_REFLEXES, Prayer.IMPROVED_REFLEXES, Prayer.CLARITY_OF_THOUGHT);
		}
		else if (prayer == Prayer.RIGOUR
			|| prayer == Prayer.DEADEYE
			|| prayer == Prayer.EAGLE_EYE
			|| prayer == Prayer.HAWK_EYE
			|| prayer == Prayer.SHARP_EYE)
		{
			remove(next, prayer, Prayer.RIGOUR, Prayer.DEADEYE, Prayer.EAGLE_EYE, Prayer.HAWK_EYE, Prayer.SHARP_EYE);
		}
		else if (prayer == Prayer.AUGURY
			|| prayer == Prayer.MYSTIC_VIGOUR
			|| prayer == Prayer.MYSTIC_MIGHT
			|| prayer == Prayer.MYSTIC_LORE
			|| prayer == Prayer.MYSTIC_WILL)
		{
			remove(next, prayer, Prayer.AUGURY, Prayer.MYSTIC_VIGOUR, Prayer.MYSTIC_MIGHT,
				Prayer.MYSTIC_LORE, Prayer.MYSTIC_WILL);
		}
	}

	private static void remove(EnumSet<Prayer> next, Prayer keep, Prayer... group)
	{
		for (int i = 0; i < group.length; i++)
		{
			if (group[i] != keep)
			{
				next.remove(group[i]);
			}
		}
	}
}
