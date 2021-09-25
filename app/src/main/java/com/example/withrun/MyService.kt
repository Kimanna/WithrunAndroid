package com.example.withrun

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_create_room.*
import kotlinx.android.synthetic.main.activity_running_active.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.*
import java.net.Socket
import java.util.*
import kotlin.collections.ArrayList

class MyService : Service() {

    val TAG:String = "MyService"

    companion object {
        val LatLng : ArrayList<LatLng> =  ArrayList()


    }

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback


    var list = ArrayList<RoomIntoUser>() // 경기 룸 참가 user list (기록 update 하기위함)

    private var beforeLatitude = 0.0
    private var beforeLongitude = 0.0
    private var afterLatitude = 0.0
    private var afterLongitude = 0.0

    var totalDistance: Double = 0.0
    var sendDistance: Int = 0 // ui변경을 위해 전달하는 distance 값 ui 에 보여지는 부분인 10m 단위로 이동시에만 전달
    var avgPace: Long = 0

    var speakCount: Int = 0

    var currentRanking:Int = 0
    var beforeRanking:Int = 0

    private var mThread: Thread? = null

    private var stopwatch: Long = 0
    private var mCount = 0
    private var roomDistanceMT = 0
    private var roomNo = 0

    var manager: NotificationManager? = null
    var builder: NotificationCompat.Builder? = null

    lateinit var tts:TextToSpeech

    private val mBinder: IBinder = MyBinder()

    var socket: Socket? = null

    var out: PrintWriter? = null
    var networkReader: BufferedReader? = null
    var networkWriter: BufferedWriter? = null

    private var html = "" // java server 에서 보내준 값을 찍어보기 위한 변수

    class MyBinder : Binder() {
        val service: MyBinder
            get() = this
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        stopwatch = intent!!.getLongExtra("stopwatch", 0)
        roomDistanceMT = intent!!.getIntExtra("goalDistance",0)
        roomNo = intent!!.getIntExtra("roomNo",0)

        Log.d(TAG, "인텐트 받아온 값" + stopwatch+ " " +roomDistanceMT)

        // RunningActive activity에서 전달하는 list 객체를 공유
        //  ( activity oncreate 시 DB 에서 가져온 참여 유저 데이터를 list 에 set 한 후 service 를 실행함 )
        list = RunningActive.list

        if (currentRanking != 0) {
            beforeRanking = list.size
        }

        // socket 생성
        try {
            coroutine (JavaServerAPI.ip, JavaServerAPI.port)
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

        locationInit()
        startForegroundService()

        if (mThread == null) {
            mThread = object : Thread("My Thread") {
                override fun run() {
                    for (i in 0 until stopwatch) {
                        try {
                            sleep(1000)
                            mCount++

                            if (mCount == 5) {
                                getInOutRoom (roomNo , MainActivity.loginId, "intoRace")
                            }

                            if (sendDistance == 0) { // 아직 이동거리가 없는경우

                                sendMessage(mCount, sendDistance, avgPace, LatLng, "경기중", "myRecord", currentRanking)

                            } else { // 이동거리가 있는경우

                                avgPace = (( mCount.toDouble() / (sendDistance * 10).toDouble() ) * 1000).toLong()

                                // ui변경을 위해 activity로 시간, total 거리, 평균페이스 정보 전달
                                sendMessage(mCount, sendDistance, avgPace, LatLng, "경기중", "myRecord", currentRanking)

                                // 목표 거리를 채움
                                if (sendDistance >= roomDistanceMT) {

                                    sendDistance = roomDistanceMT

                                    // arraylist에 본인 상태 변경 activity로 경기종료 메시지전달, stopforeground
                                    changeRank ("finishRace", 0, MainActivity.loginId)
                                    finishSpeakSummary (sendDistance, mCount, avgPace, currentRanking)


                                    getInOutRoom(roomNo , MainActivity.loginId, "finishRace")
                                    sendMessage(mCount, sendDistance, avgPace, LatLng, "경기완료","myRecord",currentRanking)

                                }
                            }

                            updateNoti(); // notification 에 현재시간, 거리 변경

                        } catch (e: InterruptedException) {
                            break
                        }
                    }
                }
            }
            (mThread as Thread).start()
        }

        iniTextToSpeech()

        // service 에서 보낸 message 받는 부분
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mAlertReceiver, IntentFilter("MoveActivityFilter")
        )

