package com.example.withrun

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.withrun.MyFirebaseMessagingService.EXTRA_ROOM_DETAIL
import com.google.gson.JsonArray
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class Home : AppCompatActivity() {

    val TAG : String = "Home"

    private var roomAdapter: RoomAdapter? = null
    private var page = 0 // 현재 페이지
    private var allRoomCount = 0 // 생성된 룸의 총 갯수

    var isLoading = false
    var list: ArrayList<RoomItem> = ArrayList()
    lateinit var tempJsonArray: JSONArray


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        bottomnavigation.setSelectedItemId(R.id.Home)
        bottomnavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Home -> {
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
                    startActivity(Intent(applicationContext,Profile::class.java))
                    finish()
                    true
                }
            }
            true
        }

        var intent = intent

        if ( !TextUtils.isEmpty(intent.getStringExtra("location")) ) { // 이동해온 위치 정보
            Log.d(TAG, "noti에서 넘겨받은 location 정보 " + intent.getStringExtra("location"))

            if ( intent.getStringExtra("location") == "Notification" ) { // notification 클릭으로 입장한 경우
                val roomData = intent.getParcelableExtra<Room_object>(EXTRA_ROOM_DETAIL)
                val currentTime = System.currentTimeMillis()

                var mFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd kk:mm:ss")
                val longStartTime = mFormat.parse(roomData.getStartDateTime()).time

                Log.d(TAG, "home 노티에서 접근시 롱 시간 체크 - 현재시간 $currentTime / 룸시간 $longStartTime")

                // 초대 된 후 notification 을 클릭해서 방에 입장한 상태이기 때문에 notification 읽음 처리함
                coroutineReadNoti ("readInviteNoti", MainActivity.loginId, roomData.getUniqueNo()!!)


                val intent = Intent(this, RoomDetail::class.java)
                intent.putExtra("location","Home")
                intent.putExtra("roomNo",roomData.getNo()!!)
                intent.putExtra("longStartTime",roomData.getStartDateTime())
                startActivity(intent)

//                if ( currentTime > longStartTime) { // 룸 시작시간이 이미 지났으면
//                    oftenUseMethod.dialogConfirmShow ("이미 시작 시간이 지나 입장이 불가합니다.", this)
//
//                } else { // 룸 시작시간이 안지났으면 룸 입장
//
//                    val intent = Intent(this, RoomDetail::class.java)
//                    intent.putExtra("roomNo",roomData.getNo())
//                    intent.putExtra("longStartTime",roomData.getStartDateTime())
//                    startActivity(intent)
//
//                }
            }

            if ( intent.getStringExtra("location") == "CreateRoom" ) { // CreateRoom으로 이동해온 경우
                Log.d(TAG, "CreateRoom 접근시 롱 시간 체크 - 개설 룸시간 " + intent.getStringExtra("longStartTime") + " 룸넘버 " + intent.getIntExtra("roomNo",0))
                var createdRoomNo = intent.getIntExtra("roomNo",0)

                val intent = Intent(this, RoomDetail::class.java)
                intent.putExtra("location","Home")
                intent.putExtra("roomNo", createdRoomNo)
                intent.putExtra("longStartTime", intent.getStringExtra("longStartTime"))
                startActivity(intent)

            }

        } else {

            if (MainActivity.loginProfileImgPath == "else") {
                profileimgHome.setImageResource(R.drawable.user)
            } else {
                Glide.with(this)
                    .load(Constants.URL + MainActivity.loginProfileImgPath)
                    .into(profileimgHome)
            }

            participateRoomList.setOnClickListener{
                val intent = Intent(this, MyRoomList::class.java)
                startActivity(intent)
            }

            alertIcon.setOnClickListener {
                var intent = Intent(this, Notification::class.java)
                startActivity(intent)
            }


            coroutineGetRoomData (page)

        }


        // 방생성 activity로 이동하는 버튼
        createRoom ()


        recyclerView ()
        initScrollListener() // 스크롤 마지막인지 감지


    }

    fun coroutineReadNoti (action: String, readUser: Int, roomNo: Int) {
        Log.d(TAG, "coroutineReadNoti 저장할 데이터 확인 "+ action+ readUser + roomNo )

        CoroutineScope(Dispatchers.Main).launch {
            this
            val invite = CoroutineScope(Dispatchers.Default).async { this
                okhttp.getRoomData(action, readUser, roomNo)

            }.await()
            Log.d(TAG, invite)
        }
    }

    override fun onResume() {
        super.onResume()

        refreshBT.setOnClickListener {

            coroutineGetRoomData (page)
        }

    }

    fun coroutineGetRoomData (pageNo: Int) {

        CoroutineScope(Dispatchers.Main).launch { this
            val rooms = CoroutineScope(Dispatchers.Default).async { this
                // network
                getRooms(pageNo)
            }.await()

            val jsonObject = JSONObject(rooms)
            val existResult = jsonObject.getString("existResult")

            if (existResult == "0") {
                Log.d(TAG, "room 정보 없음")
            } else {
                Log.d(TAG, "room 정보 있음" + rooms)
                val jsonArray = jsonObject.getJSONArray("result")

                if(page == 0){
                    participateRoomCount.text =
                        jsonObject.getJSONArray("my_room_count").getJSONObject(0).getInt("myroom").toString()

                    val getNotiCount = jsonObject.getJSONArray("my_noti_count").getJSONObject(0).getInt("noti")
                    allRoomCount = jsonObject.getJSONArray("room_total_count").getJSONObject(0).getInt("roomCount")

                    if ( getNotiCount > 0 ) {
                        notiCount.visibility = View.VISIBLE
                        notiCount.text = getNotiCount.toString()
                    } else {
                        notiCount.visibility = View.GONE
                    }

                    for (i in 0..jsonArray.length()-1) {

                        var room = RoomItem()

                        room.setIsLoading(1)
                        room.setNo(jsonArray.getJSONObject(i).getInt("no"))
                        room.setCreatedAt(jsonArray.getJSONObject(i).getString("created_at"))
                        room.setRoomImgPath(jsonArray.getJSONObject(i).getString("roomImgPath"))
                        room.setStartDate(jsonArray.getJSONObject(i).getString("startDate"))
                        room.setRoomTitle(jsonArray.getJSONObject(i).getString("roomTitle"))
                        room.setStartTime(jsonArray.getJSONObject(i).getString("startTime"))
                        room.setFinishTime(jsonArray.getJSONObject(i).getString("finishTime"))
                        room.setDistance(jsonArray.getJSONObject(i).getInt("distance"))
                        room.setSortGender(jsonArray.getJSONObject(i).getString("sortGender"))
                        room.setSortLevel(jsonArray.getJSONObject(i).getString("sortLevel"))
                        room.setRoomManager(jsonArray.getJSONObject(i).getInt("roomManager"))
                        room.setMaxPeople(jsonArray.getJSONObject(i).getInt("maxPeople"))
                        room.setMemberCount(jsonArray.getJSONObject(i).getInt("memberCount"))
                        room.setAloneMode(jsonArray.getJSONObject(i).getInt("aloneMode"))
                        room.setActiveGame(jsonArray.getJSONObject(i).getInt("activeGame"))

                        list.add(room)
                    } // for 문 끝남

                    Log.d(TAG, "포문 끝난 후 arraylist 사이즈 확인 - "+ list.size.toString())
                    roomAdapter!!.notifyDataSetChanged()

                } else {
                    tempJsonArray = jsonObject.getJSONArray("result")
                }

            }
        }
    }

    fun addRoomList () {

        for (i in 0..tempJsonArray.length()-1) {

            var room = RoomItem()

            room.setIsLoading(1)
            room.setNo(tempJsonArray.getJSONObject(i).getInt("no"))
            room.setCreatedAt(tempJsonArray.getJSONObject(i).getString("created_at"))
            room.setRoomImgPath(tempJsonArray.getJSONObject(i).getString("roomImgPath"))
            room.setStartDate(tempJsonArray.getJSONObject(i).getString("startDate"))
            room.setRoomTitle(tempJsonArray.getJSONObject(i).getString("roomTitle"))
            room.setStartTime(tempJsonArray.getJSONObject(i).getString("startTime"))
            room.setFinishTime(tempJsonArray.getJSONObject(i).getString("finishTime"))
            room.setDistance(tempJsonArray.getJSONObject(i).getInt("distance"))
            room.setSortGender(tempJsonArray.getJSONObject(i).getString("sortGender"))
            room.setSortLevel(tempJsonArray.getJSONObject(i).getString("sortLevel"))
            room.setRoomManager(tempJsonArray.getJSONObject(i).getInt("roomManager"))
            room.setMaxPeople(tempJsonArray.getJSONObject(i).getInt("maxPeople"))
            room.setMemberCount(tempJsonArray.getJSONObject(i).getInt("memberCount"))
            room.setAloneMode(tempJsonArray.getJSONObject(i).getInt("aloneMode"))
            room.setActiveGame(tempJsonArray.getJSONObject(i).getInt("activeGame"))

            list.add(room)
        } // for 문 끝남
    }




    fun getRooms (pageNo: Int) : String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url("http://ec2-13-209-169-218.ap-northeast-2.compute.amazonaws.com:12888/rooms/room?userId=${MainActivity.loginId}&pageNo=$pageNo").build()
        client.newCall(req).execute().use {
            response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }


    // 리싸이클러뷰 실행
    // 아이템 클릭시 해당 룸으로 입장안내문구띄움, 안내문구 확인 시 해당 룸넘버 intent 로 전달
    fun recyclerView () {

        recyclerView.layoutManager = LinearLayoutManager(this)
        roomAdapter = RoomAdapter(this,list) {RoomItem ->

            // 남자만 입장가능한 룸에서 woman이거나 성별지정을 하지 않은 유저가 입장할때
            if ( RoomItem.getSortGender() == "남자만" && MainActivity.loginGender != "man" ) {
                dialogshow ("해당룸은 남자만 입장 가능한 룸입니다.")
                return@RoomAdapter
            }
            Log.d(TAG,RoomItem.getSortGender()+"  "+MainActivity.loginGender)
            // 여자만 입장가능한 룸에서 man이거나 성별지정을 하지 않은 유저가 입장할때
            if ( RoomItem.getSortGender() == "여자만" && MainActivity.loginGender != "woman" ) {
                dialogshow ("해당룸은 여자만 입장 가능한 룸입니다.")
                return@RoomAdapter
            }


            intoRoom (RoomItem)

        }
        recyclerView.adapter = roomAdapter

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
                        if (page * 10 > allRoomCount) {
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

        if (page * 10 > allRoomCount) {
            return
        }

        var room = RoomItem()
        room.setIsLoading(0)
        list.add(room)
        roomAdapter!!.notifyItemInserted(list.size - 1)       // 로딩버튼 리싸이클러뷰에 추가함

        //통신후 데이터 더 가져옴
        coroutineGetRoomData (page)


        val handler = Handler()
        handler.postDelayed(Runnable {

            list.removeAt(list.size - 1)
            val scrollPosition = list.size
            roomAdapter!!.notifyItemRemoved(scrollPosition)

            // arraylist 안에 서버에서 추가로 가져온 room data 추가해줌
            addRoomList ()

            roomAdapter!!.notifyItemRangeInserted((page + 1) * 10, 10)
            isLoading = false

        }, 2000)
    }

    // 방으로 입장하는 intent - 2 가지 루트
    // 1. 아이템 클릭시, 2. 초대시 전달 받은 nitification을 통해
    fun intoRoom (roomItem: RoomItem) {

        val intent = Intent(this, RoomDetail::class.java)
        intent.putExtra("location","Home")
        intent.putExtra("roomNo",roomItem.getNo())
        intent.putExtra("intoUserId", MainActivity.loginId)
        intent.putExtra("aloneMode", roomItem.getAloneMode())
        intent.putExtra("longStartTime",roomItem.getStartDate()?.substring(0,10)+" "+roomItem.getStartTime())
        startActivity(intent)

    }



    fun createRoom () {
        creatRoomBT.setOnClickListener {
            val intent = Intent(this, CreateRoom::class.java)
            startActivity(intent)
        }
    }

    fun dialogshow (msg: String) {

        val dlg: AlertDialog.Builder = AlertDialog.Builder(this,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
        dlg.setMessage(msg) // 메시지
        dlg.setNeutralButton("확인", DialogInterface.OnClickListener { dialog, which ->

        })
        dlg.show()
    }

    fun coroutineSaveRoomMember (roomNo:String, userId:Int) {

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                // network
                getHtml(roomNo, userId)
            }.await()

//            Log.d(TAG,html)
        }
    }

    fun getHtml(roomNo: String, userId: Int) : String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL+"/rooms/intoRoom?roomNo=$roomNo&userId=$userId").build()
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
