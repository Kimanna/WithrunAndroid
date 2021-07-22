package com.example.withrun

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject

class Ranking : AppCompatActivity() {

    val TAG : String = "Ranking"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        bottomnavigation.setSelectedItemId(R.id.Ranking)
        bottomnavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Home -> {
                    startActivity(Intent(applicationContext,Home::class.java))
                    finish()
                    true
                }
                R.id.Ranking -> {
                    true
                }
                R.id.MyRanking -> {
                    startActivity(Intent(applicationContext,MyRanking::class.java))
                    finish()
                    true
                }
                R.id.Profile -> {
                    startActivity(Intent(applicationContext,Profile::class.java))
                    finish()
                    true
                }
            }
            true
        }

        coroutineGetRanking (10)

    }


    fun recyclerView (list: ArrayList<class_Ranking>) {

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = RankingAdapter(this, list) { class_Ranking ->


        }
    }

    fun coroutineGetRanking (pageNo: Int) {

        CoroutineScope(Dispatchers.Main).launch { this
            val rankingRecord = CoroutineScope(Dispatchers.Default).async { this
                // network
                getRanking(pageNo)
            }.await()

            val jsonObject = JSONObject(rankingRecord)
            val existResult = jsonObject.getString("existResult")

            if (existResult == "0") {
                Log.d(TAG, "ranking 정보 없음")
            } else {
                Log.d(TAG, "ranking 정보 있음" + rankingRecord)

                val list = ArrayList<class_Ranking>()

                val jsonArray = jsonObject.getJSONArray("result")
                Log.d(TAG, "포문 전 jsonArray size - "+jsonArray.length().toString())

                for (i in 0..jsonArray.length()-1) {

                    var userRanking = class_Ranking()
                    userRanking.setId(jsonArray.getJSONObject(i).getInt("id"))
                    userRanking.setNickname(jsonArray.getJSONObject(i).getString("nickname"))
                    userRanking.setProfileImgPath(jsonArray.getJSONObject(i).getString("profileImgPath"))
                    userRanking.setGoldMedal(jsonArray.getJSONObject(i).getInt("gold_medal"))
                    userRanking.setSilverMedal(jsonArray.getJSONObject(i).getInt("silver_medal"))
                    userRanking.setBronzeMedal(jsonArray.getJSONObject(i).getInt("bronze_medal"))

                    list.add(userRanking)
                } // for 문 끝남

                Log.d(TAG, "포문 끝난 후 arraylist 사이즈 확인 - "+ list.size.toString())

                // 리싸이클러뷰 어댑터 실행코드 + 클릭리스너
                recyclerView (list)

            }
        }
    }

    fun getRanking (pageNo: Int) : String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL + "/record/allUserRanking").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }
}