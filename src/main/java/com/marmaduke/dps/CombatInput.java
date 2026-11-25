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

	/**
	 * Gear, prayers, and boosted levels. NPC defence is compared separately so a
	 * failed NPC read cannot move the overlay on its own.
	 */
	static boolean offenceChanged(CombatInput previous, CombatInput next)
	{
		if (previous == null || next == null)
		{
			return true;
		}
		return previous.attackLevel != next.attackLevel
			|| previous.strengthLevel != next.strengthLevel
			|| previous.rangedLevel != next.rangedLevel
			|| previous.magicLevel != next.magicLevel
			|| previous.stabBonus != next.stabBonus
			|| previous.slashBonus != next.slashBonus
			|| previous.crushBonus != next.crushBonus
			|| previous.magicBonus != next.magicBonus
			|| previous.rangedBonus != next.rangedBonus
			|| previous.strengthBonus != next.strengthBonus
			|| previous.rangedStrengthBonus != next.rangedStrengthBonus
			|| previous.magicDamagePermille != next.magicDamagePermille
			|| previous.combatStyle != next.combatStyle
			|| previous.stance != next.stance
			|| previous.meleeStyle != next.meleeStyle
			|| previous.rangedStyle != next.rangedStyle
			|| previous.weaponSpeed != next.weaponSpeed
			|| !sameText(previous.weaponName, next.weaponName)
			|| !sameText(previous.spellName, next.spellName)
			|| previous.spellMaxHit != next.spellMaxHit
			|| !sameText(previous.spellElement, next.spellElement)
			|| !sameText(previous.spellbook, next.spellbook)
			|| previous.voidMelee != next.voidMelee
			|| previous.voidRanged != next.voidRanged
			|| previous.voidRangedElite != next.voidRangedElite
			|| previous.voidMagic != next.voidMagic
			|| previous.voidMagicElite != next.voidMagicElite
			|| previous.salve != next.salve
			|| previous.salveEnchanted != next.salveEnchanted
			|| previous.salveImbued != next.salveImbued
			|| previous.salveEnchantedImbued != next.salveEnchantedImbued
			|| previous.blackMask != next.blackMask
			|| previous.blackMaskImbued != next.blackMaskImbued
			|| previous.onSlayerTask != next.onSlayerTask
			|| previous.avarice != next.avarice
			|| previous.tomeFire != next.tomeFire
			|| previous.tomeWater != next.tomeWater
			|| previous.tomeEarth != next.tomeEarth
			|| previous.harmonisedStaff != next.harmonisedStaff
			|| previous.twinflameStaff != next.twinflameStaff
			|| previous.shadow != next.shadow
			|| previous.twistedBow != next.twistedBow
			|| previous.fang != next.fang
			|| previous.crystalBow != next.crystalBow
			|| previous.crystalPieces != next.crystalPieces
			|| previous.arclight != next.arclight
			|| previous.emberlight != next.emberlight
			|| previous.darklight != next.darklight
			|| previous.dragonHunterLance != next.dragonHunterLance
			|| previous.dragonHunterCrossbow != next.dragonHunterCrossbow
			|| previous.dragonHunterWand != next.dragonHunterWand
			|| previous.chaosGauntlets != next.chaosGauntlets
			|| previous.virtusPieces != next.virtusPieces
			|| previous.poweredStaff != next.poweredStaff
			|| previous.uncharged != next.uncharged
			|| previous.meleeAttackPrayer != next.meleeAttackPrayer
			|| previous.meleeStrengthPrayer != next.meleeStrengthPrayer
			|| previous.rangedAttackPrayer != next.rangedAttackPrayer
			|| previous.rangedStrengthPrayer != next.rangedStrengthPrayer
			|| previous.magicAttackPrayer != next.magicAttackPrayer
			|| previous.magicDamagePrayerPermille != next.magicDamagePrayerPermille;
	}

	static boolean monsterChanged(CombatInput previous, CombatInput next)
	{
		if (previous == null || next == null)
		{
			return true;
		}
		return previous.npcDefence != next.npcDefence
			|| previous.npcMagic != next.npcMagic
			|| previous.npcOffensiveMagic != next.npcOffensiveMagic
			|| previous.npcStab != next.npcStab
			|| previous.npcSlash != next.npcSlash
			|| previous.npcCrush != next.npcCrush
			|| previous.npcMagicDefence != next.npcMagicDefence
			|| previous.npcLight != next.npcLight
			|| previous.npcStandard != next.npcStandard
			|| previous.npcHeavy != next.npcHeavy
			|| previous.npcFlatArmour != next.npcFlatArmour
			|| previous.undead != next.undead
			|| previous.demon != next.demon
			|| previous.dragon != next.dragon
			|| previous.revenant != next.revenant
			|| previous.xerician != next.xerician
			|| previous.toa != next.toa
			|| previous.magicUsesDefenceLevel != next.magicUsesDefenceLevel
			|| !sameText(previous.weaknessElement, next.weaknessElement)
			|| previous.weaknessPercent != next.weaknessPercent;
	}

	/**
	 * The equipment container is sometimes missing for a tick. A missing read must
	 * not wipe the bonuses the overlay is already showing.
	 */
	static void keepGearIfUnread(CombatInput previous, CombatInput next)
	{
		if (previous == null || next == null)
		{
			return;
		}
		next.stabBonus = previous.stabBonus;
		next.slashBonus = previous.slashBonus;
		next.crushBonus = previous.crushBonus;
		next.magicBonus = previous.magicBonus;
		next.rangedBonus = previous.rangedBonus;
		next.strengthBonus = previous.strengthBonus;
		next.rangedStrengthBonus = previous.rangedStrengthBonus;
		next.magicDamagePermille = previous.magicDamagePermille;
		next.weaponSpeed = previous.weaponSpeed;
		next.weaponName = previous.weaponName;
		next.spellName = previous.spellName;
		next.spellMaxHit = previous.spellMaxHit;
		next.spellElement = previous.spellElement;
		next.spellbook = previous.spellbook;
		next.voidMelee = previous.voidMelee;
		next.voidRanged = previous.voidRanged;
		next.voidRangedElite = previous.voidRangedElite;
		next.voidMagic = previous.voidMagic;
		next.voidMagicElite = previous.voidMagicElite;
		next.salve = previous.salve;
		next.salveEnchanted = previous.salveEnchanted;
		next.salveImbued = previous.salveImbued;
		next.salveEnchantedImbued = previous.salveEnchantedImbued;
		next.blackMask = previous.blackMask;
		next.blackMaskImbued = previous.blackMaskImbued;
		next.avarice = previous.avarice;
		next.tomeFire = previous.tomeFire;
		next.tomeWater = previous.tomeWater;
		next.tomeEarth = previous.tomeEarth;
		next.harmonisedStaff = previous.harmonisedStaff;
		next.twinflameStaff = previous.twinflameStaff;
		next.shadow = previous.shadow;
		next.twistedBow = previous.twistedBow;
		next.fang = previous.fang;
		next.crystalBow = previous.crystalBow;
		next.crystalPieces = previous.crystalPieces;
		next.arclight = previous.arclight;
		next.emberlight = previous.emberlight;
		next.darklight = previous.darklight;
		next.dragonHunterLance = previous.dragonHunterLance;
		next.dragonHunterCrossbow = previous.dragonHunterCrossbow;
		next.dragonHunterWand = previous.dragonHunterWand;
		next.chaosGauntlets = previous.chaosGauntlets;
		next.virtusPieces = previous.virtusPieces;
		next.poweredStaff = previous.poweredStaff;
		next.uncharged = previous.uncharged;
		next.combatStyle = previous.combatStyle;
		next.stance = previous.stance;
		next.meleeStyle = previous.meleeStyle;
		next.rangedStyle = previous.rangedStyle;
	}

	/**
	 * Autocast varbit reads come back empty on some ticks. Keep the spell that
	 * was already casting instead of dropping the max hit to 0.
	 */
	static void keepSpellIfUnread(CombatInput previous, CombatInput next)
	{
		if (previous == null || next == null)
		{
			return;
		}
		if (next.combatStyle != CombatStyle.MAGIC || next.poweredStaff)
		{
			return;
		}
		if (next.spellName != null && !next.spellName.isEmpty())
		{
			return;
		}
		if (previous.combatStyle != CombatStyle.MAGIC || previous.spellName == null || previous.spellName.isEmpty())
		{
			return;
		}
		next.spellName = previous.spellName;
		next.spellMaxHit = previous.spellMaxHit;
		next.spellElement = previous.spellElement;
		next.spellbook = previous.spellbook;
	}

	private static boolean sameText(String left, String right)
	{
		if (left == null || left.isEmpty())
		{
			return right == null || right.isEmpty();
		}
		return left.equals(right);
	}
}
