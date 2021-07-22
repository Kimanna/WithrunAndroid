package com.example.withrun

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide


class TransferManagerAdapter(val context: Context, val userList: ArrayList<RoomIntoUser>) :
    RecyclerView.Adapter<TransferManagerAdapter.ManagerHolder>() {

    val TAG:String = "TransferManagerAdapter"
    var row_index: Int? = null

    interface ItemClick {
        fun onClick(view: View, position: Int)
    }

    var itemClick: ItemClick? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManagerHolder {

        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(R.layout.item_transfer_manager, parent, false)
        return ManagerHolder(view)
    }

    override fun onBindViewHolder(holder: ManagerHolder, position: Int) {


        if(itemClick != null)
        {
            holder?.itemView?.setOnClickListener { v ->
                itemClick?.onClick(v, position)

                row_index = position
                notifyDataSetChanged()

            }
        }

        if (userList.get(position).getProfileImgUrl() != "else") {
            Glide.with(context)
                .load(Constants.URL +userList.get(position).getProfileImgUrl())
                .into(holder.ProfileImgItemManager!!)
        } else {
            holder.ProfileImgItemManager!!.setImageResource(R.drawable.avatar)
        }
        holder.nicknameItemManager!!.setText(userList.get(position).getNickname())


        if ( row_index == null ) {

            if (userList.get(position).getRoomManager()!!) {
                holder.chooseManager?.visibility = View.VISIBLE
            } else {
                holder.chooseManager?.visibility = View.GONE
            }

        } else if ( row_index == position ) {
            holder.chooseManager?.visibility = View.VISIBLE
        } else {
            holder.chooseManager?.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {

        return userList.size
    }

    inner class ManagerHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        val ProfileImgItemManager = itemView?.findViewById<ImageView>(R.id.ProfileImgItemManager)
        val nicknameItemManager = itemView?.findViewById<TextView>(R.id.nicknameItemManager)
        val chooseManager = itemView?.findViewById<ImageView>(R.id.chooseManager)

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
