package org.solovyev.android.game2048;

import android.graphics.Typeface;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;

import javax.annotation.Nonnull;

public final class CellStyle {
	private final int value;
	private final int textColorResId;
	private final int bgColorResId;

	private Font font;

	CellStyle(int value, int textColorResId, int bgColorResId) {
		this.value = value;
		this.textColorResId = textColorResId;
		this.bgColorResId = bgColorResId;
	}

	@Nonnull
	public static CellStyle newCellStyle(int value, int textColorResId, int bgColorResId) {
		return new CellStyle(value, textColorResId, bgColorResId);
	}

	public int getTextColorResId() {
		return textColorResId;
	}

	public int getBgColorResId() {
		return bgColorResId;
	}

	public int getValue() {
		return value;
	}

	public void loadFont(GameActivity a, float fontSize) {
		font = FontFactory.create(a.getFontManager(), a.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), fontSize, a.getColor(textColorResId).getARGBPackedInt());
		font.load();
	}

	public Font getFont() {
		return font;
	}
}
