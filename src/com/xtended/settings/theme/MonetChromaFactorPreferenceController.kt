/*
 * Copyright (C) 2022
 * SPDX-License-Identifier: Apache-2.0
*/

package com.xtended.settings.theme

import android.content.Context
import android.os.UserHandle
import android.provider.Settings

import androidx.preference.Preference

import com.android.settings.core.BasePreferenceController
import com.xtended.support.preferences.CustomSeekBarPreference

class MonetChromaFactorPreferenceController(
    context: Context,
    key: String,
) : BasePreferenceController(context, key),
    Preference.OnPreferenceChangeListener {

    override fun getAvailabilityStatus(): Int = AVAILABLE

    override fun updateState(preference: Preference) {
        super.updateState(preference)
        val chromaFactor = Settings.Secure.getFloatForUser(
            mContext.contentResolver,
            Settings.Secure.MONET_ENGINE_CHROMA_FACTOR,
            CHROMA_DEFAULT,
            UserHandle.USER_CURRENT
        ) * 100
        (preference as CustomSeekBarPreference).apply {
            setValue(chromaFactor.toInt())
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        return Settings.Secure.putFloatForUser(
            mContext.contentResolver,
            Settings.Secure.MONET_ENGINE_CHROMA_FACTOR,
            (newValue as Int) / 100f,
            UserHandle.USER_CURRENT
        )
    }

    companion object {
        private const val CHROMA_DEFAULT = 1f
    }
}
