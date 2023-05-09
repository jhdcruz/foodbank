package com.ptech.foodbank.data

import com.google.firebase.Timestamp

data class Donation(
    val bank: String,
    val category: String,
    val serving: Int,
    val pickupDate: Timestamp,
    val dateCreated: Timestamp,
    val notes: String? = null,
    val pickupAddress: String,
    val donor: Map<String, Any> = mapOf(
        "id" to "",
        "name" to "",
        "email" to "",
        "phone" to ""
    )
) {
    @Suppress("unused")
    constructor() : this(
        bank = "",
        category = "",
        serving = 0,
        pickupDate = Timestamp.now(),
        dateCreated = Timestamp.now(),
        notes = "",
        pickupAddress = "",
        donor = mapOf(
            "id" to "",
            "name" to "",
            "email" to "",
            "phone" to ""
        )
    )
}
