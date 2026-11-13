package com.marmaduke.dps;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/**
 * Development entry point. {@code ./gradlew run} loads the plugin the same way
 * the Plugin Hub does, through {@link ExternalPluginManager}.
 */
public class DpsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DpsPlugin.class);
		RuneLite.main(args);
	}
}
