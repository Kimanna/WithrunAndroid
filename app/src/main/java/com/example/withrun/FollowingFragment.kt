package com.example.withrun

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_follower.*
import kotlinx.android.synthetic.main.fragment_following.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject

class FollowingFragment : Fragment() {

    val TAG : String = "FollowingFragment"
    val followingList = ArrayList<Follow_object>()
    val followingSuggestList = ArrayList<Follow_object>()


    fun newInstant() : FollowingFragment {
        val args = Bundle()
        val frag = FollowingFragment()
        frag.arguments = args

        return frag
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView Following 지난후 " )

        coroutinegetFollowData()

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_following, container, false)
    }

    fun recyclerviewUser() {
        Log.d(TAG, "recyclerviewUser ()" )
        Log.d(TAG, "arraylist size 확인 " +followingList.size +"  "+ followingSuggestList.size)

        followingLV.layoutManager = LinearLayoutManager(activity)
        followingLV.adapter = activity?.let {
            FollowingAdapter(it, followingList) {

                goProfileIntroduce (it.getYouId()!!)
            }
        }

        suggestFollower.layoutManager = LinearLayoutManager(activity)
        suggestFollower.adapter = activity?.let {
            FollowingSuggestAdapter(it, followingSuggestList) {

                goProfileIntroduce (it.getYouId()!!)
            }
        }
    }

    fun goProfileIntroduce (profileUserId: Int) {

        val intent = Intent (getActivity(), ProfileIntroduce::class.java)
        intent.putExtra("profileUserId", profileUserId)
        intent.putExtra("location", "FollowingFragment")
        getActivity()?.startActivity(intent)

    }

    fun coroutinegetFollowData() {
        CoroutineScope(Dispatchers.Main).launch {
            this
            val follows = CoroutineScope(Dispatchers.Default).async {
                this
                getFollows()
            }.await()
            Log.d(TAG, "following 정보 있음" + follows)

            val jsonObject = JSONObject(follows)
            val jsonArray = jsonObject.getJSONArray("result")
            val jsonArraySuggest = jsonObject.getJSONArray("result_Suggest")


            for (i in 0..jsonArray.length() - 1) {

                var follows = Follow_object()
                follows.setYouId(jsonArray.getJSONObject(i).getInt("youId"))
                follows.setYouImg(jsonArray.getJSONObject(i).getString("youImg"))
                follows.setYouNickname(jsonArray.getJSONObject(i).getString("youNickname"))
                follows.setAcceptStatus(jsonArray.getJSONObject(i).getInt("accept_status"))
                follows.setRequestAt(jsonArray.getJSONObject(i).getString("request_at"))
                follows.setHideFollower(jsonArray.getJSONObject(i).getInt("hide_follower"))
                follows.setHideNewsfeed(jsonArray.getJSONObject(i).getInt("hide_newsfeed"))

                followingList.add(follows)

            }

            var userAvgPace = 0

            for (i in 0..jsonArraySuggest.length() - 1) {
                if (jsonArraySuggest.getJSONObject(i).getInt("id") == MainActivity.loginId) {
                    userAvgPace = jsonArraySuggest.getJSONObject(i).getInt("avgPace")
                }
            }

            for (i in 0..jsonArraySuggest.length() - 1) {

                if (jsonArraySuggest.getJSONObject(i).getInt("id") != MainActivity.loginId) {

                    var tempAvgPaceGap = Math.abs( userAvgPace - jsonArraySuggest.getJSONObject(i).getInt("avgPace"))
                    var tempAvgPaceString = "평균페이스 " + oftenUseMethod.secondsToTimeOverMinuteAddHour(jsonArraySuggest.getJSONObject(i).getInt("avgPace").toLong()) + " /km"

                    var followsSuggest = Follow_object()
                    followsSuggest.setYouId(jsonArraySuggest.getJSONObject(i).getInt("id"))
                    followsSuggest.setYouImg(jsonArraySuggest.getJSONObject(i).getString("profileImgPath"))
                    followsSuggest.setYouNickname(jsonArraySuggest.getJSONObject(i).getString("nickname"))
                    followsSuggest.setRecordOpenStatus(jsonArraySuggest.getJSONObject(i).getInt("record_open_status"))
                    followsSuggest.setFcmToken(jsonArraySuggest.getJSONObject(i).getString("fcmToken"))
                    followsSuggest.setAvgPaceGap(tempAvgPaceGap)
                    followsSuggest.setAvgPace(tempAvgPaceString)

                    followingSuggestList.add(followsSuggest)

                }
            }
            followingSuggestList.sortBy { it.getAvgPaceGap() }


            // 리싸이클러뷰 어댑터 실행코드 + 클릭리스너
            recyclerviewUser()

        }
    }

    fun getFollows(): String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder()
            .url("http://ec2-13-209-169-218.ap-northeast-2.compute.amazonaws.com:12888/follow/followingList?meId=${MainActivity.loginId}")
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