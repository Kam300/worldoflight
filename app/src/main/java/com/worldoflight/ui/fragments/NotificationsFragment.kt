package com.worldoflight.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.worldoflight.databinding.FragmentNotificationsBinding
import com.worldoflight.ui.adapters.NotificationsAdapter
import com.worldoflight.ui.viewmodels.NotificationsViewModel

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationsViewModel: NotificationsViewModel
    private lateinit var notificationsAdapter: NotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationsViewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]

        setupRecyclerView()
        observeViewModel()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        notificationsAdapter = NotificationsAdapter { notification ->
            // Обработка клика по уведомлению
            notificationsViewModel.markAsRead(notification.id)
        }

        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationsAdapter
        }
    }

    private fun observeViewModel() {
        notificationsViewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            if (notifications.isEmpty()) {
                binding.rvNotifications.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.VISIBLE
            } else {
                binding.rvNotifications.visibility = View.VISIBLE
                binding.emptyStateLayout.visibility = View.GONE
                notificationsAdapter.submitList(notifications)
            }
        }

        notificationsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun loadNotifications() {
        notificationsViewModel.loadNotifications()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
