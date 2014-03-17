package org.solovyev.android.games.game2048;

import android.graphics.Point;
import com.google.common.base.Splitter;
import org.andengine.entity.IEntity;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;

public class Board {

	private static final String JSON_SIZE = "size";
	private static final String JSON_CELLS = "cells";
	private static final String JSON_SEPARATOR_ROW = ";";
	private static final String JSON_SEPARATOR_CELL = ",";

	private final Random r = new Random(currentTimeMillis());

	final Cell[][] cells;
	final int size;
	boolean withWalls;

	private IEntity view;

	private Board(int size) {
		this.size = size;
		this.cells = new Cell[size][size];
	}

	@Nonnull
	public static Board newBoard(int size, boolean withWalls) {
		final Board board = new Board(size);
		board.reset(withWalls);
		return board;
	}

	@Nonnull
	public Board random(boolean withWalls) {
		reset(withWalls);

		final int empty = getEmptyCells().size();
		for (int i = 0; i < empty; i++) {
			addNewRandomCells(1, (int) Math.pow(2, 1 + r.nextInt(14)));
		}

		return this;
	}

	@Nonnull
	public Board reset(boolean withWalls) {
		this.withWalls = withWalls;

		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cells[i][j] = Cell.newEmpty();
			}
		}

		addNewRandomCells(2);
		if (withWalls) {
			addNewRandomCells(max(2, size * size / 6), Cell.WALL);
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
		if (emptyCells.size() == 0) {
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
			final Cell s = cells[row][col];
			final Cell d = cells[newRow][newCol];
			if (s.getValue() == d.getValue()) {
				assert !d.isMerged();
				assert !s.isMerged();

				cells[newRow][newCol] = s;
				cells[row][col] = Cell.newEmpty();
				s.merge();
				return new CellChange.Move.Merge(s, new Point(row, col), new Point(newRow, newCol), d);
			} else if (d.isEmpty()) {
				cells[newRow][newCol] = s;
				cells[row][col] = d;
				return new CellChange.Move(s, new Point(row, col), new Point(newRow, newCol));
			}
		}

		return null;
	}

	@Nonnull
	public List<CellChange.New> prepareNextTurn(@Nonnull Difficulty difficulty) {
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cells[i][j].unmerge();
			}
		}
		return addNewRandomCells(difficulty.getNewCellCount(size));
	}

	@Nonnull
	public JSONObject toJson() throws JSONException {
		final JSONObject result = new JSONObject();
		result.put(JSON_SIZE, size);
		final StringBuilder sb = new StringBuilder(size * size * 3);
		for (int i = 0; i < cells.length; i++) {
			if (i > 0) {
				sb.append(JSON_SEPARATOR_ROW);
			}
			for (int j = 0; j < cells[i].length; j++) {
				if (j > 0) {
					sb.append(JSON_SEPARATOR_CELL);
				}
				sb.append(cells[i][j].toJson());
			}
		}
		result.put(JSON_CELLS, sb.toString());
		return result;
	}

	@Nonnull
	public static Board fromJson(@Nonnull JSONObject json) throws JSONException {
		final int size = json.getInt(JSON_SIZE);
		final Board board = new Board(size);
		board.withWalls = false;
		final String cells = json.getString(JSON_CELLS);

		int i = 0;
		final Splitter rowSplitter = Splitter.on(JSON_SEPARATOR_ROW);
		final Splitter cellSplitter = Splitter.on(JSON_SEPARATOR_CELL);
		for (String row : rowSplitter.split(cells)) {
			int j = 0;
			for (String cell : cellSplitter.split(row)) {
				board.cells[i][j] = Cell.fromJson(cell);
				board.withWalls |= board.cells[i][j].isWall();
				j++;
			}
			i++;
		}
		return board;
	}

	public void releaseViews() {
		view = null;
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cells[i][j].releaseView();
			}
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
