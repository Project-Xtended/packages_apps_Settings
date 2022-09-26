/*
 * Copyright (C) 2022
 * SPDX-License-Identifier: Apache-2.0
*/

package com.xtended.settings.theme

import android.content.ContentResolver
import android.provider.Settings

import androidx.preference.PreferenceDataStore

class SecureSettingsStore(
    private val contentResolver: ContentResolver
): PreferenceDataStore() {

    override fun getBoolean(key: String, defValue: Boolean) =
        getInt(key, if (defValue) 1 else 0) == 1

    override fun getFloat(key: String, defValue: Float) =
        Settings.Secure.getFloat(contentResolver, key, defValue)

    override fun getInt(key: String, defValue: Int) =
        Settings.Secure.getInt(contentResolver, key, defValue)

    override fun getLong(key: String, defValue: Long) =
        Settings.Secure.getLong(contentResolver, key, defValue)

    override fun getString(key: String, defValue: String?): String? =
        Settings.Secure.getString(contentResolver, key) ?: defValue

    override fun putBoolean(key: String, value: Boolean) {
        putInt(key, if (value) 1 else 0)
    }

    override fun putFloat(key: String, value: Float) {
        Settings.Secure.putFloat(contentResolver, key, value)
    }

    override fun putInt(key: String, value: Int) {
        Settings.Secure.putInt(contentResolver, key, value)
    }

    override fun putLong(key: String, value: Long) {
        Settings.Secure.putLong(contentResolver, key, value)
    }

    override fun putString(key: String, value: String?) {
        Settings.Secure.putString(contentResolver, key, value)
    }
}
