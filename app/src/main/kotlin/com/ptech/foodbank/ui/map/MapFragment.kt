package com.ptech.foodbank.ui.map

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.ptech.foodbank.R
import com.ptech.foodbank.databinding.FragmentMapBinding
import com.ptech.foodbank.utils.Feedback.showToast
import com.ptech.foodbank.utils.Mapbox
import com.ptech.foodbank.utils.Mapbox.Utils.bitmapFromDrawableRes
import com.ptech.foodbank.utils.Mapbox.Utils.isLocationEnabled
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapBox: Mapbox
    private lateinit var mapView: MapView
    private lateinit var mapViewModel: MapViewModel

    private lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mapViewModel = ViewModelProvider(this)[MapViewModel::class.java]
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        val view = binding.root

        // Check if location is enabled
        // redirect to location settings on ACCEPT,
        // show toast of tracking unavailability on CANCEL
        if (!isLocationEnabled(requireContext())) {
            MaterialAlertDialogBuilder(requireContext())
                .setCancelable(false)
                .setIcon(R.drawable.baseline_location_24)
                .setTitle("Location disabled")
                .setMessage("Location is currently disabled, would you like to enable it?")
                .setPositiveButton("Enable") { _: DialogInterface, _: Int ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("Ignore") { _: DialogInterface, _: Int ->
                    showToast(requireContext(), "User tracking will not be available")
                }
                .create()
                .show()
        }

        fab = binding.fabCurrentLocation

        mapView = binding.mapView
        mapBox = Mapbox(mapView)

        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS,
        ) {
            mapBox.setupGesturesListener()
            mapBox.initLocationComponent()

            runBlocking {
                addAnnotationsToMap()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var isTracking = true

        // allow toggling user tracking
        fab.setOnClickListener {
            isTracking = if (isTracking) {
                mapBox.onCameraTrackingDismissed()

                Toast.makeText(
                    requireContext(),
                    "User tracking disabled",
                    Toast.LENGTH_SHORT,
                ).show()
                fab.setImageResource(R.drawable.baseline_location_24)

                false
            } else {
                mapBox.setupGesturesListener()
                mapBox.initLocationComponent()

                Toast.makeText(
                    requireContext(),
                    "User tracking enabled",
                    Toast.LENGTH_SHORT,
                ).show()
                fab.setImageResource(R.drawable.baseline_location_searching_24)

                true
            }
        }
    }

    /** Add annotations (markers) to the map using the data fetched from firestore asynchronously */
    private suspend fun addAnnotationsToMap() = coroutineScope {
        launch {
            val pointAnnotationManager = mapView.annotations.createPointAnnotationManager()
            val pointAnnotationOptions = PointAnnotationOptions()

            val pinMarker = bitmapFromDrawableRes(
                requireContext(),
                R.drawable.baseline_red_marker_24,
            )

            // get locations from firebase
            mapViewModel.availableBanks().observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    for (coordinate in it) {
                        pointAnnotationOptions
                            .withPoint(coordinate)
                            .withIconImage(pinMarker!!)

                        pointAnnotationManager.create(pointAnnotationOptions)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        mapBox.onCameraTrackingDismissed()
    }
}
