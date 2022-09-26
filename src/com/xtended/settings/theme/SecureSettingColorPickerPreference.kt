/*
 * Copyright (C) 2022
 * SPDX-License-Identifier: Apache-2.0
*/

package com.xtended.settings.theme

import android.content.Context
import android.util.AttributeSet

public class SecureSettingColorPickerPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
): ColorPickerPreference(context, attrs) {
    init {
        setPreferenceDataStore(SecureSettingsStore(context.contentResolver))
    }
}