        return START_REDELIVER_INTENT
    }

    // service 에서 activity 로 값을 전달할때 사용하는 메서드
    private fun sendMessage( stopwatch: Int, totalDistance: Int,  avgPace: Long, latLng: ArrayList<LatLng>, state: String, action: String, currentRanking: Int) {
        Log.d(TAG, "service 에서 activity 로 전달주는 값 $stopwatch $totalDistance $avgPace $state $action $currentRanking"  )

        val intent = Intent("MoveServiceFilter")
        intent.putExtra("stopwatch", stopwatch)
        intent.putExtra("totalDistance", totalDistance)
        intent.putExtra("avgPace", avgPace)
        intent.putParcelableArrayListExtra("latLng", latLng)
        intent.putExtra("state", state)
        intent.putExtra("action", action)
        intent.putExtra("currentRanking", currentRanking)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private val mAlertReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            var action = intent.getStringExtra("action")
            currentRanking = intent.getIntExtra("myRunningRank",0)

            if ( action == "랭킹" ) { // 랭킹 변경을 알려주는 message인 경우 음성 tts로 현재순위 알려즘


                // 러닝 중도 포기 버튼 클릭시 유저 나갔음 표시해주며 + service 중단
            } else if ( action == "stopService" ) {
                Log.d(TAG, "MyService 에서 stopService 지나감 ")

                // TCP로 타 유저들에게 유저 나감표시하기 위한 통신, recyclerview 갱신하기 위해
                // 메시지 보내고 나면 ForegroundService 종료 메서드 호출
                getInOutRoom(roomNo , MainActivity.loginId, "exitRoom")

            } else if ( action == "leaveRoom" ) {

                stopForegroundService(action)
            }
        }
    }




    fun coroutine (ip:String, port:Int) {

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                try {

                    socket = Socket(ip, port)

                    networkWriter = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))
                    networkReader = BufferedReader(InputStreamReader(socket!!.getInputStream()))

                } catch (e: IOException) {
                    println(e)
                    e.printStackTrace()
                }
            }.await()

        }
    }

    fun getInOutRoom (roomNo: Int, intoUserId: Int, action: String) {

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this

                out = PrintWriter(networkWriter!!, true)

                out!!.println("$roomNo/$intoUserId/$action")
                out!!.flush()

            }.await()

            if ( checkUpdate1.state == Thread.State.NEW ) {

                checkUpdate1.start()
            }

            if (action == "exitRoom") {
                stopForegroundService("giveUpRace")
            } else if (action == "finishRace") {
                stopForegroundService(action)

            }
        }
    }

    private val checkUpdate1: Thread = object : Thread() {
        override fun run() {
            try {
                var line: String
                Log.d(TAG, "142 Start Thread")
                while (true) {
                    line = networkReader!!.readLine()
                    html = line

                    Log.d(TAG, "message html 출력 $html")

                    // roomNo/userId/intoRace or distanceGap/58 (distanceGap number)
                    val strs = html.split("/").toTypedArray()


                    //유저 입장시 전달 받는 값 49/3/intoRace/앙나/images/beauty_20210410183905.jpg
                    if ( strs[2] == "runIntoRoom" ) {
                        Log.d(TAG, "runIntoRoom 지나감")


                        // 1. 입장한 유저를 '러닝 중' 상태로 변경해 주기위해 setRunningState = 1 로 변경 후 list sort
                        // 2. 현재 순위를 변수에 지정해줌
                        changeRank ("intoRace", 0, strs[1].toInt())
//                        beforeRanking = currentRanking


                        // ui 변경을 위해 activity 로 message 보내줌 ( 현재 순위 포함하여 전달 )
                        sendMessage(0,0,0, LatLng,"경기중","intoRace", currentRanking)
                    }

                    // 다른 유저들에게 남은 거리 update ( 10미터 간격으로 )
                    if ( strs[2] == "distanceGap" ) {
                        Log.d(TAG, "distanceGap 지나감")

                        changeRank ("distanceGap", strs[3].toInt(), strs[1].toInt())

                        // 순위변동이 있을 시 TTS 로 순위변동 알림
                        rankingTTS()

                        sendMessage(mCount, sendDistance, avgPace, LatLng, "경기중", "distanceGap", currentRanking)


                    }

                    if ( strs[2] == "exitRoom" ) {
                        Log.d(TAG, "exitRoom 지나감")

                        changeRank ("exitRoom", 0, strs[1].toInt())
                        sendMessage(mCount, sendDistance, avgPace, LatLng, "경기중", "exitRoom", currentRanking)

                    }

                    if ( strs[2] == "finishRace" ) {
                        Log.d(TAG, "finishRace 지나감 평균페이스 값 확인 " + avgPace)

                        changeRank ("finishRace", 0, strs[1].toInt())
                        sendMessage(mCount, sendDistance, avgPace, LatLng, "경기중", "finishRace", currentRanking)

                    }


                }
            } catch (e: Exception) {
                Log.d(TAG, "chat error 123 line "+ e.toString())
            }
        }
    }


    // tts 로 현재 순위 알려줌
    private fun rankingTTS () {

        Log.d(TAG, "이전 랭킹 beforeRanking "+beforeRanking.toString()+" " + currentRanking)

        var ttsSpeakString = ""

        if ( beforeRanking != 0 && beforeRanking < currentRanking ) {                          // 순위가 하락한 경우

             ttsSpeakString = "현재순위 " + currentRanking + " 위 입니다. 다시 역전할 수 있어요"

        } else if ( beforeRanking != 0 && beforeRanking > currentRanking ) {                     // 순위가 오른 경우

            ttsSpeakString = "현재순위 " + currentRanking + " 위 입니다. 잘하고 있어요"

//            when(currentRanking){
//
//                1 -> {
//                    ttsSpeakString = "현재순위 " + currentRanking + " 위 입니다. 이대로 골인까지"
//                }
//                2 -> {
//                    ttsSpeakString = "현재순위 " + currentRanking + " 위 입니다. 1위 가즈아 ~"
//                }
//                3 -> {
//                    ttsSpeakString = "현재순위 " + currentRanking + " 위 입니다. 잘하고 있어요 조금 더 속도를 올려 보세요"
//                }
//            }
//            ttsSpeack ("현재순위 " + currentRanking + " 위 입니다. 이대로 골인까지 ~ ~ ")

        } else { // 이전 랭킹과 현재 랭킹이 동일한 경우 아무런 말이 없음
            Log.d(TAG, "rankingTTS else 문 지나감 ")

        }

        ttsSpeack (ttsSpeakString)
//        ttsSpeack (ttsSpeakString)

        beforeRanking = currentRanking

    }

    fun changeRank (action: String, distanceGap: Int, userId: Int) {
        Log.d(TAG, "changeRank 에서 액션 확인 $action $distanceGap $userId")

        // 유저 러닝 입장
        if ( action == "intoRace" ) {

            // 모든 유저에게 입장한 유저 접속 중 상태로 변경하기위해 모든 유저의 정보가 있는 arraylist 안에 객체 값을 update 해줌
            for ( i  in 0 until list.size ) {
                if ( list.get(i).getId() == userId ) {
                    list.get(i).setRunningState(1)
                    break
                }
            }
            Log.d(TAG, "411 라인 지나감")

        }
        Log.d(TAG, "414 라인 지나감")


        // 10m 이동시 마다 남은거리 갱신
        if ( action == "distanceGap" ) {
            for(i in 0 until list.size) { // 안에있는 내용 in 배열명

                if (list.get(i).getId() == userId) {
                    list.get(i).setDistanceGap(distanceGap)
                    break
                }
            }
        }

        // 경기 포기 ( 중도에 나감 )
        if ( action == "exitRoom" ) {

            for ( i  in 0 until list.size ) {
                if ( list.get(i).getId() == userId ) {
                    list.get(i).setRunningState(2)
                    list.get(i).setMyActiveRaceRank(0)
                    break
                }
            }
        }

        // 경기 완주
        if ( action == "finishRace" ) {

            for ( i  in 0 until list.size ) {
                if ( list.get(i).getId() == userId ) {
                    list.get(i).setRunningState(0)
                    break
                }
            }
        }

        list.sortBy { it.getRunningState() }
        list.sortBy { it.getDistanceGap() }
//        list.sortBy { it.getMyActiveRaceRank() }

        // 현재 순위 변수에 update
        for ( i  in 0 until list.size ) {

            if ( list.get(i).getDistanceGap() != roomDistanceMT || list.get(i).getRunningState() != 2) {
                list.get(i).setMyActiveRaceRank(i+1)

                if ( list.get(i).getId() == MainActivity.loginId ) {
                    currentRanking = list.get(i).getMyActiveRaceRank()!!
                }
            }
        }

//        for ( i  in 0 until list.size ) {
//
//            if ( list.get(i).getDistanceGap() != roomDistanceMT ) {
//                Log.d(TAG, "473 지나감 랭킹 세팅하는 곳  " + list.get(i).getId().toString());
//                list.get(i).setMyActiveRaceRank(i+1)
//
//            }
//
//        }
    }





    private fun iniTextToSpeech() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(this,"SDK version is low", Toast.LENGTH_LONG).show()
            return
        }
        tts = TextToSpeech(this) {
            if ( it == TextToSpeech.SUCCESS ) {
                val result = tts.setLanguage(Locale.KOREA)
                if ( result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show()
                    return@TextToSpeech
                }
            } else {
            }
        }
        tts.setPitch(0.5.toFloat())
        tts.setSpeechRate(1.3.toFloat())
        tts.playSilentUtterance(1500, TextToSpeech.QUEUE_ADD, null)
    }

    private fun ttsSpeack (strTTS: String) {
        tts.speak(strTTS, TextToSpeech.QUEUE_ADD, null, null)
    }


    private fun speakSummary (sendDistance: Int, time: Int, avgPace: Long) {

        var distanceString = "현재 경주 거리 " + sendDistance * 10 + " 미터" // --------------------------------------------------------킬로미터로 수정해야함

        tts.speak(distanceString,TextToSpeech.QUEUE_ADD,null,null)
        tts.playSilentUtterance(400,TextToSpeech.QUEUE_ADD,null)

        var timeString = " 시간 "

        if ( (time / (60 * 60) % 24) != 0 ) {
            timeString += (time / (60 * 60) % 24).toString() + "시간"
        }
        if ( (time / 60 % 60) != 0 ) {
            timeString += oftenUseMethod.twoDigitString((time / 60 % 60).toLong()) + "분"
        }
        timeString += oftenUseMethod.twoDigitString((time % 60).toLong()) + "초"

        tts.speak(timeString,TextToSpeech.QUEUE_ADD,null,null)
        tts.playSilentUtterance(400,TextToSpeech.QUEUE_ADD,null)

        var paceString = " 평균 페이스 " + avgPace / 60 + "분" + avgPace % 60 + "초";

        tts.speak(paceString,TextToSpeech.QUEUE_ADD,null,null)
    }

    private fun finishSpeakSummary (sendDistance: Int, time: Int, avgPace: Long, ranking: Int) {
        Log.d(TAG, "finishSpeakSummary :  ")

        var speckString1 = "축하합니다 " + ranking.toString() + "위 로 경주를 완주 했습니다"
        tts.speak(speckString1,TextToSpeech.QUEUE_ADD,null,null)
        tts.playSilentUtterance(500,TextToSpeech.QUEUE_ADD,null)

        var speckString2 = "총 경주 내역을 알려드립니다"
        tts.speak(speckString2,TextToSpeech.QUEUE_ADD,null,null)
        tts.playSilentUtterance(500,TextToSpeech.QUEUE_ADD,null)


        var distanceString = "총 경주 거리 " + sendDistance * 10 + " 미터" // --------------------------------------------------------킬로미터로 수정해야함

        tts.speak(distanceString,TextToSpeech.QUEUE_ADD,null,null)
        tts.playSilentUtterance(400,TextToSpeech.QUEUE_ADD,null)

        var timeString = " 시간 "

        if ( (time / (60 * 60) % 24) != 0 ) {
            timeString += (time / (60 * 60) % 24).toString() + "시간"
        }
        if ( (time / 60 % 60) != 0 ) {
            timeString += oftenUseMethod.twoDigitString((time / 60 % 60).toLong()) + "분"
        }
        timeString += oftenUseMethod.twoDigitString((time % 60).toLong()) + "초"

        tts.speak(timeString,TextToSpeech.QUEUE_ADD,null,null)
        tts.playSilentUtterance(400,TextToSpeech.QUEUE_ADD,null)

        var paceString = " 평균 페이스 " + avgPace / 60 + "분" + avgPace % 60 + "초";

        tts.speak(paceString,TextToSpeech.QUEUE_ADD,null,null)
    }

    fun startForegroundService() {
        Log.d(TAG, "startForegroundService :  ")

        builder = NotificationCompat.Builder(this, "default")
        builder!!.setSmallIcon(R.mipmap.ic_launcher)
        builder!!.setContentTitle("러닝 경기 중")
        builder!!.setContentText("러닝 시작")
        builder!!.setColor(Color.RED)
        builder!!.setOngoing(true)
        builder!!.setOnlyAlertOnce(true)

        val notificationIntent = Intent(this, RunningActive::class.java)
        notificationIntent.putExtra("location","notification")
        notificationIntent.putExtra("roomNo",roomNo)
        notificationIntent.putExtra("currentRanking",currentRanking)

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

        val pendingIntent = PendingIntent.getActivity(this,0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT )
        builder!!.setContentIntent(pendingIntent)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager!!.createNotificationChannel(
                NotificationChannel("default","기본채널", NotificationManager.IMPORTANCE_DEFAULT )
            )
        }
        startForeground(1, builder!!.build())
    }

    fun updateNoti() {

        var distanceKm = (sendDistance / 100).toInt()
        var distanceMT = oftenUseMethod.distanceDigitString((sendDistance).toLong())

        var stopwatchstr = (oftenUseMethod.twoDigitString((mCount / (60 * 60) % 24).toLong()) + " : " + oftenUseMethod.twoDigitString((mCount / 60 % 60).toLong()) + " : "
                + oftenUseMethod.twoDigitString((mCount % 60).toLong()))
        var notiText = "운동시간 :  " + stopwatchstr + ", 운동거리 :  " + distanceKm.toString() +"."+distanceMT+" (km)";

        builder!!.setContentText(notiText);
        manager!!.notify(1,builder!!.build());

    }


    inner class MyLocationCallBack: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)

            val location = locationResult?.lastLocation   // GPS가 꺼져 있을 경우 Location 객체가null이 될 수도 있음
            location?.run {
                val latLng = LatLng(latitude, longitude)   // 위도, 경도
                LatLng.add(latLng)

                afterLatitude = latitude
                afterLongitude = longitude

                if (beforeLatitude == 0.0) { // 처음 시작 위치 지정해줌

                    beforeLatitude = latitude
                    beforeLongitude = longitude

                } else {

                    var distance = googleMap.getDistance(beforeLatitude,beforeLongitude,afterLatitude,afterLongitude)
                    totalDistance += distance
                    Log.d(TAG,"distance 차이 확인 : "+distance)
//                    Log.d(TAG,"distance 차이와 totalDistance 확인 : "+distance+"  "+totalDistance+"  "+totalDistance/10)

                    // 10m 이동시 activity 로 데이터 전송
                    if ( Math.floor(totalDistance / 10) > sendDistance ) {

                        sendDistance = Math.floor( totalDistance / 10 ).toInt()                 // 소수점 자른 10미터 단위로 저장 ex) 30.024317330867052 이라면 3으로 저장


                        // 목표거리 도달 전 까지만 TTS  알림
                        if (sendDistance < roomDistanceMT) {

                            rankingTTS()                                                        // 순위변동이 있을 시 TTS 로 순위변동 알림

                            // 100m 마다 경주 summary 음성알림
                            if ( sendDistance % 10 == 0 && sendDistance / 10 != speakCount) {

                                speakCount = sendDistance / 10
                                speakSummary (sendDistance, mCount, avgPace) // 기획은 1km 마다 알림 test 를 위해 10m 로 변경

                            }
                        } else {

                            sendDistance = roomDistanceMT

                        }


                        var distanceGap = roomDistanceMT - sendDistance


                        // list 갱신
                        changeRank ("distanceGap", distanceGap, MainActivity.loginId)

                        // activity 로 list 갱신 알림 ( action: String "distanceGap" )
                        sendMessage(mCount, sendDistance, avgPace, LatLng, "경기중", "distanceGap", currentRanking)

                        // TCP 타 유저들에게도 공유
                        getInOutRoom (roomNo, MainActivity.loginId, "distanceGap/$distanceGap")



                    }
                }

                beforeLatitude = location.latitude
                beforeLongitude = location.longitude
            }
        }
    }

    fun coroutineSaveRaceRecord (action: String) {
        Log.d(TAG, "coroutineSaveRaceRecord : ")

        CoroutineScope(Dispatchers.Main).launch {this
            val roomInfo = CoroutineScope(Dispatchers.Default).async { this
                saveRaceRecord(action)
            }.await()
            Log.d(TAG, roomInfo)

        }
    }

    fun saveRaceRecord (action: String): String {

        Log.d(TAG, "서버 발송할 데이터 확인 : "+ roomNo + mCount + sendDistance + beforeRanking)

        val jsonObject = JSONObject()
        jsonObject.putOpt("action", action)
        jsonObject.putOpt("roomNo", roomNo)
        jsonObject.putOpt("userId", MainActivity.loginId)
        jsonObject.putOpt("gameFinishTime", Date(System.currentTimeMillis()).toString())
        jsonObject.putOpt("raceTime", mCount)
        jsonObject.putOpt("distance", sendDistance)
        jsonObject.putOpt("myRanking", beforeRanking)
//            jsonObject.putOpt("mapSnapshot", )

        Log.d(TAG, jsonObject.toString())

        val okHttpClient = OkHttpClient()
        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .method("POST", requestBody)
            .url("http://ec2-13-209-169-218.ap-northeast-2.compute.amazonaws.com:12888/record/recordUpdate")
            .build()

        okHttpClient.newCall(request).execute().use { response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }

    fun stopForegroundService(action: String) {
        Log.d(TAG, "stopForegroundService : ")

        if (action == "giveUpRace") {

            mCount = 0
            totalDistance = 0.0
            LatLng.clear()

            stopForeground(true)
            stopSelf()

            if ( mThread != null ) {
                mThread!!.interrupt();
                mThread = null;
            }

            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }


            networkWriter?.close()
            socket?.close()

            checkUpdate1.interrupt()

        } else if (action == "finishRace") {

            stopForeground(true)

            if ( mThread != null ) {
                mThread!!.interrupt();
                mThread = null;
            }

            fusedLocationProviderClient.removeLocationUpdates(locationCallback)

        } else { // leaveRoom 인 경우

            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }

            mCount = 0
            totalDistance = 0.0
            LatLng.clear()

            stopSelf()

            networkWriter?.close()
            socket?.close()

            checkUpdate1.interrupt()
        }

    }


    private fun locationInit() {
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        locationCallback = MyLocationCallBack()

        locationRequest = LocationRequest()   // LocationRequest객체로 위치 정보 요청 세부 설정을 함
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY   // GPS 우선
        locationRequest.interval = 0   // 10초. 상황에 따라 다른 앱에서 더 빨리 위치 정보를 요청하면자동으로 더 짧아질 수도 있음
        locationRequest.fastestInterval = 0  // 이보다 더 빈번히 업데이트 하지 않음 (고정된 최소 인터벌)
        locationRequest.expirationTime = SystemClock.elapsedRealtime() + 10000000

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,  Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ) {
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)  // 혹시 안드로이드 스튜디오에서 비정상적으로 권한 요청 오류를 표시할 경우, 'Alt+Enter'로
    }

