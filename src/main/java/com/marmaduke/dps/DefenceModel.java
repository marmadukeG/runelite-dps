package com.marmaduke.dps;

/**
 * Defence, magic, and the other combat levels of one NPC after reductions this client has seen.
 * Percent drains use integer truncation: {@code level - level * percent / 100}.
 */
final class DefenceModel
{
	private static final int VULNERABILITY_TICKS = 100;

	int defence;
	int magic;
	int attack;
	int strength;
	int ranged;
	private final int baseDefence;
	private final int baseMagic;

	private int dwhHits;
	private int elderHits;
	private int bgsDamage;
	private int arclightHits;
	private int emberlightHits;
	private int darklightHits;
	private boolean accursed;
	private int tonalzticsPercent;
	private int vulnLeft;
	private int vulnAmount;
	private boolean vulnFifteen;

	DefenceModel(NpcCombatStats stats)
	{
		this.defence = stats.def;
		this.magic = stats.magic;
		this.attack = stats.atk;
		this.strength = stats.str;
		this.ranged = stats.ranged;
		this.baseDefence = stats.def;
		this.baseMagic = stats.magic;
	}

	void applyDragonWarhammer(boolean tektonMiss)
	{
		int percent = tektonMiss ? 5 : 30;
		defence = reducePercent(defence, percent);
		if (tektonMiss)
		{
			return;
		}
		dwhHits++;
	}

	void applyElderMaul(boolean tektonMiss)
	{
		defence = reducePercent(defence, tektonMiss ? 5 : 35);
		if (!tektonMiss)
		{
			elderHits++;
		}
	}

	void applyBandosGodsword(int damage)
	{
		if (damage <= 0)
		{
			return;
		}
		bgsDamage += damage;
		int left = damage;
		left = drainFlat(Stat.DEFENCE, left);
		left = drainFlat(Stat.STRENGTH, left);
		left = drainFlat(Stat.ATTACK, left);
		left = drainFlat(Stat.MAGIC, left);
		drainFlat(Stat.RANGED, left);
	}

	/**
	 * Accursed sceptre drains 15% of current Defence and Magic, and cannot push either
	 * below 85% of the level the NPC had when it was first tracked.
	 */
	void applyAccursedSceptre()
	{
		accursed = true;
		defence = capPercent(defence, baseDefence, 15);
		magic = capPercent(magic, baseMagic, 15);
	}

	void applyArclight()
	{
		arclightHits++;
		attack = reducePercent(attack, 5);
		strength = reducePercent(strength, 5);
		defence = reducePercent(defence, 5);
	}

	void applyDarklight()
	{
		darklightHits++;
		attack = reducePercent(attack, 5);
		strength = reducePercent(strength, 5);
		defence = reducePercent(defence, 5);
	}

	void applyEmberlight()
	{
		emberlightHits++;
		attack = reducePercent(attack, 5);
		strength = reducePercent(strength, 5);
		defence = reducePercent(defence, 5);
		magic = reducePercent(magic, 5);
		ranged = reducePercent(ranged, 5);
	}

	/**
	 * @param damagingHits number of tonalztics hitsplats above 0. Two damaging hits drain
	 *                     20% of the target's Magic level from Defence; one drains 10%.
	 */
	void applyTonalztics(int damagingHits)
	{
		int percent = damagingHits >= 2 ? 20 : damagingHits == 1 ? 10 : 0;
		if (percent == 0)
		{
			return;
		}
		tonalzticsPercent += percent;
		int drain = magic * percent / 100;
		defence = Math.max(0, defence - drain);
	}

	void applyVulnerability(int percent)
	{
		if (vulnLeft > 0)
		{
			vulnLeft = VULNERABILITY_TICKS;
			return;
		}
		vulnAmount = defence * percent / 100;
		defence = Math.max(0, defence - vulnAmount);
		vulnLeft = VULNERABILITY_TICKS;
		vulnFifteen = percent >= 15;
	}

	void tick()
	{
		if (vulnLeft <= 0)
		{
			return;
		}
		vulnLeft--;
		if (vulnLeft == 0)
		{
			defence += vulnAmount;
			vulnAmount = 0;
		}
	}

	/**
	 * Overlay text for defence. Unreduced levels are the current number. A reduced level is the
	 * starting defence, a hyphen, and the points removed: 90 reduced by 30 is {@code 90-30}.
	 */
	String overlayDefence()
	{
		if (defence < baseDefence)
		{
			return baseDefence + "-" + (baseDefence - defence);
		}
		return Integer.toString(defence);
	}

	String describe()
	{
		StringBuilder builder = new StringBuilder();
		append(builder, dwhHits, "DWH");
		append(builder, elderHits, "Elder maul");
		if (bgsDamage > 0)
		{
			appendRaw(builder, "BGS " + bgsDamage);
		}
		if (accursed)
		{
			appendRaw(builder, "Accursed");
		}
		append(builder, arclightHits, "Arclight");
		append(builder, darklightHits, "Darklight");
		append(builder, emberlightHits, "Emberlight");
		if (tonalzticsPercent > 0)
		{
			appendRaw(builder, "Tonalztics " + tonalzticsPercent + "%");
		}
		if (vulnLeft > 0)
		{
			appendRaw(builder, vulnFifteen ? "Vuln 15%" : "Vuln");
		}
		return builder.toString();
	}

	private static void append(StringBuilder builder, int hits, String label)
	{
		if (hits <= 0)
		{
			return;
		}
		appendRaw(builder, hits == 1 ? label : label + " x" + hits);
	}

	private static void appendRaw(StringBuilder builder, String text)
	{
		if (builder.length() > 0)
		{
			builder.append(", ");
		}
		builder.append(text);
	}

	private int drainFlat(Stat stat, int amount)
	{
		if (amount <= 0)
		{
			return 0;
		}
		int current = get(stat);
		int taken = Math.min(current, amount);
		set(stat, current - taken);
		return amount - taken;
	}

	private static int reducePercent(int level, int percent)
	{
		return Math.max(0, level - level * percent / 100);
	}

	private static int capPercent(int current, int base, int percent)
	{
		int floor = base * (100 - percent) / 100;
		int drained = current - current * percent / 100;
		if (drained < floor)
		{
			drained = floor;
		}
		return Math.min(current, Math.max(0, drained));
	}

	private int get(Stat stat)
	{
		switch (stat)
		{
			case DEFENCE:
				return defence;
			case MAGIC:
				return magic;
			case ATTACK:
				return attack;
			case STRENGTH:
				return strength;
			case RANGED:
			default:
				return ranged;
		}
	}

	private void set(Stat stat, int value)
	{
		switch (stat)
		{
			case DEFENCE:
				defence = value;
				break;
			case MAGIC:
				magic = value;
				break;
			case ATTACK:
				attack = value;
				break;
			case STRENGTH:
				strength = value;
				break;
			case RANGED:
			default:
				ranged = value;
				break;
		}
	}

	private enum Stat
	{
		DEFENCE,
		STRENGTH,
		ATTACK,
		MAGIC,
		RANGED
	}
}
