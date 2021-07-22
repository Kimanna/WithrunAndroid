package com.example.withrun

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.util.Log

class FollowFragmentAdapter (fm : FragmentManager): FragmentStatePagerAdapter(fm) {

    val TAG : String = "FollowFragmentAdapter"


    override fun getItem(position: Int): Fragment {
        Log.d(TAG, "FollowFragmentAdapter getItem 지난후 " )

        val fragment = when(position) {
            0->FollowerFragment().newInstant()
            1->FollowingFragment().newInstant()
            else -> FollowerFragment().newInstant()
        }
        return fragment
    }

    override fun getCount(): Int = 2

    override fun getPageTitle(position: Int): CharSequence? {
        Log.d(TAG, "FollowFragmentAdapter getPageTitle 지난후 " )

        val title = when(position) {
            0->"팔로워"
            1->"팔로잉"
            else -> "main"
        }
        return title
    }
}