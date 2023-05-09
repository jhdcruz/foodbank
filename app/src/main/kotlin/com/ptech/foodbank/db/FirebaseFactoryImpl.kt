package com.ptech.foodbank.db

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.ptech.foodbank.data.Donation

internal class FirebaseFactoryImpl : FirebaseFactory {
    private val db = FirebaseFirestore.getInstance()

    override fun getBanks(): CollectionReference {
        return db.collection(Collections.BANKS.path)
    }

    override fun getNotifications(): CollectionReference {
        return db.collection(Collections.NOTIFICATIONS.path)
    }

    override fun getDonations(): CollectionReference {
        return db.collection(Collections.DONATIONS.path)
    }

    override suspend fun addDonation(data: Donation): Task<DocumentReference> {
        return db.collection(Collections.DONATIONS.path).add(data)
    }
}
