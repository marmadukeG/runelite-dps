package com.marmaduke.dps;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
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
	private static final int COLUMN_GAP = 8;
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
		List<String[]> rows = numbersOnly
			? new ArrayList<String[]>()
			: fullRows(estimate, config.showBreakdown());
		String note = estimate.note == null ? "" : estimate.note;
		String hint = config.reactiveMode() ? "" : UNPIN_HINT;
		int innerWidth = contentWidth(metrics, rows, note, hint);
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
			for (int i = 0; i < rows.size(); i++)
			{
				String[] row = rows.get(i);
				Color leftColor = Color.WHITE;
				Color rightColor = "DPS".equals(row[0]) ? DPS_COLOR : leftColor;
				addLine(row[0], row[1], leftColor, rightColor);
			}
			if (!note.isEmpty())
			{
				addLine(note, "", NOTE_COLOR, NOTE_COLOR);
			}
		}

		if (!hint.isEmpty())
		{
			addLine(hint, "", MUTED, MUTED);
		}

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
	 * One full-overlay row per stat. Each entry is {label, value}. The value is never shortened.
	 */
	static List<String[]> fullRows(DpsEstimate estimate, boolean breakdown)
	{
		List<String[]> rows = new ArrayList<String[]>();
		rows.add(new String[] {"DPS", String.format(Locale.US, "%.2f", estimate.dps)});
		if (breakdown)
		{
			rows.add(new String[] {"Max hit", Integer.toString(estimate.maxHit)});
			rows.add(new String[] {
				"Hit chance",
				String.format(Locale.US, "%.1f%%", estimate.accuracy * 100.0)
			});
		}
		if (estimate.defenceLabel != null && !estimate.defenceLabel.isEmpty())
		{
			rows.add(new String[] {estimate.defenceLabel, ""});
		}
		if (breakdown)
		{
			rows.add(new String[] {
				"Attack speed",
				String.format(Locale.US, "%.1fs", estimate.attackSpeedTicks * 0.6)
			});
			rows.add(new String[] {
				"Stance",
				estimate.styleLabel == null ? "" : estimate.styleLabel
			});
		}
		return rows;
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

	static int contentWidth(FontMetrics metrics, List<String[]> rows, String note, String hint)
	{
		int width = 0;
		for (int i = 0; i < rows.size(); i++)
		{
			String[] row = rows.get(i);
			int left = metrics.stringWidth(row[0] == null ? "" : row[0]);
			int right = row.length > 1 && row[1] != null ? metrics.stringWidth(row[1]) : 0;
			int gap = right > 0 ? COLUMN_GAP : 0;
			width = Math.max(width, left + right + gap);
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
