
package com.example.withrun

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.withrun.oftenUseMethod.distanceDigitString
import kotlin.collections.ArrayList


class ActiveRankingAdapter (val context: Context, val userList: ArrayList<RoomIntoUser>, val itemClick: (RoomIntoUser) -> Unit ) :
    RecyclerView.Adapter<ActiveRankingAdapter.ActiveHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveHolder {

        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(R.layout.item_running_rank,parent,false)
        return ActiveHolder(view, itemClick)

    }

    override fun onBindViewHolder(holder: ActiveHolder, position: Int) {

        holder?.bind(userList[position], context)

    }

    override fun getItemCount(): Int {

        return userList.size
    }

    inner class ActiveHolder(itemView: View?, itemClick: (RoomIntoUser) -> Unit) : RecyclerView.ViewHolder(itemView!!) {

        val outerLayout = itemView?.findViewById<LinearLayout>(R.id.outerLayout)
        val RankingProfileImgItem = itemView?.findViewById<ImageView>(R.id.RankingProfileImgItem)
        val rankingNo = itemView?.findViewById<TextView>(R.id.rankingNo)
        val RankingNicknameItem = itemView?.findViewById<TextView>(R.id.RankingNicknameItem)
        val gap = itemView?.findViewById<TextView>(R.id.gap)

        val joinRunning = itemView?.findViewById<TextView>(R.id.joinRunning)
        val notJoinRunning = itemView?.findViewById<TextView>(R.id.notJoinRunning)
        val exitRunning = itemView?.findViewById<TextView>(R.id.exitRunning)
        val finishRunning = itemView?.findViewById<TextView>(R.id.finishRunning)
        val notComeRunning = itemView?.findViewById<TextView>(R.id.notComeRunning)

        fun bind (user: RoomIntoUser, context: Context) {
            itemView.setOnClickListener{ itemClick(user) }

            if(user.getId() == MainActivity.loginId){
                outerLayout?.setBackgroundColor(ContextCompat.getColor(context, R.color.mint))
            } else {
                outerLayout?.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }

            if (user.getProfileImgUrl() != "else") {
                if (RankingProfileImgItem != null) {
                    Glide.with(context)
                            .load(Constants.URL + user.getProfileImgUrl())
                            .into(RankingProfileImgItem)
                }
            } else {
                RankingProfileImgItem?.setImageResource(R.drawable.user)
            }

            if (user.getMyActiveRaceRank() == 0) {
                rankingNo?.text = "-"
            } else {
                rankingNo?.text = user.getMyActiveRaceRank().toString()
            }

//            rankingNo?.text = (adapterPosition + 1).toString()
            RankingNicknameItem?.text = user.getNickname()
            gap?.text = "${(user.getDistanceGap()?.div(100))}.${user.getDistanceGap()?.toLong()?.let { distanceDigitString(it) }} km"

            // 유저 상태 변경  0 - 완주 1-입장, 2-중도나감 3 -입장전 4 - 미참
            if (user.getRunningState() == 0) {
                joinRunning!!.visibility = View.GONE // 러닝중
                notJoinRunning!!.visibility = View.GONE // 입장전
                exitRunning!!.visibility = View.GONE // 미완주
                finishRunning!!.visibility = View.VISIBLE // 완주
                notComeRunning!!.visibility = View.GONE // 미참
            } else if (user.getRunningState() == 1) {
                joinRunning!!.visibility = View.VISIBLE
                notJoinRunning!!.visibility = View.GONE
                exitRunning!!.visibility = View.GONE
                finishRunning!!.visibility = View.GONE
                notComeRunning!!.visibility = View.GONE
            } else if (user.getRunningState() == 2){
                joinRunning!!.visibility = View.GONE
                notJoinRunning!!.visibility = View.GONE
                exitRunning!!.visibility = View.VISIBLE
                finishRunning!!.visibility = View.GONE
                notComeRunning!!.visibility = View.GONE
            } else if (user.getRunningState() == 3){
                joinRunning!!.visibility = View.GONE
                notJoinRunning!!.visibility = View.VISIBLE
                exitRunning!!.visibility = View.GONE
                finishRunning!!.visibility = View.GONE
                notComeRunning!!.visibility = View.GONE
            } else {
                joinRunning!!.visibility = View.GONE
                notJoinRunning!!.visibility = View.GONE
                exitRunning!!.visibility = View.GONE
                finishRunning!!.visibility = View.GONE
                notComeRunning!!.visibility = View.VISIBLE
            }
        }
    }



}
