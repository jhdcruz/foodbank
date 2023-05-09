package com.ptech.foodbank.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.MetadataChanges
import com.ptech.foodbank.data.Notifications
import com.ptech.foodbank.db.FirebaseFactoryImpl

class NotificationsViewModel : ViewModel() {
    private val db = FirebaseFactoryImpl()

    fun notifications(): LiveData<List<Notifications>> {
        val notificationsList = MutableLiveData<List<Notifications>>()

        db.getNotifications().addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            notificationsList.value = snapshot?.toObjects(Notifications::class.java)
        }

        return notificationsList
    }
}
