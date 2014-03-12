package org.solovyev.android.game2048;

import android.graphics.Point;

import javax.annotation.Nonnull;

public abstract class CellChange {

	@Nonnull
	public final Cell cell;

	public CellChange(@Nonnull Cell cell) {
		this.cell = cell;
	}

	public static final class New extends CellChange {

		@Nonnull
		public final Point position;

		public New(@Nonnull Cell cell, @Nonnull Point position) {
			super(cell);
			this.position = position;
		}
	}
	public static final class Move extends CellChange {

		@Nonnull
		public final Point from;

		@Nonnull
		public final Point to;

		public Move(@Nonnull Cell cell, @Nonnull Point from, @Nonnull Point to) {
			super(cell);
			this.from = from;
			this.to = to;
		}
	}
}
