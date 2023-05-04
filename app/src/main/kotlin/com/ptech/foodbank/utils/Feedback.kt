package com.ptech.foodbank.utils

import android.content.Context
import android.widget.Toast

object Feedback {
    fun Context.showToast(msg: String, duration: Int? = null) {
        Toast.makeText(
            this,
            msg,
            duration ?: Toast.LENGTH_SHORT,
        )
            .show()
    }
}
