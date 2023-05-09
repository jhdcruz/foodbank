package com.ptech.foodbank.ui.home

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.firestore.GeoPoint
import com.ptech.foodbank.R
import com.ptech.foodbank.ui.home.BankUtils.getAddress
import com.ptech.foodbank.ui.home.BankUtils.getBankActionCall
import com.ptech.foodbank.ui.home.BankUtils.getBankActionEmail
import com.ptech.foodbank.ui.home.BankUtils.getBankActionWeb
import com.ptech.foodbank.ui.home.BankUtils.getBankCapacity
import com.ptech.foodbank.ui.home.BankUtils.getBankImage
import com.ptech.foodbank.utils.Auth.getAuth
import com.ptech.foodbank.utils.Feedback.showToast

class BankViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    private var currentUser = getAuth.currentUser
    private val viewContext = view.context

    fun setBankImage(image: String) {
        val imageView = view.findViewById<ImageView>(R.id.bank_image)

        viewContext.getBankImage(image, imageView)
    }

    fun setBankName(name: String) {
        val textView = view.findViewById<TextView>(R.id.bank_name)
        textView.text = name
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

    fun setBankActionDonate(bank: String) {
        val offerButton = view.findViewById<MaterialButton>(R.id.bank_action_offer)

        offerButton.setOnClickListener {
            // Action requires user to be logged in
            if (currentUser == null) {
                viewContext.showToast("Please login to donate")
            } else {
                val route = HomeFragmentDirections.homeToDonateFragment(bank)
                view.findNavController().navigate(route)
            }
        }
    }

    fun setBankAction(email: String, phone: String, website: String) {
        val callView = view.findViewById<MaterialButton>(R.id.bank_action)

        callView.setOnClickListener {
            val layout =
                LayoutInflater.from(viewContext).inflate(R.layout.component_inquire_dialog, null)
            val dialog = MaterialAlertDialogBuilder(viewContext).setView(layout).create()

            val callButton = layout.findViewById<MaterialButton>(R.id.bank_action_call)
            val emailButton = layout.findViewById<MaterialButton>(R.id.bank_action_email)
            val webButton = layout.findViewById<MaterialButton>(R.id.bank_action_web)

            viewContext.getBankActionCall(callButton, phone)
            viewContext.getBankActionEmail(emailButton, email)
            viewContext.getBankActionWeb(webButton, website)

            dialog.show()
        }
    }
}
