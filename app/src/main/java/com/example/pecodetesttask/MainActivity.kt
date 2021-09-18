package com.example.pecodetesttask

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.pecodetesttask.databinding.ActivityMainBinding
import kotlin.math.log

class MainActivity : FragmentActivity() {

    companion object {
        const val ARG_FRAGMENT_COUNT = "fragment_count"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager: ViewPager2

    private val viewModel: ApplicationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = binding.viewPager
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return viewModel.fragmentsCount.value ?: 1
            }

            override fun createFragment(position: Int): Fragment {
                return NotificationFragment.newInstance(position + 1)
            }
        }

        setFragmentsCountOnApplicationLaunch()
    }

    override fun onStop() {
        super.onStop()
        saveCurrentState()
    }

    private fun saveCurrentState() {
        with(getPreferences(MODE_PRIVATE).edit()) {
            putInt(ARG_FRAGMENT_COUNT, viewModel.getFragmentsCount())
            apply()
        }
    }

    private fun setFragmentsCountOnApplicationLaunch() {

        var fragmentsCount = intent.getIntExtra(ARG_FRAGMENT_COUNT, 0)

        if (fragmentsCount == 0) {
            fragmentsCount = getPreferences(MODE_PRIVATE).getInt(ARG_FRAGMENT_COUNT, 1)
        }

        if (fragmentsCount > 1) {
            viewModel.fragmentsCount.value = fragmentsCount
            notifyPagerDataChanged()
            viewPager.setCurrentItem(fragmentsCount, false)
        }
    }

    fun goToLastFragment() {
        viewPager.setCurrentItem(getLastFragmentIndex(), true)
    }

    fun removeLastFragmentAndDecreaseFragmentsCount() {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(viewModel.getFragmentsCount())
        if (viewPager.currentItem == getLastFragmentIndex()) {
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrollStateChanged(state: Int) {
                    if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        viewPager.unregisterOnPageChangeCallback(this)
                        viewModel.decreaseFragmentsCount()
                        viewPager.post {
                            notifyPagerDataChanged()
                        }
                    }
                }
            })
            viewPager.setCurrentItem(getLastFragmentIndex() - 1, true)
        } else {
            viewModel.decreaseFragmentsCount()
            notifyPagerDataChanged()
        }
    }

    fun notifyPagerDataChanged() {
        viewPager.adapter?.notifyDataSetChanged()
    }

    private fun getLastFragmentIndex() = viewModel.getFragmentsCount() - 1
}