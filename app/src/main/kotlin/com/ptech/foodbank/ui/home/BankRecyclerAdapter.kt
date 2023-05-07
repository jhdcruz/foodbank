package com.ptech.foodbank.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ptech.foodbank.R
import com.ptech.foodbank.data.Bank

class BankRecyclerAdapter(private val data: List<Bank>) :
    RecyclerView.Adapter<BankViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.component_bank_card, parent, false)

        return BankViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    // create a function to bind a bank view holder
    override fun onBindViewHolder(holder: BankViewHolder, position: Int) {
        val bank = data[position]

        holder.apply {
            setBankImage(bank.image)
            setBankName(bank.name)
            setBankCapacity(bank.capacity)
            setBankBio(bank.bio)
            setBankAddress(bank.location)
            setBankOffer()

            bank.contacts["phone"]?.let { setBankActionCall(it) }
            bank.contacts["website"]?.let { setBankActionWeb(it) }
        }
    }
}
