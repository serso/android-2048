package org.solovyev.android.games.game2048;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class PreferencesActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.simple);

		if (savedInstanceState == null) {
			final PreferenceListFragment fragment = new PreferenceListFragment();
			fragment.setArguments(PreferenceListFragment.newPreferencesArguments(R.xml.preferences));

			final FragmentManager fm = getSupportFragmentManager();
			final FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.root, fragment, "preferences");
			ft.commit();
		}
	}
}
