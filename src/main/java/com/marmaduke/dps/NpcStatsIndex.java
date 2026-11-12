package com.marmaduke.dps;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
		List<NpcCombatStats> stats = gson.fromJson(
			new InputStreamReader(in, StandardCharsets.UTF_8),
			new TypeToken<List<NpcCombatStats>>()
			{
			}.getType());
		Map<Integer, NpcCombatStats> byId = new HashMap<Integer, NpcCombatStats>();
		if (stats != null)
		{
			for (NpcCombatStats stat : stats)
			{
				if (!byId.containsKey(stat.id))
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
