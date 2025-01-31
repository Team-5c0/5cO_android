package com.example.mywiselaundrylife.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StartActVM : ViewModel() {
    private val _errorTxt = MutableStateFlow("")
    val errorTxt = _errorTxt.asStateFlow()

    fun setErrorTxt(txt : String){
        _errorTxt.value = txt
    }
}