//    private fun permissionCheck(cancel: () -> Unit, ok: () -> Unit) {   // 전달인자도, 리턴값도 없는
//
//        // 두 개의 함수를 받음
//        if (ContextCompat.checkSelfPermission(this,                  // 권한이 없는 경우
//                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION)) {       // 권한 거부 이력이 있는 경우
//
//                cancel()
//
//            } else {
//                ActivityCompat.requestPermissions(this,
//                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                    REQUEST_ACCESS_FINE_LOCATION)
//            }
//        } else {                                                    // 권한이 있는 경우
//            ok()
//        }
//    }
//    private  fun showPermissionInfoDialog() {
//
//        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
//        builder.setTitle("위치 서비스 비활성화")
//        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n 위치 설정을 수정하실래요?".trimIndent()
//        )
//        builder.setCancelable(true)
//
//        builder.setPositiveButton("설정", DialogInterface.OnClickListener { dialog, id ->
//            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//            ActivityCompat.requestPermissions(this,  // 첫 전달인자: Context 또는 Activity
//                // this: DialogInterface 객체
//                // this@MapsActivity는 액티비티를 명시적으로 가리킨 것임
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                REQUEST_ACCESS_FINE_LOCATION)
//        })
//    }
//
//    private fun addLocationListener() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return
//        }
//        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)  // 혹시 안드로이드 스튜디오에서 비정상적으로 권한 요청 오류를 표시할 경우, 'Alt+Enter'로
//
//    }


}
