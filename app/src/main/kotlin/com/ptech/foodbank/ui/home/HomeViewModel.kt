package com.ptech.foodbank.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.google.firebase.firestore.QuerySnapshot
import com.ptech.foodbank.db.DbFactory
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val db = DbFactory()

    val savedBanks = flow<QuerySnapshot> {
        emit(db.getBanks().get().await())
    }.asLiveData()
}
