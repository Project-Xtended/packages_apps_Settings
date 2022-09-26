/*
 * Copyright (C) 2022
 * SPDX-License-Identifier: Apache-2.0
*/

package com.xtended.settings.theme

import com.android.settings.R
import com.android.settings.search.BaseSearchIndexProvider
import com.android.settingslib.search.SearchIndexable
import com.xtended.settings.theme.XDashboardFragment

@SearchIndexable
class MonetEngine : XDashboardFragment() {

    override protected fun getPreferenceScreenResId() = R.xml.monet

    override protected fun getLogTag() = TAG

    companion object {
        private const val TAG = "MonetEngine"

        @JvmField
        val SEARCH_INDEX_DATA_PROVIDER = BaseSearchIndexProvider(R.xml.monet)
    }
}
