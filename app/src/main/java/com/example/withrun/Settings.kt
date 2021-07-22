package com.example.withrun

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import com.sun.mail.imap.protocol.FLAGS
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject


class Settings : AppCompatActivity() {

    val TAG:String = "Settings"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        // 유저 setting 데이터 가져옴
        coroutineGetSetting ()

        pushNoti.setOnCheckedChangeListener(MyCheckedChangeListener())
        emailNoti.setOnCheckedChangeListener(MyCheckedChangeListener())
        allowInvitation.setOnCheckedChangeListener(MyCheckedChangeListener())
        openrecord.setOnCheckedChangeListener(MyCheckedChangeListener())
//        turnOffScreen.setOnCheckedChangeListener(MyCheckedChangeListener())


        // 뒤로가기 - 프로필로 화면으로 이동
        backtoProfile.setOnClickListener(View.OnClickListener { view ->
            val intent = Intent(this, Profile::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            finish()
        })

        // 약관 txt 파일을 보여주는 activity 이동
        terms.setOnClickListener(View.OnClickListener { View ->
            val intent = Intent(this, NewMemberTerms::class.java)
            intent.putExtra("location","Settings")
            startActivity(intent)
        })
        personalPolicy.setOnClickListener(View.OnClickListener { View ->
            val intent = Intent(this, NewMemberTerms1::class.java)
            intent.putExtra("location","Settings")
            startActivity(intent)
        })

        agreeServiceUse.setOnClickListener(View.OnClickListener { View ->
            val intent = Intent(this, NewMemberTerms2::class.java)
            intent.putExtra("location","Settings")
            startActivity(intent)
        })

        support.setOnClickListener(View.OnClickListener { View ->
            val sendEmail = Intent(Intent.ACTION_SEND)
            sendEmail.setType("message/rfc822")
            sendEmail.putExtra(Intent.EXTRA_EMAIL,"withrun@withrun.co.kr".toCharArray())
            sendEmail.putExtra(Intent.EXTRA_SUBJECT, "< 위드런에 문의합니다. >")
            sendEmail.setType("text/html")
            startActivity(sendEmail)
        })


        // 회원탈퇴
        withdrawal.setOnClickListener(View.OnClickListener { view ->
            val dlg: AlertDialog.Builder = AlertDialog.Builder(
                this,
                android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth
            )
            dlg.setMessage("위드런어플 회원 탈퇴를 하시겠습니까?") // 메시지
            dlg.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->

                coroutine ()

            })
            dlg.show()
        })

        // 로그아웃
        logout.setOnClickListener(View.OnClickListener { view ->

            val prefs : SharedPreferences = getSharedPreferences("User", Context.MODE_PRIVATE)
            val editor : SharedPreferences.Editor = prefs.edit()
            editor.clear()
            editor.commit()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        })
    }

    fun coroutine () {

        val progressDialog = ProgressDialog(this)
        showProgressBar (progressDialog)

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                // network
                getHtml(MainActivity.loginId)
            }.await()

            Log.d(TAG,html)
            val jsonObject = JSONObject(html).getJSONObject("result")
            val jsonObject1 = jsonObject.getString("affectedRows")
            Log.d(TAG,jsonObject1.toString())

            dismissProgressBar(progressDialog)

            if (jsonObject1.toInt() == 1) { // 회원탈퇴완료
                dialog ()
            } else {
            }
        }
    }

    fun getHtml(userId: Int) : String {
        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url("http://ec2-13-209-169-218.ap-northeast-2.compute.amazonaws.com:12888/users/memberWithdrawal/$userId").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }

    fun coroutineGetSetting () {

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                // network
                getSetting ()
            }.await()
            Log.d(TAG,html)

            val jsonObject = JSONObject(html).getJSONObject("result")

            if ( jsonObject.getInt("push_noti") == 1 ) {
                pushNoti.isChecked = true
            }

            if ( jsonObject.getInt("email_noti") == 1 ) {
                emailNoti.isChecked = true
            }

            if ( jsonObject.getInt("record_open_status") == 1 ) {
                openrecord.isChecked = true
            }

            if ( jsonObject.getInt("invitation_status") == 1 ) {
                allowInvitation.isChecked = true
            }

//            if ( jsonObject.getInt("voice_noti") == 1 ) {
//                turnOffScreen.isChecked = true
//            }

        }
    }

    fun getSetting () : String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL + "/users/setting/"+MainActivity.loginId).build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }

    fun coroutineSetting (action: String, changeValue: Int) {
        Log.d(TAG, "coroutineSetting 데이터 출력 "+action + changeValue)

        CoroutineScope(Dispatchers.Main).launch { this
            val html2 = CoroutineScope(Dispatchers.Default).async { this
                // network
                updateSettings (MainActivity.loginId, action, changeValue)
            }.await()
            Log.d(TAG,html2)

        }
    }

    fun updateSettings (userId: Int, action: String, changeValue: Int) : String {
        Log.d(TAG, "updateSettings 데이터 출력 "+action + changeValue)

//      setting 변경 가능한 값  push_noti,email_noti,record_open_status,invitation_status,voice_noti
        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL + "/users/settings/setting?userId=$userId&action=$action&changeValue=$changeValue").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }

    fun dialog () { // 확인 버튼만 존재
        val dlg: AlertDialog.Builder = AlertDialog.Builder(
            this,
            android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth
        )
        dlg.setMessage("그동안 이용해 주셔서 감사합니다.") // 메시지
        dlg.setNeutralButton("확인", DialogInterface.OnClickListener { dialog, which ->

            ActivityCompat.finishAffinity(this)
                finish() // 앱종료
        })
        dlg.show()
    }

    private fun showProgressBar(progressDialog: ProgressDialog) {
        progressDialog.setTitle("회원 탈퇴")
        progressDialog.setMessage("잠시만 기다려 주세요")
        progressDialog.show()
    }

    private fun dismissProgressBar(progressDialog: ProgressDialog) {
        progressDialog.dismiss()
    }


    inner class MyCheckedChangeListener : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            when( buttonView?.id ){
              R.id.pushNoti -> {
                  if ( isChecked ) {
                      coroutineSetting ("push_noti", 1)
                  } else {
                      coroutineSetting ("push_noti", 0)
                  }
              }
                R.id.emailNoti -> {
                    if (isChecked) {
                        coroutineSetting ("email_noti", 1)
                    } else {
                        coroutineSetting ("email_noti", 0)
                    }
                }
                R.id.allowInvitation -> {
                    if (isChecked) {
                        coroutineSetting ("invitation_status", 1)
                    } else {
                        coroutineSetting ("invitation_status", 0)
                    }
                }
                R.id.openrecord -> {
                    if (isChecked) {
                        coroutineSetting ("record_open_status", 1)
                    } else {
                        coroutineSetting ("record_open_status", 0)
                    }
                }
//                R.id.turnOffScreen -> {
//                    if (isChecked) {
//                        coroutineSetting ("voice_noti", 1)
//                    } else {
//                        coroutineSetting ("voice_noti", 0)
//                    }
//                }
            }
        }

    }
}

