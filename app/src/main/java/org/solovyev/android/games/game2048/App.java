package org.solovyev.android.games.game2048;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Toast;
import org.solovyev.android.Threads;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public final class App {

	@Nonnull
	public static final String TAG = "2048+";

	@Nonnull
	public static final String SHARE_URL = "https://play.google.com/store/apps/details?id=org.solovyev.android.games.game2048";

	@Nonnull
	private static App instance = new App();

	@Nonnull
	private Application application;

	@Nonnull
	private SharedPreferences preferences;

	@Nonnull
	private Handler uiHandler;

	@Nonnull
	private Game game;

	@Nonnull
	private HighScores highScores;

	private App() {
	}

	private void init0(@Nonnull Application application) {
		this.application = application;
		this.preferences = getDefaultSharedPreferences(application);
		GamePreferences.applyDefaultValues(this.preferences);
		this.uiHandler = Threads.newUiHandler();
		this.game = Game.newFromSave(this.preferences);
		this.highScores = HighScores.newFromPreferences(this.preferences);
	}

	public static void showToast(final int textResId) {
		if (Threads.isUiThread()) {
			newToastRunnable(textResId).run();
		} else {
			getUiHandler().post(newToastRunnable(textResId));
		}
	}

	@Nonnull
	private static Runnable newToastRunnable(final int textResId) {
		return new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplication(), textResId, Toast.LENGTH_SHORT).show();
			}
		};
	}

	public static void showToast(@Nullable final CharSequence text) {
		if (Threads.isUiThread()) {
			newToastRunnable(text).run();
		} else {
			getUiHandler().post(newToastRunnable(text));
		}
	}

	@Nonnull
	private static Runnable newToastRunnable(final CharSequence text) {
		return new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplication(), text, Toast.LENGTH_SHORT).show();
			}
		};
	}

	public static void init(@Nonnull Application application) {
		instance.init0(application);
	}

	@Nonnull
	public static Application getApplication() {
		return instance.application;
	}

	@Nonnull
	public static SharedPreferences getPreferences() {
		return instance.preferences;
	}

	@Nonnull
	public static Handler getUiHandler() {
		return instance.uiHandler;
	}

	@Nonnull
	public static Game getGame() {
		return instance.game;
	}

	@Nonnull
	public static HighScores getHighScores() {
		return instance.highScores;
	}
}
