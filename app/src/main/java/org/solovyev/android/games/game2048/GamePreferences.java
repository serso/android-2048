package org.solovyev.android.games.game2048;

import android.content.SharedPreferences;
import org.solovyev.android.prefs.BooleanPreference;
import org.solovyev.android.prefs.IntegerPreference;
import org.solovyev.android.prefs.StringPreference;
import org.solovyev.common.text.Mapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.games.game2048.Board.newBoard;

final class GamePreferences {

	@Nonnull
	private static final StringPreference<Difficulty> DIFFICULTY = StringPreference.ofEnum("game.difficulty", Difficulty.normal, Difficulty.class);

	@Nonnull
	private static final StringPreference<Integer> SIZE = StringPreference.ofTypedValue("game.size", "6", new Mapper<Integer>() {
		@Nullable
		@Override
		public String formatValue(@Nullable Integer integer) throws IllegalArgumentException {
			return String.valueOf(integer);
		}

		@Nullable
		@Override
		public Integer parseValue(@Nullable String s) throws IllegalArgumentException {
			return Integer.valueOf(s);
		}
	});

	@Nonnull
	private static final BooleanPreference WITH_WALLS = BooleanPreference.of("game.withWalls", true);

	private GamePreferences(@Nonnull Difficulty difficulty, int size, boolean withWalls) {
		this.difficulty = difficulty;
		this.size = size;
		this.withWalls = withWalls;
	}

	@Nonnull
	public final Difficulty difficulty;
	public final int size;
	public final boolean withWalls;

	public static void applyDefaultValues(@Nonnull SharedPreferences preferences) {
		DIFFICULTY.tryPutDefault(preferences);
		SIZE.tryPutDefault(preferences);
		WITH_WALLS.tryPutDefault(preferences);
	}

	@Nonnull
	static GamePreferences getDefault() {
		final SharedPreferences preferences = App.getPreferences();
		final Difficulty difficulty = DIFFICULTY.getPreferenceNoError(preferences);
		final Integer size = SIZE.getPreferenceNoError(preferences);
		final Boolean withWalls = WITH_WALLS.getPreferenceNoError(preferences);
		return new GamePreferences(difficulty, size, withWalls);
	}
}
