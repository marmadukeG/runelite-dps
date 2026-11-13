# Check DPS

Sideloadable RuneLite plugin that estimates your damage per second against one NPC.

Right-click an attackable NPC and choose **Check DPS**. A panel in the top-right shows the estimate and updates every game tick (0.6s). It clears when that NPC dies or despawns. Unpin it from the overlay menu (**Clear**) or by right-clicking the same NPC and choosing **Clear DPS**.

The estimate uses the gear you are wearing, your active prayers, and your boosted skill levels as RuneLite already reports them. Potions are not added a second time. Defence reductions are applied only for hits this client sees after the plugin is running.

Hover a prayer icon in the prayer book or the quick-prayer screen, or a combat potion in the inventory or bank, to see the DPS gain or loss if that prayer or potion were toggled. The overlay number stays put until the prayer or potion is actually used. The delta uses the pinned NPC and the attack style you have selected. Turning on Rigour while Eagle Eye is up replaces Eagle Eye, the same way the client does. Ruinous prayers are not in this estimate.

## Sideload

Requirements: JDK 11 or newer.

```bash
./gradlew build
```

The jar is `build/libs/runelite-dps-1.0.0.jar`. It is a plain jar: `com/marmaduke/dps/DpsPlugin.class` sits at the jar root, the class extends `Plugin`, and `@PluginDescriptor(name = "Check DPS")` is visible at runtime. It is compiled against RuneLite client 1.13.6. Copy it into the sideload folder:

- Linux and macOS: `~/.runelite/sideloaded-plugins`
- Windows: `%USERPROFILE%\.runelite\sideloaded-plugins`

RuneLite 1.13.6 only scans that folder when developer mode is actually on. Developer mode is `--developer-mode` **and** no launcher version. Launcher 2.6.10 sets the `runelite.launcher.version` system property, so a client started from the Jagex Launcher or RuneLite.exe keeps developer mode off even when the startup log shows `args: --developer-mode`. In that case `loadSideLoadPlugins` returns immediately, the log never says "Side-loading plugin", and hub plugins still load through `ExternalPluginManager`.

Start the client jar yourself, with assertions, so the launcher property is absent:

```text
java -ea -jar RuneLite.jar --developer-mode
```

