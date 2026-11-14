package com.marmaduke.dps;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.ItemComposition;
import net.runelite.api.MenuEntry;
import net.runelite.api.ParamID;
import net.runelite.api.Point;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;

/**
 * Hover tooltip for the prayer book, quick prayers, and combat potions.
 * The pinned DPS number is left unchanged.
 */
class DpsTooltipOverlay extends Overlay
{
	private static final Color GAIN = new Color(0, 180, 70);
	private static final Color LOSS = new Color(220, 60, 60);
	private static final Map<String, Prayer> PRAYERS = prayerNames();

	private final Client client;
	private final ItemManager itemManager;
	private final TooltipManager tooltipManager;
	private final DpsPlugin plugin;

	@Inject
	DpsTooltipOverlay(Client client, ItemManager itemManager, TooltipManager tooltipManager, DpsPlugin plugin)
	{
		this.client = client;
		this.itemManager = itemManager;
		this.tooltipManager = tooltipManager;
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (client.isMenuOpen())
		{
			return null;
		}
		CombatInput current = plugin.getCurrentInput();
		if (current == null)
		{
			return null;
		}
		MenuEntry[] menu = client.getMenuEntries();
		Widget widget = menu.length == 0 ? null : menu[menu.length - 1].getWidget();

		String text = null;
		Prayer prayer = prayerUnderMouse();
		if (prayer == null)
		{
			prayer = prayerFromMenu(menu);
		}
		if (prayer != null)
		{
			if (prayer.name().startsWith("RP_"))
			{
				text = "Not included in this estimate";
			}
			else
			{
				Double delta = DpsDelta.prayer(current, plugin.getCurrentPrayers(), prayer);
				if (delta != null)
				{
					text = DpsDelta.format(delta.doubleValue());
					text = ColorUtil.wrapWithColorTag(text, color(delta.doubleValue()));
				}
			}
		}
		else if (widget != null)
		{
			String itemName = potionAt(widget);
			if (itemName != null)
			{
				Double delta = DpsDelta.potion(
					current,
					client.getRealSkillLevel(Skill.ATTACK),
					client.getRealSkillLevel(Skill.STRENGTH),
					client.getRealSkillLevel(Skill.RANGED),
					client.getRealSkillLevel(Skill.MAGIC),
					itemName);
				if (delta != null)
				{
					text = DpsDelta.format(delta.doubleValue());
					text = ColorUtil.wrapWithColorTag(text, color(delta.doubleValue()));
				}
			}
		}
		if (text != null)
		{
			tooltipManager.add(new Tooltip(text));
		}
		return null;
	}

	/**
	 * Prayer icons do not put the prayer widget on the menu the way a potion puts its item there.
	 * Hit-test the prayer book and the quick-prayer buttons under the cursor instead.
	 */
	private Prayer prayerUnderMouse()
	{
		Point mouse = client.getMouseCanvasPosition();
		if (mouse == null)
		{
			return null;
		}
		EnumComposition prayers = prayerBook();
		if (prayers == null)
		{
			return null;
		}
		int x = mouse.getX();
		int y = mouse.getY();
		int[] keys = prayers.getKeys();
		for (int i = 0; i < keys.length; i++)
		{
			ItemComposition definition = client.getItemDefinition(prayers.getIntValue(keys[i]));
			if (definition == null)
			{
				continue;
			}
			int component = definition.getIntValue(ParamID.OC_PRAYER_COMPONENT);
			Widget widget = component > 0 ? client.getWidget(component) : null;
			if (widget != null && containsMouse(widget, x, y, 0))
			{
				Prayer prayer = prayerFromWidget(definition, widget);
				if (prayer != null)
				{
					return prayer;
				}
				return prayerFromBookText();
			}
		}
		return quickPrayerUnderMouse(prayers, x, y);
	}

	private Prayer quickPrayerUnderMouse(EnumComposition prayers, int x, int y)
	{
		Widget buttons = client.getWidget(InterfaceID.Quickprayer.BUTTONS);
		if (buttons == null || buttons.isHidden())
		{
			return null;
		}
		Widget[] children = buttons.getDynamicChildren();
		if (children == null)
		{
			return null;
		}
		int[] keys = prayers.getKeys();
		for (int i = 0; i < keys.length; i++)
		{
			int key = keys[i];
			if (key < 0 || key >= children.length || !containsMouse(children[key], x, y, 0))
			{
				continue;
			}
			ItemComposition definition = client.getItemDefinition(prayers.getIntValue(key));
			Prayer prayer = prayerFromWidget(definition, children[key]);
			if (prayer != null)
			{
				return prayer;
			}
		}
		for (int i = 0; i < children.length; i++)
		{
			if (!containsMouse(children[i], x, y, 0))
			{
				continue;
			}
			Prayer prayer = prayerFromLabel(children[i].getName());
			if (prayer == null)
			{
				prayer = prayerFromLabel(children[i].getText());
			}
			if (prayer != null)
			{
				return prayer;
			}
		}
		return null;
	}

