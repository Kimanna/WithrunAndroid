package com.example.withrun

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley.newRequestQueue
import kotlinx.android.synthetic.main.activity_new_member2.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.internal.wait
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.IOException
import java.util.*
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class NewMember2 : AppCompatActivity() {

    val TAG:String = "NewMember2"
    var certify:  Boolean = false

    var random_no: String? = null

    companion object {
        var email : String = ""
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_member2)

        // 뒤로가기 버튼 클릭시 login 화면으로 돌아감
        back_Newmember.setOnClickListener(View.OnClickListener { view ->
            val intent1 = Intent(this, NewMember::class.java)
            startActivity(intent1)
            finish()
        })


        next_newmember3_page.isEnabled = false

        // 기존가입된 회원인지 확인하는 버튼
        // 1. email 주소 패턴검사, 2. 기존회원인지 email주소로 확인 후 기존회원이 아닌경우 인증번호 발송 버튼을 보여줌
        ck_email.setOnClickListener(View.OnClickListener { view ->

            notApprovalNoti.visibility = View.GONE // 인증이 확인되지 않았다는 문구 가려줌


            // 사용자가 입력한 email 변수에 선언
            // 인증번호를 받기위한 email 주소 입력란
            email = input_email.text.toString().trim()
            var email_pattern = android.util.Patterns.EMAIL_ADDRESS

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

        // 인증번호받기 버튼 클릭리스너
        // 1. email 주소 패턴검사, 2. email 주소로 랜덤난수 4자리 발송
        // 버튼 클릭시마다 랜덤 난수 생성하기 때문에 버튼 클릭시 마다 바뀜
        certi_no_get_bt.setOnClickListener(View.OnClickListener { view ->

            mTextMain.visibility = View.GONE // 아이디 중복시 안내 문구 보여주는 부분 gone

            // 사용자가 입력한 email 변수에 선언
            email = input_email.text.toString().trim()

            var email_pattern = android.util.Patterns.EMAIL_ADDRESS;
            if(email_pattern.matcher(email).matches()) { // 이메일 패턴 맞음, email주소로 인증번호 발송
                Log.d(TAG, email)

                random_no = randomInt() // 랜덤 넘버 생성 후 변수에 담음
//                val body = "인증번호 : " + random_no // 인증번호 (0~9) 사이의 난수 4자리 발송

                coroutineNumber (email, random_no!!) // email주소와 인증번호를 서버로 발송


            } else { // 이메일 패턴 틀림, 유효하지않은 이메일주소 alert 띄움

                val dlg: AlertDialog.Builder = AlertDialog.Builder(this,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                dlg.setMessage("유효하지않은 email주소 입니다.") // 메시지
                dlg.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->

                })
                dlg.show()
            }
        })

        next_newmember3_page.setOnClickListener(View.OnClickListener { view ->
            val intent = Intent(this, NewMember3::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        })
    }


    // 인증번호입력 제한 타이머
    @SuppressLint("ResourceAsColor")
    fun emailConfirm () {
        // 인증번호를 입력하는 layout 공간
        input_certi_area.visibility = View.VISIBLE // 인증번호 입력란 보여줌
        // 다운타이머 1분
        val timer = object: CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timer.setText((millisUntilFinished / 1000).toString())
            }

            override fun onFinish() {

                // 인증확인이 되지 않은 경우 시간초과 dialog 띄워줌
                if (!certify) {
                    showDialog()
                    oftenUseMethod.setUseableEditText(input_email,true)
                    ck_email.visibility = View.VISIBLE      // email 중복검사 버튼 visible
                    notApprovalNoti.visibility = View.VISIBLE // 인증이 확인되지 않았다는 문구 띄워줌
                    certi_no_get_bt.visibility = View.GONE  // 인증번호 받기 gone
                    input_certi_area.visibility = View.GONE // 인증번호 입력칸 다시 숨김
                }
            }
        }
        timer.start()

        // 인증넘버 확인 버튼
        certi_confirm.setOnClickListener(View.OnClickListener { view ->
            val input_no = input_certi_no.text.toString().trim()
            if (input_no == random_no) {
                val dlg: AlertDialog.Builder = AlertDialog.Builder(this,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                dlg.setMessage("인증 확인 되었습니다.") // 메시지
                dlg.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                })
                dlg.show()
                certi_email_area.visibility = View.GONE // 이메일 입력 + 인증번호 입력하는 layout 공간
                certi_success.visibility = View.VISIBLE
                next_newmember3_page.setBackgroundColor(R.color.ultimategray)
                next_newmember3_page.isEnabled = true
                certify = true
            } else {
                val dlg: AlertDialog.Builder = AlertDialog.Builder(this,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
                dlg.setMessage("인증번호가 맞지 않습니다. 다시 입력해주세요") // 메시지
                dlg.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                })
                dlg.show()
                certify = false
            }
        })
    }

    fun <T> List<T>.random() : T {
        val random = Random().nextInt((size))
        return get(random)
    }


    fun showDialog() {

        val dlg: AlertDialog.Builder = AlertDialog.Builder(this,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
        dlg.setMessage("입력시간이 초과되었습니다.") // 메시지
        dlg.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->

        })
        dlg.show()
    }

    // 인증번호 (난수4자리) 생성
    fun randomInt (): String {
        val random = Random()
        val random_no = random.nextInt(10).toString()+random.nextInt(10).toString()+random.nextInt(10).toString()+random.nextInt(10).toString()
        Log.d(TAG, random_no.toString())

        return random_no
    }

    fun coroutine (email:String) {
        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
            // network
                getHtml(email)
            }.await()

            Log.d(TAG,html)
            if (html == "1") { // 이미 가입된 회원인 경우 return 값은 1 이므로 ismember 변수 true로 변경
                mTextMain.text = "이미 가입된 email 주소 입니다."
            } else {
                mTextMain.text = "신규가입이 가능한 email 주소 입니다."
                oftenUseMethod.setUseableEditText(input_email,false)
                ck_email.visibility = View.GONE // email중복검사 버튼 gone
                certi_no_get_bt.visibility = View.VISIBLE // 인증번호 발송 버튼 보여줌
            }
        }
    }

    fun getHtml(email: String) : String {
        Log.d(TAG,email)
        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url("http://ec2-13-209-169-218.ap-northeast-2.compute.amazonaws.com:12888/users/$email").build()
        client.newCall(req).execute().use {
            response -> return if(response.body != null) {
                response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }

    fun coroutineNumber (email: String, number:String) {
        CoroutineScope(Dispatchers.Main).launch { this
            val confirmEmail = CoroutineScope(Dispatchers.Default).async { this
                // network
                getEmailCheck(email,number)
            }.await()

            Log.d(TAG,confirmEmail)

            if (confirmEmail.equals("ok")) {
                emailConfirm () // 타이머와 인증번호 입력칸 보여줌
            } else {

            }
        }
    }

    fun getEmailCheck (email: String, number: String) : String {
        Log.d(TAG,email)
        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url("http://ec2-13-209-169-218.ap-northeast-2.compute.amazonaws.com:12888/users/number?email=$email&number=$number").build()
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

