package com.example.withrun

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.*
import java.net.Socket

class ProfileImgAdapter (var context: Context, val userList: ArrayList<RoomIntoUser>, val aloneMode: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {

    val TAG : String = "ProfileImgAdapter"

    private val TYPE_ITEM : Int = 1
    private val TYPE_FOOTER : Int = 2

    private var onItemClick : View.OnClickListener? = null

    override fun onCreateViewHolder(p0: ViewGroup, viewType: Int): RecyclerView.ViewHolder { //p0 = parent
        if(viewType == TYPE_ITEM) {
            val mainView : View = LayoutInflater.from(p0!!.context).inflate(R.layout.item_room_into_user, p0, false)
            mainView.setOnClickListener(onItemClick)
            return Holder(mainView)
        } else {
            val footerView : View = LayoutInflater.from(p0!!.context).inflate(R.layout.item_room_into_user_footer, p0, false)
            footerView.setOnClickListener(onItemClick)
            return footerHolder(footerView)
        }
    }

    override fun getItemViewType(position: Int): Int {

        return if (position == userList.size) TYPE_FOOTER else TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return userList.size + 1
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if ( holder is footerHolder) {
            var itemHolder1 : footerHolder? = holder


            if ( aloneMode == 0 ) { // 혼자뛰기 모드가 아닌경우 초대이미지를 넣어줌
                itemHolder1!!.ProfileImgItemPooter!!.setImageResource(R.drawable.add_group)
                itemHolder1!!.nicknameItemPooter!!.setText("초대")
            } else { // 혼자뛰기 모드인 경우 러닝 트레이너 이미지를 넣어줌
                itemHolder1!!.ProfileImgItemPooter!!.setImageResource(R.drawable.running)
                itemHolder1!!.nicknameItemPooter!!.setText("러닝트레이너")
            }


        } else  if (holder is Holder) {
            var itemHolder : Holder? = holder

            if (userList.get(position).getProfileImgUrl() != "else") {
                    Glide.with(context)
                        .load(Constants.URL +userList.get(position).getProfileImgUrl())
                        .into(itemHolder!!.ProfileImgItem!!)
            } else {
                itemHolder!!.ProfileImgItem!!.setImageResource(R.drawable.avatar)
            }
            itemHolder.nicknameItem!!.setText(userList.get(position).getNickname())

            if ( userList.get(position).getRoomManager() == true ) {
                itemHolder.roomManager!!.visibility = View.VISIBLE
            }
            if ( userList.get(position).getRoomManager() == false ) {
                itemHolder.roomManager!!.visibility = View.GONE
            }

        }
    }

    fun setOnItemClickListener(l : View.OnClickListener) {
        onItemClick = l
    }


//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
//
//        val layoutInflater = LayoutInflater.from(parent?.context)
//        val view = layoutInflater.inflate(R.layout.item_room_into_user,parent,false)
//        return Holder(view)
//    }
//
//    override fun onBindViewHolder(holder: Holder, position: Int) {
//
//        holder?.bind(userList[position], context)
//    }



    internal class footerHolder(itemView : View?) : RecyclerView.ViewHolder(itemView!!) {

        val ProfileImgItemPooter = itemView?.findViewById<ImageView>(R.id.ProfileImgItemPooter)
        val nicknameItemPooter = itemView?.findViewById<TextView>(R.id.nicknameItemPooter)

    }

    internal class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        val ProfileImgItem = itemView?.findViewById<ImageView>(R.id.ProfileImgItem)
        val roomManager = itemView?.findViewById<ImageView>(R.id.roomManager)
        val nicknameItem = itemView?.findViewById<TextView>(R.id.nicknameItem)

//        fun bind(user: RoomIntoUser, context: Context) {
//
//            if (user.getProfileImgUrl() != "else") {
//                if (ProfileImgItem != null) {
//                    Glide.with(context)
//                        .load(Constants.URL +user.getProfileImgUrl())
//                        .into(ProfileImgItem)
//                }
//            } else {
//                ProfileImgItem?.setImageResource(R.drawable.avatar)
//            }
//
//            ProfileImgItem?.setOnClickListener {
//                user.getId()?.let { it1 -> coroutine (it1) }
//                Toast.makeText(context,"2번 타입", Toast.LENGTH_SHORT).show()
//            }
//
//            nicknameItem?.text = user.getNickname()
//
//        }
    }

}