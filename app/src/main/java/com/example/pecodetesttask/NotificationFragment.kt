package com.example.pecodetesttask

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_ALL
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.activityViewModels
import com.example.pecodetesttask.databinding.FragmentNotificationBinding

private const val ARG_POSITION = "position"
private const val CHANNEL_ID = "pecode_test_task"

class NotificationFragment : Fragment() {

    private var position = 1

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ApplicationViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            position = it.getInt(ARG_POSITION)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.notificationNumber.text = position.toString()
        createNotificationChannel()

        setListeners()
        setObservers()
    }

    private fun setListeners() {
        binding.addFragment.setOnClickListener {
            viewModel.increaseFragmentsCount()

            with(requireActivity() as MainActivity) {
                this.notifyPagerDataChanged()
                this.goToLastFragment()
            }
        }

        binding.removeLastFragment.setOnClickListener {
            (requireActivity() as MainActivity).removeLastFragmentAndDecreaseFragmentsCount()
        }

        binding.createNotification.setOnClickListener {

            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                putExtra(MainActivity.ARG_FRAGMENT_COUNT, position)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent: PendingIntent = PendingIntent.getActivity(requireContext(), position, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val notificationBuilder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_avatar_icon))
                .setContentTitle(getString(R.string.notification_title))
                .setDefaults(DEFAULT_ALL)
                .setContentText(getString(R.string.notification_content, position))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(requireContext())) {
                notify(position, notificationBuilder.build())
            }
        }
    }

    private fun setObservers() {
        viewModel.fragmentsCount.observe(viewLifecycleOwner) {
            binding.removeLastFragment.visibility = if (it > 1) {
                VISIBLE
            } else {
                INVISIBLE
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        fun newInstance(position: Int) =
            NotificationFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_POSITION, position)
                }
            }
    }
}