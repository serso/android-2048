package org.solovyev.android.games.game2048;

import org.json.JSONException;
import org.json.JSONObject;
import org.solovyev.common.collections.Collections;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Date;

public final class HighScore {

	private static final String JSON_SCORE = "score";
	private static final String JSON_DATE = "date";

	@Nonnull
	private static final HighScore NO_HIGH_SCORE = newHighScore(Score.newScore(), new Date(0));

	@Nonnull
	private final Score score;

	@Nonnull
	private final Date date;

	private HighScore(@Nonnull Score score, @Nonnull Date date) {
		this.score = score;
		this.date = date;
	}

	@Nonnull
	public static HighScore newHighScore(@Nonnull Game game) {
		final Date startDate = game.getStartDate();
		final Score score = game.getScore();
		return newHighScore(score, startDate);
	}

	@Nonnull
	private static HighScore newHighScore(@Nonnull Score score, @Nonnull Date date) {
		return new HighScore(score, date);
	}

	@Nonnull
	public static HighScore newNoHighScore() {
		return NO_HIGH_SCORE;
	}

	@Nonnull
	public Date getDate() {
		return date;
	}

	@Nonnull
	public Score getScore() {
		return score;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final HighScore that = (HighScore) o;

		if (!date.equals(that.date)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return date.hashCode();
	}

	@Nonnull
	public JSONObject toJson() throws JSONException {
		final JSONObject json = new JSONObject();
		json.put(JSON_SCORE, score.toJson());
		json.put(JSON_DATE, date.getTime());
		return json;
	}

	@Nonnull
	public static HighScore fromJson(@Nonnull JSONObject json) throws JSONException {
		final Date date = new Date(json.getLong(JSON_DATE));
		final Score score = Score.fromJson(json.getJSONObject(JSON_SCORE));
		return newHighScore(score, date);
	}

	public boolean hasPoints() {
		return score.hasPoints();
	}

	public int getPoints() {
		return score.getPoints();
	}

	@Nonnull
	public static Comparator<HighScore> getComparator() {
		return HighScoreComparator.instance;
	}

	private static final class HighScoreComparator implements Comparator<HighScore> {

		@Nonnull
		private static final Comparator<HighScore> instance = Collections.reversed(new HighScoreComparator());

		@Override
		public int compare(@Nonnull HighScore lhs, @Nonnull HighScore rhs) {
			final int lPoints = lhs.getPoints();
			final int rPoints = rhs.getPoints();
			if (lPoints > rPoints) {
				return 1;
			} else if (lPoints < rPoints) {
				return -1;
			} else {
				return lhs.date.compareTo(rhs.date);
			}
		}
	}
}
