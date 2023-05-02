package com.ptech.foodbank.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat

object Permissions {
    private const val PERMISSIONS_CODE = 0

    /** Check for requested [permission] */
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission,
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getPermissions(context: Context, permissions: Array<String>) {
        requestPermissions(
            context as Activity,
            permissions,
            PERMISSIONS_CODE,
        )
    }
}
