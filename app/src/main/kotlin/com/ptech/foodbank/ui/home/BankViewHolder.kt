package com.ptech.foodbank.ui.home

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.firestore.GeoPoint
import com.mapbox.geojson.Point
import com.mapbox.search.ResponseInfo
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.result.SearchResult
import com.ptech.foodbank.R
import com.ptech.foodbank.utils.Coil.imageLoader
import com.ptech.foodbank.utils.Coil.imageRequest
import com.ptech.foodbank.utils.Crashlytics.reporter
import com.ptech.foodbank.utils.Feedback.showToast
import com.ptech.foodbank.utils.Permissions
import com.ptech.foodbank.utils.Permissions.isPermissionGranted

private const val CAPACITY_STABLE = 5
private const val CAPACITY_HIGH = 7

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

    fun setVerified(verified: Boolean) {
        val mark = view.findViewById<ImageView>(R.id.bank_verified)

        if (!verified) {
            mark.visibility = View.GONE
        } // image shows by default
    }

    fun setBankCapacity(capacity: Int) {
        val capacityBar = view.findViewById<LinearProgressIndicator>(R.id.bank_capacity)
        capacityBar.progress = capacity

        // change color based on capacity
        when {
            capacity <= CAPACITY_STABLE -> {
                capacityBar.setIndicatorColor(view.context.getColor(R.color.green))
            }

            capacity <= CAPACITY_HIGH -> {
                capacityBar.setIndicatorColor(view.context.getColor(R.color.yellow))
            }

            else -> {
                capacityBar.setIndicatorColor(view.context.getColor(R.color.red))
            }
        }
    }

    fun setBankAddress(address: GeoPoint) {
        val textView = view.findViewById<TextView>(R.id.bank_address)

        // init mapbox search api engine
        val searchEngine = SearchEngine.createSearchEngine(
            SearchEngineSettings(
                view.context.getString(R.string.mapbox_access_token),
            ),
        )

        // reverse geocoding, get address from geo point
        val options = ReverseGeoOptions(
            center = Point.fromLngLat(address.longitude, address.latitude),
            limit = 1,
            countries = listOf(IsoCountryCode.PHILIPPINES),
        )

        // response callback, handles result/error
        val searchCallback = object : SearchCallback {
            override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                if (results.isNotEmpty()) {
                    textView.text = results[0].fullAddress
                }
            }

            override fun onError(e: Exception) {
                reporter.recordException(e)
            }
        }

        searchEngine.search(options, searchCallback)
    }

    fun setBankBio(bio: String) {
        val textView = view.findViewById<TextView>(R.id.bank_bio)
        textView.text = bio
    }

    fun setBankActionDirections(address: GeoPoint) {
        val bundle = Bundle()
        bundle.putString("address", address.toString())

        val dirButton = view.findViewById<View>(R.id.bank_action_direction)

        dirButton.setOnClickListener {
        }
    }

    fun setBankActionCall(phone: String) {
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.parse("tel:$phone")
        }

        val callView = view.findViewById<View>(R.id.bank_action_call)
        callView.setOnClickListener {
            // check for call/dialing permission
            if (!isPermissionGranted(view.context, Manifest.permission.CALL_PHONE)) {
                Permissions.getPermissions(
                    view.context,
                    arrayOf(
                        Manifest.permission.CALL_PHONE,
                    ),
                )
            }

            // callbacks
            @Suppress("SwallowedException")
            try {
                // start call session
                view.context.startActivity(callIntent)
            } catch (e: SecurityException) {
                // starting call without proper permissions
                view.context.showToast("Phone permission required to continue")
            } catch (e: ActivityNotFoundException) {
                // report unusual error
                view.context.showToast("Cannot initiate call session")

                reporter.recordException(e)
            }
        }
    }

    fun setBankActionWeb(website: String) {
        val webIntent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.parse("https://$website")
        }

        val webButton = view.findViewById<View>(R.id.bank_action_web)
        webButton.setOnClickListener {
            view.context.startActivity(webIntent)
        }
    }
}
