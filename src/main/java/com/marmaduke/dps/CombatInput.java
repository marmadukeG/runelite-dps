package com.marmaduke.dps;

/**
 * Everything the damage formula needs for one attack cycle.
 * Levels are the boosted levels RuneLite already reports. Prayer multipliers are separate.
 */
final class CombatInput
{
	int attackLevel;
	int strengthLevel;
	int rangedLevel;
	int magicLevel;

	int stabBonus;
	int slashBonus;
	int crushBonus;
	int magicBonus;
	int rangedBonus;
	int strengthBonus;
	int rangedStrengthBonus;
	/**
	 * Visible magic damage bonus in tenths of a percent. 100 means +10%.
	 */
	int magicDamagePermille;

	CombatStyle combatStyle = CombatStyle.MELEE;
	AttackStance stance = AttackStance.MELEE_AGGRESSIVE;
	MeleeStyle meleeStyle = MeleeStyle.SLASH;
	RangedStyle rangedStyle = RangedStyle.STANDARD;

	int weaponSpeed = 4;
	String weaponName = "";
	String spellName = "";
	int spellMaxHit;
	String spellElement = "";
	String spellbook = "";

	boolean voidMelee;
	boolean voidRanged;
	boolean voidRangedElite;
	boolean voidMagic;
	boolean voidMagicElite;
	boolean salve;
	boolean salveEnchanted;
	boolean salveImbued;
	boolean salveEnchantedImbued;
	boolean blackMask;
	boolean blackMaskImbued;
	boolean onSlayerTask;
	boolean avarice;
	boolean tomeFire;
	boolean tomeWater;
	boolean tomeEarth;
	boolean harmonisedStaff;
	boolean twinflameStaff;
	boolean shadow;
	boolean twistedBow;
	boolean fang;
	boolean crystalBow;
	int crystalPieces;
	boolean arclight;
	boolean emberlight;
	boolean darklight;
	boolean dragonHunterLance;
	boolean dragonHunterCrossbow;
	boolean dragonHunterWand;
	boolean chaosGauntlets;
	int virtusPieces;
	boolean poweredStaff;
	boolean uncharged;

	int meleeAttackPrayer = 100;
	int meleeStrengthPrayer = 100;
	int rangedAttackPrayer = 100;
	int rangedStrengthPrayer = 100;
	int magicAttackPrayer = 100;
	int magicDamagePrayerPermille;

	int npcDefence = 1;
	int npcMagic = 1;
	int npcOffensiveMagic;
	int npcStab;
	int npcSlash;
	int npcCrush;
	int npcMagicDefence;
	int npcLight;
	int npcStandard;
	int npcHeavy;
	int npcFlatArmour;
	boolean undead;
	boolean demon;
	boolean dragon;
	boolean revenant;
	boolean xerician;
	boolean toa;
	boolean magicUsesDefenceLevel;
	String weaknessElement = "";
	int weaknessPercent;

	int meleeAttackBonus()
	{
		switch (meleeStyle)
		{
			case STAB:
				return stabBonus;
			case CRUSH:
				return crushBonus;
			case SLASH:
			default:
				return slashBonus;
		}
	}

	int npcMeleeDefence()
	{
		switch (meleeStyle)
		{
			case STAB:
				return npcStab;
			case CRUSH:
				return npcCrush;
			case SLASH:
			default:
				return npcSlash;
		}
	}

	int npcRangedDefence()
	{
		switch (rangedStyle)
		{
			case LIGHT:
				return npcLight;
			case HEAVY:
				return npcHeavy;
			case STANDARD:
			default:
				return npcStandard;
		}
	}
}
