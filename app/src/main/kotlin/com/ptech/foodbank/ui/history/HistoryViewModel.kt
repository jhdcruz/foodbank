package com.ptech.foodbank.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.ptech.foodbank.data.Donation
import com.ptech.foodbank.db.FirebaseFactoryImpl
import com.ptech.foodbank.utils.Auth.getAuth

class HistoryViewModel : ViewModel() {
    private val db = FirebaseFactoryImpl()

    private val currentUser = getAuth.currentUser

    /** Get donations made by current user */
    fun getDonations(): LiveData<List<Donation>> {
        val donations: MutableLiveData<List<Donation>> = MutableLiveData()

        db.getDonations().whereEqualTo("donor.id", currentUser?.uid)
            .orderBy("dateCreated", Query.Direction.DESCENDING)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                donations.value = snapshot?.toObjects(Donation::class.java)
            }

        return donations
    }
}
