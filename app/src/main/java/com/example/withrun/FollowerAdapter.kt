
package com.example.withrun

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.withrun.oftenUseMethod.distanceDigitString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import kotlin.collections.ArrayList


class FollowerAdapter(val context: Context, val followList: ArrayList<Follow_object>, val itemClick: (Follow_object) -> Unit ) :
    RecyclerView.Adapter<FollowerAdapter.ActiveHolder>() {
    val TAG: String = "FollowerAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveHolder {

        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(R.layout.item_follower,parent,false)
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
        val nicknameItemfollow = itemView?.findViewById<TextView>(R.id.nicknameItemfollow)
        val deleteFollower = itemView?.findViewById<Button>(R.id.deleteFollower) // 리스트에서 삭제
        val requestfollowText = itemView?.findViewById<TextView>(R.id.requestfollowText) // 맞팔이 아닌경우 클릭하면 팔로우 요청 보내짐
        val requestedfollowText = itemView?.findViewById<TextView>(R.id.requestedfollowText) // 맞팔 요청을 보낸경우 계정 비공개일때 요청을 보냈다고 보여주는 문구


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

            nicknameItemfollow?.text = user.getYouNickname()

            // 유저 삭제하겠냐는 코드
            deleteFollower!!.setOnClickListener {
                user.getYouId()?.let { it1 ->
                    dialogConfirmShow("팔로워를 삭제하시겠어요? \n ${user.getYouNickname()} 님은 회원님의 팔로워리스트에서 삭제된 사실을 알 수 없습니다.", context,
                        it1
                    )
                }
            }

        }
    }

    fun dialogConfirmShow (msg: String, context: Context, youId: Int)  {

        val dlg: AlertDialog.Builder = AlertDialog.Builder(context,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
        dlg.setMessage(msg) // 메시지
        dlg.setNeutralButton("삭제", DialogInterface.OnClickListener { dialog, which ->

            coroutineAcceptFollower(youId, "deleteMyFollowerList")

            for (i in 0 until followList.size) { // 팔로우 거절한 유저를 리스트에서 찾아서 지워줌

                if (followList.get(i).getYouId() == youId) {
                    followList.removeAt(i)
                    break
                }
            }
            notifyDataSetChanged()
        })
        dlg.show()
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




}
