package com.marmaduke.dps;

import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
}
