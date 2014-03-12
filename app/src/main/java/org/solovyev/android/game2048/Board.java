package org.solovyev.android.game2048;

import android.graphics.Point;
import org.andengine.entity.IEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;

public class Board {

	private final Random r = new Random(currentTimeMillis());

	final Cell[][] cells;
	final int size;
	final boolean withWalls;

	@Nonnull
	private IEntity view;

	private Board(int size, boolean withWalls) {
		this.size = size;
		this.withWalls = withWalls;
		this.cells = new Cell[size][size];
		reset();
	}

	@Nonnull
	public static Board newBoard(int size, boolean withWalls) {
		return new Board(size, withWalls);
	}

	@Nonnull
	public Board random() {
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cells[i][j] = Cell.newEmpty();
			}
		}

		if (withWalls) {
			addNewRandomCells(max(2, size * size / 10), Cell.WALL);
		}

		final int empty = getEmptyCells().size();
		for (int i = 0; i < empty; i++) {
			addNewRandomCells(1, (int) Math.pow(2, 1 + r.nextInt(14)));

		}

		return this;
	}

	@Nonnull
	public Board reset() {
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cells[i][j] = Cell.newEmpty();
			}
		}

		addNewRandomCells(2);
		if(withWalls) {
			addNewRandomCells(max(2, size * size / 10), Cell.WALL);
		}

		return this;
	}

	@Nonnull
	public List<Game.Change> addNewRandomCell() {
		return addNewRandomCells(1);
	}

	@Nonnull
	private List<Game.Change> addNewRandomCells(int count) {
		return addNewRandomCells(count, Cell.START_VALUE);
	}

	@Nonnull
	private List<Game.Change> addNewRandomCells(int count, int value) {
		final List<Game.Change> emptyCells = getEmptyCells();
		if(emptyCells.size() == 0) {
			return Collections.emptyList();
		} else {
			final List<Game.Change> newCells = new ArrayList<Game.Change>(count);
			do {
				final int position = r.nextInt(emptyCells.size());
				final Game.Change change = emptyCells.get(position);
				change.cell.value = value;
				newCells.add(change);
				emptyCells.remove(position);
				count--;
			} while (count > 0 && !emptyCells.isEmpty());
			return newCells;
		}
	}

	@Nonnull
	private List<Game.Change> getEmptyCells() {
		final List<Game.Change> result = new ArrayList<Game.Change>();
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				if (cells[i][j].isEmpty()) {
					final Point position = new Point(i, j);
					result.add(new Game.Change(cells[i][j], position, position));
				}
			}
		}
		return result;
	}

	public void setView(@Nonnull IEntity view) {
		this.view = view;
	}

	@Nonnull
	public IEntity getView() {
		return view;
	}

	@Nullable
	Game.Change updateBoard(int row, int col, int newRow, int newCol) {
		if (newRow != row || newCol != col) {
			final Board.Cell cell = cells[row][col];
			cells[row][col] = cells[newRow][newCol];
			cells[newRow][newCol] = cell;
			return new Game.Change(cell, new Point(row, col), new Point(newRow, newCol));
		} else {
			return null;
		}
	}

	public static final class Cell {
		static final int START_VALUE = 2;
		private static final int NO_VALUE = 0;
		private static final int WALL = -1;

		private int value;

		@Nonnull
		private IEntity view;

		public Cell(int value) {
			this.value = value;
		}

		@Nonnull
		public static Cell newWall() {
			return new Cell(WALL);
		}

		@Nonnull
		public static Cell newEmpty() {
			return new Cell(NO_VALUE);
		}

		public int getValue() {
			return value;
		}

		public boolean hasValue() {
			return value > NO_VALUE;
		}

		public boolean isEmpty() {
			return value == NO_VALUE;
		}

		public void setView(@Nonnull IEntity view) {
			this.view = view;
		}

		@Nonnull
		public IEntity getView() {
			return view;
		}

		public boolean isWall() {
			return value == WALL;
		}
	}

	@Nonnull
	public Cell getCell(int i, int j) {
		return cells[i][j];
	}

	public int getSize() {
		return size;
	}
}
