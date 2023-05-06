/*
 * Copyright (C) 2020 Wave-OS
 * Copyright (C) 2022 Project Arcana
 * Copyright (C) 2022 XtendedDroid
 * Copyright (C) 2023 Project-Xtended
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

 package com.android.settings.deviceinfo.firmwareversion;

 import android.content.Context;
 import android.os.Build;
 import android.os.SystemProperties;
 import android.widget.TextView;
 
 import androidx.preference.PreferenceScreen;
 
 import com.android.settings.R;
 import com.android.settingslib.core.AbstractPreferenceController;
 import com.android.settingslib.widget.LayoutPreference;
 
 public class XtendedInfoPreferenceController extends AbstractPreferenceController {
 
     private static final String KEY_xtended_info = "xtended_info";
 
     private static final String PROP_XTENDED_DEVICE = "ro.xtended.device";
     private static final String PROP_XTENDED_RELEASETYPE = "ro.xtended.build.type";
     private static final String PROP_XTENDED_EDITION = "ro.xtended.build.version";
     private static final String PROP_XTENDED_MAINTAINER = "ro.xtended.build.maintainer";
 
     public XtendedInfoPreferenceController(Context context) {
         super(context);
     }
 
     private String getxtendedVersion() {
         final String displayDate = SystemProperties.get(PROP_XTENDED_EDITION,
                 this.mContext.getString(R.string.device_info_default));
 
         return displayDate;
     }
 
     private String getDeviceName() {
         String device = SystemProperties.get(PROP_XTENDED_DEVICE, "");
         if (device.equals("")) {
             device = Build.MANUFACTURER + " " + Build.MODEL;
         }
         return device;
     }
 
     private String getxtendedReleaseType() {
         final String releaseType = SystemProperties.get(PROP_XTENDED_RELEASETYPE,
                 this.mContext.getString(R.string.device_info_default));
         return releaseType.substring(0, 1).toUpperCase() +
                  releaseType.substring(1).toLowerCase();
     }
 
     private String getxtendedMaintainer() {
         final String xtendedMaintainer = SystemProperties.get(PROP_XTENDED_MAINTAINER,
                 this.mContext.getString(R.string.device_info_default));
 
         return xtendedMaintainer;
     }
 
     @Override
     public void displayPreference(PreferenceScreen screen) {
         super.displayPreference(screen);
         final LayoutPreference XtendedInfoPreference = screen.findPreference(KEY_xtended_info);
         final TextView version = (TextView) XtendedInfoPreference.findViewById(R.id.version_message);
         final TextView device = (TextView) XtendedInfoPreference.findViewById(R.id.device_message);
         final TextView releaseType = (TextView) XtendedInfoPreference.findViewById(R.id.release_type_message);
         final TextView maintainer = (TextView) XtendedInfoPreference.findViewById(R.id.maintainer_message);
         final String xtendedVersion = getxtendedVersion();
         final String xtendedDevice = getDeviceName();
         final String xtendedReleaseType = getxtendedReleaseType();
         final String xtendedMaintainer = getxtendedMaintainer();
         version.setText(xtendedVersion);
         device.setText(xtendedDevice);
         releaseType.setText(xtendedReleaseType);
         maintainer.setText(xtendedMaintainer);
     }
 
     @Override
     public boolean isAvailable() {
         return true;
     }
 
     @Override
     public String getPreferenceKey() {
         return KEY_xtended_info;
     }
 }
 
