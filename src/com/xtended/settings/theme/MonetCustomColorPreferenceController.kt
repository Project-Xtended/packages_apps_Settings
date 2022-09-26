/*
 * Copyright (C) 2022
 * SPDX-License-Identifier: Apache-2.0
*/

package com.xtended.settings.theme

import android.content.Context
import android.os.UserHandle
import android.provider.Settings

import com.android.settings.R
import com.android.settings.core.BasePreferenceController

class MonetCustomColorPreferenceController(
    context: Context,
    key: String,
) : BasePreferenceController(context, key) {

    override fun getAvailabilityStatus(): Int = AVAILABLE

    override fun getSummary(): CharSequence? {
        val customColor = Settings.Secure.getStringForUser(
            mContext.contentResolver,
            Settings.Secure.MONET_ENGINE_COLOR_OVERRIDE,
            UserHandle.USER_CURRENT,
        )
        return if (customColor == null || customColor.isBlank()) {
            mContext.getString(R.string.color_override_default_summary)
        } else {
            mContext.getString(
                R.string.custom_color_override_summary_placeholder,
                customColor
            )
        }
    }
}
