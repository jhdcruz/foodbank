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

const val MAX_CHAR = 100

class NotifViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    fun setImage(image: String) {
        val imageView = view.findViewById<ImageView>(R.id.notif_header)

        val loader = Coil.imageLoader(view.context)
        val request = Coil.imageRequest(image, imageView, view.context)

        loader.enqueue(request)
    }

    fun setType(type: String) {
        val notifType = view.findViewById<TextView>(R.id.notif_type)

        notifType.text = type.replaceFirstChar { it.uppercase() }
    }

    fun setTitle(title: String) {
        view.findViewById<TextView>(R.id.notif_title).text = title
    }

    @SuppressLint("SetTextI18n")
    fun setBody(body: String) {
        val truncBody = view.findViewById<TextView>(R.id.notif_body)

        // cut off body if it's too long
        if (body.length > MAX_CHAR) {
            truncBody.text = body.substring(0, MAX_CHAR) + "..."
        } else {
            truncBody.text = body
        }
    }

    fun setDateCreated(dateCreated: Timestamp) {
        val date = view.findViewById<TextView>(R.id.notif_date)

        date.text = DateFormat.getDateTimeInstance().format(dateCreated.toDate())
    }
}
