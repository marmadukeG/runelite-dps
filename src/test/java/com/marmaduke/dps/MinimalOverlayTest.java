package com.marmaduke.dps;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

	@Test
	public void fullOverlayPutsEveryStatOnOneLine()
	{
		DpsEstimate estimate = new DpsEstimate(
			"Doom of Mokhaiotl (Delve 1)",
			12.34,
			86,
			0.512,
			5,
			"Rapid",
			"Def 120 (vulnerability)",
			"");

		String line = DpsOverlay.fullLine(estimate, true);

		assertEquals(
			"DPS 12.34   Max hit 86   Hit chance 51.2%   Def 120 (vulnerability)   3.0s   Rapid",
			line);
		assertFalse(line.contains("(standard)"));
		assertFalse(line.contains("..."));
		assertFalse(line.contains("\n"));

		FontMetrics metrics = metrics();
		int inner = DpsOverlay.contentWidth(metrics, line, "", "Right-click overlay to unpin");
		assertTrue(inner >= metrics.stringWidth(line));
	}

	private static FontMetrics metrics()
	{
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
		return graphics.getFontMetrics();
	}
}
