package com.example.withrun

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_my_ranking.*
import kotlinx.android.synthetic.main.activity_my_room_list.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.bottomnavigation
import kotlinx.android.synthetic.main.activity_profile.recyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MyRanking : AppCompatActivity() {

    val TAG : String = "MyRanking"

    private var MyNewsfeedAdapter: MyRankingAdapter? = null
    private var page = 0 // 현재 페이지
    private var allNewsfeedCount = 0 // newsfeed 총 갯수

    var isLoading = false
    var list: ArrayList<class_RaceRecord> = ArrayList()
    lateinit var tempJsonArray: JSONArray


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_ranking)

        bottomnavigation.setSelectedItemId(R.id.MyRanking)
        bottomnavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Home -> {
                    startActivity(Intent(applicationContext,Home::class.java))
                    finish()
                    true
                }
                R.id.Ranking -> {
                    startActivity(Intent(applicationContext,Ranking::class.java))
                    finish()
                    true
                }
                R.id.MyRanking -> {
                    true
                }
                R.id.Profile -> {
                    startActivity(Intent(applicationContext,Profile::class.java))
                    finish()
                    true
                }
            }
            true
        }

        coroutineGetNewsfeed (page)

        recyclerView ()
        initScrollListener() // 스크롤 마지막인지 감지

    }

    fun recyclerView () {

//        list.sortByDescending { it.getGameFinishTimeToLong() }

        recyclerView.layoutManager = LinearLayoutManager(this)
        MyNewsfeedAdapter = MyRankingAdapter(this, list) { class_RaceRecord ->

            goRaceHistoryDetail (class_RaceRecord.getRoomNo()!!, class_RaceRecord.getId()!!)

        }
        recyclerView.adapter = MyNewsfeedAdapter

    }

    fun goRaceHistoryDetail (roomNo: Int, userId: Int) {

        val intent = Intent(this, MyRaceHistoryDetail::class.java)
        intent.putExtra("location","MyRanking")
        intent.putExtra("roomNo",roomNo)
        intent.putExtra("userId",userId)
        startActivity(intent)
    }

    fun coroutineGetNewsfeed (pageNo: Int) {

        CoroutineScope(Dispatchers.Main).launch { this
            val newsfeed = CoroutineScope(Dispatchers.Default).async { this
                // network
                getNewsfeed(pageNo)
            }.await()

            val jsonObject = JSONObject(newsfeed)
            val existResult = jsonObject.getString("existResult")

            if (existResult == "0") {
                Log.d(TAG, "newsfeed 정보 없음")
            } else {
                Log.d(TAG, "newsfeed 정보 있음" + newsfeed)

                if (page == 0) {

                    val jsonArray = jsonObject.getJSONArray("result")
                    allNewsfeedCount = jsonObject.getJSONArray("all_newsfeed_count").getJSONObject(0).getInt("newsfeedCount")

                    for (i in 0..jsonArray.length()-1) {

                        var userRaceNewsfeed = class_RaceRecord()
                        userRaceNewsfeed.setIsLoading(1)
                        userRaceNewsfeed.setId(jsonArray.getJSONObject(i).getInt("userId"))
                        userRaceNewsfeed.setRoomNo(jsonArray.getJSONObject(i).getInt("roomNo"))
                        userRaceNewsfeed.setRecordNickname(jsonArray.getJSONObject(i).getString("nickname"))
                        userRaceNewsfeed.setProfileImgPath(jsonArray.getJSONObject(i).getString("profileImgPath"))
                        userRaceNewsfeed.setMyRanking(jsonArray.getJSONObject(i).getInt("myRanking"))
                        userRaceNewsfeed.setRaceTime(jsonArray.getJSONObject(i).getInt("raceTime"))
                        userRaceNewsfeed.setDistance(jsonArray.getJSONObject(i).getInt("distance"))
                        userRaceNewsfeed.setCompleted(jsonArray.getJSONObject(i).getInt("completed"))
                        userRaceNewsfeed.setGameFinishTime(jsonArray.getJSONObject(i).getString("gameFinishTime"))
                        userRaceNewsfeed.setMapSnapshot(jsonArray.getJSONObject(i).getString("mapSnapshot"))

                        val paceText: String
                        val speedText: String

                        if ( userRaceNewsfeed.getRaceTime() == 0 || userRaceNewsfeed.getDistance() == 0 ) {
                            paceText = "00:00 /km"
                            speedText = "00:00"
                        } else {

                            var tempRaceTime = jsonArray.getJSONObject(i).getInt("raceTime").toDouble()
                            var tempRaceDistance = (jsonArray.getJSONObject(i).getInt("distance") * 10).toDouble()

                            paceText = oftenUseMethod.secondsToTimeOverMinuteAddHour(((tempRaceTime/tempRaceDistance)*1000).toLong()) + " /km"
                            speedText = (((tempRaceDistance/tempRaceTime)*3600)/1000).toString()
                        }

                        userRaceNewsfeed.setRaceAvgPaceString(paceText)
                        userRaceNewsfeed.setRaceAvgSpeedString(speedText)

                        var tempGameFinishTime = jsonArray.getJSONObject(i).getString("gameFinishTime")
                        userRaceNewsfeed.setGameFinishTimeToLong(oftenUseMethod.timeStringToLong(tempGameFinishTime))

                        list.add(userRaceNewsfeed)
                    } // for 문 끝남

                    Log.d(TAG, "포문 끝난 후 arraylist 사이즈 확인 - "+ list.size.toString())
                    MyNewsfeedAdapter!!.notifyDataSetChanged()


                } else {
                    tempJsonArray = jsonObject.getJSONArray("result")

                }

            }
        }
    }

    fun addRoomList () {

        for (i in 0..tempJsonArray.length()-1) {

            var userRaceNewsfeed = class_RaceRecord()
            userRaceNewsfeed.setIsLoading(1)
            userRaceNewsfeed.setId(tempJsonArray.getJSONObject(i).getInt("userId"))
            userRaceNewsfeed.setRoomNo(tempJsonArray.getJSONObject(i).getInt("roomNo"))
            userRaceNewsfeed.setRecordNickname(tempJsonArray.getJSONObject(i).getString("nickname"))
            userRaceNewsfeed.setProfileImgPath(tempJsonArray.getJSONObject(i).getString("profileImgPath"))
            userRaceNewsfeed.setMyRanking(tempJsonArray.getJSONObject(i).getInt("myRanking"))
            userRaceNewsfeed.setRaceTime(tempJsonArray.getJSONObject(i).getInt("raceTime"))
            userRaceNewsfeed.setDistance(tempJsonArray.getJSONObject(i).getInt("distance"))
            userRaceNewsfeed.setCompleted(tempJsonArray.getJSONObject(i).getInt("completed"))
            userRaceNewsfeed.setGameFinishTime(tempJsonArray.getJSONObject(i).getString("gameFinishTime"))
            userRaceNewsfeed.setMapSnapshot(tempJsonArray.getJSONObject(i).getString("mapSnapshot"))

            val paceText: String
            val speedText: String

            if ( userRaceNewsfeed.getRaceTime() == 0 || userRaceNewsfeed.getDistance() == 0 ) {
                paceText = "00:00 /km"
                speedText = "00:00"
            } else {

                var tempRaceTime = tempJsonArray.getJSONObject(i).getInt("raceTime").toDouble()
                var tempRaceDistance = (tempJsonArray.getJSONObject(i).getInt("distance") * 10).toDouble()

                paceText = oftenUseMethod.secondsToTimeOverMinuteAddHour(((tempRaceTime/tempRaceDistance)*1000).toLong()) + " /km"
                speedText = (((tempRaceDistance/tempRaceTime)*3600)/1000).toString()
            }

            userRaceNewsfeed.setRaceAvgPaceString(paceText)
            userRaceNewsfeed.setRaceAvgSpeedString(speedText)

            var tempGameFinishTime = tempJsonArray.getJSONObject(i).getString("gameFinishTime")
            userRaceNewsfeed.setGameFinishTimeToLong(oftenUseMethod.timeStringToLong(tempGameFinishTime))

            list.add(userRaceNewsfeed)

        } // for 문 끝남
    }

    // 리싸이클러뷰 이벤트시
    private fun initScrollListener() {
        recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                Log.d(TAG, "onScrollStateChanged: ")
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.d(TAG, "onScrolled: ")
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == list.size - 1) {
                        if (page * 10 > allNewsfeedCount) {
                            return
                        }
                        page++
                        dataMore()
                        isLoading = true
                    }
                }
            }
        })
    }

    private fun dataMore() {
        Log.d(TAG, "dataMore: ")

        if (page * 10 > allNewsfeedCount) {
            return
        }

        var newsfeed = class_RaceRecord()
        newsfeed.setIsLoading(0)
        list.add(newsfeed)
        MyNewsfeedAdapter!!.notifyItemInserted(list.size - 1)       // 로딩버튼 리싸이클러뷰에 추가함

        //통신후 데이터 더 가져옴
        coroutineGetNewsfeed (page)

        val handler = Handler()
        handler.postDelayed(Runnable {

            list.removeAt(list.size - 1)
            val scrollPosition = list.size
            MyNewsfeedAdapter!!.notifyItemRemoved(scrollPosition)

            // arraylist 안에 서버에서 추가로 가져온 room data 추가해줌
            addRoomList ()

            MyNewsfeedAdapter!!.notifyItemRangeInserted((page + 1) * 10, 10)
            isLoading = false

        }, 2000)
    }



    fun getNewsfeed (pageNo: Int) : String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL + "/record/newsfeed?userId=+${MainActivity.loginId}&pageNo=$pageNo").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }

    inner class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) :
        RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = verticalSpaceHeight
        }
    }
}