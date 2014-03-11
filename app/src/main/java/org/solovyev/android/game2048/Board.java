package org.solovyev.android.game2048;

import android.graphics.Point;

import javax.annotation.Nonnull;
import java.util.Random;

import static java.lang.System.currentTimeMillis;

public class Board {
	static final int SIZE = 4;
	static final int START_VALUE = 2;

	private final Random r = new Random(currentTimeMillis());

	private final Cell[][] cells = new Cell[SIZE][SIZE];

	{
		reset();
	}

	private Board() {
	}

	@Nonnull
	public static Board newBoard() {
		return new Board();
	}

	@Nonnull
	public Board random() {
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cells[i][j] = new Cell();
				cells[i][j].value = (int) Math.pow(2, 1 + r.nextInt(14));
			}
		}

		return this;
	}

	@Nonnull
	public Board reset() {
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cells[i][j] = new Cell();
			}
		}

		final Point firstPoint = newRandomPoint();
		setStartCell(firstPoint);

		Point secondPoint;
		do {
			secondPoint = newRandomPoint();
		} while (secondPoint.equals(firstPoint));
		setStartCell(secondPoint);

		return this;
	}

	private void setStartCell(@Nonnull Point point) {
		cells[point.x][point.y].value = START_VALUE;
	}

	@Nonnull
	private Point newRandomPoint() {
		return new Point(r.nextInt(SIZE), r.nextInt(SIZE));
	}

	public static final class Cell {
		private int value = 0;

		public int getValue() {
			return value;
		}

		public boolean hasValue() {
			return value != 0;
		}
	}

	@Nonnull
	public Cell getCell(int i, int j) {
		return cells[i][j];
	}

}
