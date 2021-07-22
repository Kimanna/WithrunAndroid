package com.example.withrun

import android.content.Context
import android.content.DialogInterface
import android.location.Location
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.*

object Constants {

    const val TAG : String = "로그"
    const val URL : String = "http://ec2-13-209-169-218.ap-northeast-2.compute.amazonaws.com:12888"
    const val EMAIL : String = "withrun@withrun.co.kr"
}

object JavaServerAPI {
    const val BASE_URL : String = "ec2-13-209-97-47.ap-northeast-2.compute.amazonaws.com"

    const val ip = "13.209.169.218" // IP
    const val port = 12889

    const val SEARCH_PHOTOS : String = "search/photos"
    const val SEARCH_USERS : String = "search/users"

}

object oftenUseMethod {

    val mFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd kk:mm:ss")

    fun lineOut(): String? {
        val level = 4
        val traces: Array<StackTraceElement>
        traces = Thread.currentThread().stackTrace
        return " at " + traces[level] + " "
    }

    fun getLineNumber(): Int {
        return Thread.currentThread().stackTrace[2].lineNumber
    }

    fun twoDigitString(number: Long): String? {
        if (number == 0L) {
            return "00"
        }
        return if (number / 10 == 0L) {
            "0$number"
        } else number.toString()
    }

    fun distanceDigitString(number: Long): String? {
        if (number == 0L) {
            return "00"
        } else if (number / 10 == 0L) {
            return "0$number"
        } else if (number / 100 >= 1) {
            val numCount = number.toString()
            return numCount.substring(numCount.length - 2, numCount.length)
        } else return number.toString()
    }

    // Convert Seconds to Time
    fun secondsToTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val seconds = seconds % 60

        return String.format("%02d : %02d",minutes, seconds)
    }

    // Convert Seconds to Time
    fun secondsToTimeOverMinute(seconds1: Long): String {
        val hours = seconds1 / 3600
        val minutes = (seconds1 % 3600) / 60
        val seconds = seconds1 % 60
        System.out.println("minutes 출력 : "+ seconds1 + " " + (seconds1 % 3600) )

        var formatStringValue = ""
        if ( hours > 0 ) {

            formatStringValue = String.format("%03d : %02d",minutes, seconds)

            System.out.println("( minutes > 99 ) if 문 지났는지 출력 : " )

        } else {
            formatStringValue = String.format("%02d : %02d",minutes, seconds)
            System.out.println("else 문 지났는지 출력 : " )

        }

        return formatStringValue
    }

    // Convert Seconds to Time
    fun secondsToTimeOverMinuteAddHour(seconds1: Long): String {
        val hours = seconds1 / 3600
        val minutes = (seconds1 % 3600) / 60
        val minutesTemp = seconds1 / 60
        val seconds = seconds1 % 60

        var formatStringValue = ""
        if ( minutesTemp > 59 ) {
            formatStringValue = String.format("%02d : %02d : %02d",hours, minutes, seconds)
        } else {
            formatStringValue = String.format("%02d : %02d",minutes, seconds)
        }

        return formatStringValue
    }

    // Convert Seconds to Time
    fun secondsToTimeRecord(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val seconds = seconds % 60

        var returnValue : String = ""
        if ( hours > 1 ) {
            returnValue = String.format("%02d : %02d : %02d", hours, minutes, seconds)
        } else {
            returnValue = String.format("%02d : %02d", minutes, seconds)
        }

        return returnValue
    }

    fun getDistance( lat1: Double, lng1: Double, lat2: Double, lng2: Double ): Double {
        val distance: Double
        val locationA = Location("point A")
        locationA.latitude = lat1
        locationA.longitude = lng1
        val locationB = Location("point B")
        locationB.latitude = lat2
        locationB.longitude = lng2
        distance = locationA.distanceTo(locationB).toDouble()
        return distance
    }

    fun formatTime (hour:Int, minute:Int): String? {

        var formatTime: String? = ""
        val cal = Calendar.getInstance()

        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)

        formatTime = SimpleDateFormat("HH:mm").format(cal.time)
        return formatTime
    }

    fun show(message: String, context: Context) {
        Toast.makeText(context,message, Toast.LENGTH_LONG).show()
    }

    fun dialogConfirmShow (msg: String, context: Context)  {

        val dlg: AlertDialog.Builder = AlertDialog.Builder(context,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
        dlg.setMessage(msg) // 메시지
        dlg.setNeutralButton("확인", DialogInterface.OnClickListener { dialog, which ->
        })
        dlg.show()
    }

    fun dateFormatSaveDB (time : Long ) : String {
        val f = SimpleDateFormat("yyyy-MM-dd kk:mm:ss")
        return f.format(time)
    }

    fun dialogshow (msg: String, context: Context) {

        val dlg: AlertDialog.Builder = AlertDialog.Builder(context,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
        dlg.setMessage(msg) // 메시지
        dlg.setNeutralButton("확인", DialogInterface.OnClickListener { dialog, which ->

        })
        dlg.show()
    }

    fun timeStringToLong (time: String): Long {

        val mFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd kk:mm:ss")
        val TimeLong = mFormat.parse(time).time

       return TimeLong
    }

    fun timeStringToLongSeoul (time: String): Long {

        val mFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.KOREA)
        mFormat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val TimeLong = mFormat.parse(time).time

        val sdfOutput = SimpleDateFormat("yyyy-MM-dd kk:mm:ss")
//        sdfOutput.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val formatted = sdfOutput.format(TimeLong)

        Log.d("시간출력 simpledateformat", TimeLong.toString() + " " + formatted )


//        val fommater = OffsetDateTime.parse(time)
//        Log.d("시간출력 OffsetDateTime", fommater.toString())

        return TimeLong
    }

    fun setUseableEditText(et: EditText, useable: Boolean) {
        et.isClickable = useable
        et.isEnabled = useable
        et.isFocusable = useable
        et.isFocusableInTouchMode = useable
    }

}

object googleMap {

    fun getDistance( lat1: Double, lng1: Double, lat2: Double, lng2: Double ): Double {
        val distance: Double
        val locationA = Location("point A")
        locationA.latitude = lat1
        locationA.longitude = lng1
        val locationB = Location("point B")
        locationB.latitude = lat2
        locationB.longitude = lng2
        distance = locationA.distanceTo(locationB).toDouble()
        return distance
    }

}

object okhttp {

    fun participationRoom (action: String, userId: Int, roomNo: Int) : String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL + "/rooms/myRoom?action=$action&userId=$userId&pageNo=$roomNo").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }

    fun getRoomData (action: String, userId: Int, roomNo: Int) : String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL + "/rooms/invitation?action=$action&userId=$userId&roomNo=$roomNo").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }



    fun userRoomInOut (action: String, userId: Int, roomNo: Int, nickname: String) : String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL + "/rooms/roomInOut?action=$action&userId=$userId&roomNo=$roomNo&nickname=$nickname").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }

    // 현재 월을 구하는 test 코드
//    fun main() {
//        val cal: Calendar = Calendar.getInstance()
//
//        val dateLast = Date( cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, -1)
//        System.out.println("마지막일 date 로 출력 :" + " " + dateLast.day)
//
//    }

}