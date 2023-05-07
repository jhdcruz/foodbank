package com.ptech.foodbank.utils

import android.content.Context
import android.widget.Toast

object Feedback {
    fun Context.showToast(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(
            this,
            msg,
            duration,
        )
            .show()
    }
}
