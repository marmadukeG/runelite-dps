package com.marmaduke.dps;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Autocast varbit 276 values, from the OSRS wiki page RuneScape:Varbit/276,
 * and the spell's base max hit before magic-damage gear.
 *
 * <p>Elemental strike, bolt, blast, wave, and surge spells no longer keep a flat
 * base hit. Since the 29 May 2024 combat rebalance, each of those spells uses the
 * base hit of the highest spell in its tier that the player's Magic level has
 * unlocked, and it never passes the fire spell of that tier. Wind Strike is 2
 * until Water Strike (4), 6 once Earth Strike is unlocked, and 8 at level 13.
 */
final class SpellTable
{
	private static final int[] STRIKE_LEVELS = {1, 5, 9, 13};
	private static final int[] STRIKE_HITS = {2, 4, 6, 8};
	private static final int[] BOLT_LEVELS = {17, 23, 29, 35};
	private static final int[] BOLT_HITS = {9, 10, 11, 12};
	private static final int[] BLAST_LEVELS = {41, 47, 53, 59};
	private static final int[] BLAST_HITS = {13, 14, 15, 16};
	private static final int[] WAVE_LEVELS = {62, 65, 70, 75};
	private static final int[] WAVE_HITS = {17, 18, 19, 20};
	private static final int[] SURGE_LEVELS = {81, 85, 90, 95};
	private static final int[] SURGE_HITS = {21, 22, 23, 24};

	final String name;
	final int maxHit;
	final String element;
	final String spellbook;
	private final Tier tier;

	private SpellTable(String name, int maxHit, String element, String spellbook, Tier tier)
	{
		this.name = name;
		this.maxHit = maxHit;
		this.element = element;
		this.spellbook = spellbook;
		this.tier = tier;
	}

	private static final Map<Integer, SpellTable> BY_AUTOCAST = build();
	private static final Map<String, SpellTable> BY_NAME = indexNames(BY_AUTOCAST);

	static SpellTable fromAutocast(int varbitValue)
	{
		return BY_AUTOCAST.get(varbitValue);
	}

	/**
	 * Base hit before chaos gauntlets and magic-damage gear. Non-elemental spells
	 * keep {@code listedMaxHit}. Elemental spells scale up to the fire spell of
	 * their tier and never scale below the hit passed in.
	 */
	static int scaledBaseHit(String spellName, int listedMaxHit, int magicLevel)
	{
		int listed = Math.max(0, listedMaxHit);
		if (spellName == null || spellName.isEmpty())
		{
			return listed;
		}
		SpellTable spell = BY_NAME.get(spellName);
		if (spell == null || spell.tier == null)
		{
			return listed;
		}
		int own = listed > 0 ? listed : spell.maxHit;
		return spell.tier.scale(magicLevel, own);
	}

