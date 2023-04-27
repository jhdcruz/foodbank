package com.ptech.foodbank.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.toCameraOptions
import com.mapbox.search.common.DistanceCalculator
import com.mapbox.search.discover.DiscoverAddress
import com.mapbox.search.discover.DiscoverResult
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.ui.view.place.SearchPlace
import java.util.UUID

/**
 * Mapbox-specific utility functions
 */
object Mapbox {
    private val MARKERS_BOTTOM_OFFSET = dpToPx(176).toDouble()
    private val MARKERS_EDGE_OFFSET = dpToPx(64).toDouble()
    private val PLACE_CARD_HEIGHT = dpToPx(300).toDouble()

    val MARKERS_INSETS = EdgeInsets(
        MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, MARKERS_BOTTOM_OFFSET, MARKERS_EDGE_OFFSET
    )

    val MARKERS_INSETS_OPEN_CARD = EdgeInsets(
        MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, PLACE_CARD_HEIGHT, MARKERS_EDGE_OFFSET
    )

    private fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    private fun Context.showToast(text: CharSequence) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    private val Context.inputMethodManager: InputMethodManager
        get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    private fun DiscoverAddress.toSearchAddress(): SearchAddress {
        return SearchAddress(
            houseNumber = houseNumber,
            street = street,
            neighborhood = neighborhood,
            locality = locality,
            postcode = postcode,
            place = place,
            district = district,
            region = region,
            country = country
        )
    }

    private fun Drawable.toBitmap(): Bitmap? {
        return if (this is BitmapDrawable) {
            bitmap
        } else {
            // copying drawable object to not manipulate on the same reference
            val constantState = constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    /**
     * Get last known location from the [LocationEngine].
     */
    @SuppressLint("MissingPermission")
    fun LocationEngine.lastKnownLocation(context: Context, callback: (Point?) -> Unit) {
        if (!PermissionsManager.areLocationPermissionsGranted(context)) {
            callback(null)
        }

        getLastLocation(object : LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(result: LocationEngineResult?) {
                val location =
                    (result?.locations?.lastOrNull() ?: result?.lastLocation)?.let { location ->
                        Point.fromLngLat(location.longitude, location.latitude)
                    }
                callback(location)
            }

            override fun onFailure(exception: Exception) {
                callback(null)
            }
        })
    }

    /**
     * Get user distance to the [destination].
     */
    fun LocationEngine.userDistanceTo(
        context: Context,
        destination: Point,
        callback: (Double?) -> Unit
    ) {
        lastKnownLocation(context) { location ->
            if (location == null) {
                callback(null)
            } else {
                val distance = DistanceCalculator.instance(latitude = location.latitude())
                    .distance(location, destination)
                callback(distance)
            }
        }
    }

    /**
     * Hide input keyboard.
     */
    fun View.hideKeyboard() {
        context.inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }
    /**
     * Show a toast message using a resource string [resId].
     */
    fun Context.showToast(@StringRes resId: Int) {
        showToast(getString(resId))
    }


    /**
     * Get a bitmap from a drawable [resourceId].
     */
    fun Context.bitmapFromDrawableRes(@DrawableRes resourceId: Int): Bitmap? {
        return AppCompatResources.getDrawable(this, resourceId)?.toBitmap()
    }


    /**
     * Get camera bounding box coordinate points
     *
     * see https://docs.mapbox.com/help/glossary/bounding-box/
     */
    fun MapboxMap.getCameraBoundingBox(): BoundingBox {
        val bounds = coordinateBoundsForCamera(cameraState.toCameraOptions())
        return BoundingBox.fromPoints(bounds.southwest, bounds.northeast)
    }

    fun geoIntent(point: Point): Intent {
        return Intent(
            Intent.ACTION_VIEW,
            Uri.parse("geo:0,0?q=${point.latitude()}, ${point.longitude()}")
        )
    }

    fun shareIntent(searchPlace: SearchPlace): Intent {
        val text = "${searchPlace.name}. " +
            "Address: ${searchPlace.address?.formattedAddress(SearchAddress.FormatStyle.Short) ?: "unknown"}. " +
            "Geo coordinate: (lat=${searchPlace.coordinate.latitude()}, lon=${searchPlace.coordinate.longitude()})"

        return Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }

    /**
     * Search a place
     */
    fun DiscoverResult.toSearchPlace(): SearchPlace {
        return SearchPlace(
            id = name + UUID.randomUUID().toString(),
            name = name,
            descriptionText = null,
            address = address.toSearchAddress(),
            resultTypes = listOf(SearchResultType.POI),
            record = null,
            coordinate = coordinate,
            routablePoints = routablePoints,
            categories = categories,
            makiIcon = makiIcon,
            metadata = null,
            distanceMeters = null,
            feedback = null,
        )
    }
}
