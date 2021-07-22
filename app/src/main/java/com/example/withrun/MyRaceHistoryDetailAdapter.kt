
package com.example.withrun

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import java.time.format.DateTimeFormatter


class MyRaceHistoryDetailAdapter(var context: Context, val RaceRecord: ArrayList<class_RaceRecord>, val itemClick: (class_RaceRecord) -> Unit ) :
    RecyclerView.Adapter<MyRaceHistoryDetailAdapter.ItemViewHolder >() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder  {

        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(R.layout.item_all_member_race_record,parent,false)
        return ItemViewHolder(view, itemClick)
    }


    override fun onBindViewHolder(holder: ItemViewHolder , position: Int) {

        holder?.onBind(RaceRecord[position], context)

    }

    override fun getItemCount(): Int {

        return RaceRecord.size
    }

    inner class ItemViewHolder(itemView: View?, itemClick: (class_RaceRecord) -> Unit) : RecyclerView.ViewHolder(itemView!!) {

        val medalImage = itemView?.findViewById<ImageView>(R.id.medalImage)
        val medaltext = itemView?.findViewById<TextView>(R.id.medaltext)
        val profileimgRaceRecord = itemView?.findViewById<ImageView>(R.id.profileimgRaceRecord)
        val nicknameRaceRecord = itemView?.findViewById<TextView>(R.id.nicknameRaceRecord)
        val raceRecordTime = itemView?.findViewById<TextView>(R.id.raceRecordTime)
        val raceStartTime = itemView?.findViewById<TextView>(R.id.raceStartTime)
        val distanceRecord = itemView?.findViewById<TextView>(R.id.distanceRecord)
        val raceRecordPace = itemView?.findViewById<TextView>(R.id.raceRecordPace)
        val completionStatus = itemView?.findViewById<TextView>(R.id.completionStatus)

        @SuppressLint("ResourceAsColor")
        fun onBind (record: class_RaceRecord, context: Context) {
            itemView.setOnClickListener{ itemClick(record) }

            if ( record.getMyRanking() == 1 ) { // 금메달
                medalImage?.visibility = View.VISIBLE
                medaltext?.visibility = View.GONE

                medalImage?.setImageResource(R.drawable.medal_gold)
            } else if ( record.getMyRanking() == 2 ) { // 은메달
                medalImage?.visibility = View.VISIBLE
                medaltext?.visibility = View.GONE
                medalImage?.setImageResource(R.drawable.medal_silver)
            } else if ( record.getMyRanking() == 3 ) { // 동메달
                medalImage?.visibility = View.VISIBLE
                medaltext?.visibility = View.GONE
                medalImage?.setImageResource(R.drawable.medal_bronze)
            } else { // 중도 포기라 순위가 없음
                medalImage?.visibility = View.GONE
                medaltext?.visibility = View.VISIBLE
                if (record.getMyRanking() == 0) {
                    medaltext?.text = " - "
                } else {
                    medaltext?.text = record.getMyRanking().toString() + " 위"
                }

            }

            if (MainActivity.loginProfileImgPath == "else") {
                profileimgRaceRecord!!.setImageResource(R.drawable.user)
            } else {
                Glide.with(context)
                    .load(Constants.URL + record.getProfileImgPath())
                    .into(profileimgRaceRecord!!)
            }

            nicknameRaceRecord?.text = record.getRecordNickname()

            var raceTotalTime = record.getRaceTime()

            if (raceTotalTime != null) {
                raceRecordTime?.text = (oftenUseMethod.twoDigitString((raceTotalTime / (60 * 60) % 24).toLong()).toString() + " : " + oftenUseMethod.twoDigitString((raceTotalTime / 60 % 60).toLong()) + " : " + oftenUseMethod.twoDigitString((raceTotalTime % 60).toLong()))
            }

            var distanceKm = (record.getDistance()!! / 100)
            var distanceMT = oftenUseMethod.distanceDigitString((record.getDistance()!!).toLong())

            distanceRecord?.text = distanceKm.toString() + "." + distanceMT + " /km"
            raceRecordPace?.text = record.getRaceAvgPaceString() + " 분/km"

            if ( record.getCompleted() == 1 ) {
                completionStatus?.text = "완주"
                completionStatus?.setTextColor(R.color.colorDeepcobaltlbue)
            } else {
                completionStatus?.text = "미완주"
                completionStatus?.setTextColor(R.color.ultimategray)
            }

//            raceStartTime?.text = "start" + record.getGameFinishTime() + "~ finish" + record.getGameFinishTime()
        }
    }



}