	private static Map<Integer, SpellTable> build()
	{
		Map<Integer, SpellTable> spells = new HashMap<Integer, SpellTable>();
		elemental(spells, 1, "Wind Strike", 2, "air", STRIKE_LEVELS, STRIKE_HITS);
		elemental(spells, 2, "Water Strike", 4, "water", STRIKE_LEVELS, STRIKE_HITS);
		elemental(spells, 3, "Earth Strike", 6, "earth", STRIKE_LEVELS, STRIKE_HITS);
		elemental(spells, 4, "Fire Strike", 8, "fire", STRIKE_LEVELS, STRIKE_HITS);
		elemental(spells, 5, "Wind Bolt", 9, "air", BOLT_LEVELS, BOLT_HITS);
		elemental(spells, 6, "Water Bolt", 10, "water", BOLT_LEVELS, BOLT_HITS);
		elemental(spells, 7, "Earth Bolt", 11, "earth", BOLT_LEVELS, BOLT_HITS);
		elemental(spells, 8, "Fire Bolt", 12, "fire", BOLT_LEVELS, BOLT_HITS);
		elemental(spells, 9, "Wind Blast", 13, "air", BLAST_LEVELS, BLAST_HITS);
		elemental(spells, 10, "Water Blast", 14, "water", BLAST_LEVELS, BLAST_HITS);
		elemental(spells, 11, "Earth Blast", 15, "earth", BLAST_LEVELS, BLAST_HITS);
		elemental(spells, 12, "Fire Blast", 16, "fire", BLAST_LEVELS, BLAST_HITS);
		elemental(spells, 13, "Wind Wave", 17, "air", WAVE_LEVELS, WAVE_HITS);
		elemental(spells, 14, "Water Wave", 18, "water", WAVE_LEVELS, WAVE_HITS);
		elemental(spells, 15, "Earth Wave", 19, "earth", WAVE_LEVELS, WAVE_HITS);
		elemental(spells, 16, "Fire Wave", 20, "fire", WAVE_LEVELS, WAVE_HITS);
		standard(spells, 17, "Crumble Undead", 15, "");
		standard(spells, 18, "Magic Dart", 0, "");
		standard(spells, 19, "Claws of Guthix", 20, "");
		standard(spells, 20, "Flames of Zamorak", 20, "");
		ancient(spells, 31, "Smoke Rush", 13);
		ancient(spells, 32, "Shadow Rush", 14);
		ancient(spells, 33, "Blood Rush", 15);
		ancient(spells, 34, "Ice Rush", 16);
		ancient(spells, 35, "Smoke Burst", 17);
		ancient(spells, 36, "Shadow Burst", 18);
		ancient(spells, 37, "Blood Burst", 21);
		ancient(spells, 38, "Ice Burst", 22);
		ancient(spells, 39, "Smoke Blitz", 23);
		ancient(spells, 40, "Shadow Blitz", 24);
		ancient(spells, 41, "Blood Blitz", 25);
		ancient(spells, 42, "Ice Blitz", 26);
		ancient(spells, 43, "Smoke Barrage", 27);
		ancient(spells, 44, "Shadow Barrage", 28);
		ancient(spells, 45, "Blood Barrage", 29);
		ancient(spells, 46, "Ice Barrage", 30);
		standard(spells, 47, "Iban Blast", 25, "");
		elemental(spells, 48, "Wind Surge", 21, "air", SURGE_LEVELS, SURGE_HITS);
		elemental(spells, 49, "Water Surge", 22, "water", SURGE_LEVELS, SURGE_HITS);
		elemental(spells, 50, "Earth Surge", 23, "earth", SURGE_LEVELS, SURGE_HITS);
		elemental(spells, 51, "Fire Surge", 24, "fire", SURGE_LEVELS, SURGE_HITS);
		standard(spells, 52, "Saradomin Strike", 20, "");
		arceuus(spells, 53, "Inferior Demonbane", 16);
		arceuus(spells, 54, "Superior Demonbane", 23);
		arceuus(spells, 55, "Dark Demonbane", 30);
		arceuus(spells, 56, "Ghostly Grasp", 12);
		arceuus(spells, 57, "Skeletal Grasp", 17);
		arceuus(spells, 58, "Undead Grasp", 24);
		return Collections.unmodifiableMap(spells);
	}

	private static void standard(Map<Integer, SpellTable> spells, int id, String name, int maxHit, String element)
	{
		spells.put(id, new SpellTable(name, maxHit, element, "standard", null));
	}

	private static void elemental(
		Map<Integer, SpellTable> spells,
		int id,
		String name,
		int maxHit,
		String element,
		int[] levels,
		int[] hits)
	{
		spells.put(id, new SpellTable(name, maxHit, element, "standard", new Tier(levels, hits)));
	}

	private static void ancient(Map<Integer, SpellTable> spells, int id, String name, int maxHit)
	{
		spells.put(id, new SpellTable(name, maxHit, "", "ancient", null));
	}

	private static void arceuus(Map<Integer, SpellTable> spells, int id, String name, int maxHit)
	{
		spells.put(id, new SpellTable(name, maxHit, "", "arceuus", null));
	}

	private static Map<String, SpellTable> indexNames(Map<Integer, SpellTable> spells)
	{
		Map<String, SpellTable> byName = new HashMap<String, SpellTable>();
		for (SpellTable spell : spells.values())
		{
			byName.put(spell.name, spell);
		}
		return Collections.unmodifiableMap(byName);
	}

	/**
	 * One elemental tier. {@code levels} and {@code hits} are parallel and run from
	 * the wind spell to the fire spell.
	 */
	private static final class Tier
	{
		private final int[] levels;
		private final int[] hits;

		private Tier(int[] levels, int[] hits)
		{
			this.levels = levels;
			this.hits = hits;
		}

		private int scale(int magicLevel, int ownHit)
		{
			int hit = ownHit;
			for (int i = 0; i < levels.length; i++)
			{
				if (magicLevel >= levels[i] && hits[i] > hit)
				{
					hit = hits[i];
				}
			}
			return hit;
		}
	}
}