The launcher stores that jar under `%USERPROFILE%\.runelite\repository2\` (on Windows) or `~/.runelite/repository2/`. Without `-ea`, 1.13.6 stops at "Developers should enable assertions" and never loads plugins.

In the plugin list, search for **Check DPS**.

Plugin settings (group `checkdps`) move the overlay and toggle the stat breakdown. **Minimal overlay** (`minimalOverlay`) hides the panel and shows only three numbers, in order: DPS, max hit, and hit chance. **Reactive mode** (`reactiveMode`) drops the pin and shows DPS for the NPC you are attacking, switching when that target changes. **On slayer task** turns on the black mask and slayer helmet bonus, which the client cannot see on its own. Salve, void, and the other worn-gear bonuses are detected from equipment.

## What the formula covers

Melee, ranged, and magic use the public OSRS integer formulas:

- Max hit is `(effective level * (bonus + 64) + 320) / 640`.
- The attack roll and the NPC defence roll use `(level + 9) * (bonus + 64)`. NPC magic defence uses the NPC's Magic level, except for the small set of monsters whose defence roll uses Defence instead.
- Accuracy is `1 - (def + 2) / (2 * (atk + 1))` when attack is higher, otherwise `atk / (2 * (def + 1))`. A successful hit is uniform from 0 through the max hit, including 0. Expected damage subtracts flat armour from each roll, then DPS divides by the attack interval in seconds.
- Ranged rapid is one tick faster. A spellbook spell is 5 ticks (4 with a harmonised nightmare staff on the standard book, 6 with a twinflame staff). Powered staves use the weapon's attack speed.
- Prayers: Burst through Zeal, including Chivalry, Piety, Rigour, Augury, Deadeye, and Mystic Vigour. Magic prayers also add their magic-damage percent.
- Magic damage from equipment is read from RuneLite's equipment stats (the displayed percent, stored as permille). Tumeken's shadow multiplies visible magic damage by 3, or by 4 inside Tombs of Amascut, capped at 100%, and multiplies the magic attack bonus the same way. Elite void mage adds 5% magic damage. Virtus adds 3% per piece on ancient spells.
- Void (melee, ranger, mage, including elite) requires the matching helm, top, robe, and void gloves. Salve amulets against undead, an imbued black mask or slayer helmet on task, and Amulet of avarice against revenants. Salve replaces the slayer bonus.
- Arclight and Emberlight against demons add 70% to melee max hit and accuracy. Darklight and Silverlight add 60%. Dragon hunter lance, crossbow, and wand use their usual dragon multipliers. Crystal bow and Bow of faerdhinen scale with crystal armour pieces. Osmumten's fang uses its stab accuracy, and the double-roll version inside Tombs of Amascut. Chaos gauntlets add 3 to bolt spells. Magic dart uses the slayer-staff formula. Tomes of fire, water, and earth multiply matching elemental damage by 11/10 when charged; the tome of water also multiplies water accuracy by 6/5. An NPC's elemental weakness adds `base * severity / 100` to the magic max hit and to the magic attack roll.
- Powered staves: trident of the seas and swamp, sanguinesti, Tumeken's shadow, Thammaron's sceptre, accursed sceptre, eye of ayak, warped sceptre, bone staff, dawnbringer, and starter staff. Uncharged weapons are not treated as powered.
- Twisted bow accuracy and damage scale from the target's Magic level.
- Defence reductions seen after the plugin starts: dragon warhammer (30%, or 5% on a Tekton miss), elder maul (35%, or 5% on a Tekton miss; the hit is taken from the Hitpoints experience change, same as RuneLite's special counter), Bandos godsword (damage through defence, strength, attack, magic, then ranged), accursed sceptre (15% defence and magic, not below 85% of the level when the NPC was first tracked), Arclight and Darklight (5% attack, strength, and defence), Emberlight (those plus magic and ranged), tonalztics of ralos (10% or 20% of the magic level removed from defence), and Vulnerability (10%, or 15% with a charged tome of water, for 100 ticks, refreshed but not stacked).

Monster defence, bonuses, attributes, elemental weakness, and the Tombs / magic-uses-defence flags come from a trimmed snapshot of the Weird Gloop monster dataset, fetched 2026-11-12. The Java is original and follows the published integer formulas. It is not a copy of the calculator's TypeScript.

## What is not modeled

The overlay is the normal attack cycle. It does not include special-attack damage (those specs are used only to lower the target's stats), thralls, multi-target barrage, gear switches during the fight, mark of darkness, sunfire runes, the charge spell, inquisitor, the obsidian set, wilderness weapon damage (Craw's bow, webweaver, ursine chainmace), blowpipe dart strength, enchanted bolt procs, salamanders, chinchompa fuse distance, ruinous prayers, Forinthry surge, soulreaper stacks, a tonalztics double hit on a normal attack, other players' specs from before the plugin was watching, Tombs invocation scaling, the Tombs warden phase-two twisted bow double roll, keris, ratbane, colossal blade, golembane, leaf-bladed weapons, or crystal blessing.

Emberlight's spec is modeled as a 5% drain of attack, strength, defence, magic, and ranged. The wiki lists every combat stat but does not publish a separate percent. When a melee weapon's stab, slash, and crush bonuses tie, the style index picks the type (stab, slash, then crush). A vulnerability that is still active when a later defence drain lands is restored as a flat amount, so the level after it expires can be slightly off. Shadow's 4x magic-damage multiplier is applied only when the monster row is flagged as Tombs of Amascut.
