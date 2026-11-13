package com.marmaduke.dps;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReactiveModeTest
{
	@Test
	public void attackActionMarksAnNpcThePlayerCanFight()
	{
		assertTrue(DpsPlugin.hasAttackAction(new String[] {"Talk-to", "Attack", null}));
		assertFalse(DpsPlugin.hasAttackAction(new String[] {"Talk-to", "Trade", null}));
		assertFalse(DpsPlugin.hasAttackAction(null));
	}
}
