package org.solovyev.android.games.game2048;

import org.andengine.opengl.font.Font;

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

	public void loadFont(@Nonnull GameActivity a, float fontSize) {
		font = getFont(a, fontSize);
	}

	@Nonnull
	Font getFont(@Nonnull GameActivity a, float fontSize) {
		return a.getFonts().getFont(fontSize, textColorResId);
	}

	public Font getFont() {
		return font;
	}
}
