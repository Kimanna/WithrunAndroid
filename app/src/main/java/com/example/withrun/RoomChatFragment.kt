package com.example.withrun

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_room_chat.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.io.PrintWriter
import java.util.*

class RoomChatFragment : Fragment() {

    val TAG: String = "RoomChatFragment"

    private var mHandler: Handler? = null
    private var html = "" // java server 에서 보내준 값을 찍어보기 위한 변수

    fun newInstant(): RoomChatFragment {
        val args = Bundle()
        val frag = RoomChatFragment()
        frag.arguments = args

        return frag
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        val view = inflater.inflate(R.layout.fragment_room_chat, container,false)
        Log.d(TAG, "onCreateView RoomChatFragment 지난후 " + RoomDetail.intoUser)

        mHandler = Handler()

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        RoomDetail.adapterChat = ChatMessageAdapter(this.context!!, RoomDetail.listChat)
        recyclerviewMessage()
    }

    fun addMessage(type: String, sendTime: Long, sendMessage: String, sendUserNickname: String) {

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

    fun recyclerviewMessage() {

        chatRecyclerview.layoutManager = LinearLayoutManager(this.context)
        chatRecyclerview.adapter = RoomDetail.adapterChat
        Log.d(TAG, "112 line chat list 사이즈 " + RoomDetail.listChat.size)

        if (RoomDetail.listChat.size > 0) {
            RoomDetail.adapterChat?.notifyDataSetChanged()
            chatRecyclerview.scrollToPosition(RoomDetail.listChat.size-1);
        }
    }

    fun getInOutRoom(roomNo: Int, intoUserId: Int, action: String) {

        CoroutineScope(Dispatchers.Main).launch {  this
            val html = CoroutineScope(Dispatchers.Default).async { this

                RoomDetail.out = PrintWriter(RoomDetail.networkWriter!!, true)
                RoomDetail.out!!.println("$roomNo/$intoUserId/$action")
                RoomDetail.out!!.flush()

            }.await()
        }
    }

    fun CallreadLine() {
        Log.d(TAG, "124 CallreadLine() 지남  ")

        if (checkUpdate.state == Thread.State.NEW) {
            Log.d(TAG, "176 line")

            checkUpdate.start()
        }
    }


    private var checkUpdate: Thread = object : Thread() {
        override fun run() {
            try {
                var line: String
                while (true) {
                    Log.d(TAG, "139 chatting is running")
                    sleep(1500) // nerworkReader 가 연결되기 위한 시간을 벌기 위해

                    line = RoomDetail.networkReader!!.readLine()
                    Log.d(TAG, "141 readLine() "  )

                    html = line
                    Log.d(TAG, "html 출력 $html")

                    // roomNo/userId/intoRoom or exitRoom/nickname/images/running1.jpeg
                    val strs = html.split("/").toTypedArray()

                    if (strs[2] == "intoRoom") { // chat 메시지 저장해야함
                        Log.d(TAG, "intoRoom 지나감")

//                        var user = RoomIntoUser()
//                        user.setNickname(strs[3])
//                        user.setProfileImgUrl( "/images/" +strs[5])
//                        user.setId(strs[1].toInt())
//                        user.setDistanceGap(distance.toInt()*100)
//                        list.add(user)
                        mHandler!!.post(updateMessage)
                    }

                    // 75/4/runIntoRoom/노바/images/running1.jpeg
                    if (strs[2] == "runIntoRoom") {
                        Log.d(TAG, "runIntoRoom  룸 입장 지나감")

//                        var messageObj = Message()
//                        messageObj.setMessagetype(2)
//                        messageObj.setMessageText(strs[3]+" 님이 참가하였습니다.")
//                        messageObj.setNickname(strs[3])
//                        RoomDetail.listChat.add(messageObj)
//
//                        mHandler!!.post(updateMessage)
                    }

                    if (strs[2] == "exitRoom") {
                        Log.d(TAG, "exitRoom 지나감")

                        var messageObj = Message()
                        messageObj.setMessagetype(2)
                        messageObj.setMessageText(strs[3])
                        messageObj.setNickname(strs[5])
                        messageObj.setMessageDate(strs[4].toLong())
                        RoomDetail.listChat.add(messageObj)

                        mHandler!!.post(updateMessage)
                    }

//                    roomNo2, intoUserId, "message/$message/${System.currentTimeMillis()}/"+MainActivity.loginNickname+"/"+MainActivity.loginProfileImgPath)
                    if (strs[2] == "message") {
                        Log.d(TAG, "message 지나감")

                        var messageObj = Message()
                        messageObj.setMessagetype(1)
                        messageObj.setMessageText(strs[3])
                        messageObj.setNickname(strs[5])
                        messageObj.setProfileImgUrl("/images/" + strs[7])
                        messageObj.setMessageDate(strs[4].toLong())
                        RoomDetail.listChat.add(messageObj)

                        mHandler!!.post(updateMessage)

                    }

                }
            } catch (e: Exception) {
                Log.d(TAG, "chat error 123 line " + e.toString())
            }
        }
    }


    // 유저 입장한 경우 TCP로 user profile 데이터 가져온 후 ui update
//    private val showUpdate =
//        Runnable {
//            recyclerviewUser()
//        }

    // 메시지를 보낸경우
    private val updateMessage =
        Runnable {
            recyclerviewMessage()
        }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart RoomChatFragment 지난후 "+ RoomDetail.intoUser)

        changeCover ()

    }


    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume RoomChatFragment 지난후 "+ RoomDetail.intoUser)

//        (activity as RoomDetail).connectFragment()

        changeCover () // 유저 참가여부에 따라 chat cover 변경해줌

        // chatFragment 메시지 발송버튼
        sendMessage.setOnClickListener {
            val message = inputMessage.text.toString().trim()

            // ---------------------------------------------------------------------------------- 리싸이클러뷰에 메시지 저장하는 부분 넣어야함
            // joinRoom / exitRoom / message
            addMessage("message", System.currentTimeMillis(), message, MainActivity.loginNickname)
            recyclerviewMessage()
            getInOutRoom( RoomDetail.roomNo2, MainActivity.loginId, "message/$message/${System.currentTimeMillis()}/${MainActivity.loginNickname}${MainActivity.loginProfileImgPath}")
            coroutinMessage( RoomDetail.roomNo2, MainActivity.loginId, Date(System.currentTimeMillis()).toString(), message ) // 메시지 저장

            inputMessage.text = null
        }

    }

    fun changeCover () {
        Log.d(TAG, "changeCover 지나감")


        if ( RoomDetail.intoUser ) { // 해당 room 에 참가신청한 상태 --------------------------------- chat 데이터 가져와야함, TCP 연결해야함
            chatAreaView.visibility = View.VISIBLE
            chatAreaHide.visibility = View.GONE

            CallreadLine()
        } else {                     // 해당 룸에 참가신청하지 않은 상태
            chatAreaView.visibility = View.GONE
            chatAreaHide.visibility = View.VISIBLE
        }
    }

    // chatmessage 저장
    fun coroutinMessage(roomNo: Int, userId: Int, sendAt: String, message: String) {

        CoroutineScope(Dispatchers.Main).launch { this
            val message = CoroutineScope(Dispatchers.Default).async {  this
                message(roomNo, userId, sendAt, message)
            }
            Log.d(TAG, "pass this line 600")

        }
    }

    fun message(roomNo: Int, userId: Int, sendAt: String, message: String) {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder()
            .url(Constants.URL + "/rooms/saveMessage?roomNo=$roomNo&userId=$userId&sendAt=$sendAt&message=$message")
            .build()
        client.newCall(req).execute()
    }



}