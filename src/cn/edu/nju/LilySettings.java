package cn.edu.nju;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.widget.EditText;

public class LilySettings extends PreferenceActivity implements
		OnPreferenceChangeListener {

	private CheckBoxPreference mCheckBox;
	private EditTextPreference mEdit;
	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.getPreferenceManager().setSharedPreferencesName("settings");
		addPreferencesFromResource(R.xml.prefers);

		preferences = getPreferenceManager().getSharedPreferences();
		mCheckBox = (CheckBoxPreference) getPreferenceScreen().findPreference(
				"showpic");
		mEdit = (EditTextPreference) getPreferenceScreen()
				.findPreference("sig");
		mEdit.setSummary(preferences.getString("sig", ""));
		mCheckBox.setOnPreferenceChangeListener(this);
		mEdit.setOnPreferenceChangeListener(this);

	}

	@Override
	public boolean onPreferenceChange(Preference arg0, Object arg1) {
		if ("showpic".equals(arg0.getKey())) {
			LilyUtil.bundle.putBoolean("showpic", (Boolean) arg1);
		} else if ("sig".equals(arg0.getKey())) {
			mEdit.setSummary(arg1.toString());
			LilyUtil.bundle.putString("sig", arg1.toString());
		} else {
		}
		return true;
	}

}
