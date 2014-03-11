package org.solovyev.android.game2048;

import android.app.Application;
import android.os.Handler;
import android.widget.Toast;
import org.solovyev.android.Threads;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class App {

	@Nonnull
	public static final String TAG = "2048";

	@Nonnull
	private static App instance = new App();

	@Nonnull
	private Application application;

	@Nonnull
	private Handler uiHandler;

	private App() {
	}

	private void init0(@Nonnull Application application) {
		this.application = application;
		this.uiHandler = Threads.newUiHandler();
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
	public static Handler getUiHandler() {
		return instance.uiHandler;
	}
}
