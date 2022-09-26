/*
 * Copyright (C) 2022
 * SPDX-License-Identifier: Apache-2.0
*/

package com.xtended.settings.theme

import androidx.preference.Preference

import com.android.internal.logging.nano.MetricsProto
import com.android.settings.dashboard.DashboardFragment
import com.xtended.settings.theme.ColorPickerFragment
import com.xtended.settings.theme.ColorPickerPreference

abstract class XDashboardFragment : DashboardFragment() {

    override fun getMetricsCategory(): Int = MetricsProto.MetricsEvent.XTENSIONS

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is ColorPickerPreference) {
            ColorPickerFragment(preference.color).apply {
                setOnConfirmListener {
                    preference.setColor(it)
                }
            }.show(childFragmentManager, COLOR_PICKER_DIALOG_KEY)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    companion object {
        const val COLOR_PICKER_DIALOG_KEY = "color_picker_dialog"
    }
}
