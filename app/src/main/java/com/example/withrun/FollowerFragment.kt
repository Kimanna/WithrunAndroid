package com.example.withrun

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.OrientationHelper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_room_detail.*
import kotlinx.android.synthetic.main.fragment_follower.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject

class FollowerFragment : Fragment() {

    val TAG: String = "FollowerFragment"

    val followRequestList = ArrayList<Follow_object>()
    val followList = ArrayList<Follow_object>()

    fun newInstant(): FollowerFragment {
        val args = Bundle()
        val frag = FollowerFragment()
        frag.arguments = args

        Log.d(TAG, "newInstant Follower 지난후 ")

        return frag
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d(TAG, "onCreateView Follower 지난후 ")
        coroutinegetFollowData()


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_follower, container, false)
    }

    fun recyclerviewUser() {
        Log.d(TAG, "recyclerviewUser ()")

        requestFollower.layoutManager = LinearLayoutManager(activity)
        requestFollower.adapter = activity?.let {
            FollowerRequestAdapter(it, followRequestList) {

                // 클릭한 user profile 확인할 수 있는 activity 로 전환
                goProfileIntroduce (it.getYouId()!!)

            }
        }

        followerList.layoutManager = LinearLayoutManager(activity)
        followerList.adapter = activity?.let {
            FollowerAdapter(it, followList) {

                goProfileIntroduce (it.getYouId()!!)
            }
        }

    }

    fun goProfileIntroduce (profileUserId: Int) {

        val intent = Intent (getActivity(), ProfileIntroduce::class.java)
        intent.putExtra("profileUserId", profileUserId)
        intent.putExtra("location", "FollowerFragment")
        getActivity()?.startActivity(intent)

    }


    fun coroutinegetFollowData() {
        Log.d(TAG, "getFollowData ()")

        CoroutineScope(Dispatchers.Main).launch {
            this
            val follows = CoroutineScope(Dispatchers.Default).async {
                this
                getFollows()
            }.await()
            Log.d(TAG, "room 정보 있음" + follows)

            val jsonObject = JSONObject(follows)

            val jsonArray = jsonObject.getJSONArray("result")

            for (i in 0..jsonArray.length() - 1) {

                var follows = Follow_object()
                follows.setYouId(jsonArray.getJSONObject(i).getInt("youId"))
                follows.setYouImg(jsonArray.getJSONObject(i).getString("youImg"))
                follows.setYouNickname(jsonArray.getJSONObject(i).getString("youNickname"))
                follows.setAcceptStatus(jsonArray.getJSONObject(i).getInt("accept_status"))
                follows.setRequestAt(jsonArray.getJSONObject(i).getString("request_at"))

                if ( follows.getAcceptStatus() == 1 ) { // follow 수락된 user
                    followList.add(follows)
                } else {                                // 아직 follow 수락되지 않은 유저
                    followRequestList.add(follows)
                }
            }
            Log.d(TAG, "request 리스트 싸이즈 " + followRequestList.size)
            Log.d(TAG, "follower 리스트 싸이즈 " + followList.size)

            // 리싸이클러뷰 어댑터 실행코드 + 클릭리스너
            recyclerviewUser()

        }
    }

    fun getFollows(): String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder()
            .url("http://ec2-13-209-169-218.ap-northeast-2.compute.amazonaws.com:12888/follow/followerList?meId=${MainActivity.loginId}")
            .build()
        client.newCall(req).execute().use { response ->
            return if (response.body != null) {
                response.body!!.string()
            } else {
                "body null"
            }
        }
    }
}








