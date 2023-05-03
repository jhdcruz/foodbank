package com.ptech.foodbank.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.google.firebase.firestore.QuerySnapshot
import com.ptech.foodbank.db.FirestoreFactory
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class NotificationsViewModel : ViewModel() {
    private val db = FirestoreFactory()

    val notifications = flow<QuerySnapshot> {
        val query = db.getNotifications()

        emit(query.get().await())
    }.asLiveData()
}
