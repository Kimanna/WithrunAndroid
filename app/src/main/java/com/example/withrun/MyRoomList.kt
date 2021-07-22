package com.example.withrun

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_my_room_list.*
import kotlinx.android.synthetic.main.activity_my_room_list.recyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject

class MyRoomList : AppCompatActivity() {

    val TAG : String = "MyRoomList"

    private var roomAdapter: RoomAdapter? = null
    private var page = 0 // 현재 페이지
    private var allRoomCount = 0 // 생성된 룸의 총 갯수

    var isLoading = false
    var list: ArrayList<RoomItem> = ArrayList()
    lateinit var tempJsonArray: JSONArray


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_room_list)

        back_Home.setOnClickListener{
            val intent = Intent(this, Home::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
        coroutineGetRoomData (page)

        recyclerView ()
        initScrollListener() // 스크롤 마지막인지 감지
    }


    // 리싸이클러뷰 실행
    // 아이템 클릭시 해당 룸으로 입장안내문구띄움, 안내문구 확인 시 해당 룸넘버 intent 로 전달
    fun recyclerView () {

        recyclerView.layoutManager = LinearLayoutManager(this)
        roomAdapter = RoomAdapter(this,list) {RoomItem ->

            // 룽에서 지정한 경기 시작시간이 경과한 경우 (5분이내) RunningActive 로 바로 이동
            if (oftenUseMethod.mFormat.parse(RoomItem.getStartDate()?.substring(0,10)+" "+RoomItem.getStartTime()).time < System.currentTimeMillis()) {

                val dlg: AlertDialog.Builder = AlertDialog.Builder(this,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                dlg.setMessage("경기가 시작되었습니다, 경기 룸으로 이동 하시겠습니까?") // 메시지
                dlg.setNeutralButton("경기 룸 이동", DialogInterface.OnClickListener { dialog, which ->
                    Log.d(TAG, "setNeutralButton : " + RoomItem.getNo())


                    // 러닝 경기 룸 입장 코드
                    goRunningActive (RoomItem.getNo()!!)


                })
                dlg.setNegativeButton ("경기 포기", DialogInterface.OnClickListener { dialog, which->
                    Log.d(TAG, "setNegativeButton : " + RoomItem.getNo())

                    // 경기 포기하게되면 미 완주에 기록 없음으로 표기
                    // 미완주 데이터 저장
                    coroutineSaveRaceRecord (RoomItem.getNo()!!)
                    goRaceHistoryDetail (RoomItem.getNo()!!, MainActivity.loginId)

                })
                dlg.show()

            } else {

                val intent = Intent(this, RoomDetail::class.java)
                intent.putExtra("location","MyRoomList")
                intent.putExtra("roomNo",RoomItem.getNo())
                intent.putExtra("intoUserId", MainActivity.loginId)
                intent.putExtra("aloneMode", RoomItem.getAloneMode())
                intent.putExtra("longStartTime",RoomItem.getStartDate()?.substring(0,10)+" "+RoomItem.getStartTime())
                startActivity(intent)

            }

//            if (oftenUseMethod.mFormat.parse(RoomItem.getStartDate()?.substring(0,10)+" "+RoomItem.getStartTime()).time + 300000 < System.currentTimeMillis()) {
//                oftenUseMethod.dialogshow ("이미 경시 시간이 5분 이상 지난 경기는 입장할 수 없습니다.", this)
//                return@RoomAdapter
//            }


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

    fun coroutineSaveRaceRecord (roomNo: Int) {

        val progressDialog = ProgressDialog(this)
        showProgressBar (progressDialog)


        CoroutineScope(Dispatchers.Main).launch { this
            val saveRecord = CoroutineScope(Dispatchers.Default).async { this
                saveRaceRecord(roomNo)
            }.await()
            Log.d(TAG, saveRecord)

            dismissProgressBar(progressDialog)

        }
    }

    fun saveRaceRecord(roomNo: Int): String {
        Log.d(TAG, "saveRaceRecord " +  roomNo)

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder()
            .url(Constants.URL + "/record/recordUpdateNotJoin?roomNo=$roomNo&userId=${MainActivity.loginId}").build()
        client.newCall(req).execute().use { response ->
            return if (response.body != null) {
                response.body!!.string()
            } else {
                "body null"
            }
        }
    }

    fun goRunningActive (roomNo: Int) {

        val intent = Intent(this, RunningActive::class.java)
        intent.putExtra("roomNo", roomNo)
        intent.putExtra("location","MyRoomList")
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()

    }

    fun goRaceHistoryDetail (roomNo: Int, userId: Int) {

        val intent = Intent(this, MyRaceHistoryDetail::class.java)
        intent.putExtra("location","MyRoomList")
        intent.putExtra("roomNo", roomNo)
        intent.putExtra("userId", userId)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }


    fun coroutineGetRoomData (roomNo: Int) {

        CoroutineScope(Dispatchers.Main).launch { this
            val rooms = CoroutineScope(Dispatchers.Default).async { this
                // network
                getRooms(roomNo)
            }.await()
            Log.d(TAG, "room 정보 " + rooms)

            val jsonObject = JSONObject(rooms)
            val existResult = jsonObject.getString("existResult")

            if (existResult == "0") {
                Log.d(TAG, "room 정보 없음")
            } else {
                Log.d(TAG, "room 정보 있음" )

                if (page == 0) { // 첫번째 페이지

                    val jsonArray = jsonObject.getJSONArray("result")
                    allRoomCount = jsonObject.getJSONArray("room_total_count").getJSONObject(0).getInt("roomCount")

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

                } else { // 추가 loading 페이지
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
        val req = okhttp3.Request.Builder().url(Constants.URL + "/rooms/myRoom?userId=${MainActivity.loginId}&pageNo=$pageNo").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }

    private fun showProgressBar(progressDialog: ProgressDialog) {
        progressDialog.setTitle("러닝 기록 저장")
        progressDialog.setMessage("러닝 기록을 저장하는 중 입니다.")
        progressDialog.show()
    }

    private fun dismissProgressBar(progressDialog: ProgressDialog) {
        progressDialog.dismiss()
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