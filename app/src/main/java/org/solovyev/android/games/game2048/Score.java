package org.solovyev.android.games.game2048;

import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class Score {

	private static final String JSON_POINTS = "points";

	private int points;

	private Score() {
	}

	@Nonnull
	public static Score newScore() {
		return new Score();
	}

	@Nonnull
	public JSONObject toJson() throws JSONException {
		final JSONObject result = new JSONObject();
		result.put(JSON_POINTS, points);
		return result;
	}

	@Nonnull
	public static Score fromJson(@Nullable JSONObject json) throws JSONException {
		final Score score = newScore();
		if (json != null) {
			score.points = json.getInt(JSON_POINTS);
		}
		return score;
	}

	public void onMoveChanged(@Nonnull List<CellChange.Move> changes) {
		for (CellChange.Move change : changes) {
			if(change instanceof CellChange.Move.Merge) {
				points += change.cell.getValue();
			}
		}
	}

	public boolean hasPoints() {
		return points > 0;
	}

	public int getPoints() {
		return points;
	}
}
