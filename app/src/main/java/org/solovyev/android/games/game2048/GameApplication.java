package org.solovyev.android.games.game2048;

import android.app.Application;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formKey = "",
		mailTo = "se.solovyev@gmail.com",
		mode = ReportingInteractionMode.SILENT)
public class GameApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		ACRA.init(this);
		App.init(this);
	}
}
