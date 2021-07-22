
package com.example.withrun

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.withrun.oftenUseMethod.secondsToTime
import kotlinx.android.synthetic.main.activity_home.*
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class MyRankingAdapter(var context: Context, val rankingRecord: ArrayList<class_RaceRecord>, val itemClick: (class_RaceRecord) -> Unit ) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder  {

        return if (viewType == VIEW_TYPE_ITEM) {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_race_activity, parent, false)
            ItemViewHolder(view, itemClick)
        } else {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loading, parent, false)
            LoadingViewHolder(view)
        }
    }


    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder , position: Int) {

        if (viewHolder is ItemViewHolder) {
            val holder: ItemViewHolder = viewHolder
            holder.onBind(rankingRecord[position],context)

        } else if (viewHolder is LoadingViewHolder) {
            showLoadingView(viewHolder, position)
        }
    }

    override fun getItemCount(): Int {

        return rankingRecord.size
    }

    private inner class ItemViewHolder(itemView: View?, itemClick: (class_RaceRecord) -> Unit) : RecyclerView.ViewHolder(itemView!!) {

        val profileImgNewsfeed = itemView?.findViewById<ImageView>(R.id.profileImgNewsfeed)
        val nicknameNewsfeed = itemView?.findViewById<TextView>(R.id.nicknameNewsfeed)
        val newsfeedUploadDate = itemView?.findViewById<TextView>(R.id.newsfeedUploadDate)
        val raceTimeNewsfeed = itemView?.findViewById<TextView>(R.id.raceTimeNewsfeed)
        val rankNewsfeed = itemView?.findViewById<TextView>(R.id.rankNewsfeed)
        val raceDistanceraceNewsfeed = itemView?.findViewById<TextView>(R.id.raceDistanceraceNewsfeed)
        val racePaceNewsfeed = itemView?.findViewById<TextView>(R.id.racePaceNewsfeed)
        val raceRankNewsfeed = itemView?.findViewById<ImageView>(R.id.raceRankNewsfeed)
        val mapNewsfeed = itemView?.findViewById<ImageView>(R.id.mapNewsfeed)
        val deleteButton = itemView?.findViewById<ImageView>(R.id.deleteButton)


        @SuppressLint("ResourceAsColor")
        fun onBind (record: class_RaceRecord, context: Context) {
            itemView.setOnClickListener{ itemClick(record) }

            if (record.getProfileImgPath() == "else") {
                profileImgNewsfeed!!.setImageResource(R.drawable.user)
            } else {
                Glide.with(context)
                    .load(Constants.URL + record.getProfileImgPath())
                    .into(profileImgNewsfeed!!)
            }

            nicknameNewsfeed?.text = record.getRecordNickname()

            var distanceKm = (record.getDistance()!! / 100)
            var distanceMT = oftenUseMethod.distanceDigitString((record.getDistance()!!).toLong())

            raceDistanceraceNewsfeed?.text = distanceKm.toString() + "." + distanceMT + " km"
            raceTimeNewsfeed?.text = (oftenUseMethod.twoDigitString((record.getRaceTime()!! / (60 * 60) % 24).toLong()).toString() + " : " + oftenUseMethod.twoDigitString((record.getRaceTime()!! / 60 % 60).toLong()) + " : "
                    + oftenUseMethod.twoDigitString((record.getRaceTime()!! % 60).toLong()))

            racePaceNewsfeed?.text =  record.getRaceAvgPaceString()

            if ( record.getMyRanking() == 1 ) { // 금메달
                raceRankNewsfeed?.setImageResource(R.drawable.medal_gold)
            } else if ( record.getMyRanking() == 2 ) { // 은메달
                raceRankNewsfeed?.setImageResource(R.drawable.medal_silver)
            } else if ( record.getMyRanking() == 3 ) { // 동메달
                raceRankNewsfeed?.setImageResource(R.drawable.medal_bronze)
            } else { // 순위권 밖
                raceRankNewsfeed?.setImageResource(R.drawable.medal_badge)
            }

            if (record.getMapSnapshot() == "") {
                mapNewsfeed!!.visibility = View.GONE
            } else {
                Glide.with(context)
                    .load(Constants.URL + record.getMapSnapshot())
                    .into(mapNewsfeed!!)
            }

            if (record.getMyRanking() == 0) {
                rankNewsfeed?.text = "순위 없음"
            } else {
                rankNewsfeed?.text = record.getMyRanking().toString() + " 위"
            }

            newsfeedUploadDate?.text = formatTimeString(oftenUseMethod.timeStringToLong(record.getGameFinishTime()!!))
//            deleteButton?.setOnClickListener {
//
//
//
//            }
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
        return if (rankingRecord[position].getIsLoading() == 0) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    private object TIME_MAXIMUM {
        const val SEC = 60
        const val MIN = 60
        const val HOUR = 24
        const val DAY = 30
        const val MONTH = 12
    }

    fun formatTimeString(regTime: Long): String? {
        val curTime = System.currentTimeMillis()
        var diffTime = (curTime - regTime) / 1000
        var msg: String? = null
        if (diffTime < TIME_MAXIMUM.SEC) {
            msg = "방금 전"
        } else if (TIME_MAXIMUM.SEC.let { diffTime /= it; diffTime } < TIME_MAXIMUM.MIN) {
            msg = diffTime.toString() + "분 전"
        } else if (TIME_MAXIMUM.MIN.let { diffTime /= it; diffTime } < TIME_MAXIMUM.HOUR) {
            msg = diffTime.toString() + "시간 전"
        } else if (TIME_MAXIMUM.HOUR.let { diffTime /= it; diffTime } < TIME_MAXIMUM.DAY) {
            msg = diffTime.toString() + "일 전"
        } else if (TIME_MAXIMUM.DAY.let { diffTime /= it; diffTime } < TIME_MAXIMUM.MONTH) {
            msg = diffTime.toString() + "달 전"
        } else {
            msg = diffTime.toString() + "년 전"
        }
        return msg
    }

    fun newsfeedUploadDateToString (daysGap: Long): String {

        var daysGapString = ""

        var daysGapLong = TimeUnit.MILLISECONDS.toDays(daysGap)
        System.out.println("daysGapLong 확인 " + daysGapLong)

        if ( DateUtils.isToday(daysGap) ) {
            daysGapString = "오늘 공유됨"
        } else {
            if (daysGapLong.toInt() == 0) {
                daysGapString = "1일 전 공유됨"
            } else if (daysGapLong > 30) {
                daysGapString = "한달 전 공유됨"
            } else if (daysGapLong > 60) {
                daysGapString = "두달 전 공유됨"
            } else if (daysGapLong > 90) {
                daysGapString = "세달 전 공유됨"
            } else {
                daysGapString = daysGapLong.toString() + "일 전 공유됨"
            }
        }

        return daysGapString
    }

    // 현재와 날자 차이를 구함 return 값 day
    fun calculateDaysBetweenCurrent (dateStr: String): Long {

        val endDateValue = Date()

        val sdf = SimpleDateFormat("yyyy-MM-dd kk:mm:ss")
        val startDateValue: Date = sdf.parse(dateStr)

        val diff: Long = endDateValue.getTime() - startDateValue.getTime()
        System.out.println("날자 차이 출력 " + diff)


        return diff
    }

}
