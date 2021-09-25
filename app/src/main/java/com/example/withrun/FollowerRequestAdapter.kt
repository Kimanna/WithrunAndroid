
package com.example.withrun

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject
import kotlin.collections.ArrayList


class FollowerRequestAdapter(val context: Context, val followList: ArrayList<Follow_object>, val itemClick: (Follow_object) -> Unit ) :
    RecyclerView.Adapter<FollowerRequestAdapter.ActiveHolder>() {
    val TAG: String = "FollowerRequestAdapter"

    var isFollowing = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveHolder {

        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(R.layout.item_follower_request,parent,false)
        return ActiveHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ActiveHolder, position: Int) {
        holder?.bind(followList[position], context)
    }

    override fun getItemCount(): Int {
        return followList.size
    }

    inner class ActiveHolder(itemView: View?, itemClick: (Follow_object) -> Unit) : RecyclerView.ViewHolder(itemView!!) {

        val ProfileImgItemFollow = itemView?.findViewById<ImageView>(R.id.ProfileImgItemFollow)
        val nicknameFollowRequest = itemView?.findViewById<TextView>(R.id.nicknameFollowRequest)
        val acceptFollowerBT = itemView?.findViewById<Button>(R.id.acceptFollowerBT) // 팔로우 수락
        val refuseFollower = itemView?.findViewById<Button>(R.id.refuseFollower) // 팔로우 거절 -> 거절시 유저 list 에서 지워줌
        val requestFollowing = itemView?.findViewById<Button>(R.id.requestFollowing) // 팔로우 수락시 보여줄 맞팔버튼
        val followingState = itemView?.findViewById<Button>(R.id.followingState) // 팔로우 수락시 이미 팔로잉 상태일때 보여주는 버튼

        fun bind (user: Follow_object, context: Context) {
            itemView.setOnClickListener{ itemClick(user) }

            if (user.getYouImg() != "else") {
                if (ProfileImgItemFollow != null) {
                    Glide.with(context)
                            .load(Constants.URL + user.getYouImg())
                            .into(ProfileImgItemFollow)
                }
            } else {
                ProfileImgItemFollow?.setImageResource(R.drawable.user)
            }

            nicknameFollowRequest?.text = user.getYouNickname()

            // 팔로우 요청 수락버튼
            acceptFollowerBT!!.setOnClickListener {

                acceptFollowerBT.visibility = View.GONE
                refuseFollower!!.visibility = View.GONE

                // 팔로우 요청 수락 시 해당 follower id와 following id accept정보 저장
                coroutineAcceptFollower(user.getYouId()!!,"acceptFollower")

                // 현재 youId 유저를 팔로잉하고있는 상태인지 확인
               coroutineFindFollowState(user.getYouId()!!,"findMyFollow", requestFollowing, followingState)


            }

            // 팔로우 요청 거절버튼
            // 신청내역 삭제 후 recyclerview 에서 해당 유저 삭제
            refuseFollower!!.setOnClickListener {

                coroutineAcceptFollower(user.getYouId()!!,"refuseFollower")

                for (i in 0 until followList.size) { // 팔로우 거절한 유저를 리스트에서 찾아서 지워줌

                    if (followList.get(i).getYouId() == user.getYouId()) {
                        followList.removeAt(i)
                        break
                    }
                }
                notifyDataSetChanged()
            }

            requestFollowing!!.setOnClickListener {
                coroutineRequestFollow (MainActivity.loginId, user.getYouId()!!, oftenUseMethod.dateFormatSaveDB(System.currentTimeMillis()))
            }
        }
    }



    fun coroutineAcceptFollower(youId: Int, action: String) {
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

    fun saveFollows(youId: Int, action: String): String {

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

    fun coroutineFindFollowState( youId: Int, action: String, requestFollowing: Button?, followingState: Button?) {
        Log.d(TAG, "coroutineAcceptFollower ()")

        CoroutineScope(Dispatchers.Main).launch {
            this
            val follows = CoroutineScope(Dispatchers.Default).async {
                this
                // network
                followsState(youId, action)
            }.await()
            Log.d(TAG, "follow 정보 " + follows)
            val jsonObject = JSONObject(follows)
            isFollowing = jsonObject.getJSONArray("result").getJSONObject(0).getInt("isFollowing")


            // following 상태이면
            if ( jsonObject.getJSONArray("result").getJSONObject(0).getInt("isFollowing") == 1 ) {

                followingState!!.visibility = View.VISIBLE

            } else {

                requestFollowing!!.visibility = View.VISIBLE

            }

        }
    }

    fun followsState (youId: Int, action: String): String {

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

    fun coroutineRequestFollow (follower: Int, following: Int, requestAt: String) {
        Log.d(TAG,"서버로 저장할 데이터 확인 "+ follower +" "+ following +" "+ requestAt)

        CoroutineScope(Dispatchers.Main).launch { this
            val requestFollow = CoroutineScope(Dispatchers.Default).async { this
                RequestFollow (follower, following, requestAt)
            }.await()

            Log.d(TAG,"80 line "+ requestFollow)

        }
    }

    fun RequestFollow (follower: Int, following: Int, requestAt: String) : String  {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL + "/follow/requestFollow?follower_id=$follower&following_id=$following&request_at=$requestAt").build()
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
