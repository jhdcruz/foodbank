package com.ptech.foodbank.data

import com.google.firebase.Timestamp

data class Notifications(
    val image: String? = null,
    val type: String,
    val title: String,
    val body: String,
    val dateCreated: Timestamp,
) {
    @Suppress("unused")
    constructor() : this(
        image = "",
        type = "",
        title = "",
        body = "",
        dateCreated = Timestamp.now(),
    )
}
