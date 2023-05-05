package com.ptech.foodbank.ui.home

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.search.SearchBar
import com.ptech.foodbank.R
import com.ptech.foodbank.data.Bank
import com.ptech.foodbank.databinding.FragmentHomeBinding
import com.ptech.foodbank.utils.Auth.getAuth

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var refresher: SwipeRefreshLayout
    private lateinit var loader: CircularProgressIndicator
    private lateinit var searchBar: SearchBar

    private var banksRecyclerView: RecyclerView? = null
    private var banksList: List<Bank>? = null

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) {
        this.onSignInResult(it)
    }

    private val signInIntent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .setTheme(R.style.Theme_FoodBank)
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
                    signInLauncher.launch(signInIntent)
                    true
                }

                else -> false
            }
        }

        getData()
    }

    private fun getData() {
        // get banks list
        homeViewModel.savedBanks.observe(viewLifecycleOwner) {
            if (it != null) {
                banksList = it.toObjects(Bank::class.java)
                banksRecyclerView?.adapter = BankRecyclerAdapter(banksList as List<Bank>)

                loader.hide()
                refresher.isRefreshing = false
            }
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse

        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = getAuth.currentUser
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
    }
}
