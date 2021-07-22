
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
import com.example.withrun.oftenUseMethod.secondsToTime
import kotlinx.android.synthetic.main.activity_home.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder


class RankingAdapter(var context: Context, val rankingRecord: ArrayList<class_Ranking>, val itemClick: (class_Ranking) -> Unit ) :
    RecyclerView.Adapter<RankingAdapter.ItemViewHolder >() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder  {

        val layoutInflater = LayoutInflater.from(parent?.context)
        val view = layoutInflater.inflate(R.layout.item_ranking,parent,false)
        return ItemViewHolder(view, itemClick)
    }


    override fun onBindViewHolder(holder: ItemViewHolder , position: Int) {

        holder?.onBind(rankingRecord[position], context)

    }

    override fun getItemCount(): Int {

        return rankingRecord.size
    }

    inner class ItemViewHolder(itemView: View?, itemClick: (class_Ranking) -> Unit) : RecyclerView.ViewHolder(itemView!!) {

        val rankText = itemView?.findViewById<TextView>(R.id.rankText)
        val profileImgRanking = itemView?.findViewById<ImageView>(R.id.profileImgRanking)
        val nicknameRanking = itemView?.findViewById<TextView>(R.id.nicknameRanking)
        val goldMedalCount = itemView?.findViewById<TextView>(R.id.goldMedalCount)
        val silverMedalCount = itemView?.findViewById<TextView>(R.id.silverMedalCount)
        val bronzeMedalCount = itemView?.findViewById<TextView>(R.id.bronzeMedalCount)
        val medalCountTotal = itemView?.findViewById<TextView>(R.id.medalCountTotal)

        @SuppressLint("ResourceAsColor")
        fun onBind (record: class_Ranking, context: Context) {
            itemView.setOnClickListener{ itemClick(record) }

            if (record.getProfileImgPath() == "else") {
                profileImgRanking!!.setImageResource(R.drawable.user)
            } else {
                Glide.with(context)
                    .load(Constants.URL + record.getProfileImgPath())
                    .into(profileImgRanking!!)
            }

            rankText?.text = (adapterPosition + 1).toString() + " 위"
            nicknameRanking?.text = record.getNickname()
            goldMedalCount?.text = record.getGoldMedal().toString()
            silverMedalCount?.text = record.getSilverMedal().toString()
            bronzeMedalCount?.text = record.getBronzeMedal().toString()
            medalCountTotal?.text = (record.getGoldMedal()!! + record.getSilverMedal()!! + record.getBronzeMedal()!!).toString()

        }
    }

}
