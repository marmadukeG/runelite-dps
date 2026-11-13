package com.marmaduke.dps;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SideloadJarTest
{
	@Test
	public void builtJarIsAFlat1136Sideload() throws Exception
	{
		File jar = new File("build/libs/runelite-dps-1.0.0.jar");
		assertTrue(jar.isFile());

		try (JarFile zip = new JarFile(jar))
		{
			assertNotNull(zip.getJarEntry("com/marmaduke/dps/DpsPlugin.class"));
			zip.stream().forEach(entry ->
			{
				String name = entry.getName();
				assertFalse(name, name.endsWith(".jar"));
				assertFalse(name, name.startsWith("BOOT-INF/"));
				assertFalse(name, name.startsWith("META-INF/versions/"));
			});
		}

		URL url = jar.toURI().toURL();
		try (URLClassLoader loader = new URLClassLoader(new URL[]{url}, SideloadJarTest.class.getClassLoader()))
		{
			Class<?> plugin = Class.forName("com.marmaduke.dps.DpsPlugin", true, loader);
			assertEquals(Plugin.class, plugin.getSuperclass());
			PluginDescriptor descriptor = plugin.getAnnotation(PluginDescriptor.class);
			assertNotNull(descriptor);
			assertEquals("Check DPS", descriptor.name());
			assertFalse(descriptor.developerPlugin());
			assertFalse(descriptor.hidden());
		}
	}
}
