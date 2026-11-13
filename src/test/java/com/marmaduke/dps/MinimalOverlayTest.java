package com.marmaduke.dps;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
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
	public void fullOverlayKeepsEachStatOnOneLine()
	{
		DpsEstimate estimate = new DpsEstimate(
			"Doom of Mokhaiotl (Delve 1)",
			12.34,
			86,
			0.512,
			5,
			"Rapid",
			"90-30",
			"");

		List<String[]> rows = DpsOverlay.fullRows(estimate, true);

		assertEquals(6, rows.size());
		assertArrayEquals(new String[] {"DPS", "12.34"}, rows.get(0));
		assertArrayEquals(new String[] {"Max hit", "86"}, rows.get(1));
		assertArrayEquals(new String[] {"Hit chance", "51.2%"}, rows.get(2));
		assertArrayEquals(new String[] {"90-30", ""}, rows.get(3));
		assertArrayEquals(new String[] {"Attack speed", "3.0s"}, rows.get(4));
		assertArrayEquals(new String[] {"Stance", "Rapid"}, rows.get(5));

		FontMetrics metrics = metrics();
		int inner = DpsOverlay.contentWidth(metrics, rows, "", "Right-click overlay to unpin");
		for (int i = 0; i < rows.size(); i++)
		{
			String[] row = rows.get(i);
			assertFalse(row[0].contains("(standard)"));
			assertFalse(row[1].contains("(standard)"));
			assertFalse(row[1].contains("..."));
			int needed = metrics.stringWidth(row[0]) + metrics.stringWidth(row[1]);
			assertTrue(row[0], inner > needed);
		}
	}

	private static FontMetrics metrics()
	{
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
		return graphics.getFontMetrics();
	}
}
