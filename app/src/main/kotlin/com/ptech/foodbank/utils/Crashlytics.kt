package com.ptech.foodbank.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics

/** Firebase Crashlytics integration for error reporting */
internal object Crashlytics {
    val reporter = FirebaseCrashlytics.getInstance()
}
