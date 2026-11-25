package com.marmaduke.dps;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class NpcStatsIndex
{
	private final Map<Integer, NpcCombatStats> byId;
	private final Map<String, NpcCombatStats> uniqueByName;

	private NpcStatsIndex(Map<Integer, NpcCombatStats> byId, Map<String, NpcCombatStats> uniqueByName)
	{
		this.byId = byId;
		this.uniqueByName = uniqueByName;
	}

	static NpcStatsIndex empty()
	{
		return new NpcStatsIndex(Collections.emptyMap(), Collections.emptyMap());
	}

	static NpcStatsIndex load(Gson gson, InputStream in)
	{
		if (in == null)
		{
			return empty();
		}
		JsonElement element;
		try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8))
		{
			element = new JsonParser().parse(reader);
		}
		catch (IOException ex)
		{
			return empty();
		}
		Map<Integer, NpcCombatStats> byId = new HashMap<Integer, NpcCombatStats>();
		Map<String, NpcCombatStats> uniqueByName = new HashMap<String, NpcCombatStats>();
		Set<String> ambiguous = new HashSet<String>();
		if (element != null && element.isJsonArray())
		{
			for (JsonElement row : element.getAsJsonArray())
			{
				NpcCombatStats stat = gson.fromJson(row, NpcCombatStats.class);
				if (stat != null && !byId.containsKey(stat.id))
				{
					byId.put(stat.id, stat);
					noteName(uniqueByName, ambiguous, stat);
				}
			}
		}
		for (String name : ambiguous)
		{
			uniqueByName.remove(name);
		}
		return new NpcStatsIndex(byId, uniqueByName);
	}

	NpcCombatStats get(int id)
	{
		if (id <= 0)
		{
			return null;
		}
		return byId.get(id);
	}

	/**
	 * Resolve the row for the NPC the player is actually fighting. A transformed
	 * id in multi often is not the id in the dataset. Fall back to the composition
	 * id, then to the stats already shown for this monster when the name matches,
	 * then to the dataset row when that name is unique.
	 */
	NpcCombatStats resolve(int id, int compositionId, int transformedId, String name, NpcCombatStats remembered)
	{
		NpcCombatStats found = get(id);
		if (found == null)
		{
			found = get(compositionId);
		}
		if (found == null)
		{
			found = get(transformedId);
		}
		if (found != null)
		{
			return found;
		}
		String plain = plainName(name);
		if (remembered != null && !plain.isEmpty() && plain.equalsIgnoreCase(plainName(remembered.name)))
		{
			return remembered;
		}
		if (plain.isEmpty())
		{
			return null;
		}
		return uniqueByName.get(plain.toLowerCase(Locale.US));
	}

	static String plainName(String name)
	{
		if (name == null || name.isEmpty())
		{
			return "";
		}
		StringBuilder out = new StringBuilder(name.length());
		boolean tag = false;
		for (int i = 0; i < name.length(); i++)
		{
			char c = name.charAt(i);
			if (c == '<')
			{
				tag = true;
				continue;
			}
			if (c == '>')
			{
				tag = false;
				continue;
			}
			if (!tag)
			{
				out.append(c);
			}
		}
		return out.toString().replace('\u00A0', ' ').trim();
	}

	private static void noteName(Map<String, NpcCombatStats> uniqueByName, Set<String> ambiguous, NpcCombatStats stat)
	{
		String key = plainName(stat.name).toLowerCase(Locale.US);
		if (key.isEmpty() || ambiguous.contains(key))
		{
			return;
		}
		if (uniqueByName.containsKey(key))
		{
			uniqueByName.remove(key);
			ambiguous.add(key);
			return;
		}
		uniqueByName.put(key, stat);
	}

	int size()
	{
		return byId.size();
	}
}
