package com.example.withrun

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.security.DigestException
import java.security.MessageDigest


class MainActivity : AppCompatActivity() {

    val TAG:String = "MainActivity"

    var email : String? = null
    var pw : String? = null

    var sharedPreference: SharedPreferences? = null


    companion object { // 현재 로그인 중인 유저의 정보

        var loginId : Int = 0
        var loginEmail : String = ""
        var loginMarketing_noti : String = ""
        var loginNickname : String = ""
        var loginGender : String = ""
        var loginBirth : String = ""
        var loginHeight : String = ""
        var loginWeight : String = ""
        var loginProfileImgPath : String = ""

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs : SharedPreferences = getSharedPreferences("User", Context.MODE_PRIVATE)
        val getUserIdInShared = prefs.getInt("loginId", 0).toString()
        // SharedPreferences 안에 값이 저장되어 있지 않을 때 -> Login
        if ( getUserIdInShared.isNullOrBlank() || getUserIdInShared == "0") {


        // 회원가입 activity로 이동
        new_member.setOnClickListener(View.OnClickListener { view ->

            val intent = Intent(this, NewMember::class.java)
            startActivity(intent)

        })


        // 로그인 area 보여주는 버튼
        email_login.setOnClickListener(View.OnClickListener { view ->

            val background = base.background
            background.alpha = 200

            val animation = AnimationUtils.loadAnimation(this,R.anim.slide_down)
            signin_area.visibility = View.VISIBLE
            signin_area.startAnimation(animation)

            val animation_out = AnimationUtils.loadAnimation(this,R.anim.slide_down_out)
            loginArea.visibility = View.GONE
            loginArea.startAnimation(animation_out)

            goFindPassword () // 비밀번호 찾기 버튼
        })



        // 로그인 버튼
        signin_bt.setOnClickListener(View.OnClickListener { view ->


            email = Edit_email.getText().toString()
            pw = Edit_pw.getText().toString()

//            if (email == "1") {
//               email = "kam1288@naver.com"
//                pw = "xlashqk0602"
//            }
//            if (email == "2") {
//                email = "kan12888@gmail.com"
//                pw = "xlashqk0602"
//            }
//            if (email == "3") {
//               email = "kam12888@naver.com"
//                pw = "xlashqk0602"
//            }
//            if (email == "4") {
//                email = "kam128888@naver.com"
//                pw = "xlashqk0602"
//            }

//            sendPushTokenToDB()
//            coroutine(email!!, pw!!)

            if (email == null || pw == null) { // 입력한 email과 pw가 비어있지 않으면 통신

                Toast.makeText(this, "이메일 혹은 패스워드를 확인 후 로그인 해 주세요",Toast.LENGTH_SHORT).show()

            } else {

                coroutine(email!!, pw!!)

            }

        })

        backToMain.setOnClickListener(View.OnClickListener { view ->

            val background = base.background
            background.alpha = 255

            val animation_out = AnimationUtils.loadAnimation(this,R.anim.slide_up_out)
            signin_area.visibility = View.GONE
            signin_area.startAnimation(animation_out)

            val animation = AnimationUtils.loadAnimation(this,R.anim.slide_up)
            loginArea.visibility = View.VISIBLE
            loginArea.startAnimation(animation)

        })
        } else { // SharedPreferences 안에 값이 저장되어 있을 때 -> MainActivity로 이동

            sendPushTokenToDB()
//            Toast.makeText(this,"자동 로그인", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            finish()
        }


    }

