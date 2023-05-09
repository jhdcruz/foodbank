package com.ptech.foodbank.ui.map

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.MetadataChanges
import com.mapbox.geojson.Point
import com.ptech.foodbank.data.Bank
import com.ptech.foodbank.db.FirebaseFactoryImpl
import com.ptech.foodbank.utils.Crashlytics.reporter
import com.ptech.foodbank.utils.Feedback.showToast

class MapViewModel : ViewModel() {
    private val db = FirebaseFactoryImpl()

    fun availableBanks(): LiveData<List<Point>> {
        val bankLocations: MutableLiveData<List<Point>> = MutableLiveData()

        db.getBanks().addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            for (doc in snapshot!!) {
                val locations = doc.getGeoPoint("location") as GeoPoint
                val point = Point.fromLngLat(locations.longitude, locations.latitude)

                bankLocations.value = listOf(point)
            }
        }

        return bankLocations
    }

    fun getBankOnLocation(context: Context, location: GeoPoint): LiveData<Bank> {
        val bankDetails = MutableLiveData<Bank>()

        db.getBanks()
            .whereEqualTo("location", location)
            .get()
            .addOnSuccessListener {
                if (it.documents.isNotEmpty()) {
                    bankDetails.value = it.documents[0].toObject(Bank::class.java)
                } else {
                    context.showToast("No banks found at this location.")
                }
            }
            .addOnFailureListener { e ->
                reporter.recordException(e)
            }

        return bankDetails
    }
}
