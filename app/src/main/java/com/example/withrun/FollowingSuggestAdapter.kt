
package com.example.withrun

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.provider.ContactsContract
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_profile_introduce.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject


class FollowingSuggestAdapter(val context: Context, val followingSuggestList: ArrayList<Follow_object>, val itemClick: (Follow_object) -> Unit ) :
    RecyclerView.Adapter<FollowingSuggestAdapter.ActiveHolder>() {

    val TAG: String = "FollowingSuggestAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveHolder {

        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(R.layout.item_following_suggest,parent,false)
        return ActiveHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ActiveHolder, position: Int) {

        holder?.bind(followingSuggestList[position], context)

    }

    override fun getItemCount(): Int {

        return followingSuggestList.size
    }

    inner class ActiveHolder(itemView: View?, itemClick: (Follow_object) -> Unit) : RecyclerView.ViewHolder(itemView!!) {

        val ProfileImgItemFollowing = itemView?.findViewById<ImageView>(R.id.ProfileImgItemFollowing)
        val nicknameItemfollowing = itemView?.findViewById<TextView>(R.id.nicknameItemfollowing)
        val suggestExplain = itemView?.findViewById<TextView>(R.id.suggestExplain)
        val followBT = itemView?.findViewById<Button>(R.id.followBT)
        val unfollowingBT = itemView?.findViewById<Button>(R.id.unfollowingBT)


        fun bind (user: Follow_object, context: Context) {
            itemView.setOnClickListener{ itemClick(user) }

            if (user.getYouImg() != "else") {
                if (ProfileImgItemFollowing != null) {
                    Glide.with(context)
                            .load(Constants.URL + user.getYouImg())
                            .into(ProfileImgItemFollowing)
                }
            } else {
                ProfileImgItemFollowing?.setImageResource(R.drawable.user)
            }
            nicknameItemfollowing?.text = user.getYouNickname()
            suggestExplain?.text = user.getAvgPace()

            followBT!!.setOnClickListener {

                coroutineRequestFollow (MainActivity.loginId, user.getYouId()!!, oftenUseMethod.dateFormatSaveDB(System.currentTimeMillis()), user.getYouNickname()!!, user.getFcmToken()!!, user.getRecordOpenStatus()!!)

                // 통신 후 비공개 계정인지 확인하여 팔로잉 혹은 팔로우 요청을 보냄
                when(user.getRecordOpenStatus()){
                    1 -> {                                // 공개 계정
                        unfollowingBT!!.text = "팔로잉"
                    }
                    0 -> {                                // 비공개 계정
                        unfollowingBT!!.text = "요청됨"
                    }
                }

                unfollowingBT?.visibility = View.VISIBLE
                followBT.visibility = View.GONE

            }

            unfollowingBT!!.setOnClickListener {

                coroutineAcceptFollower (user.getYouId(), "unFollow")

                unfollowingBT?.visibility = View.GONE
                followBT.visibility = View.VISIBLE

            }

//            deleteSuggest!!.setOnClickListener {
//
//                coroutinegetHideNewsfeedStatus (user.getYouId()!!, "getHideNewsfeedStatus")
//
//
//            }

        }
    }



    fun coroutinegetHideNewsfeedStatus(youId: Int?, action: String) {
        Log.d(TAG, "coroutinegetHideNewsfeedStatus ()")

        CoroutineScope(Dispatchers.Main).launch {
            this
            val follows = CoroutineScope(Dispatchers.Default).async { this
                saveFollows(youId, action)
            }.await()
            Log.d(TAG, "coroutinegetHideNewsfeedStatus 정보 있음" + follows)

        }
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

    fun coroutineRequestFollow (follower: Int, following: Int, requestAt: String, followingNickname: String, followingFCMtoken: String, followingRecordOpenState: Int) {
        Log.d(TAG,"서버로 저장할 데이터 확인 "+ follower +" "+ following +" "+ requestAt)

        CoroutineScope(Dispatchers.Main).launch { this
            val requestFollow = CoroutineScope(Dispatchers.Default).async { this
                RequestFollow (follower, following, requestAt)
            }.await()

            Log.d(TAG,"80 line "+ requestFollow)

            sendNotiFollow (JSONObject(requestFollow).getInt("result"), follower, following, followingNickname, followingFCMtoken, followingRecordOpenState)

        }
    }

    fun sendNotiFollow (followUniqueNo: Int, follower: Int, following: Int, followingNickname: String, followingFCMtoken: String, followingRecordOpenState: Int) {
        Log.d(TAG,"sendNotiFollow "+ followUniqueNo + " " + following + " " + followingNickname + " " + followingFCMtoken + " " + followingRecordOpenState)

        var followRequest = Follow_object()
        followRequest.setYouId(following)
        followRequest.setYouNickname(followingNickname)
        followRequest.setMeId(MainActivity.loginId)
        followRequest.setMeNickname(MainActivity.loginNickname)
        followRequest.setUniqueNo(followUniqueNo)


        var notiTitle = ""
        var notiMessage = ""

        if (followingRecordOpenState == 1) { // 공개 계정

            notiTitle = "팔로우"
            notiMessage = MainActivity.loginNickname + " 님께서 회원님을 팔로우 했습니다."

        } else {                     // 비공개 계정

            notiTitle = "팔로우 요청"
            notiMessage = MainActivity.loginNickname + " 님께서 회원님을 팔로우 하고 싶어합니다."

        }

        // 초대한 유저 noti (FCM) 전송
        SendNotification.sendNotification(followingFCMtoken, notiTitle, notiMessage,"1", followRequest)

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
