package com.ptech.foodbank.db

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

internal class FirestoreFactory {
    private val db = FirebaseFirestore.getInstance()

    init {
        // allow local persistence (offline)
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
    }

    fun getBanks(): CollectionReference {
        return db.collection("banks")
    }
}
