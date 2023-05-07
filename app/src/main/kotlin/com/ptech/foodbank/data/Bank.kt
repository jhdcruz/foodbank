package com.ptech.foodbank.data

import com.google.firebase.firestore.GeoPoint

data class Bank(
    val affiliation: String,
    val bio: String,
    val capacity: Int,
    val contacts: Map<String, String>,
    val image: String,
    var location: GeoPoint? = null,
    val name: String,
    val timeOpen: Int,
    val timeClosing: Int,
) {
    @Suppress("unused")
    constructor() : this(
        affiliation = "",
        bio = "",
        capacity = 0,
        contacts = mapOf(),
        image = "",
        location = null,
        name = "",
        timeOpen = 0,
        timeClosing = 0,
    )
}
