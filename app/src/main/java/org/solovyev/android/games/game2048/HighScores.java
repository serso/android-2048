package org.solovyev.android.games.game2048;

import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.solovyev.android.prefs.StringPreference;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;
import static java.util.Collections.sort;
import static org.solovyev.android.games.game2048.HighScore.newHighScore;
import static org.solovyev.android.games.game2048.HighScore.newNoHighScore;

public final class HighScores {

	private static final int SIZE = 5;

	@Nonnull
	private static final StringPreference<String> preference = StringPreference.of("highScores", null);

	@Nonnull
	private final List<HighScore> highScores;

	private HighScores() {
		this(new ArrayList<HighScore>(SIZE));
	}

	private HighScores(@Nonnull List<HighScore> highScores) {
		this.highScores = highScores;
		sort(this.highScores, HighScore.getComparator());
	}

	@Nonnull
	public HighScore getHighestScore() {
		if (highScores.size() > 0) {
			return highScores.get(0);
		} else {
			return newNoHighScore();
		}
	}

	public boolean addHighScore(@Nonnull Game game) {
		return addHighScore(newHighScore(game));
	}

	private boolean addHighScore(@Nonnull HighScore score) {
		if (!score.hasPoints()) {
			return false;
		}

		highScores.add(score);
		sort(highScores, HighScore.getComparator());

		if (highScores.size() > SIZE) {
			final HighScore oldScore = highScores.remove(highScores.size() - 1);
			return oldScore != score;
		}

		return true;
	}

	@Nonnull
	public static HighScores newFromPreferences(@Nonnull SharedPreferences preferences) {
		final HighScores highScores = fromJson(preference.getPreference(preferences));
		return highScores == null ? new HighScores() : highScores;
	}

	@Nullable
	private static HighScores fromJson(@Nullable String state) {
		try {
			if (!Strings.isEmpty(state)) {
				final JSONArray json = new JSONArray(state);

				final List<HighScore> highScores = new ArrayList<HighScore>(json.length());
				for (int i = 0; i < min(json.length(), SIZE); i++) {
					highScores.add(HighScore.fromJson(json.optJSONObject(i)));
				}

				return new HighScores(highScores);
			}
		} catch (RuntimeException e) {
			Log.e(App.TAG, e.getMessage(), e);
		} catch (JSONException e) {
			Log.e(App.TAG, e.getMessage(), e);
		}

		return null;
	}

	@Nullable
	private String toJson() {
		try {
			final JSONArray json = new JSONArray();
			for (int i = 0; i < highScores.size(); i++) {
				json.put(i, highScores.get(i).toJson());
			}
			return json.toString();
		} catch (RuntimeException e) {
			Log.e(App.TAG, e.getMessage(), e);
		} catch (JSONException e) {
			Log.e(App.TAG, e.getMessage(), e);
		}

		return null;
	}

	public void save(@Nonnull SharedPreferences preferences) {
		preference.putPreference(preferences, toJson());
	}

	@Nonnull
	public final List<HighScore> asList() {
		return new ArrayList<HighScore>(this.highScores);
	}
}
