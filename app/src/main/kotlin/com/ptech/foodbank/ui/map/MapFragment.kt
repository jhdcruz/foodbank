package com.ptech.foodbank.ui.map

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location2
import com.mapbox.search.discover.Discover
import com.mapbox.search.discover.DiscoverOptions
import com.mapbox.search.discover.DiscoverQuery
import com.mapbox.search.discover.DiscoverResult
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
import com.ptech.foodbank.R
import com.ptech.foodbank.databinding.FragmentMapBinding
import com.ptech.foodbank.utils.Mapbox.MARKERS_INSETS
import com.ptech.foodbank.utils.Mapbox.MARKERS_INSETS_OPEN_CARD
import com.ptech.foodbank.utils.Mapbox.bitmapFromDrawableRes
import com.ptech.foodbank.utils.Mapbox.lastKnownLocation
import com.ptech.foodbank.utils.Mapbox.toSearchPlace
import com.ptech.foodbank.utils.Mapbox.userDistanceTo
import kotlinx.coroutines.launch

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var context: Context
    private lateinit var discover: Discover
    private lateinit var locationEngine: LocationEngine

    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapMarkersManager: MapMarkersManager

    private lateinit var searchNearby: View
    private lateinit var searchPlaceView: SearchPlaceBottomSheetView

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        @Suppress("UNUSED_VARIABLE") // to be used when getting data from firebase
        val mapViewModel = ViewModelProvider(this)[MapViewModel::class.java]

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root
        context = binding.root.context

        discover = Discover.create(getString(R.string.mapbox_access_token))
        locationEngine = LocationEngineProvider.getBestLocationEngine(context)

        mapView = binding.mapView
        mapMarkersManager = MapMarkersManager(mapView)

        // initialize mapbox
        mapView.getMapboxMap().also { mapboxMap ->
            this.mapboxMap = mapboxMap

            mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
                mapView.location2.updateSettings {
                    enabled = true
                }

                // user location tracker
                mapView.gestures.addOnMoveListener(onMoveListener)
                mapView.location2.addOnIndicatorPositionChangedListener(
                    onIndicatorPositionChangedListener,
                )
                mapView.location2.addOnIndicatorBearingChangedListener(
                    onIndicatorBearingChangedListener,
                )
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Search for makers in current location radius
        searchNearby = binding.fabCurrentLocation
        searchNearby.setOnClickListener {
            locationEngine.lastKnownLocation(context) { location ->
                if (location == null) {
                    return@lastKnownLocation
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        val response = discover.search(
                            // TODO: Update query to search for food banks
                            //       from firebase
                            query = DiscoverQuery.Category.RESTAURANTS,
                            proximity = location,
                            options = DiscoverOptions(limit = 10),
                        )

                        response.onValue { results ->
                            mapMarkersManager.showResults(results)
                        }.onError { exception ->
                            Toast.makeText(
                                context,
                                "Something went wrong while searching.",
                                Toast.LENGTH_SHORT,
                            ).show()

                            throw RuntimeException(exception)
                        }
                    }
                }
            }
        }

        searchPlaceView = binding.searchPlaceView
        searchPlaceView.apply {
            initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))
            isFavoriteButtonVisible = false

            addOnCloseClickListener {
                mapMarkersManager.adjustMarkersForClosedCard()
                searchPlaceView.hide()
            }
        }

        mapMarkersManager.onResultClickListener = { result ->
            mapMarkersManager.adjustMarkersForOpenCard()
            searchPlaceView.open(result.toSearchPlace())
            locationEngine.userDistanceTo(context, result.coordinate) { distance ->
                distance?.let { searchPlaceView.updateDistance(distance) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        onCameraTrackingDismissed()
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

    private class MapMarkersManager(mapView: MapView) {
        private val annotations = mutableMapOf<Long, DiscoverResult>()
        private val mapboxMap: MapboxMap = mapView.getMapboxMap()
        private val pointAnnotationManager = mapView.annotations.createPointAnnotationManager(null)
        private val pinBitmap =
            mapView.context.bitmapFromDrawableRes(R.drawable.baseline_red_marker_24)

        var onResultClickListener: ((DiscoverResult) -> Unit)? = null

        init {
            pointAnnotationManager.addClickListener {
                annotations[it.id]?.let { result ->
                    onResultClickListener?.invoke(result)
                }
                true
            }
        }

        fun clearMarkers() {
            pointAnnotationManager.deleteAll()
            annotations.clear()
        }

        fun adjustMarkersForOpenCard() {
            val coordinates = annotations.values.map { it.coordinate }
            val cameraOptions = mapboxMap.cameraForCoordinates(
                coordinates,
                MARKERS_INSETS_OPEN_CARD,
                bearing = null,
                pitch = null,
            )
            mapboxMap.setCamera(cameraOptions)
        }

        fun adjustMarkersForClosedCard() {
            val coordinates = annotations.values.map { it.coordinate }
            val cameraOptions = mapboxMap.cameraForCoordinates(
                coordinates,
                MARKERS_INSETS,
                bearing = null,
                pitch = null,
            )
            mapboxMap.setCamera(cameraOptions)
        }

        fun showResults(results: List<DiscoverResult>) {
            clearMarkers()

            if (results.isEmpty()) {
                return
            }

            val coordinates = ArrayList<Point>(results.size)
            results.forEach { result ->
                val options = pinBitmap?.let {
                    PointAnnotationOptions()
                        .withPoint(result.coordinate)
                        .withIconImage(it)
                        .withIconAnchor(IconAnchor.BOTTOM)
                }

                val annotation = options?.let { pointAnnotationManager.create(it) }
                annotations[annotation!!.id] = result

                coordinates.add(result.coordinate)
            }

            val cameraOptions = mapboxMap.cameraForCoordinates(
                coordinates,
                MARKERS_INSETS,
                bearing = null,
                pitch = null,
            )
            mapboxMap.setCamera(cameraOptions)
        }
    }
}
