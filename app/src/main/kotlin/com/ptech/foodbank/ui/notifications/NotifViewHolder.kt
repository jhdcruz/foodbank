package com.ptech.foodbank.ui.notifications

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.ptech.foodbank.R
import com.ptech.foodbank.utils.Coil
import java.text.DateFormat

class NotifViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    fun setImage(image: String) {
        val imageView = view.findViewById<ImageView>(R.id.notif_header)

        val loader = Coil.imageLoader(view.context)
        val request = Coil.imageRequest(image, imageView, view.context)

        loader.enqueue(request)
    }

    fun setType(type: String) {
        val capitalize = type.replaceFirstChar { it.uppercase() }

        view.findViewById<TextView>(R.id.notif_type).text = capitalize
    }

    fun setTitle(title: String) {
        view.findViewById<TextView>(R.id.notif_title).text = title
    }

    @SuppressLint("SetTextI18n")
    fun setBody(body: String) {
        view.findViewById<TextView>(R.id.notif_body).text = body
    }

    fun setDateCreated(dateCreated: Timestamp) {
        val date = DateFormat.getDateTimeInstance().format(dateCreated.toDate())

        view.findViewById<TextView>(R.id.notif_date).text = date
    }
}
