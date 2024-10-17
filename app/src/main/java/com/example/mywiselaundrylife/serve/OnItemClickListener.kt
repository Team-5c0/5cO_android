package com.example.mywiselaundrylife.serve

import com.example.mywiselaundrylife.data.Laundry

interface OnItemClickListener {
    fun onItemClicked(selectLaundry : Laundry)
}