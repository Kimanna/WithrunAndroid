package com.example.withrun

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_find_password.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class FindPassword : AppCompatActivity() {

    val TAG : String = "FindPassword"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_password)

        var email : String = ""

        // 뒤로가기 버튼 클릭시 login 화면으로 돌아감
        back_Main.setOnClickListener(View.OnClickListener { view ->
            val intent1 = Intent(this, MainActivity::class.java)
            startActivity(intent1)
            finish()
        })

        sendTempPw.setOnClickListener(View.OnClickListener { view ->

            email = inputEmail.text.toString().trim()
            var email_pattern = android.util.Patterns.EMAIL_ADDRESS;

            if(email_pattern.matcher(email).matches()) { // 이메일 패턴 맞음, email주소로 인증번호 발송
                coroutine(email) // 이미 가입된 email인지 확인 , http 연결 코드, 가입돼있지 않다면 email 인증 발송 버튼 보여줌
            } else {

                val dlg: AlertDialog.Builder = AlertDialog.Builder(this,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                dlg.setMessage("유효하지않은 email주소 입니다.") // 메시지
                dlg.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->

                })
                dlg.show()
            }

        })
    }

    fun coroutine (email:String) {
        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                // network
                getHtml(email, randomPw())
            }.await()

            Log.d(TAG,html)
            if (html == "ok") {

                goMain ()
            }

        }
    }

    fun goMain () {

        val dlg: AlertDialog.Builder = AlertDialog.Builder(this,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
        dlg.setMessage("입력하신 email 로 임시 비밀번호 발송 드렸습니다. \n 임시 비밀번호로 로그인 해주시기 바랍니다.") // 메시지
        dlg.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->

            val intent1 = Intent(this, MainActivity::class.java)
            intent1.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent1)
            finish()
        })
        dlg.show()

    }

    fun getHtml(email: String, tempPw: String) : String {
        Log.d(TAG,email)
        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url("http://ec2-13-209-169-218.ap-northeast-2.compute.amazonaws.com:12888/users/resetPw/pw?email=$email&tempPw=$tempPw").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }

    fun randomPw(): String {
        val pwCollectionSpCha =
            charArrayOf('!', '@', '#', '$', '%', '^', '&', '*', '(', ')')
        val pwCollectionNum =
            charArrayOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
        val pwCollectionAll = charArrayOf(
            '1','2','3','4','5','6','7','8','9','0',
            'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
            '!','@','#','$','%','^','&','*','(',')'
        )
        return getRandPw(1, pwCollectionSpCha) + getRandPw(8, pwCollectionAll) + getRandPw(1, pwCollectionNum)
    }

    fun getRandPw(size: Int, pwCollection: CharArray): String {
        var ranPw = ""
        for (i in 0 until size) {
            val selectRandomPw = (Math.random() * pwCollection.size).toInt()
            ranPw += pwCollection[selectRandomPw]
        }
        return ranPw
    }
}