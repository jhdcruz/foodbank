package com.ptech.foodbank.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ptech.foodbank.R
import com.ptech.foodbank.data.Donation

class DonationRecyclerAdapter(private val data: List<Donation>) :
    RecyclerView.Adapter<DonationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.component_donation_card, parent, false)

        return DonationViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    // create a function to bind a bank view holder
    override fun onBindViewHolder(holder: DonationViewHolder, position: Int) {
        val donation = data[position]

        holder.apply {
            setDonationBank(donation.bank)
            setDonationDate(donation.dateCreated)
            setDonationCategory(donation.category)
            setDonationServing(donation.serving)
            setPickupAddress(donation.pickupAddress)
            setPickupDate(donation.pickupDate)
            setDonationNotes(donation.notes)
        }
    }
}
