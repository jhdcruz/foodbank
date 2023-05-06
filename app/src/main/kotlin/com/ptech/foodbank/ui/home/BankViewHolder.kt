package com.ptech.foodbank.ui.home

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.firestore.GeoPoint
import com.ptech.foodbank.R
import com.ptech.foodbank.ui.home.BankUtils.getAddress
import com.ptech.foodbank.ui.home.BankUtils.getBankActionCall
import com.ptech.foodbank.ui.home.BankUtils.getBankActionWeb
import com.ptech.foodbank.ui.home.BankUtils.getBankCapacity
import com.ptech.foodbank.ui.home.BankUtils.getBankImage
import com.ptech.foodbank.ui.home.BankUtils.getVerification

class BankViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    private val viewContext = view.context

    fun setBankImage(image: String) {
        val imageView = view.findViewById<ImageView>(R.id.bank_image)

        viewContext.getBankImage(image, imageView)
    }

    fun setBankName(name: String) {
        val textView = view.findViewById<TextView>(R.id.bank_name)
        textView.text = name
    }

    fun setVerified(verified: Boolean) {
        val mark = view.findViewById<ImageView>(R.id.bank_verified)

        getVerification(mark, verified)
    }

    fun setBankCapacity(capacity: Int) {
        val capacityBar = view.findViewById<LinearProgressIndicator>(R.id.bank_capacity)

        viewContext.getBankCapacity(capacityBar, capacity)
    }

    fun setBankAddress(address: GeoPoint?) {
        val textView = view.findViewById<TextView>(R.id.bank_address)

        viewContext.getAddress(textView, address)
    }

    fun setBankBio(bio: String) {
        val textView = view.findViewById<TextView>(R.id.bank_bio)
        textView.text = bio
    }

    fun setBankOffer() {
        val dirButton = view.findViewById<MaterialButton>(R.id.bank_action_offer)

        dirButton.setOnClickListener {
        }
    }

    fun setBankActionCall(phone: String) {
        val callView = view.findViewById<MaterialButton>(R.id.bank_action_call)

        viewContext.getBankActionCall(callView, phone)
    }

    fun setBankActionWeb(website: String) {
        val webButton = view.findViewById<MaterialButton>(R.id.bank_action_web)

        viewContext.getBankActionWeb(webButton, website)
    }
}
