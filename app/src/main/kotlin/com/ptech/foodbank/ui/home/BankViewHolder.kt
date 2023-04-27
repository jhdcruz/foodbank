package com.ptech.foodbank.ui.home

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.GeoPoint
import com.mapbox.geojson.Point
import com.mapbox.search.ResponseInfo
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.result.SearchResult
import com.ptech.foodbank.R
import com.ptech.foodbank.utils.Coil.imageLoader
import com.ptech.foodbank.utils.Coil.imageRequest


class BankViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    fun setBankImage(image: String) {
        val imageView = view.findViewById<ImageView>(R.id.bank_image)

        val loader = imageLoader(view.context)
        val request = imageRequest(image, imageView, view.context)

        loader.enqueue(request)
    }

    fun setBankName(name: String) {
        val textView = view.findViewById<TextView>(R.id.bank_name)
        textView.text = name
    }

    fun setBankCapacity(capacity: Int) {
        val capacityBar = view.findViewById<ProgressBar>(R.id.bank_capacity)
        capacityBar.progress = capacity
    }

    fun setBankAddress(address: GeoPoint) {
        val textView = view.findViewById<TextView>(R.id.bank_address)

        // init mapbox search api engine
        val searchEngine = SearchEngine.createSearchEngine(
            SearchEngineSettings(
                view.context.getString(R.string.mapbox_access_token)
            )
        )

        // reverse geocoding, get address from geo point
        val options = ReverseGeoOptions(
            center = Point.fromLngLat(address.longitude, address.latitude),
            limit = 1
        )

        // response callback, handles result/error
        val searchCallback = object : SearchCallback {
            override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                if (results.isEmpty()) {
                    Log.i("SearchApiExample", "No reverse geocoding results")
                } else {
                    textView.text = results[0].fullAddress
                }
            }

            override fun onError(e: Exception) {
                textView.text = ""
                Log.i("SearchApiExample", "Reverse geocoding error", e)
            }
        }

        searchEngine.search(options, searchCallback)
    }

    fun setBankBio(bio: String) {
        val textView = view.findViewById<TextView>(R.id.bank_bio)
        textView.text = bio
    }

    fun setBankActionCall(phone: String) {
        val callIntent = Intent(Intent.ACTION_DIAL)
        callIntent.data = Uri.parse(phone)


        val callView = view.findViewById<View>(R.id.bank_action_call)
        callView.setOnClickListener {
            view.context.startActivity(callIntent)
        }
    }

    fun setBankActionWeb(website: String) {
        val webIntent = Intent(Intent.ACTION_WEB_SEARCH)
        webIntent.data = Uri.parse("https://$website")

        val webButton = view.findViewById<View>(R.id.bank_action_web)
        webButton.setOnClickListener {
            view.context.startActivity(webIntent)
        }
    }
}
