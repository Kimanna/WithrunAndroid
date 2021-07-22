
package com.example.withrun

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.withrun.oftenUseMethod.secondsToTime
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class MyRaceHistoryAdapter(var context: Context, val RaceRecord: ArrayList<class_RaceRecord>, val itemClick: (class_RaceRecord) -> Unit ) :
    RecyclerView.Adapter<MyRaceHistoryAdapter.ItemViewHolder >() {

    val TAG: String = "MyRaceHistoryAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder  {

        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(R.layout.item_my_race_record,parent,false)
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
        val medalText = itemView?.findViewById<TextView>(R.id.medalText)
        val myRanking = itemView?.findViewById<TextView>(R.id.myRanking)
        val paceRace = itemView?.findViewById<TextView>(R.id.paceRace)
        val raceTime = itemView?.findViewById<TextView>(R.id.raceTime)
        val raceTotalDistance = itemView?.findViewById<TextView>(R.id.raceTotalDistance)
        val raceStartTime = itemView?.findViewById<TextView>(R.id.raceStartTime)

        fun onBind (record: class_RaceRecord, context: Context) {
            itemView.setOnClickListener{ itemClick(record) }

            if ( record.getMyRanking() == 1 ) { // 금메달
                medalImage?.setImageResource(R.drawable.medal_gold)
                medalText?.text = "금메달"
            } else if ( record.getMyRanking() == 2 ) { // 은메달
                medalImage?.setImageResource(R.drawable.medal_silver)
                medalText?.text = "은메달"
            } else if ( record.getMyRanking() == 3 ) { // 동메달
                medalImage?.setImageResource(R.drawable.medal_bronze)
                medalText?.text = "동메달"
            } else { // 순위권 밖
                medalImage?.setImageResource(R.drawable.medal_badge)
                medalText?.text = "순위 없음"
            }

            var startDate = record.getGameFinishTime()!!

            var totalDistance = record.getDistance()!!
            var totalRaceTime = record.getRaceTime()!!


            if (record.getMyRanking() == 0) {
                myRanking?.text = "- 위 (" + record.getMemberCount() + "명)"
            } else {
                myRanking?.text = record.getMyRanking().toString() + "위 (" + record.getMemberCount() + "명)"
            }
            paceRace?.text = record.getRaceAvgPaceString() + " 분/km"
            raceTime?.text = (oftenUseMethod.twoDigitString((totalRaceTime / (60 * 60) % 24).toLong()).toString() + " : " + oftenUseMethod.twoDigitString((totalRaceTime / 60 % 60).toLong()) + " : "
                    + oftenUseMethod.twoDigitString((totalRaceTime % 60).toLong()))

            raceTotalDistance?.text = "( 총 거리 " + (record.getDistance()?.div(100)).toString() +"."+ oftenUseMethod.distanceDigitString((totalDistance).toLong()) + " km )"
            raceStartTime?.text = startDate.substring(2,10)

        }
    }



}
