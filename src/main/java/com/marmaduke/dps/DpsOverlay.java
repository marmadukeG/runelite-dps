package com.marmaduke.dps;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Locale;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class DpsOverlay extends OverlayPanel
{
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

		panelComponent.getChildren().clear();
		panelComponent.getChildren().add(TitleComponent.builder()
			.text(estimate.targetName)
			.color(Color.WHITE)
			.build());

		if (estimate.note != null && !estimate.note.isEmpty() && estimate.attackSpeedTicks == 0)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left(estimate.note)
				.leftColor(Color.ORANGE)
				.build());
		}
		else
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("DPS")
				.right(String.format(Locale.US, "%.2f", estimate.dps))
				.rightColor(new Color(255, 180, 40))
				.build());
			if (config.showBreakdown())
			{
				panelComponent.getChildren().add(LineComponent.builder()
					.left(estimate.styleLabel)
					.right("max " + estimate.maxHit)
					.build());
				panelComponent.getChildren().add(LineComponent.builder()
					.left(String.format(Locale.US, "%.1f%%", estimate.accuracy * 100.0))
					.right(String.format(Locale.US, "%.1fs", estimate.attackSpeedTicks * 0.6))
					.build());
			}
			if (estimate.defenceLabel != null && !estimate.defenceLabel.isEmpty())
			{
				panelComponent.getChildren().add(LineComponent.builder()
					.left(estimate.defenceLabel)
					.leftColor(Color.LIGHT_GRAY)
					.build());
			}
			if (estimate.note != null && !estimate.note.isEmpty())
			{
				panelComponent.getChildren().add(LineComponent.builder()
					.left(estimate.note)
					.leftColor(Color.ORANGE)
					.build());
			}
		}

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Right-click overlay to unpin")
			.leftColor(new Color(160, 160, 160))
			.build());
		return super.render(graphics);
	}
}
