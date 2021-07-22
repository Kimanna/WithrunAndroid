package com.example.withrun

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.content.LocalBroadcastManager.getInstance
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_running_active.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class RunningActive : AppCompatActivity(), OnMapReadyCallback {

    val TAG: String = "RunningActive"

    companion object {
        var list = ArrayList<RoomIntoUser>()

    }

    private val REQUEST_ACCESS_FINE_LOCATION = 1000

    lateinit var mMap: GoogleMap

    var roomNo = 0
    var roomDistanceMT = 0

    var stopwatchTotal: Long = 0 // 러닝 종료 시간 ( 단위 sec )

    var runningRankAdapter: ActiveRankingAdapter? = null

    var marker: Marker? = null
    var polylineOptions = PolylineOptions().width(7F).color(R.color.teal_700).geodesic(true)


    var stopwatch = 0
    var totaldistance = 0
    var pace: Long = 0
    var currentRanking:Int = 0

    var isBackground: Boolean = false
    var isFinishRace: Boolean = false


    // 경기가 종료된 후 최종적으로 기록을 저장하기 위해 선언한 변수 ( 앱이 background에 있다가 foreground로 돌아올때 사용하기위함 )
    var curTimeString = ""
    var finishRaceTime = ""
    var finishRaceDistance = ""
    var finishRacePace = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_running_active)


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        var intent = intent
        if (!TextUtils.isEmpty(intent.getStringExtra("location"))) {
            var location = intent.getStringExtra("location")
            roomNo = intent.getIntExtra("roomNo", 0)
            Log.d(TAG, "getIntent 결과 : " + roomNo + " " +location)


            // notification 을 통해 접근한 경우 case1 러닝 종료 case2 activity 띄우기 ----------------------------------- 구현 고민중
            if ( location == "notification" ) {
                Log.d(TAG, " if ( location == \"notification\" ) if문 안쪽으로 들어왔는지 확인 " + roomNo)

                recyclerView (list)


                for ( i  in 0 until list.size ) {
                    if ( list.get(i).getId() == MainActivity.loginId ) {
                        currentRanking = i+1
                        break
                    }
                }

                recyclerviewUser(currentRanking)
                runningTotalMember.text = "("+list.size+")"


            } else {
                Log.d(TAG, "RunningActive 시작시 else 문 안쪽으로 들어왔는지 확인 " + roomNo)

                stopwatchTotal = SystemClock.elapsedRealtime() + 100 * 1000 // 1분 페이스로 시간단위 맞춰줌

                // 러닝 시작 시 유저 입장중인 상태로 (activeRunState = 1) 업데이트 후 해당 룸 데이터와 룸에 참여하고있는 유저리스트를 가져오는 부분
                getRunningMember (roomNo)

            }



        }


        // service 에서 보낸 message 받는 부분
        getInstance(this).registerReceiver(
            mAlertReceiver, IntentFilter("MoveServiceFilter")
        )

        goMyRaceHistoryDetail.setOnClickListener {

            sendMessage (0, "leaveRoom")
            goRaceHistoryDetail () // intent 이동
        }

        // 경기 완주를 포기하는 버튼 클릭 시 현재까지 이동한 위치 snapshot 과 경주 기록을 저장
        giveupRaceBT.setOnClickListener {

            val dlg: android.support.v7.app.AlertDialog.Builder = android.support.v7.app.AlertDialog.Builder(this,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
            dlg.setMessage("경기를 중단하고 방을 나가시겠습니까?") // 메시지
            dlg.setPositiveButton("방 나가기", DialogInterface.OnClickListener { dialog, which ->
                Log.d(TAG, "setPositiveButton : " )

                // google map snapshot 과 함께 유저 경기 현재까지의 기록 저장 통신 메서드
                snapShot("giveUpRace", Date(System.currentTimeMillis()).toString())

                // service 종료
                sendMessage (0, "stopService")

            })

            dlg.setNegativeButton ("경기 계속하기", DialogInterface.OnClickListener { dialog, which->

            })
            dlg.show()

        }


    }


    fun snapShot(action: String, gameFinishTime: String) {

        val builder = LatLngBounds.Builder()
        var latLngInclude: LatLng

        Log.d(TAG, "Latlng.size " + MyService.LatLng.size)

        for (i in 0 until MyService.LatLng.size-1)  {

            latLngInclude = LatLng( MyService.LatLng[i].latitude, MyService.LatLng[i].longitude ) // in this line put you lat and long
            builder.include(latLngInclude) //add latlng to builder
        }

        val bounds = builder.build()
        val padding = 10 // offset from edges of the map in pixels
        val cu: CameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)

        if (marker != null ) {
            marker!!.remove()
        }


        mMap.moveCamera(cu)

        val callback: GoogleMap.SnapshotReadyCallback = object : GoogleMap.SnapshotReadyCallback {

            var bitmap: Bitmap? = null
            override fun onSnapshotReady(snapshot: Bitmap?) {
                Log.d(TAG, "onSnapshotReady ")

                bitmap = snapshot
                Log.d(TAG, "bitmap " )
                encodeTobase64(bitmap)?.let { coroutineSaveRaceRecord (action,it, gameFinishTime) }

            }
        }
        mMap.snapshot(callback)
    }

    fun encodeTobase64(image: Bitmap?): String? {
        val baos = ByteArrayOutputStream()
        image?.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }


    fun startService () {

        // service 에서 러닝 timer 시작
        intent = Intent(this, MyService::class.java)
        intent.putExtra("stopwatch", stopwatchTotal)
        intent.putExtra("goalDistance", roomDistanceMT)
        intent.putExtra("roomNo", roomNo)

        startService(intent)

   }


    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "Activity onMapReady ")

        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(this,  Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,  Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            return
        }

