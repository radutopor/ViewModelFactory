package com.radutopor.viewmodelfactory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radutopor.viewmodelfactory.annotations.Provided
import com.radutopor.viewmodelfactory.annotations.ViewModelFactory

@ViewModelFactory
class SampleViewModel(@Provided private val appName: String, private val userName: String) : ViewModel() {
    private val greeting = MutableLiveData<String>()

    init {
        greeting.value = "Hi $userName, this is $appName"
    }

    fun getGreeting() = greeting as LiveData<String>
}