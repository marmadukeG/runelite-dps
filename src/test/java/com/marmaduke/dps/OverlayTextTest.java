package com.marmaduke.dps;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OverlayTextTest
{
	@Test
	public void longNpcNameStaysWithinThePanelWidth()
	{
		FontMetrics metrics = metrics();
		int width = 70;
		List<String> lines = OverlayText.wrap(metrics, "Doom of Mokhaiotl", width);

		assertTrue(lines.size() > 1);
		for (int i = 0; i < lines.size(); i++)
		{
			assertTrue(lines.get(i), metrics.stringWidth(lines.get(i)) <= width);
		}
	}

	@Test
	public void unbrokenTokenIsSplitInsteadOfOverflowing()
	{
		FontMetrics metrics = metrics();
		int width = 40;
		List<String> lines = OverlayText.wrap(metrics, "MokhaiotlMokhaiotl", width);

		assertTrue(lines.size() > 1);
		for (int i = 0; i < lines.size(); i++)
		{
			assertTrue(metrics.stringWidth(lines.get(i)) <= width);
		}
	}

	@Test
	public void clipKeepsTheRightColumnInsideItsBudget()
	{
		FontMetrics metrics = metrics();
		int width = 36;
		String clipped = OverlayText.clip(metrics, "max 123456789", width);

		assertTrue(clipped.endsWith("..."));
		assertTrue(metrics.stringWidth(clipped) <= width);
		assertFalse(clipped.equals("max 123456789"));
	}

	private static FontMetrics metrics()
	{
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
		return graphics.getFontMetrics();
	}
}
