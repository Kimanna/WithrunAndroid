package com.example.withrun

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import kotlinx.android.synthetic.main.activity_room_detail.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.internal.wait
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class RoomDetail : AppCompatActivity() {

    val TAG: String = "RoomDetail"



//    private var mHandler: Handler? = null
//    private var html = "" // java server 에서 보내준 값을 찍어보기 위한 변수

    companion object {

        var intoUserId: Int = 0
        var roomNo2: Int = 0
        var distance: String = ""
        var longStartTime: String = "" // 해당 룸 시간
        var aloneMode: Int = 0 // 0 은 모두 뛰기, 1은 혼자뛰기 모드
        var roomTitle: String = ""
        var roomImgPath: String = ""
        var sortGender: String = ""
        var memberCountV: Int = 0

        var roomManager: Int? = null
        var intoUser = false // 해당룸에 참여중인지 체크하는 변수

        var jsonArray: JSONArray? = null


        var socket: Socket? = null

        var out: PrintWriter? = null
        var networkReader: BufferedReader? = null
        var networkWriter: BufferedWriter? = null

        val listChat: MutableList<Message> = mutableListOf()
        var adapterChat: ChatMessageAdapter? = null

        var currentServerTime: Long = 0

        var baseLocation: String = ""
    }


    val list = ArrayList<RoomIntoUser>()
    var adapter: ProfileImgAdapter? = null


    var notification = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_detail)


        var intent = intent
        if (!TextUtils.isEmpty(intent.getStringExtra("location"))) {
            roomNo2 = intent.getIntExtra("roomNo", 0)
            baseLocation = intent.getStringExtra("location")
            Log.d(TAG, "getIntent 결과 : " + intent.getStringExtra("location"))
            Log.d(TAG, "getIntent 결과 : " + roomNo2)


                // object 초기화 코드
                listChat.clear()
                intoUser = false

                // 해당 룸데이터와, 참가인원 조회
                coroutineGetRoomInfo(roomNo2, 0)


        }
    }

    fun getRoomData(): Int? {
        return roomNo2
    }

    fun getInOutRoom(roomNo: Int, intoUserId: Int, action: String) {

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                out = PrintWriter(networkWriter!!, true)
                out!!.println("$roomNo/$intoUserId/$action")
                out!!.flush()

            }.await()

        }
    }


    fun coroutineGetRoomInfo(roomNo: Int, aloneMode: Int) {

        CoroutineScope(Dispatchers.Main).launch {
            this
            val roomInfo = CoroutineScope(Dispatchers.Default).async { this
                // network
                getRoomInfo(roomNo, aloneMode)
            }.await()
            Log.d(TAG, roomInfo)

            val jsonObject = JSONObject(roomInfo)
            val existResult = jsonObject.getString("existResult")
            currentServerTime = oftenUseMethod.timeStringToLongSeoul(jsonObject.getString("currentServerTime"))


            if (existResult == "0") {

            } else {
                jsonArray = jsonObject.getJSONArray("result")
                Log.d(TAG, "643 after jsonArray ")

                setRoomData(jsonArray!!)

            }
        }
    }

    fun getRoomInfo(roomNo: Int, aloneMode: Int): String {
        Log.d(TAG, "665 getRoomInfo ")

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder()
            .url(Constants.URL + "/rooms/number?roomNo=$roomNo&aloneMode=$aloneMode").build()
        client.newCall(req).execute().use { response ->
            return if (response.body != null) {
                response.body!!.string()
            } else {
                "body null"
            }
        }
    }

    fun setRoomData(jsonArray: JSONArray) {
        Log.d(TAG, "setRoomData ()")

        val roomDataObject = jsonArray.getJSONObject(0) // 룸데이터
        val startTime = roomDataObject.getString("startTime")


        distance = roomDataObject.getString("distance")
        longStartTime = roomDataObject.getString("startDate").substring(0, 10) + " " + startTime
        aloneMode = roomDataObject.getInt("aloneMode")
        roomTitle = roomDataObject.getString("roomTitle")
        roomImgPath = roomDataObject.getString("roomImgPath")
        sortGender = roomDataObject.getString("sortGender")
        sortGender = roomDataObject.getString("sortGender")
        memberCountV = roomDataObject.getInt("memberCount")

        roomTitleToolbar.text = roomTitle

        for (i in 0 until jsonArray.length()) { // 현재 해당 룸에 참여하고 있는지 확인

            if (MainActivity.loginId == jsonArray.getJSONObject(i).getInt("id")) {
                intoUser = true
                break
            }
        }
        if (intoUser) { // 해당룸에 참여중인경우 socket 연결
            connectSocket()
        }

        Log.d(TAG, "longStartTime 출력해봄 "  + longStartTime)


        // fragment convert
        connectFragment ()

    }

    fun connectSocket() {
        try {
            coroutine(JavaServerAPI.ip, JavaServerAPI.port)
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
    }

    fun coroutine(ip: String, port: Int) {
        Log.d(TAG, "coroutine   229 line ")

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

            getInOutRoom (roomNo2, MainActivity.loginId, "intoRoom")


        }
    }

    // fragment 연결하는 adapter
    fun connectFragment () {
        Log.d(TAG, "connectFragment() " )

        val roomPagerAdapter = RoomFragmentAdapter(supportFragmentManager)
        roomViewPager.adapter = roomPagerAdapter
        roomTab.setupWithViewPager(roomViewPager)
        roomViewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled( position: Int, positionOffset: Float, positionOffsetPixels: Int ) { }

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    Log.d(TAG, "onPageSelected if(0) " +  position.toString())
                } else {
                    Log.d(TAG, "onPageSelected else " + position.toString())

                    if ( intoUser ) {


//                        roomPagerAdapter.getItem(position).onAttach(baseContext)


//                        val fm: FragmentManager = supportFragmentManager
//                        val fragment: RoomChatFragment = (fm.findFragmentById(R.id.roomChatFragment) as? RoomChatFragment)!!
//                        fragment.changeCover()



                        // 룸 메시지 데이터 가져옴
                        coroutineGetMessage (roomNo2, MainActivity.loginId)

                    }

                }
            }

            override fun onPageScrollStateChanged(state: Int) { }
        })

    }

    fun coroutineGetMessage (roomNo: Int, meId: Int) {

        CoroutineScope(Dispatchers.Main).launch { this
            val roomMessage = CoroutineScope(Dispatchers.Default).async { this
                getRoomMessage (roomNo, meId)
            }.await()
            Log.d(TAG, "coroutineGetMessage 메시지 데이터 확인" + roomMessage)

            val jsonObject = JSONObject(roomMessage)
            val existResult = jsonObject.getString("existResult")

            if (existResult == "0") {

            } else {
                val jsonArray = jsonObject.getJSONArray("result")

                addChatMessage(jsonArray)

            }
        }
    }

    fun addChatMessage(jsonArray: JSONArray?) {
        Log.d(TAG, "addChatMessage ()" + jsonArray)

        listChat.clear()

        val mFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd kk:mm:ss")

        for (i in 0 until jsonArray!!.length()) {


            var messageObj = Message()

            var messageTimeToLong = mFormat.parse(jsonArray.getJSONObject(i).getString("send_at")).time
            messageObj.setMessageDate(messageTimeToLong)
            messageObj.setMessageText(jsonArray.getJSONObject(i).getString("message"))
            messageObj.setNickname(jsonArray.getJSONObject(i).getString("sendUserNickname"))
            messageObj.setProfileImgUrl(jsonArray.getJSONObject(i).getString("sendUserProfileImg"))

            /*
            CharMessageAdapter 안에 변수, 메시지 구분
            val TYPE_1 = 0 // 내가보낸 메시지
            val TYPE_2 = 1 // 남이보낸 메시지
            val TYPE_3 = 2 // 알림 메시지
            */

            // 참가 or 퇴장 메시지 인 경우 id = 0 으로 들어옴
            if ( jsonArray.getJSONObject(i).getInt("id") == 0 ) {

                messageObj.setMessagetype(2)


            // 현재 로그인 중인 유저가 보낸 메시지 인 경우
            } else if ( jsonArray.getJSONObject(i).getInt("id") == MainActivity.loginId ) {

                messageObj.setMessagetype(0)


            // 다른 사람이 보낸 메시지 인 경우
            } else {
                messageObj.setMessagetype(1)

            }
            listChat.add(messageObj)
        }

        adapterChat?.notifyDataSetChanged()

    }

    fun getRoomMessage (roomNo: Int, meId: Int): String {
        Log.d(TAG, "getRoomMessage " + roomNo + ' ')

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder()
            .url(Constants.URL + "/rooms/roomMessage?roomNo=$roomNo&userId=$meId").build()
        client.newCall(req).execute().use { response ->
            return if (response.body != null) {
                response.body!!.string()
            } else {
                "body null"
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // --------------------------------------------------------------------------------뒤로가기 아이콘 클릭시
        back_Home.setOnClickListener {
            // 해당방을 나갈 시 roomMembers 에서 해당 userId 제거 후 홈 액티비티로 이동
            goBack ()
        }
    }



    // 백버튼을 클릭시, 화면 나갈시 roomMembers 에서 해당 userId 제거 후 홈 액티비티로 이동
    override fun onBackPressed() {
        super.onBackPressed()

        goBack ()

    }

    fun goBack () {

        when (baseLocation) {
            "Home" -> {// 플래그 필요없음

                val intent = Intent(this, Home::class.java)
                intent.putExtra("location","RoomDetail")
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()


            }
            "Notification" -> { // 플래그 추가

                val intent = Intent(this, Notification::class.java)
                intent.putExtra("location","RoomDetail")
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()

            }

            "CreateRoom" -> {// 플래그 필요없음

                val intent = Intent(this, Home::class.java)
                intent.putExtra("location","RoomDetail")
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()


            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {

            networkWriter?.close()
            socket?.close()

//            checkUpdate.interrupt()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

//    fun newLauncherIntent(context: Context?): Intent? {
//        val intent = Intent(context, MainActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        intent.action = Intent.ACTION_MAIN
//        intent.addCategory(Intent.CATEGORY_LAUNCHER)
//        return intent
//    }
//
//
//    fun backHome () {
//
//        val dlg: AlertDialog.Builder = AlertDialog.Builder(this,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
//        dlg.setMessage("해당 룸을 나가시면 해당 룸의 경기를 진행할 수 없습니다. \n" +
//                "현재 룸을 나가시겠습니까?") // 메시지
//        dlg.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
//

//            // 유저 실시간 나감 - java 통신
//            getInOutRoom (roomNo2, intoUserId, "exitRoom")
//            // roomMember 테이블에서 유저데이터 지워줌
//            coroutineExitRoom (roomNo2.toInt() , MainActivity.loginId)
//            // home 으로 이동 intent
//            intentHome ()
//
//        })
//        dlg.setNegativeButton("취소", null)
//        dlg.show()
//    }
//
//    fun startGameNoti() {
//        Log.d(TAG,"startGameNoti ()")
//
//        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
//            .setContentTitle("경기시작 10초 전")
//            .setStyle(NotificationCompat.BigTextStyle()
//                .bigText("현재 참여중인 경기가 있습니다, 경기시작을 위해 준비해 주세요."))
//            .setVibrate(longArrayOf(1000,1000,1000,1000))
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            .setAutoCancel(true)
//                .setOnlyAlertOnce(true)
//
//
//
//        val paddingIntent1 = PendingIntent.getActivity(this, 0, newLauncherIntent(this), 0)
//
//        builder.setContentIntent(paddingIntent1)
//        createNotificationChannel(builder, notificationId)
//
//    }


}

