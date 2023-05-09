package com.ptech.foodbank.db

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

internal class FirestoreFactory {
    private val db = FirebaseFirestore.getInstance()


    fun getBanks(): CollectionReference {
        return db.collection("banks")
    }

    fun getNotifications(): CollectionReference {
        return db.collection("notifications")
    }
    fun searchBanks(query: String): Query {
        return db.collection("banks")
            .whereEqualTo("name", query)
    }
}
