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
import java.util.Map;

final class NpcStatsIndex
{
	private final Map<Integer, NpcCombatStats> byId;

	private NpcStatsIndex(Map<Integer, NpcCombatStats> byId)
	{
		this.byId = byId;
	}

	static NpcStatsIndex empty()
	{
		return new NpcStatsIndex(Collections.emptyMap());
	}

	static NpcStatsIndex load(Gson gson, InputStream in)
	{
		if (in == null)
		{
			return new NpcStatsIndex(Collections.emptyMap());
		}
		JsonElement element;
		try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8))
		{
			element = new JsonParser().parse(reader);
		}
		catch (IOException ex)
		{
			return new NpcStatsIndex(Collections.emptyMap());
		}
		Map<Integer, NpcCombatStats> byId = new HashMap<Integer, NpcCombatStats>();
		if (element != null && element.isJsonArray())
		{
			for (JsonElement row : element.getAsJsonArray())
			{
				NpcCombatStats stat = gson.fromJson(row, NpcCombatStats.class);
				if (stat != null && !byId.containsKey(stat.id))
				{
					byId.put(stat.id, stat);
				}
			}
		}
		return new NpcStatsIndex(byId);
	}

	NpcCombatStats get(int id)
	{
		return byId.get(id);
	}

	int size()
	{
		return byId.size();
	}
}
