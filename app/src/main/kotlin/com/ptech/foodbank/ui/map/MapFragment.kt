package com.ptech.foodbank.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.ptech.foodbank.R
import com.ptech.foodbank.databinding.FragmentMapBinding
import com.ptech.foodbank.utils.Mapbox
import com.ptech.foodbank.utils.Mapbox.Utils.bitmapFromDrawableRes


class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapBox: Mapbox
    private lateinit var mapView: MapView
    private lateinit var mapViewModel: MapViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mapViewModel = ViewModelProvider(this)[MapViewModel::class.java]

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val view = binding.root

        mapView = binding.mapView
        mapBox = Mapbox(mapView)

        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS,
        ) {
            mapBox.initLocationComponent()
            mapBox.setupGesturesListener()

            addAnnotationsToMap()
        }

        return view
    }

    private fun addAnnotationsToMap() {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        mapBox.onCameraTrackingDismissed()
    }
}
