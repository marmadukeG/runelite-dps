package com.marmaduke.dps;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class DpsOverlay extends OverlayPanel
{
	private static final Color DPS_COLOR = new Color(255, 180, 40);
	private static final Color NOTE_COLOR = Color.ORANGE;
	private static final Color MUTED = new Color(160, 160, 160);
	private static final String UNPIN_HINT = "Right-click overlay to unpin";

	private final DpsPlugin plugin;
	private final DpsConfig config;

	@Inject
	DpsOverlay(DpsPlugin plugin, DpsConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_RIGHT);
		addMenuEntry(net.runelite.api.MenuAction.RUNELITE_OVERLAY, "Clear", "DPS target");
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		DpsEstimate estimate = plugin.getEstimate();
		if (estimate == null)
		{
			return null;
		}

		if (config.minimalOverlay())
		{
			panelComponent.getChildren().clear();
			return renderMinimal(graphics, estimate);
		}

		FontMetrics metrics = graphics.getFontMetrics();
		panelComponent.getChildren().clear();

		boolean numbersOnly = estimate.note != null && !estimate.note.isEmpty() && estimate.attackSpeedTicks == 0;
		String statsLine = numbersOnly ? "" : fullLine(estimate, config.showBreakdown());
		String note = estimate.note == null ? "" : estimate.note;
		int innerWidth = contentWidth(metrics, statsLine, note, UNPIN_HINT);
		Dimension userSize = getPreferredSize();
		if (userSize != null && userSize.width > innerWidth + ComponentConstants.STANDARD_BORDER * 2)
		{
			innerWidth = userSize.width - ComponentConstants.STANDARD_BORDER * 2;
		}
		int panelWidth = innerWidth + ComponentConstants.STANDARD_BORDER * 2;

		List<String> nameLines = OverlayText.wrap(metrics, estimate.targetName, innerWidth);
		if (nameLines.isEmpty())
		{
			nameLines = OverlayText.wrap(metrics, "NPC", innerWidth);
		}
		for (int i = 0; i < nameLines.size(); i++)
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text(nameLines.get(i))
				.color(Color.WHITE)
				.build());
		}

		if (numbersOnly)
		{
			addLine(note, "", NOTE_COLOR, NOTE_COLOR);
		}
		else
		{
			addLine(statsLine, "", Color.WHITE, Color.WHITE);
			if (!note.isEmpty())
			{
				addLine(note, "", NOTE_COLOR, NOTE_COLOR);
			}
		}

		addLine(UNPIN_HINT, "", MUTED, MUTED);

		panelComponent.setPreferredSize(new Dimension(panelWidth, 0));
		Shape previousClip = graphics.getClip();
		graphics.clip(new Rectangle(0, -10000, panelWidth, 20000));
		try
		{
			return panelComponent.render(graphics);
		}
		finally
		{
			graphics.setClip(previousClip);
			panelComponent.getChildren().clear();
		}
	}

	/**
	 * Every full-overlay stat on one line. Max hit is the full integer.
	 */
	static String fullLine(DpsEstimate estimate, boolean breakdown)
	{
		StringBuilder line = new StringBuilder();
		line.append("DPS ").append(String.format(Locale.US, "%.2f", estimate.dps));
		if (breakdown)
		{
			line.append("   Max hit ").append(estimate.maxHit);
			line.append("   Hit chance ").append(String.format(Locale.US, "%.1f%%", estimate.accuracy * 100.0));
		}
		if (estimate.defenceLabel != null && !estimate.defenceLabel.isEmpty())
		{
			line.append("   ").append(estimate.defenceLabel);
		}
		if (breakdown)
		{
			line.append("   ").append(String.format(Locale.US, "%.1fs", estimate.attackSpeedTicks * 0.6));
			String stance = estimate.styleLabel == null ? "" : estimate.styleLabel;
			if (!stance.isEmpty())
			{
				line.append("   ").append(stance);
			}
		}
		return line.toString();
	}

	private Dimension renderMinimal(Graphics2D graphics, DpsEstimate estimate)
	{
		FontMetrics metrics = graphics.getFontMetrics();
		String[] lines = minimalLines(estimate);
		Color[] colors = new Color[] {DPS_COLOR, Color.WHITE, Color.WHITE};
		int lineHeight = metrics.getHeight();
		int width = 1;
		for (int i = 0; i < lines.length; i++)
		{
			width = Math.max(width, metrics.stringWidth(lines[i]) + 1);
		}
		int baseline = metrics.getAscent();
		for (int i = 0; i < lines.length; i++)
		{
			drawShadowed(graphics, lines[i], 0, baseline, colors[i]);
			baseline += lineHeight;
		}
		return new Dimension(width, lineHeight * lines.length + 1);
	}

	static String[] minimalLines(DpsEstimate estimate)
	{
		return new String[] {
			String.format(Locale.US, "%.2f", estimate.dps),
			Integer.toString(estimate.maxHit),
			String.format(Locale.US, "%.1f%%", estimate.accuracy * 100.0)
		};
	}

	private static void drawShadowed(Graphics2D graphics, String text, int x, int y, Color color)
	{
		graphics.setColor(Color.BLACK);
		graphics.drawString(text, x + 1, y + 1);
		graphics.setColor(color);
		graphics.drawString(text, x, y);
	}

	private void addLine(String left, String right, Color leftColor, Color rightColor)
	{
		LineComponent.LineComponentBuilder builder = LineComponent.builder()
			.left(left)
			.leftColor(leftColor);
		if (right != null && !right.isEmpty())
		{
			builder.right(right).rightColor(rightColor);
		}
		panelComponent.getChildren().add(builder.build());
	}

	static int contentWidth(FontMetrics metrics, String statsLine, String note, String hint)
	{
		int width = 0;
		if (statsLine != null && !statsLine.isEmpty())
		{
			width = Math.max(width, metrics.stringWidth(statsLine));
		}
		if (note != null && !note.isEmpty())
		{
			width = Math.max(width, metrics.stringWidth(note));
		}
		if (hint != null && !hint.isEmpty())
		{
			width = Math.max(width, metrics.stringWidth(hint));
		}
		int standardInner = ComponentConstants.STANDARD_WIDTH - ComponentConstants.STANDARD_BORDER * 2;
		return Math.max(width, standardInner);
	}
}
