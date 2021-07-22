
package com.example.withrun

import android.content.Context
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient


class NotificationAdapter(val context: Context, val notiList: ArrayList<class_Noti>, val itemClick: (class_Noti) -> Unit ) :
    RecyclerView.Adapter<NotificationAdapter.Holder>() {

    val TAG: String = "NotificationAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(R.layout.item_noti,parent,false)
        return Holder(view, itemClick)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {

        holder.deleteNotiBT!!.setOnClickListener {

            val popupMenu = PopupMenu(context, holder.itemView, Gravity.RIGHT)
            popupMenu.inflate(R.menu.delete)

            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.deleteBT -> {
                        coroutineRemoveNoti(notiList.get(position).getUniqueNo()!!)
                        notiList.removeAt(position)
                        notifyDataSetChanged()
                        true
                    }
                    else -> false
                }
            })
            popupMenu.show()
        }


        holder?.bind(notiList[position], context)
    }

    override fun getItemCount(): Int {

        return notiList.size
    }

    inner class Holder(itemView: View?, itemClick: (class_Noti) -> Unit) : RecyclerView.ViewHolder(itemView!!) {

//        val notiProfileImgItem = itemView?.findViewById<ImageView>(R.id.notiProfileImgItem)
        val notiMessage = itemView?.findViewById<TextView>(R.id.notiMessage)
        val notiTimeItem = itemView?.findViewById<TextView>(R.id.notiTimeItem)

        val notiActiveIcon = itemView?.findViewById<ImageView>(R.id.notiActiveIcon)
        val notiInvitationIcon = itemView?.findViewById<ImageView>(R.id.notiInvitationIcon)
        val notiFollowIcon = itemView?.findViewById<ImageView>(R.id.notiFollowIcon)
        val deleteNotiBT = itemView?.findViewById<ImageView>(R.id.deleteNotiBT)


        fun bind (noti: class_Noti, context: Context) {
            itemView.setOnClickListener{ itemClick(noti) }

//            if (noti.get() != "else") {
//                if (notiProfileImgItem != null) {
//                    Glide.with(context)
//                            .load(Constants.URL + noti.getRoomImgPath())
//                            .into(notiProfileImgItem)
//                }
//            } else {
//                notiProfileImgItem?.setImageResource(R.drawable.avatar)
//            }

            notiMessage?.text = noti.getMessage()
            notiTimeItem?.text = noti.getReceivedAt()

            if ( noti.getNotiType() == "invitation" ) {
                notiInvitationIcon!!.visibility = View.VISIBLE
                notiActiveIcon!!.visibility = View.GONE
                notiFollowIcon!!.visibility = View.GONE
            }
            if ( noti.getNotiType() == "follow" ) {
                notiInvitationIcon!!.visibility = View.GONE
                notiActiveIcon!!.visibility = View.GONE
                notiFollowIcon!!.visibility = View.VISIBLE
            }
            if ( noti.getNotiType() == "active") {
                notiInvitationIcon!!.visibility = View.GONE
                notiActiveIcon!!.visibility = View.VISIBLE
                notiFollowIcon!!.visibility = View.GONE
            }

        }
    }

    private fun coroutineRemoveNoti(uniqueNo: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            this
            val noti = CoroutineScope(Dispatchers.Default).async {
                this
                removeNoti(uniqueNo)
            }.await()
            Log.d(TAG, "noti 정보 " + noti)

        }
    }

    fun removeNoti(uniqueNo: Int): String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder()
            .url(Constants.URL + "/notification/deleteNoti?userId=${MainActivity.loginId}&uniqueNo=$uniqueNo")
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
