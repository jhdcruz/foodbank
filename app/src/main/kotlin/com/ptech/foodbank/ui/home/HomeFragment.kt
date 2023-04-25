package com.ptech.foodbank.ui.home

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.PuckBearingSource
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location2
import com.ptech.foodbank.R
import com.ptech.foodbank.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView

    // Get the user's location as coordinates
    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .bearing(it)
                .build(),
        )
    }
    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .center(it)
                .build(),
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

    @Suppress("MagicNumber")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val root: View = binding.root
        val animInflater = TransitionInflater.from(requireContext())

        exitTransition = animInflater.inflateTransition(R.transition.fade)
        enterTransition = animInflater.inflateTransition(R.transition.fade)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // setup mapbox maps
        mapView = binding.mapView
        // setup mapbox map
        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS,
        ) {
            mapView.location2.updateSettings {
                enabled = true
                pulsingEnabled = true
            }
            mapView.gestures.addOnMoveListener(onMoveListener)
        }

        // request permission
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (!isGranted) {
                val text = "We couldn't get your location"
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            } else {
                // get and mark current location
                mapView.location2.puckBearingSource = PuckBearingSource.HEADING
                mapView.location2.puckBearingSource = PuckBearingSource.COURSE
            }
        }

        // Pass the user's location to camera
        mapView.location2.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location2.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)

        return root
    }

    private fun onCameraTrackingDismissed() {
        mapView.location2.removeOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener,
        )
        mapView.location2.removeOnIndicatorBearingChangedListener(
            onIndicatorBearingChangedListener,
        )
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        mapView.location2.removeOnIndicatorBearingChangedListener(
            onIndicatorBearingChangedListener,
        )
        mapView.location2.removeOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener,
        )
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }
}
