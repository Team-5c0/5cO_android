package com.example.mywiselaundrylife.adapter

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.Duration

class TimerViewModel : ViewModel() {
    private var job : Job? = null
    private val _remainingDuration = MutableLiveData<Duration>()
    val remainDuration : LiveData<Duration> get() = _remainingDuration

    @RequiresApi(Build.VERSION_CODES.O)
    fun startTimer(endTime : LocalDateTime){
        stopTimer()
        job = viewModelScope.launch {
            while (true){
                val now = LocalDateTime.now()
                val duration = Duration.between(now, endTime)

                if(duration.isNegative || duration.isZero){
                    onTimerEnd()
                    break
                }
                _remainingDuration.postValue(duration)
                delay(1000)
            }
        }
    }

    fun stopTimer() {
        job?.cancel()
        job = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onTimerEnd() {
        _remainingDuration.postValue(Duration.ZERO)
    }

    override fun onCleared() {
        stopTimer()
        super.onCleared()
    }
}