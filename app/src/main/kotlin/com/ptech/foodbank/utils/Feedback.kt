package com.ptech.foodbank.utils

import android.content.Context
import android.widget.Toast

object Feedback {
    fun showToast(context: Context, msg: String, duration: Int? = null) {
        Toast.makeText(
            context,
            msg,
            duration ?: Toast.LENGTH_SHORT
        )
            .show()
    }
}
