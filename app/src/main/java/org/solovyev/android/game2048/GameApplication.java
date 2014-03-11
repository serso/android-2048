package org.solovyev.android.game2048;

import android.app.Application;

public class GameApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		App.init(this);
	}
}
