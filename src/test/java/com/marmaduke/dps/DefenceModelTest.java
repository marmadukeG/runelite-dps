package com.marmaduke.dps;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DefenceModelTest
{
	@Test
	public void dragonWarhammerStacksOnTheCurrentLevel()
	{
		DefenceModel model = new DefenceModel(stats(135));
		model.applyDragonWarhammer(false);
		assertEquals(95, model.defence);
		model.applyDragonWarhammer(false);
		assertEquals(67, model.defence);
		assertTrue(model.describe().contains("DWH x2"));
	}

	@Test
	public void bandosGodswordDrainsDefenceBeforeStrength()
	{
		NpcCombatStats stats = stats(30);
		stats.str = 40;
		stats.atk = 10;
		stats.magic = 5;
		DefenceModel model = new DefenceModel(stats);
		model.applyBandosGodsword(50);

		assertEquals(0, model.defence);
		assertEquals(20, model.strength);
		assertEquals(10, model.attack);
		assertEquals(5, model.magic);
	}

	@Test
	public void accursedSceptreStopsAtFifteenPercent()
	{
		DefenceModel model = new DefenceModel(stats(100));
		model.applyAccursedSceptre();
		assertEquals(85, model.defence);
		assertEquals(85, model.magic);
		model.applyAccursedSceptre();
		assertEquals(85, model.defence);
	}

	@Test
	public void vulnerabilityExpiresAfterOneMinute()
	{
		DefenceModel model = new DefenceModel(stats(100));
		model.applyVulnerability(10);
		assertEquals(90, model.defence);
		for (int tick = 0; tick < 99; tick++)
		{
			model.tick();
		}
		assertEquals(90, model.defence);
		model.tick();
		assertEquals(100, model.defence);
	}

	private static NpcCombatStats stats(int defence)
	{
		NpcCombatStats stats = new NpcCombatStats();
		stats.def = defence;
		stats.magic = defence;
		stats.atk = 1;
		stats.str = 1;
		stats.ranged = 1;
		return stats;
	}
}
