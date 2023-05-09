package com.ptech.foodbank.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
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
import com.ptech.foodbank.ui.home.BankUtils.getAddress
import com.ptech.foodbank.ui.home.BankUtils.getBankActionCall
import com.ptech.foodbank.ui.home.BankUtils.getBankActionEmail
import com.ptech.foodbank.ui.home.BankUtils.getBankActionWeb
import com.ptech.foodbank.ui.home.BankUtils.getBankCapacity
import com.ptech.foodbank.ui.home.BankUtils.getBankImage
import com.ptech.foodbank.utils.Auth.authUi
import com.ptech.foodbank.utils.Auth.getAuth
import com.ptech.foodbank.utils.Auth.loginProviders
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
    private lateinit var fabCurrentLocation: ExtendedFloatingActionButton
    private lateinit var fabMapStyle: FloatingActionButton

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) {
        this.onSignInResult(it)
    }

    private val signInIntent = authUi
        .createSignInIntentBuilder()
        .setAvailableProviders(loginProviders)
        .build()

    private var currentUser = getAuth.currentUser

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
            MaterialAlertDialogBuilder(viewContext)
                .setCancelable(false)
                // set icon and adapt on theme change
                .setTitle("Location is currently disabled")
                .setMessage("Please enable location to use this feature")
                .setPositiveButton("Enable") { _, _ ->
                    // redirect to device location setting
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("Ignore") { _, _ ->
                    // do nothing
                }
                .show()
        }

        fabCurrentLocation = binding.fabCurrentLocation
        fabMapStyle = binding.fabMapStyle

        mapView = binding.mapView
        mapBox = Mapbox(mapView)
        viewAnnotationManager = mapView.viewAnnotationManager
        pointAnnotationManager = mapView.annotations.createPointAnnotationManager()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var isTracking = true
        var isSatellite = false

        // load mapbox
        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS,
        ) {
            mapBox.setupGesturesListener()
            mapBox.initLocationComponent()

            addAnnotationsToMap()
            pointAnnotationManager.addClickListener {
                val geoPoint = GeoPoint(it.point.latitude(), it.point.longitude())
                showBankDialog(geoPoint)

                true
            }
        }

        fabCurrentLocation.setOnClickListener {
            if (!viewContext.isLocationEnabled()) {
                viewContext.showToast("Location is currently disabled")
            } else {
                isTracking = if (isTracking) {
                    mapBox.onCameraTrackingDismissed()
                    mapView.location2.updateSettings {
                        enabled = false
                    }

                    fabCurrentLocation.setIconResource(R.drawable.baseline_location_searching_24)
                    viewContext.showToast("User tracking disabled")
                    false
                } else {
                    mapBox.setupGesturesListener()
                    mapBox.initLocationComponent()

                    viewContext.showToast("User tracking enabled")
                    fabCurrentLocation.setIconResource(R.drawable.baseline_location_24)
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
            R.drawable.baseline_red_marker_32,
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

    /** Show bank dialog when clicking markers */
    @SuppressLint("SetTextI18n")
    private fun showBankDialog(geopoint: GeoPoint) {
        // reuse bank component card used in home tab
        val card = LayoutInflater.from(viewContext).inflate(R.layout.component_bank_card, null)

        val dialog = MaterialAlertDialogBuilder(viewContext)
            .setView(card)
            .create()

        val cardImage = card.findViewById<ImageView>(R.id.bank_image)
        val cardName = card.findViewById<TextView>(R.id.bank_name)
        val cardBio = card.findViewById<TextView>(R.id.bank_bio)
        val cardCapacity = card.findViewById<LinearProgressIndicator>(R.id.bank_capacity)
        val cardAddress = card.findViewById<TextView>(R.id.bank_address)
        val cardOffer = card.findViewById<MaterialButton>(R.id.bank_action_offer)
        val cardAction = card.findViewById<MaterialButton>(R.id.bank_action)
        val closeButton = card.findViewById<MaterialButton>(R.id.close_button)

        mapViewModel.getBankOnLocation(
            requireContext(),
            geopoint,
        ).observe(viewLifecycleOwner) { bank ->
            cardName.text = bank.name
            cardBio.text = bank.bio

            viewContext.getAddress(cardAddress, bank.location)
            viewContext.getBankCapacity(cardCapacity, bank.capacity)
            viewContext.getBankImage(bank.image, cardImage)

            cardAction.setOnClickListener {
                val layout =
                    LayoutInflater.from(viewContext)
                        .inflate(R.layout.component_inquire_dialog, null)
                val inquireDialog = MaterialAlertDialogBuilder(viewContext).setView(layout).create()

                val callButton = layout.findViewById<MaterialButton>(R.id.bank_action_call)
                val emailButton = layout.findViewById<MaterialButton>(R.id.bank_action_email)
                val web = layout.findViewById<MaterialButton>(R.id.bank_action_web)

                viewContext.getBankActionCall(callButton, bank.contacts["phone"]!!)
                viewContext.getBankActionEmail(emailButton, bank.contacts["email"]!!)
                viewContext.getBankActionWeb(web, bank.contacts["website"]!!)

                inquireDialog.show()
            }

            cardOffer.setOnClickListener {
                if (currentUser == null) {
                    viewContext.showToast("Please login to donate")
                    // launch sign in flow
                    signInLauncher.launch(signInIntent)
                } else {
                    val route = MapFragmentDirections.mapToDonateFragment(bank.name)
                    view?.findNavController()?.navigate(route)

                    dialog.dismiss()
                }
            }

            closeButton.visibility = View.VISIBLE
            closeButton.setOnClickListener { dialog.dismiss() }
            dialog.show()
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            currentUser = getAuth.currentUser
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _context = null

        mapBox.onCameraTrackingDismissed()
    }
}
