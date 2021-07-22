package com.example.withrun

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.withrun.oftenUseMethod.secondsToTimeRecord
import kotlinx.android.synthetic.main.activity_profile_introduce.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject

class ProfileIntroduce : AppCompatActivity() {

    val TAG:String = "ProfileIntroduce"

    var profileUserId: Int? = null
    var profileUserNickname: String? = null
    var recordOpenStatus: Int? = null
    var profileUserToken: String = ""

    var location: String = ""

    var isFollowUser = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_introduce)


        var intent = intent
        profileUserId = intent.getIntExtra("profileUserId",0)
        location = intent.getStringExtra("location")

        Log.d(TAG,"intent 로 받아온 유저 id 찍어봄 "+profileUserId + location)

        coroutineUserProfile (MainActivity.loginId)

        // 뒤로가기 image 에 버튼 event
        back_room.setOnClickListener {
            moveLocation()
        }

        // acceptFollowBT - 팔로우 완료됨을 나타내는 버튼
        // RequestedFollowBT - 팔로우 요청 완료 버튼
        // requestFollowBT - 팔로우 요청버튼
        requestFollowBT.setOnClickListener { // 팔로우 요청 버튼

            // follow 요청내역 서버로 저장
            // 저장 후 받은 follow unique 넘버와 함께 notification 발송
            coroutineRequestFollow(MainActivity.loginId, profileUserId!!, oftenUseMethod.dateFormatSaveDB(System.currentTimeMillis()))

        }

        acceptFollowBT.setOnClickListener {

            // 서버에서 Following 데이터 지움
            coroutineAcceptFollower (profileUserId, "unFollow")

             acceptFollowBT.visibility = View.GONE
             RequestedFollowBT.visibility = View.GONE
             requestFollowBT.visibility = View.VISIBLE

        }
    }

    fun moveLocation() {
        when(location){

            "Notification" -> {
                val intent = Intent(this, Notification::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
                finish()
            }
            "RoomDetail" -> {
                val intent = Intent(this, RoomDetail::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onBackPressed() {

        moveLocation()

        super.onBackPressed()
    }

    fun coroutineAcceptFollower(youId: Int?, action: String) {
        Log.d(TAG, "coroutineAcceptFollower ()")

        CoroutineScope(Dispatchers.Main).launch {
            this
            val follows = CoroutineScope(Dispatchers.Default).async {
                this
                // network
                saveFollows(youId, action)
            }.await()
            Log.d(TAG, "room 정보 있음" + follows)

        }
    }

    fun saveFollows(youId: Int?, action: String): String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder()
            .url("http://ec2-13-209-169-218.ap-northeast-2.compute.amazonaws.com:12888/follow/acceptFollower?meId=${MainActivity.loginId}&youId=$youId&action=$action")
            .build()
        client.newCall(req).execute().use { response ->
            return if (response.body != null) {
                response.body!!.string()
            } else {
                "body null"
            }
        }
    }

    // 해당 유저 프로필을 UI 에 보여주는 부분
    fun setProfileIntroduce(jsonObject: JSONObject) {

        val jsonArray = jsonObject.getJSONArray("result") //  유저 (프로필 주인) 프로필데이터
        val jsonArrayRecord = jsonObject.getJSONArray("result_record") //
        val jsonArrayRecordPace = jsonObject.getJSONArray("result_record_pace")
        val jsonArrayRecordFollow = jsonObject.getJSONArray("result_record_follow")

        if ( jsonArrayRecordPace.length() != 0 ) { // record 데이터가 존재함

            // 평균페이스를 분으로 보여주는 부분 (ex. 평균페이스 00:00 분)
            proAveragePace.text = oftenUseMethod.secondsToTimeOverMinuteAddHour(jsonArrayRecordPace.getJSONObject(0).getInt("avgPace").toLong())

        }


        // 프로필 제목, 사진, 닉네임 보여주는 부분
        var profileNickname = jsonArray.getJSONObject(0).getString("nickname")
        var profileimgpath = jsonArray.getJSONObject(0).getString("profileImgPath")
        profileUserNickname = profileNickname
        profileUserToken = jsonArray.getJSONObject(0).getString("fcmToken")
        recordOpenStatus = jsonArray.getJSONObject(0).getInt("record_open_status")

        titleProfileName.text = profileNickname + " 님의 프로필"
        proNickname.text = profileNickname

        if (profileimgpath != "else") {
            if (profileimg != null) {
                Glide.with(this)
                    .load(Constants.URL + profileimgpath)
                    .into(profileimg)
            }
        } else {
            profileimg?.setImageResource(R.drawable.avatar)
        }


        // if = 팔로워 내역에 없는경우, else = 팔로우 내역에 있는경우
        // 수락 여부에 따라 요청됨 버튼 or 팔로잉 버튼 으로 구분하여 보여줌
        if ( jsonArrayRecordFollow.length() == 0 ){ // 팔로워 내역에 없는경우, 팔로우 버튼 보여줌

            requestFollowBT.visibility = View.VISIBLE
            if (recordOpenStatus == 1) {
                isFollowUser = true
            } else {
                isFollowUser = false
            }

        } else {

            var accept_status = jsonArrayRecordFollow.getJSONObject(0).getInt("accept_status")
            when(accept_status){
              0 -> {                                                   // 상대가 팔로우 수락하기 전

                  RequestedFollowBT.visibility = View.VISIBLE
                  isFollowUser = false

              }
              1 -> {                                                   // 팔로우 수락 완료

                  acceptFollowBT.visibility = View.VISIBLE             // 기록보기 비공개 설정 상태 이지만 팔로워 수락한 유저에게는 기록 공개해줌
                  isFollowUser = true
              }
            }
        }

        // 현재 프로필에 해당하는 유저의 race 기록을 보여주는 부분
        raceRecordUi (jsonArrayRecord)

    }

    fun raceRecordUi(jsonArrayRecord: JSONArray) {
        Log.d(TAG, "raceRecordUi " )

        when(isFollowUser) {
            false -> {                                              // 팔로우 유저가 아닌 경우
                nondisclosureArea.visibility = View.VISIBLE         // 비공개 계정이기에 화면을 가림
                monthRecordArea.visibility = View.GONE              // 기록 숨김
                monthRankingArea.visibility = View.GONE

            }
            true -> {                                               // 팔로우 유저인 경우

                nondisclosureArea.visibility = View.GONE            // 팔로우 유저이기에 화면 가리기를 숨김

                if (jsonArrayRecord.length() == 0) {                // 기록이 없는경우
                    emptyRecord.visibility = View.VISIBLE           // 기록이 없다는 문구 보여줌
                    monthRecordArea.visibility = View.GONE          // 기록 나타내는 부분 가림
                    monthRankingArea.visibility = View.GONE

                } else {
                    emptyRecord.visibility = View.GONE               // 기록이 있는경우
                    monthRecordArea.visibility = View.VISIBLE        // 기록을 출력하는 부분 보여줌
                    monthRankingArea.visibility = View.VISIBLE
                    setMonthRaceRecord (jsonArrayRecord)
                }
            }
        }




    }

    fun setMonthRaceRecord (jsonArrayRecord: JSONArray) {
        Log.d(TAG, "jsonArrayRecord 출력해봄  "+ jsonArrayRecord.getJSONObject(0))

        val tempTotalRaceTime = jsonArrayRecord.getJSONObject(0).getInt("monthTotalRaceTime")
        val tempTotalDistance = jsonArrayRecord.getJSONObject(0).getInt("monthTotalDistance")

        monthTotalRaceTime.text = (oftenUseMethod.twoDigitString((tempTotalRaceTime / (60 * 60) % 24).toLong()).toString() + " : " + oftenUseMethod.twoDigitString((tempTotalRaceTime / 60 % 60).toLong()) + " : "
                + oftenUseMethod.twoDigitString((tempTotalRaceTime % 60).toLong()))
        gold_medal.text = jsonArrayRecord.getJSONObject(0).getInt("gold_medal").toString()
        silver_medal.text = jsonArrayRecord.getJSONObject(0).getInt("silver_medal").toString()
        bronze_medal.text = jsonArrayRecord.getJSONObject(0).getInt("bronze_medal").toString()
        completion_race.text = jsonArrayRecord.getJSONObject(0).getInt("completion_race").toString()
        giveup_race.text = jsonArrayRecord.getJSONObject(0).getInt("giveup_race").toString()
        raceCount.text = jsonArrayRecord.getJSONObject(0).getInt("raceCount").toString()
    }


    fun coroutineUserProfile (userId: Int) {

        CoroutineScope(Dispatchers.Main).launch { this
            val roomUserProfile = CoroutineScope(Dispatchers.Default).async { this
                roomUserProfile (userId)
            }.await()

            Log.d(TAG,"80 line "+ roomUserProfile)

            val jsonObject = JSONObject(roomUserProfile)

            setProfileIntroduce (jsonObject)

        }
    }

    fun roomUserProfile (userId: Int) : String  {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL+"/users/profile?userId=$userId&profileUserId=$profileUserId").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }

    fun coroutineRequestFollow (follower: Int, following: Int, requestAt: String) {
        Log.d(TAG,"서버로 저장할 데이터 확인 "+ follower +" "+ following +" "+ requestAt)

        CoroutineScope(Dispatchers.Main).launch { this
            val requestFollow = CoroutineScope(Dispatchers.Default).async { this
                RequestFollow (follower, following, requestAt)
            }.await()
            Log.d(TAG,"80 line "+ requestFollow)

            sendNotiFollow (JSONObject(requestFollow).getInt("result"))


        }
    }

    fun sendNotiFollow (followUniqueNo: Int) {

        var followRequest = Follow_object()
        followRequest.setYouId(profileUserId)
        followRequest.setYouNickname(profileUserNickname)
        followRequest.setMeId(MainActivity.loginId)
        followRequest.setMeNickname(MainActivity.loginNickname)
        followRequest.setUniqueNo(followUniqueNo)


        var notiTitle = ""
        var notiMessage = ""

        if (recordOpenStatus == 1) { // 공개 계정

            notiTitle = "팔로우"
            notiMessage = MainActivity.loginNickname + " 님께서 회원님을 팔로우 했습니다."

            requestFollowBT.visibility = View.GONE
            acceptFollowBT.visibility = View.VISIBLE

            Toast.makeText(baseContext,"팔로우 했습니다.", Toast.LENGTH_SHORT).show()

        } else {                     // 비공개 계정

            notiTitle = "팔로우 요청"
            notiMessage = MainActivity.loginNickname + " 님께서 회원님을 팔로우 하고 싶어합니다."

            requestFollowBT.visibility = View.GONE
            RequestedFollowBT.visibility = View.VISIBLE

            Toast.makeText(baseContext,"팔로우 요청을 했습니다.", Toast.LENGTH_SHORT).show()

        }

        // 초대한 유저 noti (FCM) 전송
        SendNotification.sendNotification(profileUserToken, notiTitle, notiMessage,"1", followRequest)

    }

    fun RequestFollow (follower: Int, following: Int, requestAt: String) : String  {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL + "/follow/requestFollow?follower_id=$follower&following_id=$following&request_at=$requestAt&following_nickname=${MainActivity.loginNickname}").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }
}