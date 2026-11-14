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

	CombatInput copy()
	{
		CombatInput copy = new CombatInput();
		copy.attackLevel = attackLevel;
		copy.strengthLevel = strengthLevel;
		copy.rangedLevel = rangedLevel;
		copy.magicLevel = magicLevel;
		copy.stabBonus = stabBonus;
		copy.slashBonus = slashBonus;
		copy.crushBonus = crushBonus;
		copy.magicBonus = magicBonus;
		copy.rangedBonus = rangedBonus;
		copy.strengthBonus = strengthBonus;
		copy.rangedStrengthBonus = rangedStrengthBonus;
		copy.magicDamagePermille = magicDamagePermille;
		copy.combatStyle = combatStyle;
		copy.stance = stance;
		copy.meleeStyle = meleeStyle;
		copy.rangedStyle = rangedStyle;
		copy.weaponSpeed = weaponSpeed;
		copy.weaponName = weaponName;
		copy.spellName = spellName;
		copy.spellMaxHit = spellMaxHit;
		copy.spellElement = spellElement;
		copy.spellbook = spellbook;
		copy.voidMelee = voidMelee;
		copy.voidRanged = voidRanged;
		copy.voidRangedElite = voidRangedElite;
		copy.voidMagic = voidMagic;
		copy.voidMagicElite = voidMagicElite;
		copy.salve = salve;
		copy.salveEnchanted = salveEnchanted;
		copy.salveImbued = salveImbued;
		copy.salveEnchantedImbued = salveEnchantedImbued;
		copy.blackMask = blackMask;
		copy.blackMaskImbued = blackMaskImbued;
		copy.onSlayerTask = onSlayerTask;
		copy.avarice = avarice;
		copy.tomeFire = tomeFire;
		copy.tomeWater = tomeWater;
		copy.tomeEarth = tomeEarth;
		copy.harmonisedStaff = harmonisedStaff;
		copy.twinflameStaff = twinflameStaff;
		copy.shadow = shadow;
		copy.twistedBow = twistedBow;
		copy.fang = fang;
		copy.crystalBow = crystalBow;
		copy.crystalPieces = crystalPieces;
		copy.arclight = arclight;
		copy.emberlight = emberlight;
		copy.darklight = darklight;
		copy.dragonHunterLance = dragonHunterLance;
		copy.dragonHunterCrossbow = dragonHunterCrossbow;
		copy.dragonHunterWand = dragonHunterWand;
		copy.chaosGauntlets = chaosGauntlets;
		copy.virtusPieces = virtusPieces;
		copy.poweredStaff = poweredStaff;
		copy.uncharged = uncharged;
		copy.meleeAttackPrayer = meleeAttackPrayer;
		copy.meleeStrengthPrayer = meleeStrengthPrayer;
		copy.rangedAttackPrayer = rangedAttackPrayer;
		copy.rangedStrengthPrayer = rangedStrengthPrayer;
		copy.magicAttackPrayer = magicAttackPrayer;
		copy.magicDamagePrayerPermille = magicDamagePrayerPermille;
		copy.npcDefence = npcDefence;
		copy.npcMagic = npcMagic;
		copy.npcOffensiveMagic = npcOffensiveMagic;
		copy.npcStab = npcStab;
		copy.npcSlash = npcSlash;
		copy.npcCrush = npcCrush;
		copy.npcMagicDefence = npcMagicDefence;
		copy.npcLight = npcLight;
		copy.npcStandard = npcStandard;
		copy.npcHeavy = npcHeavy;
		copy.npcFlatArmour = npcFlatArmour;
		copy.undead = undead;
		copy.demon = demon;
		copy.dragon = dragon;
		copy.revenant = revenant;
		copy.xerician = xerician;
		copy.toa = toa;
		copy.magicUsesDefenceLevel = magicUsesDefenceLevel;
		copy.weaknessElement = weaknessElement;
		copy.weaknessPercent = weaknessPercent;
		return copy;
	}
}
