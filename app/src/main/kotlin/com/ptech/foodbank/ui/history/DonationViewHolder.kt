package com.ptech.foodbank.ui.history

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.ptech.foodbank.R
import java.text.DateFormat

class DonationViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    fun setDonationBank(name: String) {
        view.findViewById<TextView>(R.id.donation_bank).text = name
    }

    fun setDonationDate(dateCreated: Timestamp) {
        val date = DateFormat.getDateTimeInstance().format(dateCreated.toDate())

        view.findViewById<TextView>(R.id.donation_date).text = date
    }

    @SuppressLint("SetTextI18n")
    fun setDonationCategory(category: String) {
        view.findViewById<TextView>(R.id.food_category).text = "Category: $category"
    }

    @SuppressLint("SetTextI18n")
    fun setDonationServing(serving: Int) {
        view.findViewById<TextView>(R.id.food_serving).text = "Serving: $serving"
    }

    fun setPickupAddress(address: String) {
        view.findViewById<TextView>(R.id.pickup_address).text = address
    }

    fun setPickupDate(pickupDate: Timestamp) {
        val date = DateFormat.getDateTimeInstance().format(pickupDate.toDate())

        view.findViewById<TextView>(R.id.pickup_date).text = date
    }

    fun setDonationNotes(notes: String?) {
        val noteView = view.findViewById<TextView>(R.id.donation_notes)

        if (notes.isNullOrEmpty()) {
            noteView.text = "N/A"
        } else {
            noteView.text = notes
        }
    }
}
