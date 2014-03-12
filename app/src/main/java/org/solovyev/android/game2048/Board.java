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
	public List<CellChange.New> addNewRandomCell() {
		return addNewRandomCells(1);
	}

	@Nonnull
	private List<CellChange.New> addNewRandomCells(int count) {
		return addNewRandomCells(count, Cell.START_VALUE);
	}

	@Nonnull
	private List<CellChange.New> addNewRandomCells(int count, int value) {
		final List<EmptyCell> emptyCells = getEmptyCells();
		if(emptyCells.size() == 0) {
			return Collections.emptyList();
		} else {
			final List<CellChange.New> newCells = new ArrayList<CellChange.New>(count);
			do {
				final int position = r.nextInt(emptyCells.size());
				final EmptyCell emptyCell = emptyCells.get(position);
				emptyCell.cell.setValue(value);
				newCells.add(new CellChange.New(emptyCell.cell, emptyCell.position));
				emptyCells.remove(position);
				count--;
			} while (count > 0 && !emptyCells.isEmpty());
			return newCells;
		}
	}

	@Nonnull
	private List<EmptyCell> getEmptyCells() {
		final List<EmptyCell> result = new ArrayList<EmptyCell>();
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				if (cells[i][j].isEmpty()) {
					result.add(new EmptyCell(cells[i][j], new Point(i, j)));
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
	CellChange.Move updateBoard(int row, int col, int newRow, int newCol) {
		if (newRow != row || newCol != col) {
			final Cell cell = cells[row][col];
			cells[row][col] = cells[newRow][newCol];
			cells[newRow][newCol] = cell;
			return new CellChange.Move(cell, new Point(row, col), new Point(newRow, newCol));
		} else {
			return null;
		}
	}

	public static final class EmptyCell {
		@Nonnull
		private final Cell cell;

		@Nonnull
		private final Point position;

		public EmptyCell(@Nonnull Cell cell, @Nonnull Point position) {
			this.cell = cell;
			this.position = position;
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
