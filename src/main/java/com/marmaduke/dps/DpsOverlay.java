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
	private static final int COLUMN_GAP = 6;

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

		int innerWidth = innerWidth();
		FontMetrics metrics = graphics.getFontMetrics();
		panelComponent.getChildren().clear();

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

		if (estimate.note != null && !estimate.note.isEmpty() && estimate.attackSpeedTicks == 0)
		{
			addWrapped(metrics, estimate.note, NOTE_COLOR, innerWidth);
		}
		else
		{
			addPair(metrics, "DPS", String.format(Locale.US, "%.2f", estimate.dps), Color.WHITE, DPS_COLOR, innerWidth);
			if (config.showBreakdown())
			{
				addPair(
					metrics,
					estimate.styleLabel,
					"max " + estimate.maxHit,
					Color.WHITE,
					Color.WHITE,
					innerWidth);
				addPair(
					metrics,
					String.format(Locale.US, "%.1f%%", estimate.accuracy * 100.0),
					String.format(Locale.US, "%.1fs", estimate.attackSpeedTicks * 0.6),
					Color.WHITE,
					Color.WHITE,
					innerWidth);
			}
			if (estimate.defenceLabel != null && !estimate.defenceLabel.isEmpty())
			{
				addWrapped(metrics, estimate.defenceLabel, Color.LIGHT_GRAY, innerWidth);
			}
			if (estimate.note != null && !estimate.note.isEmpty())
			{
				addWrapped(metrics, estimate.note, NOTE_COLOR, innerWidth);
			}
		}

		addWrapped(metrics, "Right-click overlay to unpin", MUTED, innerWidth);

		Shape previousClip = graphics.getClip();
		graphics.clip(new Rectangle(0, -10000, panelWidth(), 20000));
		try
		{
			return super.render(graphics);
		}
		finally
		{
			graphics.setClip(previousClip);
		}
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

	private void addWrapped(FontMetrics metrics, String text, Color color, int innerWidth)
	{
		List<String> lines = OverlayText.wrap(metrics, text, innerWidth);
		for (int i = 0; i < lines.size(); i++)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left(lines.get(i))
				.leftColor(color)
				.build());
		}
	}

	private void addPair(FontMetrics metrics, String left, String right, Color leftColor, Color rightColor, int innerWidth)
	{
		int rightBudget = Math.max(metrics.stringWidth("0"), innerWidth / 3);
		String rightText = OverlayText.clip(metrics, right == null ? "" : right, rightBudget);
		int leftBudget = Math.max(1, innerWidth - metrics.stringWidth(rightText) - COLUMN_GAP);
		List<String> leftLines = OverlayText.wrap(metrics, left == null ? "" : left, leftBudget);
		if (leftLines.isEmpty())
		{
			leftLines.add("");
		}
		for (int i = 0; i < leftLines.size(); i++)
		{
			LineComponent.LineComponentBuilder builder = LineComponent.builder()
				.left(leftLines.get(i))
				.leftColor(leftColor);
			if (i == 0 && !rightText.isEmpty())
			{
				builder.right(rightText).rightColor(rightColor);
			}
			panelComponent.getChildren().add(builder.build());
		}
	}

	private int panelWidth()
	{
		Dimension preferred = getPreferredSize();
		if (preferred != null && preferred.width > 0)
		{
			return preferred.width;
		}
		return ComponentConstants.STANDARD_WIDTH;
	}

	private int innerWidth()
	{
		int inner = panelWidth() - ComponentConstants.STANDARD_BORDER * 2;
		return Math.max(inner, 1);
	}
}
