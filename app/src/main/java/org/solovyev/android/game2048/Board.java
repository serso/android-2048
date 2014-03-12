package org.solovyev.android.game2048;

import android.graphics.Point;
import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static java.lang.System.currentTimeMillis;

public class Board {
	static final int START_VALUE = 2;

	private final Random r = new Random(currentTimeMillis());

	final Cell[][] cells;
	final int size;

	@Nonnull
	private IEntity view;

	private Board(int size) {
		this.size = size;
		this.cells = new Cell[size][size];
		reset();
	}

	@Nonnull
	public static Board newBoard(int size) {
		return new Board(size);
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

		addNewRandomPoints(2);

		return this;
	}

	@Nonnull
	public List<Game.Change> addNewRandomPoint() {
		return addNewRandomPoints(1);
	}

	@Nonnull
	private List<Game.Change> addNewRandomPoints(int count) {
		final List<Game.Change> emptyCells = new ArrayList<Game.Change>();
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				if (!cells[i][j].hasValue()) {
					final Point position = new Point(i, j);
					emptyCells.add(new Game.Change(cells[i][j], position, position));
				}
			}
		}

		if(emptyCells.size() == 0) {
			return Collections.emptyList();
		} else {
			final List<Game.Change> newCells = new ArrayList<Game.Change>(count);
			do {
				final int position = r.nextInt(emptyCells.size());
				final Game.Change change = emptyCells.get(position);
				change.cell.value = START_VALUE;
				newCells.add(change);
				emptyCells.remove(position);
				count--;
			} while (count > 0 && !emptyCells.isEmpty());
			return newCells;
		}
	}

	public void setView(@Nonnull IEntity view) {
		this.view = view;
	}

	@Nonnull
	public IEntity getView() {
		return view;
	}

	public static final class Cell {
		private int value = 0;
		@Nonnull
		private IEntity view;

		public int getValue() {
			return value;
		}

		public boolean hasValue() {
			return value != 0;
		}

		public void setView(@Nonnull IEntity view) {
			this.view = view;
		}

		@Nonnull
		public IEntity getView() {
			return view;
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
