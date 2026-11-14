package com.marmaduke.dps;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PluginJarTest
{
	@Test
	public void jarContainsThePluginClassAtTheRoot() throws Exception
	{
		java.io.File jar = new java.io.File("build/libs/runelite-dps-1.0.0.jar");
		assertTrue(jar.isFile());

		try (JarFile zip = new JarFile(jar))
		{
			JarEntry plugin = zip.getJarEntry("com/marmaduke/dps/DpsPlugin.class");
			assertNotNull(plugin);
			zip.stream().forEach(entry ->
			{
				String name = entry.getName();
				assertFalse(name, name.endsWith(".jar"));
				assertFalse(name, name.startsWith("BOOT-INF/"));
				assertFalse(name, name.startsWith("META-INF/versions/"));
				assertFalse(name, name.startsWith("META-INF/services/"));
			});
			try (InputStream in = zip.getInputStream(plugin))
			{
				String utf = new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
				assertTrue(utf.contains("Check DPS"));
				assertFalse(utf.contains("developerPlugin"));
			}
		}
	}
}
