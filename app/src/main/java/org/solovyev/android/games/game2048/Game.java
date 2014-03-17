package org.solovyev.android.games.game2048;

import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.solovyev.android.prefs.StringPreference;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.solovyev.android.games.game2048.App.showToast;
import static org.solovyev.android.games.game2048.Board.newBoard;
import static org.solovyev.android.games.game2048.Score.newScore;

public class Game {

	@Nonnull
	private static final StringPreference<String> state = StringPreference.of("state", null);

	private static final String JSON_BOARD = "board";
	private static final String JSON_SCORE = "score";
	private static final String JSON_DIFFICULTY = "difficulty";
	private static final String JSON_START_DATE = "startDate";

	@Nonnull
	private Board board;

	@Nonnull
	private Score score;

	@Nonnull
	private Difficulty difficulty;

	@Nonnull
	private Date startDate;

	private Game(@Nonnull Board board, @Nonnull Score score, @Nonnull Date startDate, @Nonnull Difficulty difficulty) {
		this.board = board;
		this.score = score;
		this.startDate = startDate;
		this.difficulty = difficulty;
	}

	private Game(@Nonnull Board board, @Nonnull Score score, @Nonnull Difficulty difficulty) {
		this(board, score, new Date(), difficulty);
	}

	@Nonnull
	private static Game newGame() {
		final GamePreferences preferences = GamePreferences.getDefault();
		return new Game(Board.newBoard(preferences.size, preferences.withWalls), newScore(), preferences.difficulty);
	}

	@Nonnull
	public static Game newFromSave(@Nonnull SharedPreferences preferences) {
		return newFromSave(state.getPreference(preferences));
	}

	@Nonnull
	public static Game newFromSave(@Nullable String state) {
		final Game game = fromJson(state);
		return game == null ? newGame() : game;
	}

	@Nonnull
	public Board getBoard() {
		return board;
	}

	@Nonnull
	public Score getScore() {
		return score;
	}

	@Nonnull
	public Date getStartDate() {
		return startDate;
	}

	@Nonnull
	public Difficulty getDifficulty() {
		return difficulty;
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

		score.onMoveChanged(changes);

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
		return board.prepareNextTurn(difficulty);
	}

	@Nullable
	private String toJson() {
		try {
			final JSONObject json = new JSONObject();
			json.put(JSON_DIFFICULTY, difficulty.name());
			json.put(JSON_BOARD, board.toJson());
			json.put(JSON_SCORE, score.toJson());
			json.put(JSON_START_DATE, startDate.getTime());
			return json.toString();
		} catch (RuntimeException e) {
			onSaveLoadException(e, R.string.unable_to_save_game);
		} catch (JSONException e) {
			onSaveLoadException(e, R.string.unable_to_save_game);
		}

		return null;
	}

	@Nullable
	private static Game fromJson(@Nullable String state) {
		try {
			if (!Strings.isEmpty(state)) {
				final JSONObject json = new JSONObject(state);
				if (json.has(JSON_DIFFICULTY) &&
						json.has(JSON_BOARD) &&
						json.has(JSON_SCORE)) {
					final Difficulty difficulty = Difficulty.valueOf(json.getString(JSON_DIFFICULTY));
					final Board board = Board.fromJson(json.getJSONObject(JSON_BOARD));
					final Score score = Score.fromJson(json.getJSONObject(JSON_SCORE));

					final Date startDate;
					if (json.has(JSON_START_DATE)) {
						final long startTime = json.getLong(JSON_START_DATE);
						if (startTime > 0) {
							startDate = new Date(startTime);
						} else {
							startDate = new Date();
						}
					} else {
						startDate = new Date();
					}

					return new Game(board, score, startDate, difficulty);
				}
			}
		} catch (RuntimeException e) {
			onSaveLoadException(e, R.string.unable_to_load_game);
		} catch (JSONException e) {
			onSaveLoadException(e, R.string.unable_to_load_game);
		}

		return null;
	}

	private static void onSaveLoadException(@Nonnull Exception e, int messageResId) {
		showToast(messageResId);
		Log.e(App.TAG, e.getMessage(), e);
	}

	public boolean hasDefaultPreferences() {
		final GamePreferences preferences = GamePreferences.getDefault();
		if(preferences.difficulty == this.difficulty) {
			if(preferences.size == this.board.size) {
				if(preferences.withWalls == this.board.withWalls) {
					return true;
				}
			}
		}

		return false;
	}

	public void reset() {
		final GamePreferences preferences = GamePreferences.getDefault();

		this.board = newBoard(preferences.size, preferences.withWalls);
		this.score = newScore();
		this.startDate = new Date();
		this.difficulty = preferences.difficulty;
	}

	public boolean isOver() {
		final Cell[][] cells = board.cells;

		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				if (canMove(i, j, cells)) {
					return false;
				}
			}
		}

		return true;
	}

	private boolean canMove(int i, int j, @Nonnull Cell[][] cells) {
		final Cell cell = cells[i][j];
		if (cell.isEmpty()) {
			return true;
		} else if (cell.isWall()) {
			return false;
		} else {
			if (i < board.size - 1) {
				final Cell adjacentCell = cells[i + 1][j];
				if (adjacentCell.isEmpty() || adjacentCell.getValue() == cell.getValue()) {
					return true;
				}
			}

			if (i > 0) {
				final Cell adjacentCell = cells[i - 1][j];
				if (adjacentCell.isEmpty() || adjacentCell.getValue() == cell.getValue()) {
					return true;
				}
			}

			if (j < board.size - 1) {
				final Cell adjacentCell = cells[i][j + 1];
				if (adjacentCell.isEmpty() || adjacentCell.getValue() == cell.getValue()) {
					return true;
				}
			}

			if (j > 0) {
				final Cell adjacentCell = cells[i][j - 1];
				if (adjacentCell.isEmpty() || adjacentCell.getValue() == cell.getValue()) {
					return true;
				}
			}

			return false;
		}
	}

	public void save(@Nonnull SharedPreferences preferences) {
		state.putPreference(preferences, toJson());
	}

	public void releaseViews() {
		board.releaseViews();
	}
}
