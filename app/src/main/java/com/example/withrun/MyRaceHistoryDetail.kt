package com.example.withrun

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_my_race_history_detail.*
import kotlinx.android.synthetic.main.activity_profile_introduce.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.ArrayList

class MyRaceHistoryDetail : AppCompatActivity() {

    val TAG: String = "MyRaceHistoryDetail"

    var overcomeStatus : String? = null
    private var roomNo = 0
    private var userId = 0 // 클릭한 유저의 아이디 ( 메인에 기록을 보여줄 유저 )
    private var location = ""

    val raceRecordList = ArrayList<class_RaceRecord>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_race_history_detail)

        var intent = intent
        if (!TextUtils.isEmpty(intent.getStringExtra("location"))) { // --------------------백버튼, 뒤로가기버튼 컨트롤
            roomNo = intent!!.getIntExtra("roomNo",0)
            userId = intent!!.getIntExtra("userId",0)
            location = intent.getStringExtra("location")

            if (intent.getStringExtra("location") == "runningActive") {

            } else {

            }
            Log.d(TAG, "MyRaceHistoryDetail 에서 location 정보, getIntent결과 : " + roomNo + userId + location)

        }




        // 보여줄 record 데이터 get 통신 하는 부분 --------------------------------------------------- test 가 끝나면 roomno 로 변경해줘야함
        coroutineGetDetailRecord(roomNo)

        backBT.setOnClickListener {
            moveActivity()
        }

    }

    // 뒤로가기 버튼 클릭시 어느액티비티에서 넘어왔는지에 따라 다르게 이동할 예정 -------------------------------------------------------------- intent미완성
    fun moveActivity () {

        when (location) {
            "runningActive" -> {// 플래그 필요없음

                val intent = Intent(this, MyRanking::class.java)
                intent.putExtra("location","MyRaceHistoryDetail")
                startActivity(intent)
                finish()

            }
            "MyRanking" -> { // 플래그 추가

                val intent = Intent(this, MyRanking::class.java)
                intent.putExtra("location","MyRaceHistoryDetail")
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()

            }
            "Profile" -> { // 플래그 추가

                val intent = Intent(this, Profile::class.java)
                intent.putExtra("location","MyRaceHistoryDetail")
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()

            }
            "MyRaceHistory" -> { // 플래그 추가

                val intent = Intent(this, MyRaceHistory::class.java)
                intent.putExtra("location","MyRaceHistoryDetail")
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()

            }

            "MyRoomList" -> { // 플래그 추가

                val intent = Intent(this, MyRanking::class.java)
                intent.putExtra("location","MyRaceHistoryDetail")
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()

            }

        }



    }

    override fun onBackPressed() {
        super.onBackPressed()

        moveActivity()
    }


    fun coroutineGetDetailRecord (roomNo: Int) {

        CoroutineScope(Dispatchers.Main).launch {
            this
            val roomInfo = CoroutineScope(Dispatchers.Default).async { this
                getDetailRecord(roomNo)
            }.await()
            Log.d(TAG, roomInfo)

            val jsonObject = JSONObject(roomInfo)
            val existResult = jsonObject.getString("existResult")

            if (existResult == "0") {

            } else {
                var jsonArray = jsonObject.getJSONArray("result")

                setRecordData (jsonArray!!)

            }
        }
    }

    fun getDetailRecord (roomNo: Int): String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder()
            .url(Constants.URL + "/record/roomRecordDetail?roomNo=$roomNo").build()
        client.newCall(req).execute().use { response ->  return if (response.body != null) {
                response.body!!.string()
            } else {
                "body null"
            }
        }
    }

    fun setRecordData (jsonArray: JSONArray) {
        Log.d(TAG, "setRecordData ()")

        val raceHistoryJson = jsonArray.getJSONObject(0) // 룸데이터
        var activeGame = raceHistoryJson.getInt("activeGame")

        for (i in 0 until jsonArray.length()) {

            var userRecord = class_RaceRecord()

            userRecord.setId(jsonArray.getJSONObject(i).getInt("userId"))
            userRecord.setIntoAt(jsonArray.getJSONObject(i).getString("into_at"))
            userRecord.setGameStartTime(jsonArray.getJSONObject(i).getString("gameStartTime"))
            userRecord.setGameFinishTime(jsonArray.getJSONObject(i).getString("gameFinishTime"))
            userRecord.setRaceTime(jsonArray.getJSONObject(i).getInt("raceTime"))
            userRecord.setDistance(jsonArray.getJSONObject(i).getInt("distance"))
            userRecord.setRunStartTime(jsonArray.getJSONObject(i).getString("runningTime"))

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
            userRecord.setMyRanking(jsonArray.getJSONObject(i).getInt("myRanking"))
            userRecord.setCompleted(jsonArray.getJSONObject(i).getInt("completed"))
            userRecord.setDeleteRoom(jsonArray.getJSONObject(i).getInt("deleteRoom"))
            userRecord.setActiveRunState(jsonArray.getJSONObject(i).getInt("activeRunState"))
            userRecord.setMapSnapshot(jsonArray.getJSONObject(i).getString("mapSnapshot"))
            userRecord.setRecordNickname(jsonArray.getJSONObject(i).getString("nickname"))
            userRecord.setProfileImgPath(jsonArray.getJSONObject(i).getString("profileImgPath"))

            raceRecordList.add(userRecord)

        }

        raceRecordList.sortBy { it.getDistance() }
        raceRecordList.sortBy { it.getMyRanking() }
        raceRecordList.sortBy { it.getCompleted() }

        recyclerView(raceRecordList)

        for (i in 0 until raceRecordList.size) { // top position 에 현재 유저의 data 만 ui set 해줌
            if (raceRecordList.get(i).getId() == userId) {
                setTopPositionMyRaceRecord (raceRecordList.get(i))
                break
            }
        }

        if ( activeGame == 0 || activeGame == 1 ) { // 남은 유저들 경기 진행 중으로 경주 랭킹은 아직 보여주지 않음

            memberRocordCover.visibility = View.VISIBLE
            coverNotice.visibility = View.VISIBLE
            rvRaceHistory.visibility = View.GONE

        } else { // 모든 유저의 경기가 완료됨

            memberRocordCover.visibility = View.GONE
            coverNotice.visibility = View.GONE
            rvRaceHistory.visibility = View.VISIBLE

        }

    }

    fun setTopPositionMyRaceRecord (myRaceRecord: class_RaceRecord) {

        val gameStartTime = myRaceRecord.getRunStartTime()
        Log.d(TAG, "setTopPositionMyRaceRecord 파라미터 오브젝트값 확인 " + myRaceRecord)

        val imagePath = myRaceRecord.getMapSnapshot()
        val myRanking = myRaceRecord.getMyRanking()
        val distance = myRaceRecord.getDistance()
        val distanceText = (distance!!.toInt() / 100).toString() + "." + oftenUseMethod.distanceDigitString((distance).toLong())
        val raceTime = myRaceRecord.getRaceTime()
        val raceTimeText = (oftenUseMethod.twoDigitString((raceTime!!.toInt() / (60 * 60) % 24).toLong()).toString() + " : " + oftenUseMethod.twoDigitString((raceTime / 60 % 60).toLong()) + " : "
                + oftenUseMethod.twoDigitString((raceTime % 60).toLong()))

        if (imagePath == "") {
            mapRace.setImageResource(R.drawable.maps)

        } else {
            Glide.with(this)
                .load(Constants.URL+imagePath)
                .into(mapRace)
        }

            when (myRanking) {
                1 -> {
                    rankImg.setBackgroundResource(R.drawable.medal_gold)
                    rankRecord.text = "금메달"
                }
                2 -> {
                    rankImg.setBackgroundResource(R.drawable.medal_silver)
                    rankRecord.text = "은메달"
                }
                3 -> {
                    rankImg.setBackgroundResource(R.drawable.medal_bronze)
                    rankRecord.text = "동메달"
                }
                else -> {
                    rankImg.setBackgroundResource(R.drawable.party_popper)
                    rankRecord.text = "순위없음"
                }
            }


        raceDateRecord.text = gameStartTime?.substring(2,4)+"/"+gameStartTime?.substring(5,7)+"/"+gameStartTime?.substring(8,10)
        distanceRecord.text = distanceText
        raceTimeRecord.text = raceTimeText
        racePaceRecord.text = myRaceRecord.getRaceAvgPaceString()

    }

    fun recyclerView (list: ArrayList<class_RaceRecord>) {

        rvRaceHistory.layoutManager = LinearLayoutManager(this)
        rvRaceHistory.adapter = MyRaceHistoryDetailAdapter(this, list) {

        }
    }

}