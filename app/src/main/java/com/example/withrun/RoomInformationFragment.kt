package com.example.withrun

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context.ALARM_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.fragment_room_information.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class RoomInformationFragment : Fragment(), View.OnClickListener {

    val TAG: String = "RoomInformationFragment"

    val list = ArrayList<RoomIntoUser>()
    var adapter: ProfileImgAdapter? = null

    var alarmManager: AlarmManager? = null
    var pendingIntent: PendingIntent? = null

    lateinit var startGametimer: CountDownTimer
    var isRunningStartGametimer = false // CountDownTimer 가 실행 중인지 check 하는 boolean

    val mFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd kk:mm:ss")

    var updateActiveRunState = false


    var longStartTimeAlarm: Long? = null

    fun newInstant(): RoomInformationFragment {
        val args = Bundle()
        val frag = RoomInformationFragment()
        frag.arguments = args

        Log.d(TAG, "newInstant RoomInformationFragment 지난후 ")

        return frag
    }

    override fun onCreateView(  inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?  ): View? {
        val view = inflater.inflate(R.layout.fragment_room_information, container, false)
        Log.d(TAG, "fragment onCreateView 메서드 지난 후 ")

        val activity: RoomDetail? = activity as RoomDetail?
        val roomNo: Int? = activity?.getRoomData()
        Log.d(TAG, "acticity 에서 전달한 bundle값 확인 " + roomNo)

        longStartTimeAlarm = mFormat.parse(RoomDetail.longStartTime).time - 600000 // 러닝 시작시간 10분 전 시간을 변수에 선언해 놓음
        Log.d(TAG, "러닝 10분전 시간 출력 "+ convertLongToTime(longStartTimeAlarm!!))

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // 룸정보 setting
        setRoomData(RoomDetail.jsonArray!!)

        if (RoomDetail.baseLocation == "CreateRoom") {
            startAlarmManager() // alarmManager 러닝 10분전 notification 등록
            startAlarmManagerStartRun () // alarmManager 러닝 시작 notification
        }

    }

    override fun onStart() {
        super.onStart()

        /*러닝 시작 10분 전이거나 타이머가 작동되고있지 않은 상태라면 timer 시작*/
        if ( !isRunningStartGametimer) {
            runStartTimer() // 타이머 작동
        }

    }

    override fun onResume() {
        super.onResume()

        // 참가신청 or 참가취소 버튼 클릭 시
        participate.setOnClickListener {
            var currentTime = System.currentTimeMillis()

            if (RoomDetail.intoUser) { // 현재 방에 참가 중인 경우

                var isRoomManager = false
                for (i in 0 until list.size) { // 해당 방에 참여하고있는 user list
                    if (list[i].getRoomManager() == true && list[i].getId() == MainActivity.loginId) { // 룸 manager 인 경우
                        isRoomManager = true
                        break
                    }
                }

                if (isRoomManager) { // 방장인 경우 - 혼자인 경우는 방삭제, 여러명인경우 방장권한 양도

                    if (list.size <= 1) { // 현재 방에 혼자인 경우

                        val dlg: AlertDialog.Builder = AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                        dlg.setMessage("현재 방에 마지막 인원으로 참가취소를 할 시 방이 삭제됩니다. \n 그대로 진행 하시겠습니까?") // 메시지
                        dlg.setNeutralButton("확인", DialogInterface.OnClickListener { dialog, which ->

                            coroutineRoomIntoOut ("leaveWithDeleteRoom", MainActivity.loginId, RoomDetail.roomNo2, MainActivity.loginNickname )


                            // 룸 시작 시간 알람매니저 취소
                            cancelAlarmManager ()
                            cancelAlarmManagerStartRun()

                            goHome ()

                                // ---------------------------------------------------------------------------------------------------------------방삭제, 방에참가중인 유저삭제 코드
                            })
                        dlg.setNegativeButton("취소", null)
                        dlg.show()


                    } else { // 현재 방에 혼자가 아닌 경우

                        // 다음 방장 지목하는 창 띄워줌
                        transferManager(list)

                    }

                } else { // 방장인 아닌경우

                    val dlg: AlertDialog.Builder = AlertDialog.Builder( this.context, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth )
                    dlg.setTitle("참가취소와 해당 룸 나가기")
                    dlg.setMessage("참가취소할 시 그동안의 대화내용은 확인할 수 없습니다. \n 그래도 참가취소를 하시겠습니까?") // 메시지
                    dlg.setNeutralButton("확인", DialogInterface.OnClickListener { dialog, which ->

                        // 룸 참가 취소 인원 DB 테이블에 유저 저장 후 message 테이블에도 참가취소 내역 저장 -----------------------------------------------------------------------------------------------------------    유저 삭제 통신 코드 테스트해야함
                        coroutineRoomIntoOut ("leaveRoom", MainActivity.loginId, RoomDetail.roomNo2, MainActivity.loginNickname) // ----------------- 유저삭제 저장해야함, tcp 메시지 보내야함
                        getInOutRoom (RoomDetail.roomNo2, MainActivity.loginId, "exitRoom/${MainActivity.loginNickname} 님이 참가를 취소 하였습니다./${currentTime}/${MainActivity.loginNickname}") // HashMap 안에 socket 정보 지워줌

                        // 룸 시작 시간 알람매니저 취소
                        cancelAlarmManager ()
                        cancelAlarmManagerStartRun()

                        goHome ()

                    })
                    dlg.setNegativeButton("취소", null)
                    dlg.show()

                }
            } else { // 현재 방에 참가중이 아닌 경우 참가 신청함
                Log.d(TAG, "243 else 현재 방에 참가중이 아닌 경우 ")

                // --------------------------------------------------------------------------------------------------- 유저 참가 통신 코드 테스트 해야함

                // 룸 참가인원 DB테이블에 유저 저장 후 message 테이블에도 참가신청 내역 저장
                coroutineRoomIntoOut ("joinRoom", MainActivity.loginId, RoomDetail.roomNo2, MainActivity.loginNickname)

                connectSocket () // 소켓연결 + 참가신청 메시지 socket 발송 (getInOutRoom 메서드)
//                addMessage ("joinRoom", currentTime, MainActivity.loginNickname + " 님이 참가신청 하셨습니다.", MainActivity.loginNickname) // addMessageObject

                addUserprofile() // 참가중인 userlist 에 프로필 추가
                RoomDetail.intoUser = true

                participateBackground() // ui 변경되는 부분 참가신청 or 참가취소로 변경

                startAlarmManagerStartRun () // alarmManager 러닝 시작 notification

                // 참가신청 클릭 시간이 러닝 시작 10분전 보다 이전이면 러닝시작 10분전을 알려주는 alarmManager 작동
                if ( longStartTimeAlarm!! > System.currentTimeMillis()) {
                    startAlarmManager() // alarmManager 러닝 10분전 notification 등록
                }
            }
        }
    }


    // 현재시간과
    fun runStartTimer() {

        val longStartTime2 = mFormat.parse(RoomDetail.longStartTime).time
        val diffTime = longStartTime2 - RoomDetail.currentServerTime
        Log.d(TAG, "runStartTimer() 타이머 시작 서버시간 "+RoomDetail.currentServerTime+" 룸시간"+longStartTime2+" 두 시간간의 차이"+diffTime)

        startGametimer = object : CountDownTimer(diffTime, 1000) {
            @SuppressLint("ResourceAsColor")
            override fun onTick(millisUntilFinished: Long) {
                isRunningStartGametimer = true

                if ( longStartTimeAlarm!! < System.currentTimeMillis()) {
                    timertitle.visibility = View.VISIBLE // "경기 시작까지 남은 시간" 문구 textView
                    timer.visibility = View.VISIBLE
                }

                if (timer != null) {

                    timer.text = (oftenUseMethod.twoDigitString((millisUntilFinished / (1000 * 60 * 60) % 24)).toString() + " : " + oftenUseMethod.twoDigitString(
                        (millisUntilFinished / (1000 * 60) % 60)  ) + " : " + oftenUseMethod.twoDigitString(millisUntilFinished / 1000 % 60))

                }


                // 경기시간전 10초 가 되면 timer 컬러를 red 로 변경, 경기가 곧 시작됨 안내문구를 띄워줌
                if ( millisUntilFinished <= 11000 ) {

                    // 경기 입장으로 변경
                    if (millisUntilFinished <= 3000 && !updateActiveRunState) {
                        updateActiveRunState = true

                        coroutineActiveRunState(RoomDetail.roomNo2)
                        cancelAlarmManagerStartRun ()

                    }

                        participate.visibility = View.GONE // 참가신청 & 참가취소 버튼
                        countdown10.visibility = View.VISIBLE
                        view2.visibility = View.VISIBLE
                        view3.visibility = View.VISIBLE
                        startGameAlert.visibility = View.VISIBLE

                        getInOutRoom (RoomDetail.roomNo2, MainActivity.loginId, "startRun") // HashMap 안에 socket 정보 지워줌

                        countdown10.text = oftenUseMethod.twoDigitString(millisUntilFinished / 1000 % 60)
                }
            }

            // 타이머가 끝나면 경기 activity로 이동
            override fun onFinish() {
                isRunningStartGametimer = false

                // 해당 룸은 running 중으로 표시하여 대기실에서 입장할 수 없도록 변경 ( activeGame = 1 로 변경 )
                changeActiveRoom ()

                // 타이머가 끝나면 참여신청한 유저는 러닝화면으로 이동 / 참여신청하지 않은 유저는 홈화면으로 이동
                if (RoomDetail.intoUser) {
                    goRunningActive ()
                } else {
                    val dlg: android.support.v7.app.AlertDialog.Builder = android.support.v7.app.AlertDialog.Builder(context!!,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                    dlg.setMessage("경주기 시작된 방으로 현재 참여가 불가합니다.") // 메시지
                    dlg.setNeutralButton("확인", DialogInterface.OnClickListener { dialog, which ->
                        goHome()
                    })
                    dlg.show()
                }
            }
        }
        startGametimer.start()
    }

    fun goRunningActive () {

        val intent = Intent(this.context, RunningActive::class.java)
        intent.putExtra("roomNo", RoomDetail.roomNo2)
        intent.putExtra("location","RoomDetail")
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        activity!!.finish()

    }

    fun goHome () {

        val intent = Intent(this.context, Home::class.java)
        intent.putExtra("roomNo", RoomDetail.roomNo2)
        intent.putExtra("location","RoomDetail")
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        activity!!.finish()

    }

    fun startAlarmManager () {
        Log.d(TAG, "startAlarmManager () ")

        alarmManager = activity!!.getSystemService(ALARM_SERVICE) as AlarmManager

        var intent = Intent(this.context, AlarmReceiver::class.java)  // 1 여기서 object 를 set 하면 됨
        intent.putExtra("roomNo", RoomDetail.roomNo2)
        intent.putExtra("longStartTime", RoomDetail.longStartTime)

        pendingIntent = PendingIntent.getBroadcast( this.context, AlarmReceiver.NOTIFICATION_ID, intent, PendingIntent.FLAG_ONE_SHOT)
        alarmManager!!.setExactAndAllowWhileIdle( AlarmManager.RTC_WAKEUP, longStartTimeAlarm!!, pendingIntent )

    }

    fun cancelAlarmManager () {
        Log.d(TAG, "cancelAlarmManager () ")

        alarmManager = activity!!.getSystemService(ALARM_SERVICE) as AlarmManager

        var intent = Intent(this.context, AlarmReceiver::class.java)  // 1 여기서 object 를 set 하면 됨
        intent.putExtra("roomNo", RoomDetail.roomNo2)
        intent.putExtra("longStartTime", RoomDetail.longStartTime)

        pendingIntent = PendingIntent.getBroadcast( this.context, AlarmReceiver.NOTIFICATION_ID, intent, PendingIntent.FLAG_ONE_SHOT)
        alarmManager!!.cancel( pendingIntent )

    }

    fun startAlarmManagerStartRun () {
        Log.d(TAG, "startAlarmManagerStartRun () ")

        alarmManager = activity!!.getSystemService(ALARM_SERVICE) as AlarmManager

        var intent = Intent(this.context, AlarmReceiverStartRun::class.java)  // 1 여기서 object 를 set 하면 됨
        intent.putExtra("roomNo", RoomDetail.roomNo2)
        intent.putExtra("longStartTime", RoomDetail.longStartTime)

        pendingIntent = PendingIntent.getBroadcast( this.context, AlarmReceiverStartRun.NOTIFICATION_ID, intent, PendingIntent.FLAG_ONE_SHOT)
        alarmManager!!.setExactAndAllowWhileIdle( AlarmManager.RTC_WAKEUP, mFormat.parse(RoomDetail.longStartTime).time, pendingIntent )

    }

    fun cancelAlarmManagerStartRun () {
        Log.d(TAG, "cancelAlarmManagerStartRun () ")

        alarmManager = activity!!.getSystemService(ALARM_SERVICE) as AlarmManager

        var intent = Intent(this.context, AlarmReceiverStartRun::class.java)  // 1 여기서 object 를 set 하면 됨
        intent.putExtra("roomNo", RoomDetail.roomNo2)
        intent.putExtra("longStartTime", RoomDetail.longStartTime)

        pendingIntent = PendingIntent.getBroadcast( this.context, AlarmReceiverStartRun.NOTIFICATION_ID, intent, PendingIntent.FLAG_ONE_SHOT)
        alarmManager!!.cancel( pendingIntent )

    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("yyyy.MM.dd kk:mm:ss")
        return format.format(date)
    }


    fun addMessage(type: String, sendTime: Long, sendMessage: String, sendUserNickname: String) {
        Log.d(TAG, "RoomInformationFragment addMessage () ")

        if (type == "joinRoom") {
            var intoMessage = sendUserNickname + "님이 참가하였습니다."

            var messageObj = Message()
            messageObj.setMessagetype(2)
            messageObj.setMessageText(intoMessage)
            messageObj.setMessageDate(sendTime)
            RoomDetail.listChat.add(messageObj)

        }

        if (type == "exitRoom") {
            var exitMessage = sendUserNickname + "님이 참가를 취소하였습니다."

            var messageObj = Message()
            messageObj.setMessagetype(2)
            messageObj.setMessageText(exitMessage)
            messageObj.setMessageDate(sendTime)
            RoomDetail.listChat.add(messageObj)

        }
        if (type == "message") { // 내가보낸 메시지 recyclerview object 로 저장

            var messageObj = Message()
            messageObj.setMessagetype(0)
            messageObj.setMessageText(sendMessage)
            messageObj.setNickname(sendUserNickname)
            messageObj.setMessageDate(sendTime)
            RoomDetail.listChat.add(messageObj)

        }
    }

    fun connectSocket() {
        try {
            coroutine(JavaServerAPI.ip, JavaServerAPI.port)
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
    }

    fun coroutine(ip: String, port: Int) {

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                try {

                    RoomDetail.socket = Socket(ip, port)

                    RoomDetail.networkWriter = BufferedWriter(OutputStreamWriter(RoomDetail.socket!!.getOutputStream()))
                    RoomDetail.networkReader = BufferedReader(InputStreamReader(RoomDetail.socket!!.getInputStream()))

                } catch (e: IOException) {
                    println(e)
                    e.printStackTrace()
                }
            }.await()

            getInOutRoom(RoomDetail.roomNo2, MainActivity.loginId, "joinRoom/${MainActivity.loginNickname} 님이 참가하였습니다./${System.currentTimeMillis()}/${MainActivity.loginId}") // 소켓 메시지 전송

            thread(start = true) {

                Thread.sleep(200)
                getInOutRoom(RoomDetail.roomNo2, MainActivity.loginId, "joinRoom/${MainActivity.loginNickname} 님이 참가하였습니다./${System.currentTimeMillis()}/${MainActivity.loginId}") // 소켓 메시지 전송

            }

        }
    }

    fun getInOutRoom(roomNo: Int, intoUserId: Int, action: String) {

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this

                RoomDetail.out = PrintWriter(RoomDetail.networkWriter!!, true)
                RoomDetail.out!!.println("$roomNo/$intoUserId/$action")
                RoomDetail.out!!.flush()

            }.await()
        }
    }

    fun addUserprofile() { //
        Log.d(TAG, "addUserprofile ()")

        var user = RoomIntoUser()

        user.setId(MainActivity.loginId)
        user.setNickname(MainActivity.loginNickname)
        user.setProfileImgUrl(MainActivity.loginProfileImgPath)
        user.setRoomManager(false)

        list.add(user)

        if (list.size > 0) {
            adapter?.notifyDataSetChanged()
            memberCount.text = "경기 참가 인원 (" + list.size + " 명)"
        }
    }

    fun transferManager(listManager: ArrayList<RoomIntoUser>) {

        val dialog = android.app.AlertDialog.Builder(this.context).create()
        val edialog: LayoutInflater = LayoutInflater.from(this.context)
        val mView: View = edialog.inflate(R.layout.transfer_manager, null)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)


        val dialogTitle: TextView = mView.findViewById(R.id.dialogTitle)
        val transferManagerRV: RecyclerView = mView.findViewById(R.id.transferManagerRV)
        val cancelManager: Button = mView.findViewById(R.id.cancelManager)
        val confirmManager: Button = mView.findViewById(R.id.confirmManager)

        dialogTitle.text = "방장 양도"
        cancelManager.visibility = View.VISIBLE
        confirmManager.visibility = View.VISIBLE

        var positionTransfer: Int? = null
        val managerAdapter = this.context?.let { TransferManagerAdapter(it, listManager) }
        managerAdapter!!.itemClick = object : TransferManagerAdapter.ItemClick {
            override fun onClick(view: View, position: Int) {
                Log.d(TAG, "클릭 아이템 " + position)

                positionTransfer = position

            }
        }
        transferManagerRV.adapter = managerAdapter
        transferManagerRV.layoutManager = LinearLayoutManager(this.context)


        confirmManager.setOnClickListener(View.OnClickListener { view ->

            if (listManager.get(positionTransfer!!).getRoomManager()!!) {
                this.context?.let { oftenUseMethod.dialogshow("현재 방장입니다. \n 방장을 양도할 유저를 클릭해 주세요.",  it ) }
                return@OnClickListener
            }

            // --------------------------------------------------------------------------------------------------------- 방에 참가중인 유저 삭제 통신코드
            Log.d(TAG,"양도 방장 클릭한 포지션 "+ listManager.get(positionTransfer!!).getId().toString())
            listManager.get(positionTransfer!!).getId()?.let { // 참가중인 유저 데이터 삭제
                coroutineTransferManager (MainActivity.loginId,
                    RoomDetail.roomNo2,
                    it
                )
            }

//            removeUserprofile(positionTransfer!!) // 방장 넘겨준 후 참가중인 유저리스트에서 유저프로필 삭제

            // 룸 참가 취소 인원 DB 테이블에 유저 저장 후 message 테이블에도 참가취소 내역 저장 -----------------------------------------------------------------------------------------------------------    유저 삭제 통신 코드 테스트해야함
            coroutineRoomIntoOut ("leaveRoom", MainActivity.loginId, RoomDetail.roomNo2, MainActivity.loginNickname) // 해당 테이블에서 유저 삭제
            getInOutRoom (RoomDetail.roomNo2, MainActivity.loginId, "exitRoom/${MainActivity.loginNickname} 님이 참가를 취소 하였습니다./${System.currentTimeMillis()}/${MainActivity.loginNickname}") // HashMap 안에 socket 정보 지워줌

            // 룸 시작 시간 알람매니저 취소
            cancelAlarmManager ()
            cancelAlarmManagerStartRun()

            goHome ()

            dialog.dismiss()
            dialog.cancel()
        })

        cancelManager.setOnClickListener(View.OnClickListener { view ->
            dialog.dismiss()
            dialog.cancel()
        })

        dialog.setView(mView)
        dialog.create()
        dialog.show()

    }

    fun removeUserprofile(position: Int) { // 방장이 양도하기로 선택한 유저 position이 파라미터로 넘어옴

        for (i in 0 until list.size) { // 방장 새로 선언해주는곳
            if (i == position) {
                list.get(i).setRoomManager(true)
            }
        }

        for (i in 0 until list.size) { // 참가 취소를 원하는 유저 지워주는곳

            if (list.get(i).getId() == MainActivity.loginId) {
                list.removeAt(i)
                break
            }
        }

        if (list.size > 0) {
            adapter?.notifyDataSetChanged()
            memberCount.text = "경기 참가 인원 (" + list.size + " 명)"
        }
    }

    fun coroutineTransferManager (userId: Int, roomNo: Int, changeUserId: Int) {
        Log.d(TAG,"coroutineTransferManager ()")

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                TransferManager (userId, roomNo, changeUserId)
            }.await()

            Log.d(TAG,html)
        }
    }

    fun TransferManager(userId: Int, roomNo: Int, changeUserId: Int) : String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL+"/rooms/transferManager?userId=$userId&roomNo=$roomNo&changeUserId=$changeUserId").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }


    fun coroutineRoomIntoOut (action: String, userId: Int, roomNo: Int, nickname: String) {
        Log.d(TAG,"coroutineRoomIntoOut ()")

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                okhttp.userRoomInOut(action, userId, roomNo, nickname)
            }.await()

            Log.d(TAG,html)
        }
    }

    fun setRoomData(jsonArray: JSONArray) {
        Log.d(TAG, "setRoomData ()" + jsonArray)

        roomDate.text = RoomDetail.longStartTime.substring(0,10)
        roomStartTime.text = RoomDetail.longStartTime.substring(11,16)
        roomDistance.text = RoomDetail.distance + " km"
        roomGender.text = RoomDetail.sortGender
        memberCount.text = "경기 참가 인원 (" + RoomDetail.memberCountV + " 명)"

        if (RoomDetail.roomImgPath == "else") {
            roomPhoto.setImageResource(R.drawable.withrun)
        } else {
            Glide.with(this)
                .load(Constants.URL + RoomDetail.roomImgPath)
                .into(roomPhoto)
        }

        // 0 = 함께 뛰기, 1 = 혼자뛰기 모드
        // 함께뛰기 모드인 경우 -> 룸데이터 + 참가인원 함께 조회
        // 혼자뛰기 모드인 경우 -> 룸데이터 + 페이스메이커 붙여줌
        aloneModeUI () // 혼자뛰기 인 경우 ui변경 하는 곳

        adapter = ProfileImgAdapter(this.context!!, list, RoomDetail.aloneMode)

        for (i in 0 until jsonArray.length()) { // 안에있는 내용 in 배열명

            var user = RoomIntoUser()

            user.setId(jsonArray.getJSONObject(i).getInt("id"))
            user.setNickname(jsonArray.getJSONObject(i).getString("nickname"))
            user.setProfileImgUrl(jsonArray.getJSONObject(i).getString("profileImgPath"))
            user.setDistanceGap(RoomDetail.distance.toInt() * 100)

            if (jsonArray.getJSONObject(i).getInt("roomManager") == user.getId()) {
                user.setRoomManager(true)
            } else {
                user.setRoomManager(false)
            }

            list.add(user)

        }

        if (RoomDetail.aloneMode == 0) { // 혼자뛰기 모드가 아닌경우에만 유저가 참여하고있는지 확인

            for (i in 0 until list.size) { // 해당 방에 user 가 참여하고 있는지 유저 리스트에서 id 확인

                if (list[i].getId() == MainActivity.loginId) {
                    RoomDetail.intoUser = true
                    break
                }
            }
            participate.visibility = View.VISIBLE // 참여하기, 참여취소버튼 보여줌
            participateBackground() // ui 만 변경 - 참가하기 or 참가취소

        }
        recyclerviewUser()
    }


    fun recyclerviewUser() {
        Log.d(TAG, "recyclerviewUser ()")

        numOfPeopleRV.layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
        numOfPeopleRV.adapter = adapter
        adapter?.setOnItemClickListener(this)

        if (list.size > 0) {
            adapter?.notifyDataSetChanged()
        }
    }

    fun participateBackground() {
        Log.d(TAG, "participateBackground ()")

        if (RoomDetail.intoUser) { // 룸 참가인원에 있는경우

            participate.text = "참가취소"
            participate.setBackgroundResource(R.drawable.count_bg_seethrough)
            return
        }

        if (!RoomDetail.intoUser) { // 룸 참가인원에 없는경우

            participate.text = "+ 참가하기"
            participate.setBackgroundResource(R.drawable.bt_login_green)
            return
        }
    }


    fun aloneModeUI () {

        if ( RoomDetail.aloneMode == 1 ) {
            raceRightAway.visibility = View.VISIBLE
        }
    }


    override fun onClick(v: View) {
        Log.d(TAG, "clickListener " + v)

        // 인덱스 넘버 0 부터 반환
        val idx: Int = numOfPeopleRV!!.getChildAdapterPosition(v)

        if (idx == list.size) { // 푸터 클릭 = 초대창으로 연결 ( 현재 참자중이 아니면 클릭 불가 )

            if ( !RoomDetail.intoUser ) {
                activity?.let { oftenUseMethod.show("초대는 참가자만 가능합니다", it) }
                return
            }
            coroutineGetInvitationUserInfo ()


        } else { // 유저 프로필 클릭 = 유저들의 프로필 연결

            if ( list.get(idx).getId() == MainActivity.loginId ) {  // 클릭한 유저가 본인 인 경우
                Toast.makeText(activity, "본인 입니다.",Toast.LENGTH_SHORT).show()
                return
            }

            val intent = Intent (getActivity(), ProfileIntroduce::class.java)
            intent.putExtra("profileUserId", list.get(idx).getId())
            intent.putExtra("location", "RoomDetail")
            getActivity()?.startActivity(intent)


        }

    }


    fun coroutineGetInvitationUserInfo () {

        CoroutineScope(Dispatchers.Main).launch {
            this
            val invitationUserInfo = CoroutineScope(Dispatchers.Default).async { this
                okhttp.getRoomData("invitation", MainActivity.loginId, RoomDetail.roomNo2)

            }.await()
            Log.d(TAG, invitationUserInfo)


            val jsonObject = JSONObject(invitationUserInfo)
            val existResult = jsonObject.getString("existResult")

            if (existResult == "0") { // 조회결과없음

            } else { // 조회결과 있음
                val jsonArray = jsonObject.getJSONArray("result")
                val jsonArrayInvited = jsonObject.getJSONArray("result2")
                invitationUser (jsonArray,jsonArrayInvited)
            }
        }
    }


    // 초대 가능한 유저 목록 list UI 보여주는 곳
    fun invitationUser(jsonArray: JSONArray, jsonArrayInvited: JSONArray) {

        val invitationUserList = ArrayList<RoomIntoUser>()

        for (i in 0 until jsonArray.length()) { // 초대할 수 있는 유저 목록 불러옴

            var user = RoomIntoUser()

            user.setId(jsonArray.getJSONObject(i).getInt("id"))
            user.setNickname(jsonArray.getJSONObject(i).getString("nickname"))
            user.setProfileImgUrl(jsonArray.getJSONObject(i).getString("profileImgPath"))
            user.setAvgPace(jsonArray.getJSONObject(i).getInt("avgPace"))
            user.setFcmToken(jsonArray.getJSONObject(i).getString("fcmToken"))

            for (j in 0 until jsonArrayInvited.length()) { // 이미 초대한 유저 목록

                if ( jsonArrayInvited.getJSONObject(j).getInt("id") == user.getId() ) { // 이미 초대한 user id 와 같다면 초대됨 표시
                    user.setInvited(true)
                }
            }

            invitationUserList.add(user)
        }


        val dialog = android.app.AlertDialog.Builder(this.context).create()
        val edialog: LayoutInflater = LayoutInflater.from(this.context)
        val mView: View = edialog.inflate(R.layout.transfer_manager, null)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        val dialogTitle: TextView = mView.findViewById(R.id.dialogTitle)
        val transferManagerRV: RecyclerView = mView.findViewById(R.id.transferManagerRV)

        dialogTitle.text = "초대"

        val invitationAdapter = this.context?.let { InvitationAdapter(it, invitationUserList) }
        invitationAdapter!!.itemClick = object : InvitationAdapter.ItemClick {
            override fun onClick(view: View, position: Int) {

                // 초대한 유저목록에 초대된 userId 저장
                // 저장후 noti_unique 넘버와 함께 fcm 발송
                coroutineInvite (invitationUserList.get(position).getId()!!,
                    invitationUserList.get(position).getId()!!,
                    invitationUserList.get(position).getFcmToken()!!,
                    invitationUserList.get(position).getNickname()!!
                )

            }
        }

        transferManagerRV.adapter = invitationAdapter
        transferManagerRV.layoutManager = LinearLayoutManager(this.context)


        dialog.setView(mView)
        dialog.create()
        dialog.show()

    }


    fun coroutineInvite (inviteUser: Int, userId: Int, fcmToken: String, nickname: String) {

        CoroutineScope(Dispatchers.Main).launch {
            this
            val inviteUniqueNo = CoroutineScope(Dispatchers.Default).async { this
                okhttp.getRoomData("invite", inviteUser, RoomDetail.roomNo2)

            }.await()
            Log.d(TAG, inviteUniqueNo)

            sendNotiInvite (JSONObject(inviteUniqueNo).getInt("result"), userId, fcmToken, nickname)
        }
    }

    fun sendNotiInvite (inviteUniqueNo: Int, userId: Int, fcmToken: String, nickname: String) {

        var ivitationRoom = Room_object()
        ivitationRoom.setNo(RoomDetail.roomNo2)
        ivitationRoom.setStartDateTime(RoomDetail.longStartTime)
        ivitationRoom.setId(userId)
        ivitationRoom.setUniqueNo(inviteUniqueNo)


        //초대한 유저 noti (FCM) 전송
        SendNotification.sendNotification(fcmToken, "초대장", RoomDetail.roomTitle + " 룸에서 "+ nickname +" 님을 초대하였습니다.",
            RoomDetail.roomNo2.toString(), ivitationRoom)
    }


    fun changeActiveRoom () {

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                activeRoom()
            }.await()

        }
    }

    fun activeRoom () : String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL + "/rooms/activeRoom/${RoomDetail.roomNo2}").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }

    fun coroutineActiveRunState (roomNo: Int) {

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                activeRunState(roomNo)
            }.await()

        }
    }

    fun activeRunState (roomNo: Int) : String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL + "/rooms/runningMember?roomNo=$roomNo&userId=${MainActivity.loginId}").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }

    override fun onPause() {
        super.onPause()


    }

    override fun onDestroy() {

        if ( isRunningStartGametimer ) {
            startGametimer.cancel()
        }

        super.onDestroy()
    }



}