
package com.example.withrun

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlin.collections.ArrayList


//class RoomAdapter(val context: Context, val roomList: ArrayList<RoomItem>, val itemClick: (RoomItem) -> Unit ) :
//    RecyclerView.Adapter<RoomAdapter.Holder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
//
//        val layoutInflater = LayoutInflater.from(parent?.context)
//        val view = layoutInflater.inflate(R.layout.item_room,parent,false)
//        return Holder(view, itemClick)
//    }
//
//    override fun onBindViewHolder(holder: Holder, position: Int) {
//
//        holder?.bind(roomList[position], context)
//
//    }
//
//    override fun getItemCount(): Int {
//
//        return roomList.size
//    }
//
//    inner class Holder(itemView: View?, itemClick: (RoomItem) -> Unit) : RecyclerView.ViewHolder(itemView!!) {
//
//        val roomPhotoItem = itemView?.findViewById<ImageView>(R.id.roomPhotoItem)
//        val roomTitle = itemView?.findViewById<TextView>(R.id.roomTitle)
//        val startTimeItem = itemView?.findViewById<TextView>(R.id.startTimeItem)
//        val DistanceItem = itemView?.findViewById<TextView>(R.id.DistanceItem)
//        val memberItem = itemView?.findViewById<TextView>(R.id.memberItem)
//        val genderItem = itemView?.findViewById<TextView>(R.id.genderItem)
//        val runDateitem = itemView?.findViewById<TextView>(R.id.runDateitem)
//        val levelItem = itemView?.findViewById<TextView>(R.id.levelItem)
//
//        val finishIcon = itemView?.findViewById<ImageView>(R.id.finishIcon)
//        val readyIcon = itemView?.findViewById<ImageView>(R.id.readyIcon)
//        val onIcon = itemView?.findViewById<ImageView>(R.id.onIcon)
//
//        fun bind (room: RoomItem, context: Context) {
//            itemView.setOnClickListener{ itemClick(room) }
//
//            if (room.getRoomImgPath() != "else") {
//                if (roomPhotoItem != null) {
//                    Glide.with(context)
//                            .load(Constants.URL + room.getRoomImgPath())
//                            .into(roomPhotoItem)
//                }
//            } else {
//                roomPhotoItem?.setImageResource(R.drawable.image)
//            }
//
//            if ( room.getActiveGame() == 0 ) {
//                readyIcon!!.visibility = View.VISIBLE
//            } else if ( room.getActiveGame() == 1 ) {
//                onIcon!!.visibility = View.VISIBLE
//            } else {
//                finishIcon!!.visibility = View.VISIBLE
//            }
//
//
//            roomTitle?.text = room.getRoomTitle()
//            startTimeItem?.text = room.getStartTime()!!.substring(0,5)
//            DistanceItem?.text = room.getDistance().toString() + " km"
//            runDateitem?.text = room.getStartDate()!!.substring(5,7) + "/" + room.getStartDate()!!.substring(8,10)
//            memberItem?.text = room.getMemberCount().toString() + " 명"
//            genderItem?.text = room.getSortGender()
//            levelItem?.text = room.getSortLevel()
//        }
//    }
//}

class RoomAdapter(val context: Context, val roomList: ArrayList<RoomItem>, val itemClick: (RoomItem) -> Unit ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

//        val layoutInflater = LayoutInflater.from(parent?.context)
//        val view = layoutInflater.inflate(R.layout.item_room,parent,false)
//        return Holder(view, itemClick)

        return if (viewType == VIEW_TYPE_ITEM) {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_room, parent, false)
            Holder(view, itemClick)
        } else {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loading, parent, false)
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

        return if (roomList == null) 0 else roomList.size
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


}