	private Prayer prayerFromMenu(MenuEntry[] menu)
	{
		EnumComposition prayers = prayerBook();
		if (prayers == null || menu == null)
		{
			return null;
		}
		for (int m = menu.length - 1; m >= 0; m--)
		{
			Widget widget = menu[m].getWidget();
			if (widget == null)
			{
				continue;
			}
			int[] keys = prayers.getKeys();
			for (int i = 0; i < keys.length; i++)
			{
				ItemComposition definition = client.getItemDefinition(prayers.getIntValue(keys[i]));
				if (definition == null)
				{
					continue;
				}
				int component = definition.getIntValue(ParamID.OC_PRAYER_COMPONENT);
				if (component > 0 && widgetChain(widget, component))
				{
					Prayer prayer = prayerFromWidget(definition, widget);
					if (prayer != null)
					{
						return prayer;
					}
					return prayerFromBookText();
				}
			}
			if (WidgetUtil.componentToInterface(widget.getId()) == InterfaceID.QUICKPRAYER)
			{
				Prayer prayer = prayerFromLabel(widget.getName());
				if (prayer == null)
				{
					prayer = prayerFromLabel(widget.getText());
				}
				if (prayer != null)
				{
					return prayer;
				}
			}
		}
		return null;
	}

	private Prayer prayerFromWidget(ItemComposition definition, Widget widget)
	{
		Prayer prayer = definition == null ? null : prayerFromLabel(definition.getName());
		if (prayer == null && widget != null)
		{
			prayer = prayerFromLabel(widget.getName());
		}
		if (prayer == null && widget != null)
		{
			prayer = prayerFromLabel(widget.getText());
		}
		return prayer;
	}

	private Prayer prayerFromBookText()
	{
		Widget info = client.getWidget(InterfaceID.Prayerbook.INFO);
		Prayer prayer = info == null ? null : prayerFromLabel(info.getText());
		if (prayer != null)
		{
			return prayer;
		}
		Widget tooltip = client.getWidget(InterfaceID.Prayerbook.TOOLTIP);
		return tooltip == null ? null : prayerFromLabel(tooltip.getText());
	}

	private EnumComposition prayerBook()
	{
		int book = client.getVarbitValue(VarbitID.PRAYERBOOK);
		return client.getEnum(book == 1 ? EnumID.PRAYERS_RUINOUS : EnumID.PRAYERS_NORMAL);
	}

	static Prayer prayerFromLabel(String raw)
	{
		if (raw == null || raw.isEmpty())
		{
			return null;
		}
		String text = Text.removeTags(raw).toLowerCase(Locale.US).trim();
		if (text.isEmpty())
		{
			return null;
		}
		Prayer exact = PRAYERS.get(text);
		if (exact != null)
		{
			return exact;
		}
		Prayer best = null;
		int bestLength = 0;
		for (Map.Entry<String, Prayer> entry : PRAYERS.entrySet())
		{
			if (text.contains(entry.getKey()) && entry.getKey().length() > bestLength)
			{
				best = entry.getValue();
				bestLength = entry.getKey().length();
			}
		}
		return best;
	}

	private boolean containsMouse(Widget widget, int x, int y, int depth)
	{
		if (widget == null || widget.isHidden() || depth > 3)
		{
			return false;
		}
		Rectangle bounds = widget.getBounds();
		if (bounds != null && bounds.width > 0 && bounds.height > 0 && bounds.contains(x, y))
		{
			return true;
		}
		return containsMouse(widget.getDynamicChildren(), x, y, depth)
			|| containsMouse(widget.getStaticChildren(), x, y, depth)
			|| containsMouse(widget.getNestedChildren(), x, y, depth);
	}

	private boolean containsMouse(Widget[] children, int x, int y, int depth)
	{
		if (children == null)
		{
			return false;
		}
		for (int i = 0; i < children.length; i++)
		{
			if (containsMouse(children[i], x, y, depth + 1))
			{
				return true;
			}
		}
		return false;
	}

	private String potionAt(Widget widget)
	{
		int itemId = itemId(widget);
		if (itemId <= 0)
		{
			return null;
		}
		String name = itemManager.getItemComposition(itemId).getName();
		if (!PotionBoosts.affectsCombat(name))
		{
			return null;
		}
		return name;
	}

	private int itemId(Widget widget)
	{
		int group = WidgetUtil.componentToInterface(widget.getId());
		if (widget.getId() == InterfaceID.Inventory.ITEMS
			|| widget.getId() == InterfaceID.Bankmain.ITEMS
			|| widget.getId() == InterfaceID.SharedBank.ITEMS
			|| group == InterfaceID.BANKSIDE
			|| group == InterfaceID.SHARED_BANK_SIDE)
		{
			return widget.getItemId();
		}
		return -1;
	}