    private fun sendPushTokenToDB() {
        //파이어베이스
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(object : OnCompleteListener<InstanceIdResult?> {
                override fun onComplete(task: Task<InstanceIdResult?>) {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "getInstanceId failed", task.getException())
                        return
                    }

                    // Get new Instance ID token
                    val token: String = task.getResult()!!.getToken()
                    Log.d(TAG, "firebase 토큰  "+ token)

                }
            })
    }

    fun goFindPassword () {

        find_pw.setOnClickListener(View.OnClickListener { view ->
            val intent1 = Intent(this, FindPassword::class.java)
            startActivity(intent1)
        })
    }

    fun joinProfile () {
        val intent1 = Intent(this, Profile::class.java)
        intent1.putExtra("email", email)
        startActivity(intent1)
        finish()
    }

    fun joinChangePw () {
        val intent1 = Intent(this, ChangePassword::class.java)
        startActivity(intent1)
        finish()
    }

    fun coroutine (email:String, pw:String) {
        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                // network
                getHtml(email, pw)
            }.await()

            val jsonObject = JSONObject(html)
            val existResult = jsonObject.getString("existResult")
            Log.d(TAG,existResult)

            if (existResult == "0") { // 조회한 email 과 pw 동일한 유저데이터가 없는 경우 0 / 데이터가 있는경우 1
                matchNoti.text = "가입되지 않은 이메일 주소 입니다."
                matchNoti.visibility = View.VISIBLE
                return@launch
            } else {
                val jsonArray = jsonObject.getJSONArray("result").getJSONObject(0)
                val pwSHA256 = jsonArray.getString("password") // 기존 비밀번호
                val temppwSHA256 = jsonArray.getString("tempPw") // 임시 비밀번호

                Log.d(TAG, "서버에서 받아온 임시비번 확인 "+temppwSHA256)

                if (temppwSHA256 == "0") { // 임시비번이 없는경우 기존 비번과 비교
                    if (hashSHA256(pw) == pwSHA256) { // 비밀번호 중복체크, sha256 그대로 server 에서 받아옴

                        setUserProfile(jsonArray)

                        matchNoti.visibility = View.GONE
                        joinProfile() // 프로필 화면으로 이동
                    } else {
                        matchNoti.text = "아이디 혹은 비밀번호가 틀립니다."
                        matchNoti.visibility = View.VISIBLE
                    }
                } else { // 임시로 발급받은 비번이 있는 경우 임시비번과 비교

                    if (hashSHA256(pw) == hashSHA256(temppwSHA256)) {
                        setUserProfile(jsonArray)

                        matchNoti.visibility = View.GONE
                        joinChangePw () // 비밀번호 변경 화면으로 이동
                    } else {
                        matchNoti.text = "아이디 혹은 비밀번호가 틀립니다."
                        matchNoti.visibility = View.VISIBLE
                    }
                }
            }
        }
    }


    // 전역변수에 로그인중인 유저 저장 & 쉐어드에 유저데이터 저장
    fun setUserProfile (jsonArray: JSONObject) {

        val saveShared = getSharedPreferences("User", Context.MODE_PRIVATE)
        var sharedEditor = saveShared.edit()

        sharedEditor.putInt("loginId", jsonArray.getInt("id"))
        sharedEditor.putString("loginEmail", jsonArray.getString("email"))
        sharedEditor.putString("loginMarketing_noti", jsonArray.getString("marketing_noti"))
        sharedEditor.putString("loginNickname", jsonArray.getString("nickname"))
        sharedEditor.putString("loginGender", jsonArray.getString("gender"))
        sharedEditor.putString("loginBirth", jsonArray.getString("birth"))
        sharedEditor.putString("loginHeight", jsonArray.getString("height"))
        sharedEditor.putString("loginWeight", jsonArray.getString("weight"))
        sharedEditor.putString("loginProfileImgPath", jsonArray.getString("profileImgPath"))

        sharedEditor.commit()

        loginId = jsonArray.getInt("id")
        loginEmail = jsonArray.getString("email")
        loginMarketing_noti = jsonArray.getString("marketing_noti")
        loginNickname = jsonArray.getString("nickname")
        loginGender = jsonArray.getString("gender")
        loginBirth = jsonArray.getString("birth")
        loginHeight = jsonArray.getString("height")
        loginWeight = jsonArray.getString("weight")
        loginProfileImgPath = jsonArray.getString("profileImgPath")

    }

    fun getHtml(email:String, pw:String) : String {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url("http://ec2-13-209-169-218.ap-northeast-2.compute.amazonaws.com:12888/users?emailLogin=$email&pwLogin=$pw").build()
        client.newCall(req).execute().use {
            response -> return if(response.body != null) {
            response.body!!.string()
              }
            else {
                "body null"
            }
        }
    }

    fun hashSHA256(msg: String) : String? {
        val hash: ByteArray
        try {
            val md = MessageDigest.getInstance("SHA-256")
            md.update(msg.toByteArray())
            hash = md.digest()
        } catch (e: CloneNotSupportedException) {
            throw DigestException("couldn't make digest of partial content");
        }

        return bytesToHex(hash)
    }

    fun bytesToHex(`in`: ByteArray): String? {
        val builder = StringBuilder()
        for (b in `in`) {
            builder.append(String.format("%02x", b))
        }
        return builder.toString()
    }



}