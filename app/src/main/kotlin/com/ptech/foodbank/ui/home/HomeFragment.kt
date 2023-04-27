package com.ptech.foodbank.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ptech.foodbank.data.Bank
import com.ptech.foodbank.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var banksRecyclerView: RecyclerView? = null
    private var banksList: List<Bank>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root

        banksRecyclerView = binding.bankList
        banksRecyclerView?.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        // get and observe banks list
        homeViewModel.savedBanks.observe(viewLifecycleOwner) {
            if (it != null) {
                banksList = it.toObjects(Bank::class.java)
                banksRecyclerView?.adapter = BankRecyclerAdapter(banksList as MutableList<Bank>)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
