package org.solovyev.android.game2048;

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
		SparseArray<Font> fontsBySize = cache.get((int) fontSize);

		Font font = null;
		if (fontsBySize != null) {
			font = fontsBySize.get(colorResId);
		} else {
			fontsBySize = new SparseArray<Font>();
			cache.append((int) fontSize, fontsBySize);
		}

		if (font == null) {
			final int textureWidth = (int) (10 * fontSize);
			final int textureHeight = (int) Math.ceil(fontSize);
			font = FontFactory.create(activity.getFontManager(), activity.getTextureManager(), textureWidth, textureHeight, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), fontSize, activity.getColor(colorResId).getARGBPackedInt());
			font.load();
			fontsBySize.append(colorResId, font);
		}

		return font;
	}
}
