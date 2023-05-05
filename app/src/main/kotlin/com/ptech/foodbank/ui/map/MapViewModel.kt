package com.ptech.foodbank.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.MetadataChanges
import com.mapbox.geojson.Point
import com.ptech.foodbank.db.FirestoreFactory

class MapViewModel : ViewModel() {
    private val db = FirestoreFactory()

    private val bankLocations: MutableLiveData<List<Point>> = MutableLiveData()

    fun availableBanks(): LiveData<List<Point>> {
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
}
