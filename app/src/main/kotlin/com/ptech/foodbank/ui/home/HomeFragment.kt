package com.ptech.foodbank.ui.home

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.search.SearchBar
import com.ptech.foodbank.R
import com.ptech.foodbank.databinding.FragmentHomeBinding
import com.ptech.foodbank.utils.Auth.authUi
import com.ptech.foodbank.utils.Auth.getAuth
import com.ptech.foodbank.utils.Auth.loginProviders
import com.ptech.foodbank.utils.Coil

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var refresher: SwipeRefreshLayout
    private lateinit var loader: CircularProgressIndicator
    private lateinit var searchBar: SearchBar
    private lateinit var avatar: MenuItem

    private var banksRecyclerView: RecyclerView? = null

    private var currentUser = getAuth.currentUser

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) {
        this.onSignInResult(it)
    }

    private val signInIntent = authUi
        .createSignInIntentBuilder()
        .setAvailableProviders(loginProviders)
        .build()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val view = binding.root

        banksRecyclerView = binding.bankList
        banksRecyclerView?.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        // search bar
        searchBar = binding.searchBar
        searchBar.inflateMenu(R.menu.search_menu)
        avatar = searchBar.menu.findItem(R.id.user)

        // pull-to-refresh feature
        refresher = binding.swipeRefresh
        refresher.setOnRefreshListener {
            getData()
        }

        // Show loading progress indicator
        loader = binding.progressIndicator
        loader.show()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.user -> {
                    if (currentUser == null) {
                        // start login instance
                        signInLauncher.launch(signInIntent)
                    } else {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Sign out?")
                            .setMessage("Are you sure you want to sign out?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Sign out") { _, _ ->
                                // proceed to sign out
                                authUi.signOut(requireContext())
                                    .addOnCompleteListener {
                                        currentUser = null

                                        // set icon to default
                                        avatar.icon = ResourcesCompat.getDrawable(
                                            resources,
                                            R.drawable.baseline_account_circle_48,
                                            null
                                        )
                                    }
                            }
                            .show()
                    }
                    true
                }

                else -> false
            }
        }

        loadAvatar()
        getData()
    }

    private fun getData() {
        // get banks list
        homeViewModel.banks().observe(viewLifecycleOwner) {
            if (it != null) {
                banksRecyclerView?.adapter = BankRecyclerAdapter(it)

                loader.hide()
                refresher.isRefreshing = false
            }
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            currentUser = getAuth.currentUser

            loadAvatar()
        }
    }

    private fun loadAvatar() {
        val loader = Coil.imageLoader(requireContext())

        val request =
            ImageRequest.Builder(requireContext())
                .data(currentUser?.photoUrl)
                .target(
                    onSuccess = { result ->
                        // clear tint to prevent image being covered
                        avatar.iconTintList = null
                        avatar.icon = result
                    },
                )
                .crossfade(true)
                .transformations(CircleCropTransformation())
                .build()

        loader.enqueue(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
