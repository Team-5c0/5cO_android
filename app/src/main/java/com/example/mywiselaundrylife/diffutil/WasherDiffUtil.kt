package com.example.mywiselaundrylife.diffutil

import androidx.recyclerview.widget.DiffUtil
import com.example.mywiselaundrylife.data.response.Laundry

class WasherDiffUtil(
    private val oldLst : ArrayList<Laundry>,
    private val newLst : ArrayList<Laundry>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldLst.size
    }

    override fun getNewListSize(): Int {
        return newLst.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldUser = oldLst[oldItemPosition].user
        val newUser = newLst[newItemPosition].user

        // 두 개의 user가 모두 null인 경우
        if (oldUser == null && newUser == null) return true

        // 한 쪽만 null인 경우
        if (oldUser == null || newUser == null) return false

        // 두 user가 모두 null이 아닌 경우
        return oldUser == newUser
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldLst[oldItemPosition]
        val newItem = newLst[newItemPosition]
        return oldItem == newItem
    }
}