package com.ptech.foodbank.utils

import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

internal object Auth {
    val getAuth = FirebaseAuth.getInstance()
    val authUi = AuthUI.getInstance()
}
