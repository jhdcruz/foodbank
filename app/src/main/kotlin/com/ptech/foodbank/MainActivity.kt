package com.ptech.foodbank

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ptech.foodbank.databinding.ActivityMainBinding
import com.ptech.foodbank.utils.Permissions.getPermissions
import com.ptech.foodbank.utils.Permissions.isPermissionGranted

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navHostFragment: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment

        val navController: NavController = navHostFragment.navController
        navView.setupWithNavController(navController)

        // check and request for required location permission
        if (!isPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            getPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }

        // check and request for notification permission for mapbox navigation
        // for Android 33 (Tiramisu) and above
        // See: https://docs.mapbox.com/android/navigation/guides/get-started/install/
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        if (!isPermissionGranted(this, Manifest.permission.POST_NOTIFICATIONS)) {
            getPermissions(
                this,
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                ),
            )
        }
    }
}
