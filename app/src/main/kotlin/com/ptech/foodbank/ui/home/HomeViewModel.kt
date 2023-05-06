package com.ptech.foodbank.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ptech.foodbank.data.Bank
import com.ptech.foodbank.db.FirestoreFactory
import com.ptech.foodbank.utils.Crashlytics.reporter

class HomeViewModel : ViewModel() {
    private val db = FirestoreFactory()

    fun banks(): LiveData<List<Bank>> {
        val bankList = MutableLiveData<List<Bank>>()

        db.getBanks()
            .get()
            .addOnSuccessListener {
                bankList.value = it.toObjects(Bank::class.java)
            }
            .addOnFailureListener { e ->
                reporter.recordException(e)
            }

        return bankList
    }
}