//        mMap.isMyLocationEnabled = true
    }


    private val mAlertReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {


            if (!TextUtils.isEmpty(intent.getStringExtra("action"))) { // action 값이 없는 경우는 없음
                var action = intent.getStringExtra("action")
                var userState = intent.getStringExtra("state")

                stopwatch = intent.getIntExtra("stopwatch", 0)
                totaldistance = intent.getIntExtra("totalDistance", 0)
                pace = intent.getLongExtra("avgPace", 0)
                currentRanking = intent.getIntExtra("currentRanking", 0)
                Log.d( TAG,"service 에서 activity로 전달주는 값 " + action+ stopwatch + " " + totaldistance + " " + pace + " " + currentRanking + userState)

                // intoRace 유저가 입장한 경우 exitRoom 유저가 경기를 중도 포기한 경우
                // distanceGap 유저의 경주 차이가 발생한 경우, finishRace 다른유저가 경주를 중도포기한 경우 recyclerview 갱신만수행 (arraylist 정렬은 service에서 완료)
                if (action == "intoRace" || action == "exitRoom" || action == "distanceGap" || action == "finishRace") {
                    recyclerviewUser(intent.getIntExtra("currentRanking",0))

                } else {

//                  LatLng = intent.getParcelableArrayListExtra<LatLng>("latLng")

                    var distanceKm = (totaldistance / 100)
                    var distanceMT = oftenUseMethod.distanceDigitString((totaldistance).toLong())

                    var runProgressTimeText = (oftenUseMethod.twoDigitString((stopwatch / (60 * 60) % 24).toLong()).toString() + " : " + oftenUseMethod.twoDigitString((stopwatch / 60 % 60).toLong()) + " : "
                                + oftenUseMethod.twoDigitString((stopwatch % 60).toLong()))
                    var runningDistanceText = distanceKm.toString() + "." + distanceMT + " (km)"
                    var averagePaceText = oftenUseMethod.secondsToTimeOverMinuteAddHour(pace)


                    runProgressTime.text = runProgressTimeText
                    runningDistance.text = runningDistanceText

                    if (distanceMT!!.toInt() != 0) { // 총 러닝 거리가 0 이 아닌경우에만 평균페이스를 표기

                        averagePace.text = averagePaceText
                    }

                    markerAndPolyline()

                    if ( userState == "경기완료" ) {
                        Log.d( TAG,"if if ( userState == \"경기완료\" ) 지나감 userState 값 " + userState)


                        recyclerviewUser(intent.getIntExtra("currentRanking",0))
                        val currentTimeStr = Date(System.currentTimeMillis()).toString()

                        curTimeString = currentTimeStr
                        finishRaceTime = runProgressTimeText
                        finishRaceDistance = runningDistanceText
                        finishRacePace = averagePaceText

                        // 경기 완료된 시점에 앱이 background 상태라면 화면이 켜졌을때 데이터 저장
                        if (isBackground) {

                            isFinishRace = true

                        } else { // 앱이 background 상태가 아니라면 기록저장 및 요약 보여줌

                            snapShot("finishRace", currentTimeStr)

                        }

                    }

                }
            }
        }
    }

    fun isBackgroundScreenOn (currentTimeStr: String, runProgressTimeText: String, runningDistanceText: String, averagePaceText: String) {

        val intentFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        intentFilter.addAction(Intent.ACTION_SCREEN_ON)
        val receiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent!!.action
                Log.d("Test", "receive : $action")

                when (action) {
                    Intent.ACTION_SCREEN_ON -> {

                        if (isFinishRace) {
                            snapShot("finishRace", currentTimeStr)
                            showSummary(runProgressTimeText, runningDistanceText, averagePaceText, currentRanking)
                        }

                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        // do something
                    }
                }
            }
        }
        registerReceiver(receiver, intentFilter)

    }

    fun showSummary (runProgressTimeText: String, runningDistanceText: String, averagePaceText: String, currentRank: Int) {


        val dialog = AlertDialog.Builder(this).create()
        val edialog : LayoutInflater = LayoutInflater.from(this)
        val mView : View = edialog.inflate(R.layout.activity_race_score,null)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        val totalTime : TextView = mView.findViewById(R.id.totalTime)
        val totalDistance1 : TextView = mView.findViewById(R.id.totalDistance1)
        val totalPace : TextView = mView.findViewById(R.id.totalPace)
        val myRanking : TextView = mView.findViewById(R.id.myRanking)

        val keepWatching : Button = mView.findViewById(R.id.keepWatching)
        val leavingRoom : Button = mView.findViewById(R.id.leavingRoom)


        totalTime.text = runProgressTimeText
        totalDistance1.text = runningDistanceText
        totalPace.text = averagePaceText
        myRanking.text = "$currentRank 위"


        // 경기 계속 관람하기 버튼
        keepWatching.setOnClickListener(View.OnClickListener { view ->

            giveupRaceBT.visibility = View.GONE
            goMyRaceHistoryDetail.visibility = View.VISIBLE

            recyclerviewUser(currentRank)

            dialog.dismiss()
            dialog.cancel()
        })

        // 룸 나가기 버튼
        leavingRoom.setOnClickListener(View.OnClickListener { view ->

            // service 종료
            sendMessage (0, "leaveRoom")

            goRaceHistoryDetail ()
            dialog.dismiss()
            dialog.cancel()
        })

        dialog.setCancelable(false);
        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }



    private fun sendMessage (myRunningRank: Int, action: String) {
        Log.d(TAG, "RunningActive 에서 sendMessage 지나감 ")

        val intent = Intent("MoveActivityFilter")
        intent.putExtra("myRunningRank", myRunningRank)
        intent.putExtra("action", action)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun markerAndPolyline () {

        if (marker != null ) {
            marker!!.remove()
        }

        if (MyService.LatLng.size > 0) {
            var markerPosition = LatLng(MyService.LatLng.last().latitude, MyService.LatLng.last().longitude)
            marker = mMap.addMarker(
                MarkerOptions()
                    .position(markerPosition)
                    .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(R.drawable.marker_circle_point_start)))
            )

            // 지도 중앙을 현재 위치로 옮겨줌
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MyService.LatLng.last(), 17.0f))

            // 이동경로 라인 그리기 (위치 정보가 갱신되면 polyLineOptions 객체에 추가되고 지도에 polylineOptions 객체를 추가 함
            polylineOptions.add(LatLng(MyService.LatLng.last().latitude,MyService.LatLng.last().longitude))
            mMap.addPolyline(polylineOptions)
        }
    }


    private fun getBitmap(drawableRes: Int): Bitmap? {
        val drawable: Drawable = resources.getDrawable(drawableRes)
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.getIntrinsicWidth(),
            drawable.getIntrinsicHeight(),
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight())
        drawable.draw(canvas)
        return bitmap
    }

    //    private val mConnection: ServiceConnection = object : ServiceConnection {
