package org.solovyev.android.game2048;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.game2048.Board.newBoard;

public class Game {

	private static final int BOARD_SIZE = 6;
	private static final boolean WITH_WALLS = true;
	private static final String JSON_BOARD = "board";

	@Nonnull
	private Board board = newBoard(BOARD_SIZE, WITH_WALLS);

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
	public List<CellChange.Move> go(@Nonnull Direction direction) {
		final List<CellChange.Move> changes = new ArrayList<CellChange.Move>();

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

	private void add(@Nonnull List<CellChange.Move> changes, @Nullable CellChange.Move change) {
		if (change != null) {
			changes.add(change);
		}
	}

	@Nullable
	private CellChange.Move goUp(int row, int col) {
		final Cell cell = board.cells[row][col];
		if (!cell.hasValue()) {
			return null;
		}
		int newRow = row;
		while (newRow > 0) {
			final Cell obstacle = board.cells[newRow - 1][col];
			if (!obstacle.isEmpty()) {
				if (!obstacle.isMerged() && obstacle.getValue() == cell.getValue()) {
					newRow--;
				}
				break;
			}
			newRow--;
		}
		return board.updateBoard(row, col, newRow, col);
	}

	@Nullable
	private CellChange.Move goDown(int row, int col) {
		final Cell cell = board.cells[row][col];
		if (!cell.hasValue()) {
			return null;
		}

		int newRow = row;
		while (newRow < board.size - 1) {
			final Cell obstacle = board.cells[newRow + 1][col];
			if (!obstacle.isEmpty()) {
				if (!obstacle.isMerged() && obstacle.getValue() == cell.getValue()) {
					newRow++;
				}
				break;
			}
			newRow++;
		}
		return board.updateBoard(row, col, newRow, col);
	}

	@Nullable
	private CellChange.Move goRight(int row, int col) {
		final Cell cell = board.cells[row][col];
		if (!cell.hasValue()) {
			return null;
		}

		int newCol = col;
		while (newCol < board.size - 1) {
			final Cell obstacle = board.cells[row][newCol + 1];
			if (!obstacle.isEmpty()) {
				if (!obstacle.isMerged() && obstacle.getValue() == cell.getValue()) {
					newCol++;
				}
				break;
			}
			newCol++;
		}
		return board.updateBoard(row, col, row, newCol);
	}

	@Nullable
	private CellChange.Move goLeft(int row, int col) {
		final Cell cell = board.cells[row][col];
		if (!cell.hasValue()) {
			return null;
		}
		int newCol = col;
		while (newCol > 0) {
			final Cell obstacle = board.cells[row][newCol - 1];
			if (!obstacle.isEmpty()) {
				if (!obstacle.isMerged() && obstacle.getValue() == cell.getValue()) {
					newCol--;
				}
				break;
			}
			newCol--;
		}
		return board.updateBoard(row, col, row, newCol);
	}

	@Nonnull
	public List<CellChange.New> prepareNextTurn() {
		return board.prepareNextTurn();
	}

	@Nullable
	public String saveState() {
		try {
			final JSONObject json = new JSONObject();
			json.put(JSON_BOARD, board.toJson());
			return json.toString();
		} catch (RuntimeException e) {
			Log.e(App.TAG, e.getMessage(), e);
		} catch (JSONException e) {
			Log.e(App.TAG, e.getMessage(), e);
		}

		return null;
	}

	public void loadState(@Nullable String state) {
		try {
			if (!Strings.isEmpty(state)) {
				final JSONObject json = new JSONObject(state);
				this.board = Board.fromJson(json.getJSONObject(JSON_BOARD));
			}
		} catch (RuntimeException e) {
			Log.e(App.TAG, e.getMessage(), e);
		} catch (JSONException e) {
			Log.e(App.TAG, e.getMessage(), e);
		}
	}
}
