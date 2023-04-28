package com.ptech.foodbank.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.ptech.foodbank.R
import java.lang.ref.WeakReference
class MapFragment : Fragment() {


    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap()?.setCamera(CameraOptions.Builder().center(it).zoom(14.0).build())
        mapView.gestures?.focalPoint = mapView.getMapboxMap()?.pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private lateinit var mapView: MapView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        mapView = view.findViewById(R.id.map_view)
        locationPermissionHelper = LocationPermissionHelper(WeakReference(requireActivity()))
        locationPermissionHelper.checkPermissions {
            mapView.getMapboxMap().loadStyleUri(
                Style.MAPBOX_STREETS,
                object : Style.OnStyleLoaded {
                    override fun onStyleLoaded(style: Style) {
                        onMapReady()
                        addAnnotationsToMap()
                    }
                }
            )
        }

        return view
    }

    private fun onMapReady() {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(15.0  )
                .build()
        )
        mapView.getMapboxMap()?.loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()
        }
    }
    private fun setupGesturesListener() {
        mapView.gestures?.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin?.updateSettings {
            enabled = true
            pulsingEnabled = true
        }
        if (locationComponentPlugin != null) {
            locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        }
        if (locationComponentPlugin != null) {
            locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        }
    }


    private fun onCameraTrackingDismissed() {
        //Toast.makeText(requireContext(), "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView.location?.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location?.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures?.removeOnMoveListener(onMoveListener)
    }

    private fun addAnnotationsToMap() {
        val TAG = "Food Bank"
        // Create an instance of the Annotation API and get the PointAnnotationManager.
        bitmapFromDrawableRes(
            requireContext(),
            R.drawable.baseline_red_marker_24
        )?.let {
            val annotationApi = mapView.annotations
            val pointAnnotationManager = annotationApi?.createPointAnnotationManager(mapView!!)

            // Reference the "banks" collection in Firestore and get all documents.
            val db = FirebaseFirestore.getInstance()
            db.collection("banks")
                .get()
                .addOnSuccessListener { documents ->
                    val coordinates = mutableListOf<Point>()
                    // Loop through each document and retrieve the "location" field, which is a GeoPoint object.
                    for (document in documents) {
                        val location = document.getGeoPoint("location")
                        // Convert the GeoPoint to a Point object and add to the list.
                        location?.let {
                            val point = Point.fromLngLat(location.longitude, location.latitude)
                            coordinates.add(point)
                        }
                    }
                    // Loop through each coordinate in the list and create a PointAnnotationOptions object for each.
                    for (coordinate in coordinates) {
                        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                            .withPoint(coordinate)
                            .withIconImage(it)
                        // Add the resulting pointAnnotation to the map.
                        pointAnnotationManager?.create(pointAnnotationOptions)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }
        }
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            // copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
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

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }
}
