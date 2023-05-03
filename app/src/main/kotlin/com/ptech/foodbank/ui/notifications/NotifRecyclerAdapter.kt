package com.ptech.foodbank.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ptech.foodbank.R
import com.ptech.foodbank.data.Notifications

class NotifRecyclerAdapter(private val data: List<Notifications>) :
    RecyclerView.Adapter<NotifViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.component_notif_card, parent, false)

        return NotifViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    // create a function to bind a bank view holder
    override fun onBindViewHolder(holder: NotifViewHolder, position: Int) {
        val notif = data[position]

        holder.apply {
            notif.image?.let { setImage(it) }

            setType(notif.type)
            setTitle(notif.title)
            setBody(notif.body)
            setDateCreated(notif.dateCreated)
        }
    }
}
