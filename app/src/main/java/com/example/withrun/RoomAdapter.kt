
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
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_my_race_history_detail.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import kotlin.collections.ArrayList

class RoomAdapter(val context: Context, val roomList: ArrayList<RoomItem>, val itemClick: (RoomItem) -> Unit ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TAG: String = "RoomAdapter"

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == VIEW_TYPE_ITEM) {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
            Holder(view, itemClick)
        } else {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_loading, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {

        if (viewHolder is Holder) {
            val holder: Holder = viewHolder
            holder.bind(roomList[position],context)

        } else if (viewHolder is LoadingViewHolder) {
            showLoadingView(viewHolder, position)
        }
    }

    override fun getItemCount(): Int {

        return roomList.size
    }

    private inner class Holder(itemView: View?, itemClick: (RoomItem) -> Unit) : RecyclerView.ViewHolder(itemView!!) {

        val roomPhotoItem = itemView?.findViewById<ImageView>(R.id.roomPhotoItem)
        val roomTitle = itemView?.findViewById<TextView>(R.id.roomTitle)
        val startTimeItem = itemView?.findViewById<TextView>(R.id.startTimeItem)
        val DistanceItem = itemView?.findViewById<TextView>(R.id.DistanceItem)
        val memberItem = itemView?.findViewById<TextView>(R.id.memberItem)
        val genderItem = itemView?.findViewById<TextView>(R.id.genderItem)
        val runDateitem = itemView?.findViewById<TextView>(R.id.runDateitem)
        val levelItem = itemView?.findViewById<TextView>(R.id.levelItem)

        val finishIcon = itemView?.findViewById<ImageView>(R.id.finishIcon)
        val readyIcon = itemView?.findViewById<ImageView>(R.id.readyIcon)
        val onIcon = itemView?.findViewById<ImageView>(R.id.onIcon)
        val deleteRoomBT = itemView?.findViewById<ImageView>(R.id.deleteRoomBT)

        fun bind (room: RoomItem, context: Context) {
            itemView.setOnClickListener{ itemClick(room) }

            if (room.getRoomImgPath() != "else") {
                if (roomPhotoItem != null) {
                    Glide.with(context)
                        .load(Constants.URL + room.getRoomImgPath())
                        .into(roomPhotoItem)
                }
            } else {
                roomPhotoItem?.setImageResource(R.drawable.image)
            }

            if ( room.getActiveGame() == 0 ) {
                readyIcon!!.visibility = View.VISIBLE
            } else if ( room.getActiveGame() == 1 ) {
                onIcon!!.visibility = View.VISIBLE
            } else {
                finishIcon!!.visibility = View.VISIBLE
                deleteRoomBT!!.visibility = View.VISIBLE

                deleteRoomBT.setOnClickListener {

                    val popupMenu = PopupMenu(context, deleteRoomBT, Gravity.RIGHT)
                    popupMenu.inflate(R.menu.delete)

                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.deleteBT -> {

                                coroutineDeleteRoom(room.getNo()!!, MainActivity.loginId)
                                roomList.removeAt(adapterPosition)
                                notifyDataSetChanged()

                                true
                            }
                            else -> false
                        }
                    })
                    popupMenu.show()

                }
            }

            roomTitle?.text = room.getRoomTitle()
            startTimeItem?.text = room.getStartTime()!!.substring(0,5)
            DistanceItem?.text = room.getDistance().toString() + " km"
            runDateitem?.text = room.getStartDate()!!.substring(5,7) + "/" + room.getStartDate()!!.substring(8,10)
            memberItem?.text = room.getMemberCount().toString() + " 명"
            genderItem?.text = room.getSortGender()
            levelItem?.text = room.getSortLevel()
        }
    }

    private inner class LoadingViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var progressBar: ImageView

        init {
            progressBar = itemView.findViewById(R.id.progressBar)
        }
    }

    private fun showLoadingView(
        viewHolder: LoadingViewHolder,
        position: Int
    ) {
        //
    }

    override fun getItemViewType(position: Int): Int {
        return if (roomList[position].getIsLoading() == 0) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    private fun coroutineDeleteRoom (roomNo: Int, userId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            this
            val deleteRecord = CoroutineScope(Dispatchers.Default).async {
                this
                deleteRecord(roomNo, userId)
            }.await()
            Log.d(TAG, "coroutineDeleteRoom SuccessInfo " + deleteRecord)

        }
    }

    fun deleteRecord(roomNo: Int, userId: Int): String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder()
            .url(Constants.URL + "/record/deleteRoom?userId=$userId&roomNo=$roomNo")
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