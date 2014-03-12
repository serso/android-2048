package org.solovyev.android.game2048;

import android.graphics.Point;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.game2048.Board.newBoard;

public class Game {

	private static final int BOARD_SIZE = 4;

	@Nonnull
	private final Board board = newBoard(BOARD_SIZE).reset();

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
		while (newRow > 0 && !board.cells[newRow - 1][col].hasValue()) {
			newRow--;
		}
		return updateBoard(row, col, newRow, col);
	}

	@Nullable
	private Change updateBoard(int row, int col, int newRow, int newCol) {
		if (newRow != row || newCol != col) {
			final Board.Cell cell = board.cells[row][col];
			board.cells[row][col] = board.cells[newRow][newCol];
			board.cells[newRow][newCol] = cell;
			return new Change(cell, new Point(row, col), new Point(newRow, newCol));
		} else {
			return null;
		}
	}

	@Nullable
	private Change goDown(int row, int col) {
		if (!board.cells[row][col].hasValue()) {
			return null;
		}

		int newRow = row;
		while (newRow < board.size - 1 && !board.cells[newRow + 1][col].hasValue()) {
			newRow++;
		}
		return updateBoard(row, col, newRow, col);
	}

	@Nullable
	private Change goRight(int row, int col) {
		if (!board.cells[row][col].hasValue()) {
			return null;
		}

		int newCol = col;
		while (newCol < board.size - 1 && !board.cells[row][newCol + 1].hasValue()) {
			newCol++;
		}
		return updateBoard(row, col, row, newCol);
	}

	@Nullable
	private Change goLeft(int row, int col) {
		if (!board.cells[row][col].hasValue()) {
			return null;
		}
		int newCol = col;
		while (newCol > 0 && !board.cells[row][newCol - 1].hasValue()) {
			newCol--;
		}
		return updateBoard(row, col, row, newCol);
	}

	@Nonnull
	public List<Change> prepareNextTurn() {
		return board.addNewRandomPoint();
	}

	public static final class Change {
		public final Board.Cell cell;

		@Nonnull
		public final Point from;

		@Nonnull
		public final Point to;

		public Change(Board.Cell cell, @Nonnull Point from, @Nonnull Point to) {
			this.cell = cell;
			this.from = from;
			this.to = to;
		}
	}
}
