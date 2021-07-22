package com.example.withrun

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.util.Log

class RoomFragmentAdapter (fm : FragmentManager): FragmentStatePagerAdapter(fm) {

    val TAG : String = "RoomFragmentAdapter"


    override fun getItem(position: Int): Fragment {
        Log.d(TAG, "RoomFragmentAdapter getItem 지난후 " )

        val fragment = when(position) {
            0->RoomInformationFragment().newInstant()
            1->RoomChatFragment().newInstant()
            else -> RoomInformationFragment().newInstant()
        }
        return fragment
    }

    override fun getCount(): Int = 2

    override fun getPageTitle(position: Int): CharSequence? {
        Log.d(TAG, "RoomFragmentAdapter getPageTitle 지난후 " )

        val title = when(position) {
            0->"정보"
            1->"채팅"
            else -> "main"
        }
        return title
    }
}