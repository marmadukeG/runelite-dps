package com.marmaduke.dps;

import java.util.Collections;
import java.util.List;

final class NpcCombatStats
{
	int id;
	String name = "";
	String version = "";
	int def;
	int atk;
	int str;
	int magic;
	int ranged;
	int omagic;
	int stab;
	int slash;
	int crush;
	int mage;
	int light;
	int std;
	int heavy;
	int flat;
	List<String> attrs = Collections.emptyList();
	String weak = "";
	int weakPct;
	boolean toa;
	boolean mageUsesDef;

	boolean has(String attribute)
	{
		return attrs != null && attrs.contains(attribute);
	}

	String displayName()
	{
		if (version == null || version.isEmpty() || "standard".equalsIgnoreCase(version))
		{
			return name;
		}
		return name + " (" + version + ")";
	}
}
