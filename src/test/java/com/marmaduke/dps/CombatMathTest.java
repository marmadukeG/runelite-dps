package com.marmaduke.dps;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CombatMathTest
{
	@Test
	public void rapidTwistedBowDoesNotMatchAccurate()
	{
		CombatInput rapid = twistedBow(AttackStance.RANGED_RAPID);
		CombatInput accurate = twistedBow(AttackStance.RANGED_ACCURATE);

		assertTrue(CombatMath.maxHit(rapid) < CombatMath.maxHit(accurate));
		assertTrue(CombatMath.attackRoll(rapid) < CombatMath.attackRoll(accurate));
		assertEquals(5, CombatMath.attackSpeed(rapid));
		assertEquals(6, CombatMath.attackSpeed(accurate));
		assertEquals("Rapid", CombatMath.estimate(rapid, "Target", "Def 0").styleLabel);
		assertEquals("Accurate", CombatMath.estimate(accurate, "Target", "Def 0").styleLabel);
		rapid.rangedStyle = RangedStyle.LIGHT;
		assertEquals("Rapid (light)", CombatMath.estimate(rapid, "Target", "Def 0").styleLabel);
	}

	@Test
	public void secondRangingStyleIsRapid()
	{
		String[] bow = new String[]{"Ranging", "Ranging", null, "Longrange"};
		assertEquals(AttackStance.RANGED_ACCURATE, PlayerGear.stanceFrom(PlayerGear.StyleSelection.of(bow, 0), false));
		assertEquals(AttackStance.RANGED_RAPID, PlayerGear.stanceFrom(PlayerGear.StyleSelection.of(bow, 1), false));
		assertEquals(AttackStance.RANGED_LONG, PlayerGear.stanceFrom(PlayerGear.StyleSelection.of(bow, 3), false));

		String[] salamander = new String[]{"Aggressive", "Ranging", "Casting"};
		assertEquals(AttackStance.RANGED_RAPID, PlayerGear.stanceFrom(PlayerGear.StyleSelection.of(salamander, 1), false));
		assertEquals(AttackStance.MAGIC_AUTOCAST, PlayerGear.stanceFrom(PlayerGear.StyleSelection.of(salamander, 2), false));
		assertEquals(AttackStance.MAGIC_ACCURATE, PlayerGear.stanceFrom(PlayerGear.StyleSelection.of(salamander, 2), true));
	}

	@Test
	public void staffAutocastDoesNotUseTheAccurateMagicBonus()
	{
		CombatInput autocast = new CombatInput();
		autocast.combatStyle = CombatStyle.MAGIC;
		autocast.stance = AttackStance.MAGIC_AUTOCAST;
		autocast.magicLevel = 99;
		autocast.magicBonus = 10;
		autocast.spellName = "Fire Surge";
		autocast.spellMaxHit = 24;

		CombatInput accurate = new CombatInput();
		accurate.combatStyle = CombatStyle.MAGIC;
		accurate.stance = AttackStance.MAGIC_ACCURATE;
		accurate.magicLevel = 99;
		accurate.magicBonus = 10;
		accurate.spellName = "Fire Surge";
		accurate.spellMaxHit = 24;

		assertTrue(CombatMath.attackRoll(accurate) > CombatMath.attackRoll(autocast));
		assertEquals(CombatMath.maxHit(accurate), CombatMath.maxHit(autocast));
	}

	@Test
	public void aggressiveMaxHitBeatsDefensive()
	{
		CombatInput aggressive = new CombatInput();
		aggressive.strengthLevel = 99;
		aggressive.stance = AttackStance.MELEE_AGGRESSIVE;
		aggressive.strengthBonus = 86;

		CombatInput defensive = new CombatInput();
		defensive.strengthLevel = 99;
		defensive.stance = AttackStance.MELEE_DEFENSIVE;
		defensive.strengthBonus = 86;

		assertTrue(CombatMath.maxHit(aggressive) > CombatMath.maxHit(defensive));
	}

	@Test
	public void meleeMaxHitUsesPietyAndAggressiveStyle()
	{
		CombatInput in = new CombatInput();
		in.strengthLevel = 99;
		in.meleeStrengthPrayer = 123;
		in.stance = AttackStance.MELEE_AGGRESSIVE;
		in.strengthBonus = 86;

		assertEquals(31, CombatMath.maxHit(in));
	}

	@Test
	public void abyssalDemonSlashDefenceRoll()
	{
		CombatInput in = new CombatInput();
		in.combatStyle = CombatStyle.MELEE;
		in.meleeStyle = MeleeStyle.SLASH;
		in.npcDefence = 135;
		in.npcSlash = 20;

		assertEquals(12096, CombatMath.defenceRoll(in));
	}

	@Test
	public void tridentMaxHitAt99()
	{
		CombatInput in = new CombatInput();
		in.combatStyle = CombatStyle.MAGIC;
		in.poweredStaff = true;
		in.magicLevel = 99;
		in.weaponName = "Trident of the seas";

		assertEquals(28, CombatMath.maxHit(in));
	}

	@Test
	public void shadowTriplesMagicDamageOutsideTombs()
	{
		CombatInput in = new CombatInput();
		in.combatStyle = CombatStyle.MAGIC;
		in.poweredStaff = true;
		in.shadow = true;
		in.magicLevel = 99;
		in.weaponName = "Tumeken's shadow";
		in.magicDamagePermille = 100;

		assertEquals(44, CombatMath.maxHit(in));
	}

	@Test
	public void windStrikeScalesUpToFireStrike()
	{
		assertEquals(2, elementalHit("Wind Strike", 2, 1));
		assertEquals(2, elementalHit("Wind Strike", 2, 4));
		assertEquals(4, elementalHit("Wind Strike", 2, 5));
		assertEquals(4, elementalHit("Water Strike", 4, 5));
		assertEquals(6, elementalHit("Wind Strike", 2, 9));
		assertEquals(6, elementalHit("Earth Strike", 6, 9));
		assertEquals(8, elementalHit("Wind Strike", 2, 13));
		assertEquals(8, elementalHit("Water Strike", 4, 13));
		assertEquals(8, elementalHit("Earth Strike", 6, 13));
		assertEquals(8, elementalHit("Fire Strike", 8, 13));
		assertEquals(8, elementalHit("Wind Strike", 2, 99));
		assertEquals(8, elementalHit("Fire Strike", 8, 99));
	}

	@Test
	public void boltBlastWaveAndSurgeScaleToTheirFireSpell()
	{
		assertEquals(9, elementalHit("Wind Bolt", 9, 17));
		assertEquals(10, elementalHit("Wind Bolt", 9, 23));
		assertEquals(11, elementalHit("Earth Bolt", 11, 29));
		assertEquals(12, elementalHit("Wind Bolt", 9, 35));
		assertEquals(12, elementalHit("Fire Bolt", 12, 99));

		assertEquals(13, elementalHit("Wind Blast", 13, 41));
		assertEquals(14, elementalHit("Water Blast", 14, 47));
		assertEquals(15, elementalHit("Earth Blast", 15, 53));
		assertEquals(16, elementalHit("Wind Blast", 13, 59));

		assertEquals(17, elementalHit("Wind Wave", 17, 62));
		assertEquals(18, elementalHit("Wind Wave", 17, 65));
		assertEquals(19, elementalHit("Earth Wave", 19, 70));
		assertEquals(20, elementalHit("Wind Wave", 17, 75));

		assertEquals(21, elementalHit("Wind Surge", 21, 81));
		assertEquals(22, elementalHit("Water Surge", 22, 85));
		assertEquals(23, elementalHit("Earth Surge", 23, 90));
		assertEquals(24, elementalHit("Wind Surge", 21, 95));
		assertEquals(24, elementalHit("Fire Surge", 24, 99));
	}

	@Test
	public void chaosGauntletsAddAfterTheScaledBoltBase()
	{
		CombatInput in = new CombatInput();
		in.combatStyle = CombatStyle.MAGIC;
		in.spellName = "Wind Bolt";
		in.spellMaxHit = 9;
		in.spellElement = "air";
		in.spellbook = "standard";
		in.magicLevel = 35;
		in.chaosGauntlets = true;

		assertEquals(15, CombatMath.maxHit(in));
	}

	@Test
	public void ancientSpellsDoNotUseElementalScaling()
	{
		CombatInput in = new CombatInput();
		in.combatStyle = CombatStyle.MAGIC;
		in.spellName = "Ice Barrage";
		in.spellMaxHit = 30;
		in.spellbook = "ancient";
		in.magicLevel = 99;

		assertEquals(30, CombatMath.maxHit(in));
	}

	@Test
	public void fireSurgeAddsOccultThenSlayerHelm()
	{
		CombatInput in = new CombatInput();
		in.combatStyle = CombatStyle.MAGIC;
		in.spellName = "Fire Surge";
		in.spellMaxHit = 24;
		in.spellElement = "fire";
		in.spellbook = "standard";
		in.magicDamagePermille = 100;
		in.blackMaskImbued = true;
		in.onSlayerTask = true;

		assertEquals(29, CombatMath.maxHit(in));
	}

	@Test
	public void expectedDamageIsHalfTheMaxHitWhenEveryRollHits()
	{
		assertEquals(5.0, CombatMath.expectedDamage(1.0, 10, 0), 0.0001);
	}

	@Test
	public void flatArmourRemovesDamageFromSuccessfulRolls()
	{
		assertEquals(0.6, CombatMath.expectedDamage(1.0, 4, 2), 0.0001);
	}

	@Test
	public void twistedBowAccuracyFloorAtZeroMagic()
	{
		assertEquals(40, CombatMath.tbowScale(100, 0, true));
	}

	@Test
	public void dpsDividesExpectedDamageByAttackInterval()
	{
		CombatInput in = new CombatInput();
		in.strengthLevel = 99;
		in.attackLevel = 99;
		in.meleeAttackPrayer = 100;
		in.meleeStrengthPrayer = 100;
		in.stance = AttackStance.MELEE_AGGRESSIVE;
		in.strengthBonus = 0;
		in.slashBonus = 0;
		in.meleeStyle = MeleeStyle.SLASH;
		in.npcDefence = 0;
		in.npcSlash = -64;
		in.weaponSpeed = 4;

		DpsEstimate estimate = CombatMath.estimate(in, "Dummy", "Def 0");
		assertEquals(4, estimate.attackSpeedTicks);
		double expected = CombatMath.expectedDamage(estimate.accuracy, estimate.maxHit, 0);
		assertEquals(expected / 2.4, estimate.dps, 0.0001);
	}

	private static int elementalHit(String name, int listed, int magic)
	{
		CombatInput in = new CombatInput();
		in.combatStyle = CombatStyle.MAGIC;
		in.spellName = name;
		in.spellMaxHit = listed;
		in.magicLevel = magic;
		return CombatMath.maxHit(in);
	}

	private static CombatInput twistedBow(AttackStance stance)
	{
		CombatInput in = new CombatInput();
		in.combatStyle = CombatStyle.RANGED;
		in.stance = stance;
		in.rangedStyle = RangedStyle.STANDARD;
		in.rangedLevel = 99;
		in.rangedBonus = 70;
		in.rangedStrengthBonus = 80;
		in.weaponSpeed = 6;
		in.twistedBow = true;
		in.weaponName = "Twisted bow";
		in.npcMagic = 100;
		in.npcOffensiveMagic = 0;
		return in;
	}
}
