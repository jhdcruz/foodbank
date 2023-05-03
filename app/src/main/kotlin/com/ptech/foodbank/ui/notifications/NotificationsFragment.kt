package com.ptech.foodbank.ui.notifications

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
import com.ptech.foodbank.data.Notifications
import com.ptech.foodbank.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: NotificationsViewModel

    private lateinit var refresher: SwipeRefreshLayout
    private lateinit var loader: CircularProgressIndicator

    private var notifRecyclerView: RecyclerView? = null
    private var notifList: List<Notifications>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        homeViewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        val view = binding.root

        notifRecyclerView = binding.notifList
        notifRecyclerView?.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)

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
        getData()
    }

    private fun getData() {
        homeViewModel.notifications.observe(viewLifecycleOwner) {
            if (it != null) {
                notifList = it.toObjects(Notifications::class.java)
                notifRecyclerView?.adapter = NotifRecyclerAdapter(notifList as List<Notifications>)

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
