package com.ptech.foodbank.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location2
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.ptech.foodbank.R
import com.ptech.foodbank.databinding.FragmentMapBinding
import com.ptech.foodbank.utils.Feedback.showToast
import com.ptech.foodbank.utils.Mapbox
import com.ptech.foodbank.utils.Mapbox.Utils.bitmapFromDrawableRes
import com.ptech.foodbank.utils.Mapbox.Utils.isLocationEnabled
import com.ptech.foodbank.utils.Permissions

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private var _context: Context? = null

    private val binding get() = _binding!!
    private val viewContext get() = _context!!

    private lateinit var mapBox: Mapbox
    private lateinit var mapView: MapView
    private lateinit var mapViewModel: MapViewModel
    private lateinit var pointAnnotation: PointAnnotation
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var viewAnnotationManager: ViewAnnotationManager
    private lateinit var fabCurrentLocation: FloatingActionButton
    private lateinit var fabMapStyle: FloatingActionButton

    override fun onAttach(context: Context) {
        super.onAttach(context)

        // check and request for required location permission
        if (!Permissions.isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Permissions.getPermissions(
                context,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mapViewModel = ViewModelProvider(this)[MapViewModel::class.java]
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        _context = requireContext()

        val view = binding.root

        // Check if location is enabled
        if (!viewContext.isLocationEnabled()) {
            Snackbar.make(
                binding.root,
                "Location is currently disabled",
                Snackbar.LENGTH_LONG,
            )
                // give option to enable it
                .setAction("ENABLE") {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .show()
        }

        fabCurrentLocation = binding.fabCurrentLocation
        fabMapStyle = binding.fabMapStyle

        mapView = binding.mapView
        mapBox = Mapbox(mapView)
        viewAnnotationManager = mapView.viewAnnotationManager
        pointAnnotationManager = mapView.annotations.createPointAnnotationManager()

        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS,
        ) {
            mapBox.setupGesturesListener()
            mapBox.initLocationComponent()

            addAnnotationsToMap()
            pointAnnotationManager.addClickListener {
                val geoPoint = GeoPoint(it.point.latitude(), it.point.longitude())
                showBankDialog(_context!!, geoPoint)

                true
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var isTracking = true
        var isSatellite = false

        // allow toggling user tracking
        if (!viewContext.isLocationEnabled()) {
            fabCurrentLocation.setOnClickListener {
                viewContext.showToast("Location is currently disabled")
            }
        } else {
            fabCurrentLocation.setOnClickListener {
                isTracking = if (isTracking) {
                    mapBox.onCameraTrackingDismissed()
                    mapView.location2.updateSettings {
                        enabled = false
                    }

                    viewContext.showToast("User tracking disabled")
                    fabCurrentLocation.setImageResource(R.drawable.baseline_location_searching_24)
                    false
                } else {
                    mapBox.setupGesturesListener()
                    mapBox.initLocationComponent()

                    viewContext.showToast("User tracking enabled")
                    fabCurrentLocation.setImageResource(R.drawable.baseline_location_24)
                    true
                }
            }
        }

        // change map styles to satellite and back
        fabMapStyle.setOnClickListener {
            isSatellite = if (isSatellite) {
                mapView.getMapboxMap().loadStyleUri(
                    Style.MAPBOX_STREETS,
                ) {
                    viewContext.showToast("Map style changed to streets")
                }
                false
            } else {
                mapView.getMapboxMap().loadStyleUri(
                    Style.SATELLITE_STREETS,
                ) {
                    viewContext.showToast("Map style changed to satellite")
                }
                true
            }
        }
    }


    /** Add annotations (markers) to the map using firestore data */
    private fun addAnnotationsToMap() {
        val pointAnnotationOptions = PointAnnotationOptions()

        // get marker bitmap from drawables
        val pinMarker = bitmapFromDrawableRes(
            viewContext,
            R.drawable.baseline_red_marker_24,
        )

        // get locations from firebase
        mapViewModel.availableBanks().observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                for (coordinate in it) {
                    pointAnnotationOptions
                        .withPoint(coordinate)
                        .withIconImage(pinMarker!!)
                    pointAnnotation = pointAnnotationManager.create(pointAnnotationOptions)
                }
            }
        }
    }
    fun showBankDialog(context: Context, geopoint: GeoPoint) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.bank_info_dialog, null)

        val titleTextView = dialogView.findViewById<TextView>(R.id.titleTextView)
        val coordsTextView = dialogView.findViewById<TextView>(R.id.coordsTextView)
        val emailTextView = dialogView.findViewById<TextView>(R.id.emailTextView)
        val phoneTextView = dialogView.findViewById<TextView>(R.id.phoneTextView)
        val websiteTextView = dialogView.findViewById<TextView>(R.id.websiteTextView)
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)

        val db = FirebaseFirestore.getInstance()
        db.collection("banks")
            .whereEqualTo("location", geopoint)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    val bank = documents.first()
                    titleTextView.text = bank.getString("name")
                    val geoPoint = bank.getGeoPoint("location")
                    val latitude = geoPoint?.latitude.toString()
                    val longitude = geoPoint?.longitude.toString()
                    coordsTextView.text = "$latitude, $longitude"
                    emailTextView.text = bank.get("contacts.email") as String
                    phoneTextView.text = bank.get("contacts.phone") as String
                    websiteTextView.text = bank.get("contacts.website") as String

                    val dialogBuilder = AlertDialog.Builder(context)
                        .setView(dialogView)
                    val dialog = dialogBuilder.create()
                    closeButton.setOnClickListener { dialog.dismiss() }
                    dialog.show()
                } else {
                    Toast.makeText(context, "No bank found at this location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error retrieving bank data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _context = null

        mapBox.onCameraTrackingDismissed()
    }
}
