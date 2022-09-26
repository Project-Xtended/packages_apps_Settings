/*
 * Copyright (C) 2022
 * SPDX-License-Identifier: Apache-2.0
*/

package com.xtended.settings.theme

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet

import androidx.preference.DialogPreference

open class ColorPickerPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : DialogPreference(context, attrs) {

    var color: String? = null
        private set

    override protected fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override protected fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        val def = defaultValue as? String
        color = if (restoreValue) getPersistedString(def) else def
        setSummary(color)
    }

    fun setColor(color: String) {
        if (!callChangeListener(color)) return
        this.color = color
        setSummary(color)
        persistString(color)
    }
}
