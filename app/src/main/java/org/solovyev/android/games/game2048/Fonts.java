package org.solovyev.android.games.game2048;

import android.graphics.Typeface;
import android.util.SparseArray;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;

import javax.annotation.Nonnull;

public class Fonts {

	@Nonnull
	private GameActivity activity;

	@Nonnull
	private final SparseArray<SparseArray<Font>> cache = new SparseArray<SparseArray<Font>>();

	public Fonts(@Nonnull GameActivity activity) {
		this.activity = activity;
	}

	@Nonnull
	Font getFont(float fontSize, int colorResId) {
		return getFont(fontSize, colorResId, 5, true, true);
	}

	@Nonnull
	Font getFont(float fontSize, int colorResId, int textSize) {
		return getFont(fontSize, colorResId, textSize, false, false);
	}

	@Nonnull
	Font getFont(float fontSize, int colorResId, int textSize, boolean bold) {
		return getFont(fontSize, colorResId, textSize, false, bold);
	}

	@Nonnull
	private Font getFont(float fontSize, int colorResId, int textSize, boolean shouldCache, boolean bold) {
		fontSize = Math.max(5, fontSize);
		SparseArray<Font> fontsBySize = null;
		if (shouldCache) {
			fontsBySize = cache.get((int) fontSize);
		}

		Font font = null;
		if (shouldCache) {
			if (fontsBySize != null) {
				font = fontsBySize.get(colorResId);
			} else {
				fontsBySize = new SparseArray<Font>();
				cache.append((int) fontSize, fontsBySize);
			}
		}

		if (font == null) {
			final int textureWidth = (int) (2 * textSize * fontSize);
			final int textureHeight = (int) (2 * fontSize);
			final Typeface typeface;
			if (bold) {
				typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
			} else {
				typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
			}
			font = FontFactory.create(activity.getFontManager(), activity.getTextureManager(), textureWidth, textureHeight, typeface, fontSize, activity.getColor(colorResId).getARGBPackedInt());
			font.load();
			if (shouldCache) {
				fontsBySize.append(colorResId, font);
			}
		}

		return font;
	}
}
