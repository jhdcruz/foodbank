package com.ptech.foodbank.db

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

internal class FirestoreFactory {
    private val db = FirebaseFirestore.getInstance()

    fun getBanks(): CollectionReference {
        return db.collection("banks")
    }

    fun getNotifications(): CollectionReference {
        return db.collection("notifications")
    }
}
