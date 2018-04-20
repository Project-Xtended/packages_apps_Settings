/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.aospextended.profiles;

import android.annotation.Nullable;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.lineageos.app.Profile;
import com.android.internal.util.lineageos.app.ProfileManager;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.SubSettings;
import com.android.settings.dashboard.SummaryLoader;

import com.xtended.xtensions.preferences.SystemSettingSwitchPreference;

import java.util.List;
import java.util.UUID;

public class ProfilesSettingsFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "ProfilesSettings";

    public static final String EXTRA_PROFILE = "Profile";
    public static final String EXTRA_NEW_PROFILE = "new_profile_mode";

    private static final int MENU_ADD = Menu.FIRST;
    private static final int MENU_RESET = Menu.FIRST +1;
    private static final int MENU_APP_GROUPS = Menu.FIRST + 2;

    private final IntentFilter mFilter;
    private final BroadcastReceiver mReceiver;

    private ProfileManager mProfileManager;
    private SystemSettingSwitchPreference mProfileEnabler;

    private boolean mEnabled;

    private ViewGroup mContainer;

    static Bundle mSavedState;

    public ProfilesSettingsFragment() {
        mFilter = new IntentFilter();
        mFilter.addAction(ProfileManager.PROFILES_STATE_CHANGED_ACTION);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ProfileManager.PROFILES_STATE_CHANGED_ACTION.equals(action)) {
                    updateProfilesEnabledState();
                }
            }
        };

        setHasOptionsMenu(true);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SYSTEM_PROFILES;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.profiles_settings);

        mEnabled = Settings.System.getInt(getContentResolver(), Settings.System.SYSTEM_PROFILES_ENABLED, 1) == 1;

        mProfileEnabler = (SystemSettingSwitchPreference) findPreference("system_profiles_enabled");
        mProfileEnabler.setTitle(mEnabled? getString(R.string.sp_on) : getString(R.string.sp_off));
        mProfileEnabler.setChecked(mEnabled);
        mProfileEnabler.setOnPreferenceChangeListener(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        FrameLayout frameLayout = new FrameLayout(getActivity());
        mContainer = frameLayout;
        frameLayout.addView(view);
        return frameLayout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDivider(new ColorDrawable(Color.TRANSPARENT));
        setDividerHeight(0);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mProfileManager = ProfileManager.getInstance(getActivity());
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, mFilter);

        // check if we are enabled
        updateProfilesEnabledState();
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(0, MENU_ADD, 0, R.string.sp_profile_reset_title)
                .setAlphabeticShortcut('r')
                .setEnabled(mEnabled)
                .setIcon(R.drawable.ic_menu_add)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, MENU_APP_GROUPS, 0, R.string.sp_profile_appgroups_title)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        menu.add(0, MENU_RESET, 0, R.string.sp_profile_reset_title)
                .setEnabled(mEnabled)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD:
                addProfile();
                return true;
            case MENU_RESET:
                resetAll();
                return true;
            case MENU_APP_GROUPS:
                startFragment(this, AppGroupList.class.getName(),
                        R.string.sp_profile_appgroups_title, 0, null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addProfile() {
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_NEW_PROFILE, true);
        args.putBoolean(SettingsActivity.EXTRA_PREFS_SHOW_BUTTON_BAR, true);
        args.putParcelable(EXTRA_PROFILE, new Profile(getString(R.string.sp_new_profile_name)));

        startFragment(this, SetupTriggersFragment.class.getCanonicalName(), 0, 0,args);
    }

    private void resetAll() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.sp_profile_reset_title)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setMessage(R.string.sp_profile_reset_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mProfileManager.resetAll();
                        mProfileManager.setActiveProfile(
                                mProfileManager.getActiveProfile().getUuid());
                        dialog.dismiss();
                        refreshList();

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateProfilesEnabledState() {
        mEnabled = Settings.System.getInt(getContentResolver(), Settings.System.SYSTEM_PROFILES_ENABLED, 1) == 1;
        getActivity().invalidateOptionsMenu();

        Preference preference = getPreferenceScreen().getPreference(0);
        if (!mEnabled) {
            getPreferenceScreen().removeAll();
            getPreferenceScreen().addPreference(preference);
            mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.sp_profile_empty_list_profiles_off);
        } else {
            getPreferenceScreen().removePreference(mFooterPreferenceMixin.createFooterPreference());
            refreshList();
        }

    }

    public void refreshList() {
        Preference preference = getPreferenceScreen().getPreference(0);
        getPreferenceScreen().removeAll();
        getPreferenceScreen().addPreference(preference);

        // Get active profile, if null
        Profile prof = mProfileManager.getActiveProfile();
        String selectedKey = prof != null ? prof.getUuid().toString() : null;

        for (Profile profile : mProfileManager.getProfiles()) {
            Bundle args = new Bundle();
            args.putParcelable(ProfilesSettingsFragment.EXTRA_PROFILE, profile);
            args.putBoolean(ProfilesSettingsFragment.EXTRA_NEW_PROFILE, false);

            ProfilesPreference ppref = new ProfilesPreference(this, args);
            ppref.setKey(profile.getUuid().toString());
            ppref.setTitle(profile.getName());
            ppref.setPersistent(false);
            ppref.setOnPreferenceChangeListener(this);
            ppref.setSelectable(true);
            ppref.setEnabled(true);

            if (TextUtils.equals(selectedKey, ppref.getKey())) {
                ppref.setChecked(true);
            }

            getPreferenceScreen().addPreference(ppref);
        }
    }

    private void setSelectedProfile(String key) {
        try {
            UUID selectedUuid = UUID.fromString(key);
            mProfileManager.setActiveProfile(selectedUuid);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mProfileEnabler == preference) {
            boolean isEnabled = (boolean) newValue;
            mProfileEnabler.setTitle(isEnabled? getString(R.string.sp_on) : getString(R.string.sp_off));
            Intent intent = new Intent(ProfileManager.PROFILES_STATE_CHANGED_ACTION);
            intent.putExtra(ProfileManager.EXTRA_PROFILES_STATE,
                    isEnabled ?
                            ProfileManager.PROFILES_STATE_ENABLED :
                            ProfileManager.PROFILES_STATE_DISABLED);
            getActivity().sendBroadcast(intent);
            return true;
        } else if (newValue instanceof String) {
            setSelectedProfile((String) newValue);
            refreshList();
            return true;
        }
        return false;
    }

    private static class SummaryProvider implements SummaryLoader.SummaryProvider {

        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            mContext = context;
            mSummaryLoader = summaryLoader;
        }

        @Override
        public void setListening(boolean listening) {
            if (listening) {
                updateSummary();
            }
        }

        private void updateSummary() {
            ProfileManager pm = ProfileManager.getInstance(mContext);
            if (!pm.isProfilesEnabled()) {
                mSummaryLoader.setSummary(this, mContext.getString(R.string.sp_profile_settings_summary_off));
                return;
            }
            Profile p = pm.getActiveProfile();
            if (p != null) {
                mSummaryLoader.setSummary(this, p.getName());
                return;
            }

            mSummaryLoader.setSummary(this, mContext.getString(R.string.sp_profiles_settings_summary));
        }
    }

    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY
            = (activity, summaryLoader) -> new SummaryProvider(activity, summaryLoader);

}
