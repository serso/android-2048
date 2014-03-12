package org.solovyev.android.game2048;

import android.graphics.Point;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.game2048.Board.newBoard;

public class Game {

	private static final int BOARD_SIZE = 6;
	private static final boolean WITH_WALLS = true;

	@Nonnull
	private final Board board = newBoard(BOARD_SIZE, WITH_WALLS);

	private Game() {
	}

	@Nonnull
	public static Game newGame() {
		return new Game();
	}

	@Nonnull
	public Board getBoard() {
		return board;
	}

	@Nonnull
	public List<Change> go(@Nonnull Direction direction) {
		final List<Change> changes = new ArrayList<Change>();

		switch (direction) {
			case left:
				for (int col = 0; col < board.size; col++) {
					for (int row = 0; row < board.size; row++) {
						add(changes, goLeft(row, col));
					}
				}
				break;
			case up:
				for (int row = 0; row < board.size; row++) {
					for (int col = 0; col < board.size; col++) {
						add(changes, goUp(row, col));
					}
				}
				break;
			case right:
				for (int col = board.size - 1; col >= 0; col--) {
					for (int row = 0; row < board.size; row++) {
						add(changes, goRight(row, col));
					}
				}
				break;
			case down:
				for (int row = board.size - 1; row >= 0; row--) {
					for (int col = 0; col < board.size; col++) {
						add(changes, goDown(row, col));
					}
				}
				break;
		}

		return changes;
	}

	private void add(@Nonnull List<Change> changes, @Nullable Change change) {
		if (change != null) {
			changes.add(change);
		}
	}

	@Nullable
	private Change goUp(int row, int col) {
		if (!board.cells[row][col].hasValue()) {
			return null;
		}
		int newRow = row;
		while (newRow > 0 && board.cells[newRow - 1][col].isEmpty()) {
			newRow--;
		}
		return board.updateBoard(row, col, newRow, col);
	}

	@Nullable
	private Change goDown(int row, int col) {
		if (!board.cells[row][col].hasValue()) {
			return null;
		}

		int newRow = row;
		while (newRow < board.size - 1 && board.cells[newRow + 1][col].isEmpty()) {
			newRow++;
		}
		return board.updateBoard(row, col, newRow, col);
	}

	@Nullable
	private Change goRight(int row, int col) {
		if (!board.cells[row][col].hasValue()) {
			return null;
		}

		int newCol = col;
		while (newCol < board.size - 1 && board.cells[row][newCol + 1].isEmpty()) {
			newCol++;
		}
		return board.updateBoard(row, col, row, newCol);
	}

	@Nullable
	private Change goLeft(int row, int col) {
		if (!board.cells[row][col].hasValue()) {
			return null;
		}
		int newCol = col;
		while (newCol > 0 && board.cells[row][newCol - 1].isEmpty()) {
			newCol--;
		}
		return board.updateBoard(row, col, row, newCol);
	}

	@Nonnull
	public List<Change> prepareNextTurn() {
		return board.addNewRandomCell();
	}

	public static final class Change {

		@Nonnull
		public final Board.Cell cell;

		@Nonnull
		public Point from;

		@Nonnull
		public Point to;

		public Change(@Nonnull Board.Cell cell, @Nonnull Point from, @Nonnull Point to) {
			this.cell = cell;
			this.from = from;
			this.to = to;
		}
	}
}