//        override fun onServiceConnected(name: ComponentName, service: IBinder) {
//            val binder = service as MyService.MyBinder
//            mService = binder.service
//            mBound = true
////            speed.text = mService?.getmCount().toString()
//
//        }
//        override fun onServiceDisconnected(name: ComponentName) {
//            // 얘기치 않은 종료시에 실행
//        }
//    }


    fun goRaceHistoryDetail () {

        MyService.LatLng.clear()
        list.clear()

        val intent = Intent(this, MyRaceHistoryDetail::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("location","runningActive")
        intent.putExtra("roomNo",roomNo)
        intent.putExtra("userId",MainActivity.loginId)
        startActivity(intent)
        finish()
    }


    fun recyclerView (list: ArrayList<RoomIntoUser>) {
//        Log.d(TAG, "recyclerView 에서 list size 확인 : " + list.size + " " + list.get(0).getNickname())

        runningRankAdapter = ActiveRankingAdapter(this, list) {

        }
        rvActiveRanking.layoutManager = LinearLayoutManager(this)
        rvActiveRanking.adapter = runningRankAdapter

    }

    fun recyclerviewUser (currentRank: Int) {

        if (currentRank == 0) {
            runningRank.text = " - "
        } else {
            runningRank.text = currentRank.toString() + " 위"
        }
        rvActiveRanking.adapter?.notifyDataSetChanged()

    }

    fun coroutineSaveRaceRecord (action: String, mapAddPolylineImg: String, gameFinishTime: String) {
        Log.d(TAG, "coroutineSaveRaceRecord : ")

        val progressDialog = ProgressDialog(this)
        showProgressBar (progressDialog)


        CoroutineScope(Dispatchers.Main).launch {this
            val roomInfo = CoroutineScope(Dispatchers.Default).async { this
                saveRaceRecord(action, mapAddPolylineImg, gameFinishTime)
            }.await()
            Log.d(TAG, roomInfo)


            dismissProgressBar(progressDialog)

            when(action){

                "giveUpRace" -> {

                    goRaceHistoryDetail () // intent 이동

                }

                "finishRace" -> {

                  showSummary(finishRaceTime, finishRaceDistance, finishRacePace, currentRanking)

              }
            }


        }
    }

    fun saveRaceRecord (action: String, encodeImage: String, gameFinishTime: String): String {

        val jsonObject = JSONObject()

        var completed = 0
        var activeRunState = 0
        var myRanking = 0 // 랭킹이 없다고 간주함

        if ( action == "giveUpRace" ) {// 경기를 도중에 포기한 경우 미완주를 의미하는 2로 저장
            completed = 2
            activeRunState = 2

            jsonObject.putOpt("myRanking", myRanking)
        } else if ( action == "finishRace" ) { // 완주한 경우 완주를 표시하는 값인 1로 저장
            completed = 1
            activeRunState = 0

            jsonObject.putOpt("myRanking", currentRanking)
        }

        jsonObject.putOpt("action", action)
        jsonObject.putOpt("roomNo", roomNo)
        jsonObject.putOpt("userId", MainActivity.loginId)
        jsonObject.putOpt("gameFinishTime", gameFinishTime)
        jsonObject.putOpt("raceTime", stopwatch)
        jsonObject.putOpt("distance", totaldistance)
        jsonObject.putOpt("completed", completed)
        jsonObject.putOpt("activeRunState", activeRunState)
        jsonObject.putOpt("mapSnapshot", encodeImage)

        Log.d(TAG, "서버 저장할 데이터 확인" + jsonObject.toString())

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



    fun getRunningMember (roomNo: Int) {

        CoroutineScope(Dispatchers.Main).launch { this
            val roomInfo = CoroutineScope(Dispatchers.Default).async { this
                getRunMember(roomNo)
            }.await()
            Log.d(TAG, roomInfo)

            val jsonObject = JSONObject(roomInfo)
            val existResult = jsonObject.getString("existResult")

            if (existResult == "0") {

            } else {
                val jsonArray = jsonObject.getJSONArray("result")

                setRunningMember (jsonArray)

            }
        }
    }

    fun getRunMember(roomNo: Int): String {
        Log.d(TAG, "665 getRoomInfo ")

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder()
            .url(Constants.URL + "/rooms/runningMember?roomNo=$roomNo&userId=${MainActivity.loginId}").build()
        client.newCall(req).execute().use { response ->
            return if (response.body != null) {
                response.body!!.string()
            } else {
                "body null"
            }
        }
    }

    fun setRunningMember (jsonArray: JSONArray) {
        Log.d(TAG, "setRunningMember ()")

        // 러닝 총 멤버 표시
        runningTotalMember.text = "("+jsonArray.length().toString()+")"
//        roomDistanceMT = jsonArray.getJSONObject(0).getInt("distance") * 100 // km 를 10m 단위로 바꿔주기위해 x100

        roomDistanceMT = 10 // ------------------------------------------------------------------------------------------- test 하기 위한 값

        for (i in 0 until jsonArray.length()) { // 현재 해당 룸에 참여하고 있는지 확인
            Log.d(TAG, "intoUser 값 확인 " + MainActivity.loginId + " 방에 참여중인 user id확인" + jsonArray.getJSONObject(i).getInt("id"))

            var user = RoomIntoUser()

            user.setId(jsonArray.getJSONObject(i).getInt("id"))
            user.setNickname(jsonArray.getJSONObject(i).getString("nickname"))
            user.setProfileImgUrl(jsonArray.getJSONObject(i).getString("profileImgPath"))
            user.setRunningState(jsonArray.getJSONObject(i).getInt("activeRunState"))
            user.setMyActiveRaceRank(jsonArray.getJSONObject(i).getInt("myRanking"))
            user.setDistanceGap(roomDistanceMT)
            list.add(user)

        }

        recyclerView (list)
        startService ()

    }



    private fun permissionCheck(cancel: () -> Unit, ok: () -> Unit) {   // 전달인자도, 리턴값도 없는
        // 두 개의 함수를 받음

        if (ContextCompat.checkSelfPermission(this,                  // 권한이 없는 경우
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {       // 권한 거부 이력이 있는 경우

                cancel()

            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_ACCESS_FINE_LOCATION)
            }
        } else {                                                    // 권한이 있는 경우
            ok()
        }
    }

//    private  fun showPermissionInfoDialog() {
//
//        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
//        builder.setTitle("위치 서비스 비활성화")
//        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n 위치 설정을 수정하실래요?".trimIndent())
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

//    // 권한 요청 결과 처리
//    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<String>,  grantResults: IntArray ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        when (requestCode) {
//            REQUEST_ACCESS_FINE_LOCATION -> {
//                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                    addLocationListener()
//                } else {
//                    Toast.makeText(this,"권한이 거부 됨", Toast.LENGTH_SHORT).show()
//                }
//                return
//            }
//        }
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
//        MyService.fusedLocationProviderClient.requestLocationUpdates(MyService.locationRequest, MyService.locationCallback, null)  // 혹시 안드로이드 스튜디오에서 비정상적으로 권한 요청 오류를 표시할 경우, 'Alt+Enter'로
//
//    }


    // 뒤로가기 버튼 막음
    override fun onBackPressed() {
    }


    private fun showProgressBar(progressDialog: ProgressDialog) {
        progressDialog.setTitle("러닝 기록 저장")
        progressDialog.setMessage("러닝 기록을 저장하는 중 입니다.")
        progressDialog.show()
    }

    private fun dismissProgressBar(progressDialog: ProgressDialog) {
        progressDialog.dismiss()
    }

    override fun onStart() {
        super.onStart()

        isBackground = false

//        intent = Intent(this, MyService::class.java)
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE) // bind_auto_create flag 는 서비스를 자동으로 생성해주고 bind 까지 해주는 flag

    }
//
    override fun onResume() {
        super.onResume()

    if (isFinishRace) {

        snapShot("finishRace", curTimeString)

    }


    }

    override fun onRestart() {
        super.onRestart()

        // 권한 요청
//        permissionCheck(
//            cancel = { showPermissionInfoDialog() },   // 권한 필요 안내창
//            ok = { addLocationListener()}      // ③   주기적으로 현재 위치를 요청
//        )
    }

    override fun onStop() {
        super.onStop()

        isBackground = true

    }


//
//    override fun onDestroy() {
//        super.onDestroy()
//
//        if (mBound) {
//            unbindService(mConnection)
//            mBound = false
//        }
//
//        networkWriter?.close()
//        socket?.close()
//
//        checkUpdate1.interrupt()
//
//        if (tts != null) {
//            tts!!.stop()
//            tts!!.shutdown()
//        }
//    }
}