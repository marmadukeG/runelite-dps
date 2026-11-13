package com.marmaduke.dps;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class MinimalOverlayTest
{
	@Test
	public void minimalLinesAreDpsMaxHitAndHitChanceWithNoLabels()
	{
		DpsEstimate estimate = new DpsEstimate(
			"Doom of Mokhaiotl",
			8.5,
			42,
			0.635,
			4,
			"Rapid (standard)",
			"defence reduced",
			"note");

		assertArrayEquals(
			new String[] {"8.50", "42", "63.5%"},
			DpsOverlay.minimalLines(estimate));
	}
}
