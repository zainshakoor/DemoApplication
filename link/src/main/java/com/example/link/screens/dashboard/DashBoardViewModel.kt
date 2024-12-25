package com.example.link.screens.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashBoardViewModel : ViewModel() {


    private val _greetings: MutableLiveData<String> = MutableLiveData()
    val greetings: LiveData<String> = _greetings


    fun UpdateGreetingMessage() {
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when (currentHour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"


        }
        _greetings.value = greeting
    }


}