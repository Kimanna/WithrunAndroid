
package com.example.withrun

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject


class FollowingAdapter(val context: Context, val followingList: ArrayList<Follow_object>, val itemClick: (Follow_object) -> Unit ) :
    RecyclerView.Adapter<FollowingAdapter.ActiveHolder>() {

    val TAG: String = "FollowingAdapter"

    var hide_newsfeed: Int? = null



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveHolder {

        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(R.layout.item_following,parent,false)
        return ActiveHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ActiveHolder, position: Int) {

        holder?.bind(followingList[position], context)

    }

    override fun getItemCount(): Int {

        return followingList.size
    }

    inner class ActiveHolder(itemView: View?, itemClick: (Follow_object) -> Unit) : RecyclerView.ViewHolder(itemView!!) {

        val ProfileImgItemFollowing = itemView?.findViewById<ImageView>(R.id.ProfileImgItemFollowing)
        val nicknameItemfollowing = itemView?.findViewById<TextView>(R.id.nicknameItemfollowing)
        val unfollowingBT = itemView?.findViewById<Button>(R.id.unfollowingBT)
        val refollowingBT = itemView?.findViewById<Button>(R.id.refollowingBT)

        val moreActionBT = itemView?.findViewById<ImageView>(R.id.moreActionBT)


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

            unfollowingBT!!.setOnClickListener {

                val dlg: AlertDialog.Builder = AlertDialog.Builder(context,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                dlg.setMessage(user.getYouNickname()+"님 팔로우를 해제하겠습니까? \n 마음이 바뀌면 "+user.getYouNickname()+"님에게 다시 팔로우 요청을 해야합니다.") // 메시지
                dlg.setNeutralButton("팔로우 해제", DialogInterface.OnClickListener { dialog, which ->

                    coroutineAcceptFollower(user.getYouId(), "unFollow")
                    unfollowingBT.visibility = View.GONE
                    refollowingBT!!.visibility = View.VISIBLE
                })
                dlg.setNegativeButton("취소", DialogInterface.OnClickListener{ dialog, which->
                })
                dlg.show()

            }

            refollowingBT!!.setOnClickListener {

                coroutineRequestFollow (MainActivity.loginId, user.getYouId()!!, oftenUseMethod.dateFormatSaveDB(System.currentTimeMillis()))
                refollowingBT.visibility = View.GONE
                unfollowingBT.visibility = View.VISIBLE
            }

            moreActionBT!!.setOnClickListener {

                coroutinegetHideNewsfeedStatus (user.getYouId()!!, "getHideNewsfeedStatus")


            }

        }
    }

    fun moreAction (hide_newsfeed_status: Int, youId: Int?, context: Context) {

        val dialog = android.app.AlertDialog.Builder(context).create()
        val edialog: LayoutInflater = LayoutInflater.from(context)
        val mView: View = edialog.inflate(R.layout.following_settings, null)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        val hide_newsfeed: Switch = mView.findViewById(R.id.hide_newsfeed)

        if ( hide_newsfeed_status == 1 ) {
            hide_newsfeed.isChecked = true
        } else {
            hide_newsfeed.isChecked = false
        }

        hide_newsfeed.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if ( isChecked ) {
                coroutineAcceptFollower(youId, "hideNewsfeed")

            } else {
                coroutineAcceptFollower(youId, "viewNewsfeed")
            }
        })

        dialog.setView(mView)
        dialog.create()
        dialog.show()

    }

    fun coroutinegetHideNewsfeedStatus(youId: Int?, action: String) {
        Log.d(TAG, "coroutinegetHideNewsfeedStatus ()")

        CoroutineScope(Dispatchers.Main).launch {
            this
            val follows = CoroutineScope(Dispatchers.Default).async { this
                saveFollows(youId, action)
            }.await()
            Log.d(TAG, "coroutinegetHideNewsfeedStatus 정보 있음" + follows)

            val jsonObject = JSONObject(follows).getJSONArray("result").getJSONObject(0)

            if (jsonObject.isNull("hide_newsfeed"))
                return@launch
            else {
                hide_newsfeed = jsonObject.getInt("hide_newsfeed")
                moreAction (hide_newsfeed!!, youId, context)
            }

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

//    fun coroutineGetNewsfeed (following: Int) {
//        Log.d(TAG,"서버로 저장할 데이터 확인 " + following)
//
//        CoroutineScope(Dispatchers.Main).launch { this
//            val hideNewsfeedStatus = CoroutineScope(Dispatchers.Default).async { this
//                GetNewsfeed (MainActivity.loginId, following)
//            }.await()
//
//            Log.d(TAG,"hideNewsfeedStatus 서버통신 값 "+ hideNewsfeedStatus)
//
//        }
//    }
//
//    fun GetNewsfeed (follower: Int, following: Int) : String  {
//
//        val client = OkHttpClient.Builder().build()
//        val req = okhttp3.Request.Builder().url(Constants.URL + "/follow/hideNewsfeed?follower_id=$follower&following_id=$following").build()
//        client.newCall(req).execute().use {
//                response -> return if(response.body != null) {
//            response.body!!.string()
//        }
//        else {
//            "body null"
//        }
//        }
//    }


}
