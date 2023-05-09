package com.ptech.foodbank.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ptech.foodbank.data.Bank
import com.ptech.foodbank.db.FirebaseFactoryImpl
import com.ptech.foodbank.utils.Crashlytics.reporter

class HomeViewModel : ViewModel() {
    private val db = FirebaseFactoryImpl()
    private val mutableBankList = MutableLiveData<List<Bank>>()

    val bankList: List<Bank> get() = mutableBankList.value ?: emptyList()

    fun banks(): LiveData<List<Bank>> {
        db.getBanks()
            .get()
            .addOnSuccessListener {
                mutableBankList.value = it.toObjects(Bank::class.java)
            }
            .addOnFailureListener { e ->
                reporter.recordException(e)
            }

        return mutableBankList
    }
}
