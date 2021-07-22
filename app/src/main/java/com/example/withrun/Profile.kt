package com.example.withrun

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_create_room.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_my_race_history.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.bottomnavigation
import kotlinx.android.synthetic.main.activity_profile.proAveragePace
import kotlinx.android.synthetic.main.activity_profile.proNickname
import kotlinx.android.synthetic.main.activity_profile.profileimg
import kotlinx.android.synthetic.main.activity_profile.recyclerView
import kotlinx.android.synthetic.main.activity_profile_introduce.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject

class Profile : AppCompatActivity() {

    val TAG : String = "Profile"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        bottomnavigation.setSelectedItemId(R.id.Profile)
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
                    startActivity(Intent(applicationContext,MyRanking::class.java))
                    finish()
                    true
                }
                R.id.Profile -> {
                    true
                }
            }
            true
        }

        // 로그인한 email주소
//        var intent = getIntent()
//        isLoginEmail = intent.getStringExtra("email")
//        Log.d(TAG, "현재 로그인 중인 유저 email " + isLoginEmail)


        if (MainActivity.loginProfileImgPath == "else") {
            profileimg.setImageResource(R.drawable.avatar)

        } else {
            Glide.with(this)
                    .load(Constants.URL+MainActivity.loginProfileImgPath)
                    .into(profileimg)
        }

        proNickname.setText(MainActivity.loginNickname)

        // 설정 창으로 이동
        settings.setOnClickListener(View.OnClickListener { view ->
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        })

        // 프로필 수정 화면으로 이동
        reviseProfile.setOnClickListener(View.OnClickListener { view ->
            val intent = Intent(this, ReviseProfile::class.java)
            startActivity(intent)
        })


        // 팔로우 유저 확인
        getFollowList.setOnClickListener{
            val intent = Intent(this, Follow::class.java)
            startActivity(intent)

        }

        // 내 모든 경기기록 보기 activity로 넘어가는 버튼
        goMyRaceHistory.setOnClickListener{
            val intent = Intent(this, MyRaceHistory::class.java)
            startActivity(intent)

        }


        coroutineUserProfile ()
        coroutineUserRecord ()


    }

    fun recyclerView (list: ArrayList<class_RaceRecord>) {

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MyRaceHistoryAdapter(this, list) { record ->

            goRaceHistoryDetail (record.getRoomNo()!!)

        }
    }

    fun goRaceHistoryDetail (roomNo: Int) {

        val intent = Intent(this, MyRaceHistoryDetail::class.java)
        intent.putExtra("location","Profile")
        intent.putExtra("roomNo",roomNo)
        intent.putExtra("userId",MainActivity.loginId)
        startActivity(intent)
    }


    // 유저의 최근 3경기만 recyclerview 를 통해 보여줌
    fun coroutineUserRecord () {

        CoroutineScope(Dispatchers.Main).launch { this
            val userRecord = CoroutineScope(Dispatchers.Default).async { this
                userRecord ()
            }.await()
            Log.d(TAG,"140 line "+ userRecord)

            val jsonObject = JSONObject(userRecord)
            val existResult = jsonObject.getString("existResult")

            if (existResult == "0") {
                Log.d(TAG, "정보 없음")

                noRecentRecord.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE

            } else {
                Log.d(TAG, "정보 있음")

                recyclerView.visibility = View.VISIBLE
                noRecentRecord.visibility = View.GONE

                val list = ArrayList<class_RaceRecord>()

                val jsonArray = jsonObject.getJSONArray("result")

                for (i in 0..jsonArray.length() - 1) {

                    var userRecord = class_RaceRecord()

                    userRecord.setId(jsonArray.getJSONObject(i).getInt("userId"))
                    userRecord.setRoomNo(jsonArray.getJSONObject(i).getInt("roomNo"))
                    userRecord.setGameStartTime(jsonArray.getJSONObject(i).getString("gameStartTime"))
                    userRecord.setGameFinishTime(jsonArray.getJSONObject(i).getString("gameFinishTime"))
                    userRecord.setRaceTime(jsonArray.getJSONObject(i).getInt("raceTime"))
                    userRecord.setDistance(jsonArray.getJSONObject(i).getInt("distance"))
                    userRecord.setMyRanking(jsonArray.getJSONObject(i).getInt("myRanking"))
                    userRecord.setCompleted(jsonArray.getJSONObject(i).getInt("completed"))
                    userRecord.setMemberCount(jsonArray.getJSONObject(i).getInt("memberCount"))



                    val paceText: String
                    val speedText: String

                    if ( userRecord.getRaceTime() == 0 || userRecord.getDistance() == 0 ) {
                        paceText = "00:00"
                        speedText = "00:00"
                    } else {

                        var tempRaceTime = jsonArray.getJSONObject(i).getInt("raceTime").toDouble()
                        var tempRaceDistance = (jsonArray.getJSONObject(i).getInt("distance") * 10).toDouble()

                        paceText = oftenUseMethod.secondsToTimeOverMinuteAddHour(((tempRaceTime/tempRaceDistance)*1000).toLong())
                        speedText = (((tempRaceDistance/tempRaceTime)*3600)/1000).toString()
                    }

                    userRecord.setRaceAvgPaceString(paceText)
                    userRecord.setRaceAvgSpeedString(speedText)

                    list.add(userRecord)
                } // for 문 끝남

                Log.d(TAG, "포문 끝난 후 arraylist 사이즈 확인 - " + list.size.toString())

                // 리싸이클러뷰 어댑터 실행코드 + 클릭리스너
                recyclerView(list)

            }


        }
    }

    fun userRecord () : String  {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL+"/record/myRecentRecord?userId=${MainActivity.loginId}").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }

    // 유저 pace 기록, follow count, 기록 그래프 가져옴
    fun coroutineUserProfile () {

        CoroutineScope(Dispatchers.Main).launch { this
            val roomUserProfile = CoroutineScope(Dispatchers.Default).async { this
                roomUserProfile ()
            }.await()

            Log.d(TAG,"80 line "+ roomUserProfile)

            val jsonObject = JSONObject(roomUserProfile)

            setProfileIntroduce (jsonObject)


        }
    }

    fun roomUserProfile () : String  {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL+"/users/myProfile?userId=${MainActivity.loginId}").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }

    // 해당 유저 프로필을 UI 에 보여주는 부분
    fun setProfileIntroduce(jsonObject: JSONObject) {

        val jsonArray = jsonObject.getJSONArray("result_record") //  유저 (프로필 주인) 프로필데이터
        val jsonArrayRecordPace = jsonObject.getJSONArray("result_record_pace").getJSONObject(0).getInt("avgPace")
        val jsonRecordFollowerCount = jsonObject.getJSONArray("result_record_follower_count").getJSONObject(0).getInt("followerCount")
        val jsonRecordFollowingCount = jsonObject.getJSONArray("result_record_following_count").getJSONObject(0).getInt("followingCount")

        proAveragePace.text = oftenUseMethod.secondsToTimeOverMinuteAddHour(jsonArrayRecordPace.toLong())
        followerCount.text = jsonRecordFollowerCount.toString()
        followingCount.text = jsonRecordFollowingCount.toString()
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