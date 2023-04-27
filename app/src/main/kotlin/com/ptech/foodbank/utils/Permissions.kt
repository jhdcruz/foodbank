package com.ptech.foodbank.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object Permissions {
    const val PERMISSIONS_CODE = 0

    /**
     * Check for requested [permission]
     */
    fun Context.isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}
