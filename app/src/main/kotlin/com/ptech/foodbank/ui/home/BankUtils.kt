package com.ptech.foodbank.ui.home

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
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
import com.ptech.foodbank.utils.Coil
import com.ptech.foodbank.utils.Crashlytics
import com.ptech.foodbank.utils.Feedback.showToast
import com.ptech.foodbank.utils.Permissions

object BankUtils {

    private const val CAPACITY_STABLE = 4
    private const val CAPACITY_HIGH = 7

    fun Context.getBankImage(image: String, imageView: ImageView) {
        val loader = Coil.imageLoader(this)
        val request = Coil.imageRequest(image, imageView, this)

        loader.enqueue(request)
    }

    fun Context.getBankCapacity(capacityBar: LinearProgressIndicator, capacity: Int) {
        capacityBar.progress = capacity

        // change color based on capacity
        when {
            capacity <= CAPACITY_STABLE -> {
                capacityBar.setIndicatorColor(this.getColor(R.color.green))
            }

            capacity <= CAPACITY_HIGH -> {
                capacityBar.setIndicatorColor(this.getColor(R.color.yellow))
            }

            else -> {
                capacityBar.setIndicatorColor(this.getColor(R.color.red))
            }
        }
    }

    fun Context.getBankActionCall(callView: MaterialButton, phone: String) {
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.parse("tel:$phone")
        }

        callView.setOnClickListener {
            // check for call/dialing permission
            if (!Permissions.isPermissionGranted(this, Manifest.permission.CALL_PHONE)) {
                Permissions.getPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.CALL_PHONE,
                    ),
                )
            }

            // callbacks
            @Suppress("SwallowedException")
            try {
                // start call session
                this.startActivity(callIntent)
            } catch (e: SecurityException) {
                // starting call without proper permissions
                this.showToast("Phone permission required to continue")
            } catch (e: ActivityNotFoundException) {
                // report unusual error
                this.showToast("Cannot initiate call session")

                Crashlytics.reporter.recordException(e)
            }
        }
    }

    /** Get [location] from [GeoPoint] */
    fun Context.getAddress(textView: TextView, location: GeoPoint?) {
        val searchEngine = SearchEngine.createSearchEngine(
            SearchEngineSettings(
                this.getString(R.string.mapbox_access_token),
            ),
        )

        // reverse geocoding, get address from geo-point
        val options = ReverseGeoOptions(
            center = Point.fromLngLat(location!!.longitude, location.latitude),
            limit = 1,
            countries = listOf(IsoCountryCode.PHILIPPINES),
        )

        // response callback, handles result/error
        val searchCallback = object : SearchCallback {
            override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                if (results.isNotEmpty()) {
                    textView.text = results[0].fullAddress.toString()
                }
            }

            override fun onError(e: Exception) {
                Crashlytics.reporter.recordException(e)
            }
        }

        searchEngine.search(options, searchCallback)
    }

    fun Context.getBankActionWeb(webView: View, website: String) {
        val webIntent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.parse("https://$website")
        }

        webView.setOnClickListener {
            this.startActivity(webIntent)
        }
    }

    fun Context.getBankActionEmail(emailButton: MaterialButton, email: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.parse("mailto:$email")
        }

        emailButton.setOnClickListener {
            this.startActivity(emailIntent)
        }
    }
}
