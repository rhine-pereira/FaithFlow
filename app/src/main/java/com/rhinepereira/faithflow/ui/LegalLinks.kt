package com.rhinepereira.faithflow.ui

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.rhinepereira.faithflow.BuildConfig

object LegalLinks {
    fun openPrivacyPolicy(context: Context) {
        openUrl(context, BuildConfig.PRIVACY_POLICY_URL)
    }

    fun openTermsAndConditions(context: Context) {
        openUrl(context, BuildConfig.TERMS_AND_CONDITIONS_URL)
    }

    fun openDataDeletion(context: Context) {
        openUrl(context, BuildConfig.DATA_DELETION_URL)
    }

    private fun openUrl(context: Context, url: String) {
        val intent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        intent.launchUrl(context, Uri.parse(url))
    }
}
