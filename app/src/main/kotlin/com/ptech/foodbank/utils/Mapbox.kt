package com.ptech.foodbank.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location2

private const val DEFAULT_CAMERA_ZOOM = 16.0

/**
 * Mapbox SDK integration helper
 *
 * @param mapView MapView instance reference
 */
class Mapbox(private val mapView: MapView) {

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .bearing(it)
                .build(),
        )
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().easeTo(
            CameraOptions.Builder()
                .center(it)
                .zoom(DEFAULT_CAMERA_ZOOM)
                .build()
        )
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        @Suppress("EmptyFunctionBlock")
        override fun onMoveEnd(detector: MoveGestureDetector) {
        }
    }

    /**
     * Track and follow user location on map
     */
    fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    // Update camera on movements
    fun initLocationComponent() {
        val locationComponentPlugin = mapView.location2

        locationComponentPlugin.updateSettings {
            enabled = true
        }

        locationComponentPlugin.addOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener,
        )
        locationComponentPlugin.addOnIndicatorBearingChangedListener(
            onIndicatorBearingChangedListener,
        )
    }

    /**
     * Removes user location tracking.
     * usually when it is no longer needed such as
     * onDestroy() or onPause()
     */
    fun onCameraTrackingDismissed() {
        mapView.location2.removeOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener,
        )

        mapView.location2.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    /**
     * Mapbox-specific utility functions
     */
    companion object Utils {

        @Suppress("MagicNumber")
        private fun Drawable.toBitmap(): Bitmap? {
            return if (this is BitmapDrawable) {
                bitmap
            } else {
                // copying drawable object to not manipulate on the same reference
                val constantState = constantState ?: return null
                val drawable = constantState.newDrawable().mutate()
                val bitmap: Bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888,
                )
                val canvas = Canvas(bitmap)

                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        }

        /**
         * Get a bitmap from a drawable [resourceId].
         */
        fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int): Bitmap? {
            return AppCompatResources.getDrawable(context, resourceId)?.toBitmap()
        }
    }
}
