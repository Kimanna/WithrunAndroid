package com.example.withrun

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import com.example.withrun.oftenUseMethod.secondsToTime
import com.example.withrun.oftenUseMethod.twoDigitString
import kotlinx.android.synthetic.main.activity_create_room.*
import kotlinx.android.synthetic.main.activity_race_score.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import kotlin.math.floor

class RaceScore : AppCompatActivity() {

    val TAG:String = "RaceScore"

    private var mService: MyService? = null
    private var mBound: Boolean = false

    var myRankingIntent: Int = 0
    var stopwatch: Int? = null
    var totaldistance: Double? = null
    var pace: Long? = null
    var participant: Int? = null // 총 인원
    var completed: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_race_score)

        var intent = intent
        myRankingIntent = intent.getIntExtra("myRanking",0)
        stopwatch = intent.getIntExtra("stopwatch",0)
        totaldistance = intent.getDoubleExtra("totaldistance",0.0)
        pace = intent.getLongExtra("pace",0)
        participant = intent.getIntExtra("participant", 0)
        completed = intent.getIntExtra("completed",0)

        Log.d(TAG, intent.getIntExtra("myRanking",0).toString())



//        goMain.setOnClickListener{
//
//            val intent = Intent(this, Home::class.java)
//            startActivity(intent)
//            finish()
//        }
//        viewMyHistory.setOnClickListener {
//            val intent = Intent(this, MyRaceHistory::class.java)
//            startActivity(intent)
//            finish()
//        }


    }

//    private val mConnection: ServiceConnection = object : ServiceConnection {
//        override fun onServiceConnected(name: ComponentName, service: IBinder) {
//            val binder = service as MyService.MyBinder
//            mService = binder.service
//            mBound = true
//
//            // 인텐트 값이 넘어오지 않았을때 중도 포기라고 가정하여 service에서 받아온 값 보여줌
//            if ( stopwatch == 0 ) {
//
//                stopwatch = mService?.getmCount()
//                totaldistance = mService?.totalDistance?.let { floor(it) }
//
//                pace = mService?.avgPace
//
//                myRankingIntent = mService?.participant!!
//                participant = mService?.participant!!
//
//                completed = 0
//            }
//
//
//            mService?.stopForegroundService()
//            finishService();
//        }
//
//        override fun onServiceDisconnected(name: ComponentName) {
//            // 얘기치 않은 종료시에 실행
//            finishService()
//        }
//    }



    override fun onStart() {
        super.onStart()

//        intent = Intent(this, MyService::class.java)
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE) // bind_auto_create flag 는 서비스를 자동으로 생성해주고 bind 까지 해주는 flag
    }




//    fun finishService() {
//
//        intent = Intent(this, MyService::class.java)
//        stopService(intent)
//        unbindService(mConnection)
//
//    }

    override fun onResume() {
        super.onResume()

        if ( totaldistance!! > RoomDetail.distance.toDouble()) {
            totaldistance = RoomDetail.distance.toDouble()*1000
        }
        setRecord () // setRecord 기록 보여줌 setText
        coroutineRaceRecord () // 러깅 기록 저장

    }

    fun setRecord () {

        var distanceKm = (totaldistance?.div(1000))?.toInt()
        var distanceMT = (totaldistance?.div(10))?.toLong()?.let { twoDigitString(it) }

        var getpaceMin = pace?.toLong()?.let { secondsToTime(it) }

        if (stopwatch != null) {
            totalTime.text = (twoDigitString((stopwatch!! / (60 * 60) % 24).toLong()).toString() + " : " + twoDigitString((stopwatch!! / 60 % 60).toLong()) + " : "
                    + twoDigitString((stopwatch!! % 60).toLong()))
        }

        totalDistance1.text = distanceKm.toString()
//        totalDistance2.text = distanceMT + " (km)"

        totalPace.text = getpaceMin + " 분/km"
        myRanking.text = myRankingIntent.toString() + " 위"
    }

    // 해당룸에서 퇴장을 원하는 user id 삭제
    fun coroutineRaceRecord () {
        Log.d(TAG,"coroutineExitRoom ()")

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                getHtml()
            }.await()

            Log.d(TAG,html)

        }
    }

    fun getHtml() : String {

        Log.d(TAG, "서버 발송할 데이터 확인 : " + RoomDetail.intoUserId + " " + RoomDetail.longStartTime + " " + stopwatch + " " + totaldistance +" "+ pace+" "+ participant+" "+myRankingIntent)

        val jsonObject = JSONObject()
        jsonObject.putOpt("id", RoomDetail.intoUserId)
        jsonObject.putOpt("gameStartTime", RoomDetail.longStartTime)
        jsonObject.putOpt("runningTime", stopwatch)
        jsonObject.putOpt("distance", totaldistance)
        jsonObject.putOpt("averagePace", pace)
        jsonObject.putOpt("allParticipant", participant)
        jsonObject.putOpt("myRanking", myRankingIntent)
        jsonObject.putOpt("completed", completed)

        val okHttpClient = OkHttpClient()
        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .method("POST", requestBody)
            .url("http://ec2-13-209-169-218.ap-northeast-2.compute.amazonaws.com:12888/record/raceRecord")
            .build()

        okHttpClient.newCall(request).execute().use { response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }



}