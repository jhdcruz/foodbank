package com.ptech.foodbank.db

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.ptech.foodbank.data.Donation

interface FirebaseFactory {
    fun getBanks(): CollectionReference

    fun getNotifications(): CollectionReference

    fun getDonations(): CollectionReference

    suspend fun addDonation(data: Donation): Task<DocumentReference>
}