	private static boolean widgetChain(Widget widget, int componentId)
	{
		Widget current = widget;
		for (int depth = 0; current != null && depth < 4; depth++)
		{
			if (current.getId() == componentId)
			{
				return true;
			}
			int parentId = current.getParentId();
			if (parentId == -1)
			{
				return false;
			}
			current = current.getParent();
		}
		return false;
	}

	private static Color color(double delta)
	{
		if (delta > 0.0005)
		{
			return GAIN;
		}
		if (delta < -0.0005)
		{
			return LOSS;
		}
		return Color.WHITE;
	}

	private static Map<String, Prayer> prayerNames()
	{
		Map<String, Prayer> names = new HashMap<String, Prayer>();
		put(names, "Thick Skin", Prayer.THICK_SKIN);
		put(names, "Burst of Strength", Prayer.BURST_OF_STRENGTH);
		put(names, "Clarity of Thought", Prayer.CLARITY_OF_THOUGHT);
		put(names, "Sharp Eye", Prayer.SHARP_EYE);
		put(names, "Mystic Will", Prayer.MYSTIC_WILL);
		put(names, "Rock Skin", Prayer.ROCK_SKIN);
		put(names, "Superhuman Strength", Prayer.SUPERHUMAN_STRENGTH);
		put(names, "Improved Reflexes", Prayer.IMPROVED_REFLEXES);
		put(names, "Rapid Restore", Prayer.RAPID_RESTORE);
		put(names, "Rapid Heal", Prayer.RAPID_HEAL);
		put(names, "Protect Item", Prayer.PROTECT_ITEM);
		put(names, "Hawk Eye", Prayer.HAWK_EYE);
		put(names, "Mystic Lore", Prayer.MYSTIC_LORE);
		put(names, "Steel Skin", Prayer.STEEL_SKIN);
		put(names, "Ultimate Strength", Prayer.ULTIMATE_STRENGTH);
		put(names, "Incredible Reflexes", Prayer.INCREDIBLE_REFLEXES);
		put(names, "Protect from Magic", Prayer.PROTECT_FROM_MAGIC);
		put(names, "Protect from Missiles", Prayer.PROTECT_FROM_MISSILES);
		put(names, "Protect from Melee", Prayer.PROTECT_FROM_MELEE);
		put(names, "Eagle Eye", Prayer.EAGLE_EYE);
		put(names, "Mystic Might", Prayer.MYSTIC_MIGHT);
		put(names, "Retribution", Prayer.RETRIBUTION);
		put(names, "Redemption", Prayer.REDEMPTION);
		put(names, "Smite", Prayer.SMITE);
		put(names, "Preserve", Prayer.PRESERVE);
		put(names, "Chivalry", Prayer.CHIVALRY);
		put(names, "Deadeye", Prayer.DEADEYE);
		put(names, "Mystic Vigour", Prayer.MYSTIC_VIGOUR);
		put(names, "Piety", Prayer.PIETY);
		put(names, "Zeal", Prayer.ZEAL);
		put(names, "Rigour", Prayer.RIGOUR);
		put(names, "Augury", Prayer.AUGURY);
		put(names, "Rejuvenation", Prayer.RP_REJUVENATION);
		put(names, "Ancient Strength", Prayer.RP_ANCIENT_STRENGTH);
		put(names, "Ancient Sight", Prayer.RP_ANCIENT_SIGHT);
		put(names, "Ancient Will", Prayer.RP_ANCIENT_WILL);
		put(names, "Ruinous Grace", Prayer.RP_RUINOUS_GRACE);
		put(names, "Dampen Magic", Prayer.RP_DAMPEN_MAGIC);
		put(names, "Dampen Ranged", Prayer.RP_DAMPEN_RANGED);
		put(names, "Dampen Melee", Prayer.RP_DAMPEN_MELEE);
		put(names, "Trinitas", Prayer.RP_TRINITAS);
		put(names, "Berserker", Prayer.RP_BERSERKER);
		put(names, "Purge", Prayer.RP_PURGE);
		put(names, "Metabolise", Prayer.RP_METABOLISE);
		put(names, "Rebuke", Prayer.RP_REBUKE);
		put(names, "Vindication", Prayer.RP_VINDICATION);
		put(names, "Decimate", Prayer.RP_DECIMATE);
		put(names, "Annihilate", Prayer.RP_ANNIHILATE);
		put(names, "Vaporise", Prayer.RP_VAPORISE);
		put(names, "Fumus' Vow", Prayer.RP_FUMUS_VOW);
		put(names, "Umbra's Vow", Prayer.RP_UMBRA_VOW);
		put(names, "Cruor's Vow", Prayer.RP_CRUORS_VOW);
		put(names, "Glacies' Vow", Prayer.RP_GLACIES_VOW);
		put(names, "Wrath", Prayer.RP_WRATH);
		put(names, "Intensify", Prayer.RP_INTENSIFY);
		return names;
	}

	private static void put(Map<String, Prayer> names, String label, Prayer prayer)
	{
		names.put(label.toLowerCase(Locale.US), prayer);
	}
}
