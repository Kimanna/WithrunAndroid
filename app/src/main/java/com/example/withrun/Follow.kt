package com.example.withrun

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.util.Log
import kotlinx.android.synthetic.main.activity_follow.*
import com.example.withrun.MyFirebaseMessagingService.EXTRA_FOLLOW

class Follow : AppCompatActivity() {


    val TAG : String = "Follow"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow)

        val pagerAdapter = FollowFragmentAdapter(supportFragmentManager)
        viewPager.adapter = pagerAdapter
        tab.setupWithViewPager(viewPager)

        val followNotiData = intent.getParcelableExtra<Follow_object>(EXTRA_FOLLOW)

        back_Home.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()

        val intent = Intent(this, Profile::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()

    }
}