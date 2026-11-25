package com.marmaduke.dps;

import com.google.gson.Gson;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NpcStatsIndexTest
{
	@Test
	public void loadsAbyssalDemonById()
	{
		NpcStatsIndex index = NpcStatsIndex.load(
			new Gson(),
			NpcStatsIndexTest.class.getResourceAsStream("/npc-stats.json"));

		assertTrue(index.size() > 1000);
		NpcCombatStats demon = index.get(415);
		assertNotNull(demon);
		assertEquals("Abyssal demon", demon.name);
		assertEquals(135, demon.def);
		assertTrue(demon.has("demon"));
	}

	@Test
	public void resolveKeepsTheMonsterWhenItsIdDisappears()
	{
		NpcStatsIndex index = sample();
		NpcCombatStats goblin = index.get(10);

		NpcCombatStats resolved = index.resolve(-1, 0, 99999, "<col=ffff00>Goblin</col>", goblin);

		assertNotNull(resolved);
		assertEquals(10, resolved.id);
		assertEquals(5, resolved.def);
	}

	@Test
	public void resolveUsesTheCompositionIdBeforeGivingUp()
	{
		NpcStatsIndex index = sample();

		NpcCombatStats resolved = index.resolve(99999, 20, -1, "Something else", null);

		assertNotNull(resolved);
		assertEquals("Imp", resolved.name);
		assertEquals(3, resolved.def);
	}

	@Test
	public void resolveUsesAUniqueNameAndDoesNotGuessVersions()
	{
		NpcStatsIndex index = sample();

		NpcCombatStats imp = index.resolve(-1, -1, -1, "Imp", null);
		assertNotNull(imp);
		assertEquals(20, imp.id);
		assertNull(index.resolve(-1, -1, -1, "Goblin", null));
	}

	@Test
	public void rememberedStatsAreNotReusedForADifferentName()
	{
		NpcStatsIndex index = sample();

		assertNull(index.resolve(-1, -1, -1, "Cow", index.get(10)));
	}

	private static NpcStatsIndex sample()
	{
		String json = "["
			+ "{\"id\":10,\"name\":\"Goblin\",\"version\":\"\",\"def\":5},"
			+ "{\"id\":11,\"name\":\"Goblin\",\"version\":\"cave\",\"def\":8},"
			+ "{\"id\":20,\"name\":\"Imp\",\"version\":\"\",\"def\":3}"
			+ "]";
		return NpcStatsIndex.load(
			new Gson(),
			new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
	}
}
