package com.example.withrun

import android.content.Intent
import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_my_room_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject

class Notification : AppCompatActivity() {

    val TAG: String = "Notification"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        back_Home.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        coroutineGetNotiData()
        readNoti()

    }

    override fun onBackPressed() {
        super.onBackPressed()

        val intent = Intent(this, Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun readNoti() {
        CoroutineScope(Dispatchers.Main).launch {
            this
            val noti = CoroutineScope(Dispatchers.Default).async {
                this
                readNotiFinish()
            }.await()
            Log.d(TAG, "noti 정보 " + noti)

        }
    }

    fun readNotiFinish(): String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder()
            .url(Constants.URL + "/notification/readNoti?userId=${MainActivity.loginId}")
            .build()
        client.newCall(req).execute().use { response ->
            return if (response.body != null) {
                response.body!!.string()
            } else {
                "body null"
            }
        }
    }

    // 리싸이클러뷰 실행
    // 아이템 클릭시 해당 룸으로 입장안내문구띄움, 안내문구 확인 시 해당 룸넘버 intent 로 전달
    fun recyclerView(list: ArrayList<class_Noti>) {

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = NotificationAdapter(this, list) { notiItem ->

            coroutineGetNotiData (notiItem.getNotiType()!!, notiItem.getUniqueNo()!!)

            }

    }

    fun coroutineGetNotiData() {

        CoroutineScope(Dispatchers.Main).launch {
            this
            val rooms = CoroutineScope(Dispatchers.Default).async {
                this
                getNotiInfo()
            }.await()
            Log.d(TAG, "noti 정보 " + rooms)

            val jsonObject = JSONObject(rooms)
            val existResult = jsonObject.getString("existResult")

            if (existResult == "0") {
                Log.d(TAG, "noti 정보 없음")
            } else {
                Log.d(TAG, "noti 정보 있음")

                val list = ArrayList<class_Noti>()

                val jsonArray = jsonObject.getJSONArray("result")
                Log.d(TAG, "포문 전 jsonArray size - " + jsonArray.length().toString())

                for (i in 0..jsonArray.length()-1) {

                    var noti = class_Noti()
                    noti.setId(jsonArray.getJSONObject(i).getInt("id"))
                    noti.setReceivedAt(jsonArray.getJSONObject(i).getString("received_at"))
                    noti.setMessage(jsonArray.getJSONObject(i).getString("message"))
                    noti.setReadStatus(jsonArray.getJSONObject(i).getInt("readStatus"))
                    noti.setDeleteNoti(jsonArray.getJSONObject(i).getInt("deleteNoti"))
                    noti.setNotiType(jsonArray.getJSONObject(i).getString("notiType"))
                    noti.setUniqueNo(jsonArray.getJSONObject(i).getInt("uniqueNo"))

                    list.add(noti)
                } // for 문 끝남

                // 리싸이클러뷰 어댑터 실행코드 + 클릭리스너
                recyclerView(list)

            }
        }
    }

    fun getNotiInfo(): String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder()
            .url(Constants.URL + "/notification/myNotiList?userId=${MainActivity.loginId}")
            .build()
        client.newCall(req).execute().use { response ->
            return if (response.body != null) {
                response.body!!.string()
            } else {
                "body null"
            }
        }
    }

    fun coroutineGetNotiData(action: String, uniqueNo: Int) {

        CoroutineScope(Dispatchers.Main).launch {
            this
            val notiInfo = CoroutineScope(Dispatchers.Default).async {
                this
                getNotiInfo(action, uniqueNo)
            }.await()
            Log.d(TAG, "notiInfo 정보 " + notiInfo)

            val jsonObject = JSONObject(notiInfo)

            when (action) {
                "invitation" -> {

                    val roomNo = jsonObject.getJSONArray("result").getJSONObject(0).getInt("roomNo")

                    val intent = Intent(baseContext, RoomDetail::class.java)
                    intent.putExtra("location", "Notification")
                    intent.putExtra("roomNo", roomNo)
                    startActivity(intent)
                    finish()

                }
                "follow" -> {

                    val follower_id = jsonObject.getJSONArray("result").getJSONObject(0).getInt("follower_id")

                    val intent = Intent(baseContext, ProfileIntroduce::class.java)
                    intent.putExtra("location", "Notification")
                    intent.putExtra("profileUserId", follower_id)
                    startActivity(intent)
                    finish()

                }
            }
        }
    }

    fun getNotiInfo(action: String, uniqueNo: Int): String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder()
            .url(Constants.URL + "/notification/getNotiInfo?action=$action&uniqueNo=$uniqueNo")
            .build()
        client.newCall(req).execute().use { response ->
            return if (response.body != null) {
                response.body!!.string()
            } else {
                "body null"
            }
        }
    }

}