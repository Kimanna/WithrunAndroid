package com.example.withrun

import android.content.Intent
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CalendarView
import kotlinx.android.synthetic.main.activity_my_race_history.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MyRaceHistory : AppCompatActivity() {

    val TAG: String = "MyRaceHistory"

    val sdf = SimpleDateFormat("yyyy-MM-dd kk:mm:ss")

    private var adapterRV1: MyRaceHistoryMonthAdapter? = null
    var monthArrayList: MutableList<class_RaceRecordParent>? = null

    val monthlyList: ArrayList<class_RaceRecord> = ArrayList()
    var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_race_history)



        monthArrayList = mutableListOf()


        val items = resources.getStringArray(R.array.my_race_record)
        val myAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

//        categorizeByDate.adapter = myAdapter
//        categorizeByDate.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//
//                //아이템이 클릭 되면 맨 위부터 position 0번부터 순서대로 동작하게 됩니다.
//                when(position) {
//                    0   ->  {
//
//                    }
//                    1   ->  {
//
//                    }
//                    //...
//                    else -> {
//
//                    }
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//
//            }
//        }

        coroutinegetMyRecord ("begin")
        setRecyclerView()

        previousButton?.setOnClickListener {

            nextButton!!.visibility = View.VISIBLE
            if (!viewYearMonth?.getText()?.toString()?.equals("")!!) {

                currentIndex--
                viewYearMonth?.text = monthlyList.get(currentIndex).getMonthlyString()
                coroutinegetMyRecordData(monthlyList.get(currentIndex).getMonthlyString()!!)

                if (currentIndex <= 0) {

                    previousButton!!.visibility = View.GONE

                }

            }
        }

        nextButton?.setOnClickListener {

            previousButton!!.visibility = View.VISIBLE
            if (!viewYearMonth?.getText()?.toString()?.equals("")!!) {

                currentIndex++
                viewYearMonth?.text = monthlyList.get(currentIndex).getMonthlyString()
                coroutinegetMyRecordData(monthlyList.get(currentIndex).getMonthlyString()!!)

                if (currentIndex >= monthlyList.size-1) {

                    nextButton!!.visibility = View.GONE

                }

            }
        }

        nextButton!!.visibility = View.GONE



        back_Profile.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()

        }


    }

    fun setRecyclerView () {

        adapterRV1 = MyRaceHistoryMonthAdapter(this)
        rvRaceHistory.adapter = adapterRV1
        rvRaceHistory.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    fun goRaceHistoryDetail (roomNo: Int) {

        val intent = Intent(this, MyRaceHistoryDetail::class.java)
        intent.putExtra("location","MyRaceHistory")
        intent.putExtra("roomNo",roomNo)
        startActivity(intent)
    }

    fun coroutinegetMyRecord (monthly: String) {
        Log.d(TAG,"coroutineUpdateActiveRoom ()")

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                // network
                getHtml(monthly)
            }.await()

            Log.d(TAG,html)

            val jsonObject = JSONObject(html)
            val existResult = jsonObject.getString("existResult")

            if (existResult == "0") {
                Log.d(TAG, "정보 없음")

                previousButton.visibility = View.GONE
            } else {
                Log.d(TAG, "정보 있음")

                val jsonArrayMonthly = jsonObject.getJSONArray("result_montly_count")

                if (monthlyList.isNullOrEmpty()) {

                    for (i in 0 until jsonArrayMonthly.length()) {

                        var userMonthRecord = class_RaceRecord()

                        userMonthRecord.setMonthlyString(jsonArrayMonthly.getJSONObject(i).getString("monthlyString"))
                        userMonthRecord.setRecordMonthCount(jsonArrayMonthly.getJSONObject(i).getInt("recordMonthCount"))
                        userMonthRecord.setTotalMonthDistance(jsonArrayMonthly.getJSONObject(i).getInt("totalMonthDistance"))
                        monthlyList.add(userMonthRecord)

                    }

                    currentIndex = monthlyList.size-1
                    viewYearMonth?.text = monthlyList.get(currentIndex).getMonthlyString()

                }

                if (monthlyList.size == 1) {
                    previousButton.visibility = View.GONE
                }
            }

            coroutinegetMyRecordData(monthlyList.get(currentIndex).getMonthlyString()!!)

//            if (existResult == "0") {
//                Log.d(TAG, "정보 없음")
//            } else {
//                Log.d(TAG, "정보 있음")
//
//                val tempAllList = ArrayList<class_RaceRecord>()
//                val jsonArray = jsonObject.getJSONArray("result")
//
//                for (i in 0..jsonArray.length() - 1) {
//
//                    var userRecord = class_RaceRecord()
//
//                    userRecord.setId(jsonArray.getJSONObject(i).getInt("userId"))
//                    userRecord.setRoomNo(jsonArray.getJSONObject(i).getInt("roomNo"))
//                    userRecord.setGameFinishTime(jsonArray.getJSONObject(i).getString("gameFinishTime"))
//                    userRecord.setRaceTime(jsonArray.getJSONObject(i).getInt("raceTime"))
//                    userRecord.setDistance(jsonArray.getJSONObject(i).getInt("distance"))
//                    userRecord.setMyRanking(jsonArray.getJSONObject(i).getInt("myRanking"))
//                    userRecord.setCompleted(jsonArray.getJSONObject(i).getInt("completed"))
//                    userRecord.setMemberCount(jsonArray.getJSONObject(i).getInt("memberCount"))
//
//                    var avgPace = 0
//                    if ( jsonArray.getJSONObject(i).getInt("distance") != 0) {
//                        avgPace = (jsonArray.getJSONObject(i).getInt("raceTime") / jsonArray.getJSONObject(i).getInt("distance")*10) * 1000
//                    }
//                    userRecord.setRaceAvgPace(avgPace)
//
//                    val paceText: String
//                    val speedText: String
//
//                    if ( userRecord.getRaceTime() == 0 || userRecord.getDistance() == 0 ) {
//                        paceText = "00:00"
//                        speedText = "00:00"
//                    } else {
//
//                        var tempRaceTime = jsonArray.getJSONObject(i).getInt("raceTime").toDouble()
//                        var tempRaceDistance = (jsonArray.getJSONObject(i).getInt("distance") * 10).toDouble()
//
//                        paceText = oftenUseMethod.secondsToTimeOverMinuteAddHour(((tempRaceTime/tempRaceDistance)*1000).toLong())
//                        speedText = (((tempRaceDistance/tempRaceTime)*3600)/1000).toString()
//                    }
//
//                    userRecord.setRaceAvgPaceString(paceText)
//                    userRecord.setRaceAvgSpeedString(speedText)
//
//                    tempAllList.add(userRecord)
//                }
//
//                val divideMonthArrayList = mutableListOf<ArrayList<class_RaceRecord>>()
//
//                divideMonthArrayList.add(ArrayList<class_RaceRecord>()) // 1월 ( index 0 )
//                divideMonthArrayList.add(ArrayList<class_RaceRecord>()) // 2월 ( index 1 )
//                divideMonthArrayList.add(ArrayList<class_RaceRecord>()) // 3월 ( index 2 )
//                divideMonthArrayList.add(ArrayList<class_RaceRecord>()) // 4월 ( index 3 )
//                divideMonthArrayList.add(ArrayList<class_RaceRecord>()) // 5월 ( index 4 )
//                divideMonthArrayList.add(ArrayList<class_RaceRecord>()) // 6월 ( index 5 )
//                divideMonthArrayList.add(ArrayList<class_RaceRecord>()) // 7월 ( index 6 )
//                divideMonthArrayList.add(ArrayList<class_RaceRecord>()) // 8월 ( index 7 )
//                divideMonthArrayList.add(ArrayList<class_RaceRecord>()) // 9월 ( index 8 )
//                divideMonthArrayList.add(ArrayList<class_RaceRecord>()) // 10월 ( index 9 )
//                divideMonthArrayList.add(ArrayList<class_RaceRecord>()) // 11월 ( index 10 )
//                divideMonthArrayList.add(ArrayList<class_RaceRecord>()) // 12월 ( index 11 )
//
//                for(i in tempAllList.indices) {
//                    if (i <= tempAllList.lastIndex) {
//
//                        var tempGameFinishTime = sdf.parse(tempAllList.get(i).getGameFinishTime())
//                        var tempMonth = tempGameFinishTime.month
//
//                        var userRecord = class_RaceRecord()
//
//                        userRecord.setId(tempAllList.get(i).getId())
//                        userRecord.setRoomNo(tempAllList.get(i).getRoomNo())
//                        userRecord.setGameFinishTime(tempAllList.get(i).getGameFinishTime())
//                        userRecord.setRaceTime(tempAllList.get(i).getRaceTime())
//                        userRecord.setDistance(tempAllList.get(i).getDistance())
//                        userRecord.setMyRanking(tempAllList.get(i).getMyRanking())
//                        userRecord.setCompleted(tempAllList.get(i).getCompleted())
//                        userRecord.setRaceAvgPaceString(tempAllList.get(i).getRaceAvgPaceString())
//                        userRecord.setRaceAvgSpeedString(tempAllList.get(i).getRaceAvgSpeedString())
//                        userRecord.setRaceAvgPace(tempAllList.get(i).getRaceAvgPace())
//                        userRecord.setMemberCount(tempAllList.get(i).getMemberCount())
//
//                        divideMonthArrayList[tempMonth].add(userRecord)
//
//                        Log.d(TAG, "Arraylist size 출력해봄 " + divideMonthArrayList[tempMonth].size)
//
//                    }
//                }
//
//                for (i in divideMonthArrayList.indices) {
//                    if (i <= divideMonthArrayList.lastIndex) {
//                        if (!divideMonthArrayList[i].isEmpty()) {
//
//
//                            var tempMonthRaceCount = divideMonthArrayList[i].size
//                            var tempMonthTotalDistance = 0
//                            var tempMonth = i + 1
//                            var tempGameFinishTimeToLong = 0.toLong()
//
//                            for(j in divideMonthArrayList[i].indices) {
//                                if (j <= divideMonthArrayList[i].lastIndex) {
//
//                                    tempGameFinishTimeToLong = sdf.parse(divideMonthArrayList[i].get(j).getGameFinishTime()).time
//                                    tempMonthTotalDistance += divideMonthArrayList[i].get(j).getDistance()!!
//
//                                }
//                            }
//                            var monthRecordParent = class_RaceRecordParent(tempMonthRaceCount,tempMonthTotalDistance,tempMonth,tempGameFinishTimeToLong,divideMonthArrayList[i])
//                            monthArrayList?.add(monthRecordParent)
//
//                        }
//                    }
//                }
//
//                monthArrayList?.sortByDescending { it.gameFinishTimeToLong }
//
//                setRecyclerView ()
//
//            }
        }
    }

    fun coroutinegetMyRecordData (monthly: String) {
        Log.d(TAG,"coroutineUpdateActiveRoom ()")

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                // network
                getHtml(monthly)
            }.await()

            Log.d(TAG,html)

            val jsonObject = JSONObject(html)
            val existResult = jsonObject.getString("existResult")

            if (existResult == "0") {
                Log.d(TAG, "정보 없음")
            } else {
                Log.d(TAG, "정보 있음")

                val tempAllList = ArrayList<class_RaceRecord>()
                val jsonArray = jsonObject.getJSONArray("result")

                for (i in 0 until jsonArray.length()) {

                    var userRecord = class_RaceRecord()

                    userRecord.setId(jsonArray.getJSONObject(i).getInt("userId"))
                    userRecord.setRoomNo(jsonArray.getJSONObject(i).getInt("roomNo"))
                    userRecord.setGameFinishTime(jsonArray.getJSONObject(i).getString("gameFinishTime"))
                    userRecord.setRaceTime(jsonArray.getJSONObject(i).getInt("raceTime"))
                    userRecord.setDistance(jsonArray.getJSONObject(i).getInt("distance"))
                    userRecord.setMyRanking(jsonArray.getJSONObject(i).getInt("myRanking"))
                    userRecord.setCompleted(jsonArray.getJSONObject(i).getInt("completed"))
                    userRecord.setMemberCount(jsonArray.getJSONObject(i).getInt("memberCount"))

                    var avgPace = 0
                    if ( jsonArray.getJSONObject(i).getInt("distance") != 0) {
                        avgPace = (jsonArray.getJSONObject(i).getInt("raceTime") / jsonArray.getJSONObject(i).getInt("distance")*10) * 1000
                    }
                    userRecord.setRaceAvgPace(avgPace)

                    val paceText: String
                    val speedText: String

                    if ( userRecord.getRaceTime() == 0 || userRecord.getDistance() == 0 ) {
                        paceText = "00:00"
                        speedText = "00:00"
                    } else {

                        var tempRaceTime = jsonArray.getJSONObject(i).getInt("raceTime").toDouble()
                        var tempRaceDistance = (jsonArray.getJSONObject(i).getInt("distance") * 10).toDouble()

                        paceText = oftenUseMethod.secondsToTimeOverMinuteAddHour(((tempRaceTime/tempRaceDistance)*1000).toLong())
                        speedText = (((tempRaceDistance/tempRaceTime)*3600)/1000).toString()
                    }

                    userRecord.setRaceAvgPaceString(paceText)
                    userRecord.setRaceAvgSpeedString(speedText)


                    tempAllList.add(userRecord)
                }

                var tempMonthRaceCount = monthlyList.get(currentIndex).getRecordMonthCount()
                var tempMonthTotalDistance = monthlyList.get(currentIndex).getTotalMonthDistance()
                var tempMonth = monthlyList.get(currentIndex).getMonthlyString()

                var monthRecordParent = class_RaceRecordParent(tempMonthRaceCount!!,tempMonthTotalDistance!!,tempMonth!!,tempAllList)

                monthArrayList?.clear()
                monthArrayList?.add(monthRecordParent)
                adapterRV1?.monthList = monthArrayList as MutableList<class_RaceRecordParent>

                adapterRV1!!.notifyDataSetChanged()

            }
        }
    }

    fun getHtml(monthly: String) : String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL+"/record/myRecentRecordTotal?userId=${MainActivity.loginId}&monthly=$monthly").build()
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