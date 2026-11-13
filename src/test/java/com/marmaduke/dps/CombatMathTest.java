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
