package org.solovyev.android.games.game2048;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;

public class HighScoresActivity extends ListActivity {

	@Nonnull
	private LayoutInflater inflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		inflater = LayoutInflater.from(this);

		getListView().addHeaderView(inflater.inflate(R.layout.high_scores_header, null));
		setListAdapter(new HighScoresAdapter(this, App.getHighScores()));
	}

	private final class HighScoresAdapter extends ArrayAdapter<HighScore> {

		private final SimpleDateFormat dateFormat = new SimpleDateFormat();

		public HighScoresAdapter(Context context, final HighScores highScores) {
			super(context, 0, highScores.asList());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view;
			if (convertView == null) {
				view = inflater.inflate(R.layout.high_score, null);
			} else {
				view = convertView;
			}

			final HighScore highScore = getItem(position);

			final TextView dateTextView = (TextView) view.findViewById(R.id.high_score_date);
			dateTextView.setText(dateFormat.format(highScore.getDate()));

			final TextView pointsTextView = (TextView) view.findViewById(R.id.high_score_points);
			pointsTextView.setText(String.valueOf(highScore.getPoints()));

			return view;
		}
	}
}
