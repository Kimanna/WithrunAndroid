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
import com.example.withrun.oftenUseMethod.secondsToTimeRecord


class InvitationAdapter(val context: Context, val userList: ArrayList<RoomIntoUser>) :
    RecyclerView.Adapter<InvitationAdapter.ManagerHolder>() {

    val TAG:String = "InvitationAdapter"

    interface ItemClick {
        fun onClick(view: View, position: Int)
    }

    var itemClick: ItemClick? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManagerHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_invitation_user, parent, false)
        return ManagerHolder(view)
    }

    override fun onBindViewHolder(holder: ManagerHolder, position: Int) {

        if (userList.get(position).getProfileImgUrl() != "else") {
            Glide.with(context)
                .load(Constants.URL +userList.get(position).getProfileImgUrl())
                .into(holder.ProfileImgItemInvitation!!)
        } else {
            holder.ProfileImgItemInvitation!!.setImageResource(R.drawable.avatar)
        }
        holder.nicknameItemInvitation!!.setText(userList.get(position).getNickname())

        if (userList.get(position).getInvited()!!) {
            holder.Invited!!.visibility = View.VISIBLE
        } else {
            holder.InvitationBT!!.visibility = View.VISIBLE
        }

        if (userList.get(position).getAvgPace() == 0) {

            holder.avgPaceInvitation!!.setText("평균페이스 : " + userList.get(position).getAvgPace()!!.toLong())

        } else {

            holder.avgPaceInvitation!!.setText("평균페이스 : " + secondsToTimeRecord(userList.get(position).getAvgPace()!!.toLong()))

        }

        if(itemClick != null)
        {
            holder.InvitationBT!!.setOnClickListener { v ->
                itemClick?.onClick(v, position)

                Log.d(TAG,"클릭 포지션 넘버 : " +position.toString())

                holder.InvitationBT!!.visibility = View.GONE
                holder.Invited!!.visibility = View.VISIBLE

            }
        }
    }

    override fun getItemCount(): Int {

        return userList.size
    }

    inner class ManagerHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        val ProfileImgItemInvitation = itemView?.findViewById<ImageView>(R.id.ProfileImgItemInvitation)
        val nicknameItemInvitation = itemView?.findViewById<TextView>(R.id.nicknameItemInvitation)
        val avgPaceInvitation = itemView?.findViewById<TextView>(R.id.avgPaceInvitation)
        val InvitationBT = itemView?.findViewById<Button>(R.id.InvitationBT)
        val Invited = itemView?.findViewById<Button>(R.id.Invited)

//        @SuppressLint("ResourceAsColor")
//        fun bind(user: RoomIntoUser, context: Context) {
//
//            if (user.getRoomManager()!!) {
//                itemView.run { setBackgroundColor(R.color.colorSlateGray) }
//                chooseManager?.visibility = View.VISIBLE
//            }
//
//
//            if (itemClick != null) {
//                itemView?.setOnClickListener { view ->
//                    itemClick?.onClick(view, position)
//
//                    Log.d(TAG, "어댑터 클릭 ")
//
//                }
//            }
//
//
//            if (user.getProfileImgUrl() != "else") {
//                if (ProfileImgItemManager != null) {
//                    Glide.with(context)
//                        .load(Constants.URL + user.getProfileImgUrl())
//                        .into(ProfileImgItemManager)
//                }
//            } else {
//                ProfileImgItemManager?.setImageResource(R.drawable.user)
//            }
//
//            nicknameItemManager?.text = user.getNickname()
//
//        }
    }

}
