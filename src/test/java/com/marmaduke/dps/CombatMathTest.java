package com.marmaduke.dps;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CombatMathTest
{
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
}
