package com.marmaduke.dps;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Autocast varbit 276 values, from the OSRS wiki page RuneScape:Varbit/276,
 * and the spell's base max hit before magic-damage gear.
 */
final class SpellTable
{
	final String name;
	final int maxHit;
	final String element;
	final String spellbook;

	private SpellTable(String name, int maxHit, String element, String spellbook)
	{
		this.name = name;
		this.maxHit = maxHit;
		this.element = element;
		this.spellbook = spellbook;
	}

	private static final Map<Integer, SpellTable> BY_AUTOCAST = build();

	static SpellTable fromAutocast(int varbitValue)
	{
		return BY_AUTOCAST.get(varbitValue);
	}

	private static Map<Integer, SpellTable> build()
	{
		Map<Integer, SpellTable> spells = new HashMap<Integer, SpellTable>();
		standard(spells, 1, "Wind Strike", 2, "air");
		standard(spells, 2, "Water Strike", 4, "water");
		standard(spells, 3, "Earth Strike", 6, "earth");
		standard(spells, 4, "Fire Strike", 8, "fire");
		standard(spells, 5, "Wind Bolt", 9, "air");
		standard(spells, 6, "Water Bolt", 10, "water");
		standard(spells, 7, "Earth Bolt", 11, "earth");
		standard(spells, 8, "Fire Bolt", 12, "fire");
		standard(spells, 9, "Wind Blast", 13, "air");
		standard(spells, 10, "Water Blast", 14, "water");
		standard(spells, 11, "Earth Blast", 15, "earth");
		standard(spells, 12, "Fire Blast", 16, "fire");
		standard(spells, 13, "Wind Wave", 17, "air");
		standard(spells, 14, "Water Wave", 18, "water");
		standard(spells, 15, "Earth Wave", 19, "earth");
		standard(spells, 16, "Fire Wave", 20, "fire");
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
		standard(spells, 48, "Wind Surge", 21, "air");
		standard(spells, 49, "Water Surge", 22, "water");
		standard(spells, 50, "Earth Surge", 23, "earth");
		standard(spells, 51, "Fire Surge", 24, "fire");
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
		spells.put(id, new SpellTable(name, maxHit, element, "standard"));
	}

	private static void ancient(Map<Integer, SpellTable> spells, int id, String name, int maxHit)
	{
		spells.put(id, new SpellTable(name, maxHit, "", "ancient"));
	}

	private static void arceuus(Map<Integer, SpellTable> spells, int id, String name, int maxHit)
	{
		spells.put(id, new SpellTable(name, maxHit, "", "arceuus"));
	}
}
