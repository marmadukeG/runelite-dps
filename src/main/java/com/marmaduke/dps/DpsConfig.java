package com.marmaduke.dps;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.ui.overlay.OverlayPosition;

@ConfigGroup("checkdps")
public interface DpsConfig extends Config
{
	@ConfigItem(
		keyName = "position",
		name = "Overlay position",
		description = "Where the DPS overlay is drawn",
		position = 0
	)
	default OverlayPosition position()
	{
		return OverlayPosition.TOP_RIGHT;
	}

	@ConfigItem(
		keyName = "onSlayerTask",
		name = "On slayer task",
		description = "Apply black mask and slayer helmet bonuses. Salve still wins against undead when both apply.",
		position = 1
	)
	default boolean onSlayerTask()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showBreakdown",
		name = "Show accuracy and max hit",
		description = "Show max hit, accuracy, and attack speed under the DPS number",
		position = 2
	)
	default boolean showBreakdown()
	{
		return true;
	}

	@ConfigItem(
		keyName = "minimalOverlay",
		name = "Minimal overlay",
		description = "Hide the panel and show only three numbers: DPS, max hit, and hit chance",
		position = 3
	)
	default boolean minimalOverlay()
	{
		return false;
	}
}
