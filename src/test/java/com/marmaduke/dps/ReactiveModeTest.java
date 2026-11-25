package com.marmaduke.dps;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReactiveModeTest
{
	@Test
	public void attackActionMarksAnNpcThePlayerCanFight()
	{
		assertTrue(DpsPlugin.hasAttackAction(new String[] {"Talk-to", "Attack", null}));
		assertFalse(DpsPlugin.hasAttackAction(new String[] {"Talk-to", "Trade", null}));
		assertFalse(DpsPlugin.hasAttackAction(null));
	}

	@Test
	public void reactiveModeKeepsTheLastTargetForSixtySeconds()
	{
		assertFalse(DpsPlugin.reactiveHoldExpired(0));
		assertFalse(DpsPlugin.reactiveHoldExpired(DpsPlugin.REACTIVE_HOLD_TICKS - 1));
		assertTrue(DpsPlugin.reactiveHoldExpired(DpsPlugin.REACTIVE_HOLD_TICKS));
	}

	@Test
	public void hiddenHealthBarDoesNotClearTheReactiveTarget()
	{
		assertFalse(DpsPlugin.clearForHealth(false, 0, 30));
		assertFalse(DpsPlugin.clearForHealth(false, -1, -1));
		assertFalse(DpsPlugin.clearForHealth(false, 0, 0));
		assertTrue(DpsPlugin.clearForHealth(true, 0, 30));
	}

	@Test
	public void overlayKeepsDpsUntilGearPrayersOrBoostsChange()
	{
		CombatInput pinned = new CombatInput();
		pinned.strengthLevel = 99;
		pinned.strengthBonus = 86;
		pinned.npcDefence = 50;

		CombatInput again = new CombatInput();
		again.strengthLevel = 99;
		again.strengthBonus = 86;
		again.npcDefence = 50;
		assertFalse(CombatInput.offenceChanged(pinned, again));
		assertFalse(CombatInput.monsterChanged(pinned, again));
		assertTrue(DpsPlugin.keepDisplayedEstimate(true, false, false));

		CombatInput boosted = new CombatInput();
		boosted.strengthLevel = 118;
		boosted.strengthBonus = 86;
		boosted.npcDefence = 50;
		assertTrue(CombatInput.offenceChanged(pinned, boosted));
		assertFalse(DpsPlugin.keepDisplayedEstimate(true, true, false));

		CombatInput prayed = new CombatInput();
		prayed.strengthLevel = 99;
		prayed.strengthBonus = 86;
		prayed.meleeStrengthPrayer = 123;
		prayed.npcDefence = 50;
		assertTrue(CombatInput.offenceChanged(pinned, prayed));

		CombatInput geared = new CombatInput();
		geared.strengthLevel = 99;
		geared.strengthBonus = 100;
		geared.npcDefence = 50;
		assertTrue(CombatInput.offenceChanged(pinned, geared));

		CombatInput drained = new CombatInput();
		drained.strengthLevel = 99;
		drained.strengthBonus = 86;
		drained.npcDefence = 35;
		assertFalse(CombatInput.offenceChanged(pinned, drained));
		assertTrue(CombatInput.monsterChanged(pinned, drained));
		assertFalse(DpsPlugin.keepDisplayedEstimate(true, false, true));
	}

	@Test
	public void lostNpcReadDoesNotReplaceNumbersAlreadyShown()
	{
		assertTrue(DpsPlugin.keepDisplayedEstimate(true, false, false));
		assertFalse(DpsPlugin.keepDisplayedEstimate(false, false, false));
	}

	@Test
	public void reactiveModeDoesNotSwitchToAnNpcWithNoStats()
	{
		assertFalse(DpsPlugin.acceptAttackSwitch(true, false));
		assertTrue(DpsPlugin.acceptAttackSwitch(true, true));
		assertTrue(DpsPlugin.acceptAttackSwitch(false, false));
	}

	@Test
	public void missingEquipmentOrAutocastReadKeepsThePreviousAttack()
	{
		CombatInput previous = new CombatInput();
		previous.combatStyle = CombatStyle.MAGIC;
		previous.strengthBonus = 40;
		previous.weaponName = "Staff of air";
		previous.spellName = "Wind Strike";
		previous.spellMaxHit = 2;
		previous.spellElement = "air";
		previous.spellbook = "standard";

		CombatInput unreadGear = new CombatInput();
		unreadGear.magicLevel = 13;
		CombatInput.keepGearIfUnread(previous, unreadGear);
		assertEquals(40, unreadGear.strengthBonus);
		assertEquals("Staff of air", unreadGear.weaponName);
		assertEquals("Wind Strike", unreadGear.spellName);
		assertEquals(13, unreadGear.magicLevel);

		CombatInput unreadSpell = new CombatInput();
		unreadSpell.combatStyle = CombatStyle.MAGIC;
		unreadSpell.magicLevel = 13;
		CombatInput.keepSpellIfUnread(previous, unreadSpell);
		assertEquals("Wind Strike", unreadSpell.spellName);
		assertEquals(2, unreadSpell.spellMaxHit);
		assertEquals("air", unreadSpell.spellElement);
	}
}
