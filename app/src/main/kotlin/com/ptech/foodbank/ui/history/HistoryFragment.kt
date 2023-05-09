package com.ptech.foodbank.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.ptech.foodbank.databinding.FragmentHistoryBinding
import com.ptech.foodbank.utils.Auth.getAuth
import com.ptech.foodbank.utils.Feedback.showToast

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null

    private val binding get() = _binding!!
    private lateinit var notificationsViewModel: HistoryViewModel

    private lateinit var refresher: SwipeRefreshLayout
    private lateinit var loader: CircularProgressIndicator

    private var donationsRecyclerView: RecyclerView? = null

    private val currentUser = getAuth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        notificationsViewModel = ViewModelProvider(this)[HistoryViewModel::class.java]

        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root = binding.root

        donationsRecyclerView = binding.donationList
        donationsRecyclerView?.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        // pull-to-refresh feature
        refresher = binding.swipeRefresh
        refresher.setOnRefreshListener {
            if (currentUser == null) {
                requireContext().showToast("Login required")
                refresher.isRefreshing = false
            } else {
                getData()
            }
        }

        // Show loading progress indicator
        loader = binding.progressIndicator
        loader.show()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (currentUser == null) {
            loader.hide()

            requireContext().showToast("Login required")
        } else {
            getData()
        }
    }

    private fun getData() {
        notificationsViewModel.getDonations().observe(viewLifecycleOwner) {
            if (it != null) {
                donationsRecyclerView?.adapter = DonationRecyclerAdapter(it)

                loader.hide()
                refresher.isRefreshing = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
