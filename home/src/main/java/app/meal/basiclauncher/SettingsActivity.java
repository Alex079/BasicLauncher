package app.meal.basiclauncher;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.MenuItem;

import java.util.Calendar;
import java.util.List;

import app.meal.basiclauncher.event.Event;
import app.meal.basiclauncher.event.LocalEventsManager;

public class SettingsActivity extends PreferenceActivity {

    /*private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else if (preference instanceof RingtonePreference) {
                if (TextUtils.isEmpty(stringValue)) {
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        preference.setSummary(null);
                    } else {
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };*/

    private static final Preference.OnPreferenceChangeListener clockFormatListener = new Preference.OnPreferenceChangeListener() {
        private final Calendar calendar = Calendar.getInstance();
        @Override public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (TextUtils.isEmpty(stringValue)) {
                preference.setSummary(R.string.clock_format_default);
            } else {
                preference.setSummary(DateFormat.format(stringValue, calendar));
            }
            LocalEventsManager.getInstance().send(new Event(Event.Type.CLOCK_FORMAT, stringValue));
            return true;
        }
    };

    private static final Preference.OnPreferenceChangeListener clockFontSizeListener = new Preference.OnPreferenceChangeListener() {
        @Override public boolean onPreferenceChange(Preference preference, Object value) {
            LocalEventsManager.getInstance().send(new Event(Event.Type.CLOCK_FONT_SIZE, value));
            return true;
        }
    };

    private static final Preference.OnPreferenceChangeListener followRotationListener = new Preference.OnPreferenceChangeListener() {
        @Override public boolean onPreferenceChange(Preference preference, Object value) {
            LocalEventsManager.getInstance().send(new Event(Event.Type.FOLLOW_ROTATION, value));
            return true;
        }
    };

    private static final Preference.OnPreferenceChangeListener dockSizeListener = new Preference.OnPreferenceChangeListener() {
        @Override public boolean onPreferenceChange(Preference preference, Object value) {
            LocalEventsManager.getInstance().send(new Event(Event.Type.DOCK_SIZE, value));
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || AppearancePreferenceFragment.class.getName().equals(fragmentName)
                || SystemPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AppearancePreferenceFragment extends MyPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_appearance);
            setHasOptionsMenu(true);

            Preference preference;

            preference = findPreference(getString(R.string.clock_format_key));
            preference.setOnPreferenceChangeListener(clockFormatListener);
            clockFormatListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), preference.getContext().getString(R.string.clock_format_default)));

            preference = findPreference(getString(R.string.clock_font_size_key));
            preference.setOnPreferenceChangeListener(clockFontSizeListener);
            clockFontSizeListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getInt(preference.getKey(), preference.getContext().getResources().getInteger(R.integer.clock_font_size_default)));

            preference = findPreference(getString(R.string.icons_follow_key));
            preference.setOnPreferenceChangeListener(followRotationListener);
            followRotationListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(), preference.getContext().getResources().getBoolean(R.bool.icons_follow_default)));

            preference = findPreference(getString(R.string.dock_size_key));
            preference.setOnPreferenceChangeListener(dockSizeListener);
            dockSizeListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getInt(preference.getKey(), preference.getContext().getResources().getInteger(R.integer.dock_size_default)));
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SystemPreferenceFragment extends MyPreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_system);
            setHasOptionsMenu(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static abstract class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
