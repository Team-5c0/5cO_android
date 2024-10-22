package com.example.mywiselaundrylife.serve

import com.example.mywiselaundrylife.data.base.Laundry

interface OnItemClickListener {
    fun onItemClicked(selectLaundry : Laundry)
}