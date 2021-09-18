package com.example.pecodetesttask

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ApplicationViewModel : ViewModel() {

    val fragmentsCount = MutableLiveData(1)

    fun increaseFragmentsCount() {
        fragmentsCount.value?.let {
            fragmentsCount.value = it + 1
        }
    }

    fun decreaseFragmentsCount() {
        fragmentsCount.value?.let {
            fragmentsCount.value = it - 1
        }
    }

    fun getFragmentsCount(): Int = fragmentsCount.value ?: 1

}