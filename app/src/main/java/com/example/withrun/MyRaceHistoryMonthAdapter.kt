package com.example.withrun

import android.content.Context
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_profile.*

class MyRaceHistoryMonthAdapter (context: Context) : RecyclerView.Adapter<MyRaceHistoryMonthAdapter.Rv1Holder>() {

    val context = context
    var monthList = mutableListOf<class_RaceRecordParent>()


    inner class Rv1Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val raceMonth = itemView?.findViewById<TextView>(R.id.raceMonth)
        val raceDistance = itemView?.findViewById<TextView>(R.id.raceDistance)
        val raceCount = itemView?.findViewById<TextView>(R.id.raceCount)
        val raceRecyclerview = itemView?.findViewById<RecyclerView>(R.id.raceRecyclerview)

        fun setData(data: class_RaceRecordParent) {


            raceMonth.text = data.raceMonth
            raceDistance.text = (data.raceDistance?.div(100)).toString() +"."+ oftenUseMethod.distanceDigitString((data.raceDistance).toLong()) + " km "
            raceCount.text = "경기 횟수 " + data.raceCount.toString()

            val daysForRV = MyRaceHistoryAdapter(context, data.raceList) {record ->

                goRaceHistoryDetail (record.getRoomNo()!!)

            }

            raceRecyclerview.layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
            raceRecyclerview.adapter = daysForRV
            daysForRV.notifyDataSetChanged()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Rv1Holder {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.item_my_race_record_parent,parent,false)
        return Rv1Holder(view)
    }

    override fun getItemCount(): Int {
        return monthList.size
    }

    override fun onBindViewHolder(holder: Rv1Holder, position: Int) {
        holder.setData(monthList[position])
    }

    fun goRaceHistoryDetail (roomNo: Int) {

        val intent = Intent(context, MyRaceHistoryDetail::class.java)
        intent.putExtra("location","MyRaceHistory")
        intent.putExtra("roomNo",roomNo)
        intent.putExtra("userId",MainActivity.loginId)
        context.startActivity(intent)

    }
}