package com.marmaduke.dps;

import java.util.EnumSet;
import net.runelite.api.Prayer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DpsDeltaTest
{
	@Test
	public void rigourOnRangedIsAGainAndDoesNotChangeTheLiveInput()
	{
		CombatInput current = ranged();
		int level = current.rangedLevel;
		int prayer = current.rangedAttackPrayer;

		Double delta = DpsDelta.prayer(current, EnumSet.noneOf(Prayer.class), Prayer.RIGOUR);

		assertTrue(delta.doubleValue() > 0);
		assertEquals(level, current.rangedLevel);
		assertEquals(prayer, current.rangedAttackPrayer);
		assertTrue(DpsDelta.format(delta.doubleValue()).startsWith("+"));
		assertTrue(DpsDelta.format(delta.doubleValue()).endsWith(" DPS"));
	}

	@Test
	public void turningRigourOffIsALoss()
	{
		CombatInput current = ranged();
		EnumSet<Prayer> active = EnumSet.of(Prayer.RIGOUR);
		PrayerBonus.apply(current, active);

		Double delta = DpsDelta.prayer(current, active, Prayer.RIGOUR);

		assertTrue(delta.doubleValue() < 0);
	}

	@Test
	public void rigourReplacesEagleEyeInsteadOfStacking()
	{
		EnumSet<Prayer> active = EnumSet.of(Prayer.EAGLE_EYE);
		EnumSet<Prayer> toggled = PrayerBonus.toggle(active, Prayer.RIGOUR);

		assertTrue(toggled.contains(Prayer.RIGOUR));
		assertFalse(toggled.contains(Prayer.EAGLE_EYE));

		CombatInput withEagle = ranged();
		PrayerBonus.apply(withEagle, active);
		CombatInput withRigour = ranged();
		PrayerBonus.apply(withRigour, toggled);
		assertTrue(withRigour.rangedStrengthPrayer > withEagle.rangedStrengthPrayer);
	}

	@Test
	public void rangingPotionRaisesDpsUntilTheBoostIsAlreadyActive()
	{
		CombatInput current = ranged();
		Double delta = DpsDelta.potion(current, 99, 99, 99, 99, "Ranging potion(4)");

		assertTrue(delta.doubleValue() > 0);
		assertEquals(99, current.rangedLevel);

		current.rangedLevel = 112;
		Double already = DpsDelta.potion(current, 99, 99, 99, 99, "Divine ranging potion(2)");
		assertEquals(0.0, already.doubleValue(), 0.0001);
	}

	@Test
	public void saradominBrewLowersRangedDps()
	{
		Double delta = DpsDelta.potion(ranged(), 99, 99, 99, 99, "Saradomin brew(4)");
		assertTrue(delta.doubleValue() < 0);
	}

	@Test
	public void prayerHoverLabelMapsToThePrayer()
	{
		assertEquals(Prayer.RIGOUR, DpsTooltipOverlay.prayerFromLabel("Rigour"));
		assertEquals(Prayer.RIGOUR, DpsTooltipOverlay.prayerFromLabel("<col=ff981f>Rigour</col>"));
		assertEquals(Prayer.RIGOUR, DpsTooltipOverlay.prayerFromLabel("Level 74<br>Rigour<br>Increases ranged"));
		assertEquals(Prayer.PROTECT_FROM_MAGIC, DpsTooltipOverlay.prayerFromLabel("Protect from Magic"));
		assertNull(DpsTooltipOverlay.prayerFromLabel("Cancel"));
	}

	@Test
	public void weaponsAndFoodAreNotPotions()
	{
		assertFalse(PotionBoosts.affectsCombat("Twisted bow"));
		assertFalse(PotionBoosts.affectsCombat("Shark"));
		assertNull(DpsDelta.potion(ranged(), 99, 99, 99, 99, "Twisted bow"));
	}

	private static CombatInput ranged()
	{
		CombatInput in = new CombatInput();
		in.combatStyle = CombatStyle.RANGED;
		in.stance = AttackStance.RANGED_RAPID;
		in.rangedLevel = 99;
		in.rangedBonus = 100;
		in.rangedStrengthBonus = 80;
		in.weaponSpeed = 5;
		in.npcDefence = 50;
		in.npcStandard = 0;
		in.npcMagic = 1;
		return in;
	}
}
