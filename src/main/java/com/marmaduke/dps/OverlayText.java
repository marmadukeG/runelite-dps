package com.marmaduke.dps;

import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Breaks overlay copy onto lines that fit a pixel width, including words longer than that width.
 */
final class OverlayText
{
	private OverlayText()
	{
	}

	static List<String> wrap(FontMetrics metrics, String text, int maxWidth)
	{
		if (text == null)
		{
			return Collections.emptyList();
		}
		String trimmed = text.trim();
		if (trimmed.isEmpty())
		{
			return Collections.emptyList();
		}
		int limit = Math.max(maxWidth, 1);
		List<String> lines = new ArrayList<String>();
		StringBuilder current = new StringBuilder();
		String[] words = trimmed.split("\\s+");
		for (int i = 0; i < words.length; i++)
		{
			List<String> pieces = breakWord(metrics, words[i], limit);
			for (int p = 0; p < pieces.size(); p++)
			{
				String piece = pieces.get(p);
				if (current.length() == 0)
				{
					current.append(piece);
					continue;
				}
				String candidate = current.toString() + " " + piece;
				if (metrics.stringWidth(candidate) <= limit)
				{
					current.append(' ').append(piece);
				}
				else
				{
					lines.add(current.toString());
					current.setLength(0);
					current.append(piece);
				}
			}
		}
		if (current.length() > 0)
		{
			lines.add(current.toString());
		}
		return lines;
	}

	static String clip(FontMetrics metrics, String text, int maxWidth)
	{
		if (text == null || text.isEmpty())
		{
			return "";
		}
		if (maxWidth <= 0 || metrics.stringWidth(text) <= maxWidth)
		{
			return text;
		}
		String ellipsis = "...";
		if (metrics.stringWidth(ellipsis) >= maxWidth)
		{
			return ellipsis;
		}
		int end = text.length();
		while (end > 0 && metrics.stringWidth(text.substring(0, end) + ellipsis) > maxWidth)
		{
			end--;
		}
		return text.substring(0, end) + ellipsis;
	}

	private static List<String> breakWord(FontMetrics metrics, String word, int limit)
	{
		List<String> pieces = new ArrayList<String>();
		int start = 0;
		while (start < word.length())
		{
			int end = start + 1;
			while (end < word.length() && metrics.stringWidth(word.substring(start, end + 1)) <= limit)
			{
				end++;
			}
			pieces.add(word.substring(start, end));
			start = end;
		}
		return pieces;
	}
}
