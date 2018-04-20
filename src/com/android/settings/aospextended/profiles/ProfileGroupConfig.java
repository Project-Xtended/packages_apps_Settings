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

import java.util.UUID;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.lineageos.app.Profile;
import com.android.internal.util.lineageos.app.ProfileGroup;
import com.android.internal.util.lineageos.app.ProfileManager;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class ProfileGroupConfig extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final CharSequence KEY_SOUNDMODE = "sound_mode";
    private static final CharSequence KEY_VIBRATEMODE = "vibrate_mode";
    private static final CharSequence KEY_LIGHTSMODE = "lights_mode";
    private static final CharSequence KEY_RINGERMODE = "ringer_mode";
    private static final CharSequence KEY_SOUNDTONE = "soundtone";
    private static final CharSequence KEY_RINGTONE = "ringtone";

    private int mRingToneRequestCode = 1;
    private int mSoundToneRequestCode = 2;

    Profile mProfile;
    ProfileGroup mProfileGroup;

    private ListPreference mSoundMode;
    private ListPreference mRingerMode;
    private ListPreference mVibrateMode;
    private ListPreference mLightsMode;
    private ProfileRingtonePreference mRingTone;
    private ProfileRingtonePreference mSoundTone;
    private ProfileManager mProfileManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.profile_settings);

        final Bundle args = getArguments();
        if (args != null) {
            mProfile = (Profile) args.getParcelable("Profile");
            UUID uuid = UUID.fromString(args.getString("ProfileGroup"));

            mProfileManager = ProfileManager.getInstance(getActivity());
            mProfileGroup = mProfile.getProfileGroup(uuid);

            mRingerMode = (ListPreference) findPreference(KEY_RINGERMODE);
            mSoundMode = (ListPreference) findPreference(KEY_SOUNDMODE);
            mVibrateMode = (ListPreference) findPreference(KEY_VIBRATEMODE);
            mLightsMode = (ListPreference) findPreference(KEY_LIGHTSMODE);
            mRingTone = (ProfileRingtonePreference) findPreference(KEY_RINGTONE);
            mSoundTone = (ProfileRingtonePreference) findPreference(KEY_SOUNDTONE);

            mRingTone.setShowSilent(false);
            mSoundTone.setShowSilent(false);

            mSoundMode.setOnPreferenceChangeListener(this);
            mRingerMode.setOnPreferenceChangeListener(this);
            mVibrateMode.setOnPreferenceChangeListener(this);
            mLightsMode.setOnPreferenceChangeListener(this);
            mSoundTone.setOnPreferenceChangeListener(this);
            mRingTone.setOnPreferenceChangeListener(this);

            updateState();
        }
    }

    private void updateState() {
        android.util.Log.v("ProfileGroupConfig", mProfileGroup.toString());
        mVibrateMode.setValue(mProfileGroup.getVibrateMode().name());
        mSoundMode.setValue(mProfileGroup.getSoundMode().name());
        mRingerMode.setValue(mProfileGroup.getRingerMode().name());
        mLightsMode.setValue(mProfileGroup.getLightsMode().name());

        mVibrateMode.setSummary(mVibrateMode.getEntry());
        mSoundMode.setSummary(mSoundMode.getEntry());
        mRingerMode.setSummary(mRingerMode.getEntry());
        mLightsMode.setSummary(mLightsMode.getEntry());

        if (mProfileGroup.getSoundOverride() != null) {
            Uri uri = mProfileGroup.getSoundOverride();
            mSoundTone.setRingtone(uri);
            Ringtone ringtone = RingtoneManager.getRingtone(getPrefContext(), uri);
            if(ringtone!=null) {
                mSoundTone.setSummary(ringtone.getTitle(getPrefContext()));
            } else {
                mSoundTone.setSummary("");
            }
        } else {
            mSoundTone.setSummary("");
        }

        if (mProfileGroup.getRingerOverride() != null) {
            Uri uri = mProfileGroup.getRingerOverride();
            mRingTone.setRingtone(uri);
            Ringtone ringtone = RingtoneManager.getRingtone(getPrefContext(), uri);
            if(ringtone!=null) {
                mRingTone.setSummary(ringtone.getTitle(getPrefContext()));
            } else {
                mRingTone.setSummary("");
            }
        } else {
            mRingTone.setSummary("");
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mVibrateMode) {
            mProfileGroup.setVibrateMode(ProfileGroup.Mode.valueOf((String) newValue));
        } else if (preference == mSoundMode) {
            mProfileGroup.setSoundMode(ProfileGroup.Mode.valueOf((String) newValue));
        } else if (preference == mRingerMode) {
            mProfileGroup.setRingerMode(ProfileGroup.Mode.valueOf((String) newValue));
        } else if (preference == mLightsMode) {
            mProfileGroup.setLightsMode(ProfileGroup.Mode.valueOf((String) newValue));
        }

        mProfileManager.updateProfile(mProfile);

        updateState();
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mRingTone) {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ((ProfileRingtonePreference)preference).onRestoreRingtone());

            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, ((ProfileRingtonePreference)preference).getShowDefault());
            if (((ProfileRingtonePreference)preference).getShowDefault()) {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(((ProfileRingtonePreference)preference).getRingtoneType()));
            }

            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, ((ProfileRingtonePreference)preference).getShowSilent());
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ((ProfileRingtonePreference)preference).getRingtoneType());
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, preference.getTitle());
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_AUDIO_ATTRIBUTES_FLAGS, AudioAttributes.FLAG_BYPASS_INTERRUPTION_POLICY);

            startActivityForResult(intent, mRingToneRequestCode);

            return true;
        } else if(preference == mSoundTone) {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ((ProfileRingtonePreference)preference).onRestoreRingtone());

            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, ((ProfileRingtonePreference)preference).getShowDefault());
            if (((ProfileRingtonePreference)preference).getShowDefault()) {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(((ProfileRingtonePreference)preference).getRingtoneType()));
            }

            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, ((ProfileRingtonePreference)preference).getShowSilent());
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, ((ProfileRingtonePreference)preference).getRingtoneType());
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, preference.getTitle());
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_AUDIO_ATTRIBUTES_FLAGS, AudioAttributes.FLAG_BYPASS_INTERRUPTION_POLICY);

            startActivityForResult(intent, mSoundToneRequestCode);
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SYSTEM_PROFILES;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == mRingToneRequestCode && data!=null){
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            Ringtone ringtone = RingtoneManager.getRingtone(getPrefContext(), uri);
            mRingTone.setSummary(ringtone.getTitle(getPrefContext()));
            mProfileGroup.setRingerOverride(uri);
            mProfileManager.updateProfile(mProfile);
            updateState();
        }else if(requestCode == mSoundToneRequestCode && data!=null){
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            Ringtone ringtone = RingtoneManager.getRingtone(getPrefContext(), uri);
            mSoundTone.setSummary(ringtone.getTitle(getPrefContext()));
            mProfileGroup.setSoundOverride(uri);
            mProfileManager.updateProfile(mProfile);
            updateState();
        }
    }
}