package com.marmaduke.dps;

final class DpsEstimate
{
	final String targetName;
	final double dps;
	final int maxHit;
	final double accuracy;
	final int attackSpeedTicks;
	final String styleLabel;
	final String defenceLabel;
	final String note;

	DpsEstimate(
		String targetName,
		double dps,
		int maxHit,
		double accuracy,
		int attackSpeedTicks,
		String styleLabel,
		String defenceLabel,
		String note)
	{
		this.targetName = targetName;
		this.dps = dps;
		this.maxHit = maxHit;
		this.accuracy = accuracy;
		this.attackSpeedTicks = attackSpeedTicks;
		this.styleLabel = styleLabel;
		this.defenceLabel = defenceLabel;
		this.note = note;
	}

	static DpsEstimate message(String targetName, String note)
	{
		return new DpsEstimate(targetName, 0, 0, 0, 0, "", "", note);
	}
}
