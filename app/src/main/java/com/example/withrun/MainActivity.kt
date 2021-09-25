package com.example.withrun

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
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

    private val REQUEST_ACCESS_FINE_LOCATION = 1000


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

        // 권한 요청
        permissionCheck(
            cancel = { showPermissionInfoDialog() },   // 권한 필요 안내창
            ok = { addLocationListener()}      // ③   주기적으로 현재 위치를 요청
        )


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

            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            finish()
        }


    }

    fun goFindPassword () {

        find_pw.setOnClickListener(View.OnClickListener { view ->

            val background = base.background
            background.alpha = 255
            loginArea.visibility = View.GONE

            val intent1 = Intent(this, FindPassword::class.java)
            startActivity(intent1)
        })

    }

    fun joinProfile () {

        val background = base.background
        background.alpha = 255

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

    private fun sendPushTokenToDB(userId:String) {

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

                    coroutineSaveFCMtoken (userId, token)

                }
            })
    }


    // 유저 pace 기록, follow count, 기록 그래프 가져옴
    fun coroutineSaveFCMtoken (userId: String, fcmToken: String) {

        CoroutineScope(Dispatchers.Main).launch { this
            val SaveFCMtoken = CoroutineScope(Dispatchers.Default).async { this
                SaveFCMtoken (userId, fcmToken)
            }.await()
        }
    }

    fun SaveFCMtoken (userId: String, fcmToken: String) : String  {

        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url(Constants.URL+"/users/fcmToken?userId=$userId&fcmToken=$fcmToken").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
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

            // 로그인시 입력한 email 동일한 email주소가 DB 에 있는 경우 0 / 데이터가 있는경우 1
            if (existResult == "0") {

                matchNoti.text = "가입되지 않은 이메일 주소 입니다."
                matchNoti.visibility = View.VISIBLE
                return@launch

            } else {

                val jsonArray = jsonObject.getJSONArray("result").getJSONObject(0)
                val userId = jsonArray.getString("id") // 기존 비밀번호
                val pwSHA256 = jsonArray.getString("password") // 기존 비밀번호
                val temppw = jsonArray.getString("tempPw") // 임시 비밀번호


                // 비밀번호 변경을 진행한 유저는 임시비밀번호가 있는경우 else / 임시비밀번호 없는경우 if
                if (temppw == "0") {    // 임시비번이 없는경우 기존 비번과 비교

                    /*
                     비밀번호 중복체크, 유저가 로그인 시 입력한 pw 를 sha256 으로 변환 후 비교
                     if 비밀번호가 동일한 경우 1) 전역변수와 쉐어드에 유저데이터 입력, 2) FCM token 넘버를 DB 에 저장, 3) profile activity로 이동
                     else 비밀번호 오류문구 띄워줌
                     */

                    if (hashSHA256(pw) == pwSHA256) {

                        setUserProfile(jsonArray)
                        sendPushTokenToDB(userId)

                        matchNoti.visibility = View.GONE
                        joinProfile() // 프로필 화면으로 이동

                    } else {

                        matchNoti.text = "아이디 혹은 비밀번호가 틀립니다."
                        matchNoti.visibility = View.VISIBLE
                        return@launch

                    }
                } else {                        // 임시로 발급받은 비번이 있는 경우 임시비번과 비교

                    Log.d(TAG, "입력한 비번 "+hashSHA256(pw)+ "서버에서 받아온 비번 "+ temppw)
                    if (pw == temppw) {

                        setUserProfile(jsonArray)
                        sendPushTokenToDB(userId)

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

    private fun permissionCheck(cancel: () -> Unit, ok: () -> Unit) {   // 전달인자도, 리턴값도 없는
        // 두 개의 함수를 받음

        if (ContextCompat.checkSelfPermission(this,                  // 권한이 없는 경우
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {       // 권한 거부 이력이 있는 경우

                cancel()

            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_ACCESS_FINE_LOCATION)
            }
        } else {                                                    // 권한이 있는 경우
            ok()
        }
    }

    private  fun showPermissionInfoDialog() {

        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n 위치 설정을 수정하실래요?".trimIndent())
        builder.setCancelable(true)

        builder.setPositiveButton("설정", DialogInterface.OnClickListener { dialog, id ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            ActivityCompat.requestPermissions(this,  // 첫 전달인자: Context 또는 Activity
                // this: DialogInterface 객체
                // this@MapsActivity는 액티비티를 명시적으로 가리킨 것임
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_ACCESS_FINE_LOCATION)
        })
    }

    private fun addLocationListener() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            return
        }
    }


    //    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<String>,  grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_ACCESS_FINE_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    addLocationListener()
                } else {
                    Toast.makeText(this,"권한이 거부 됨", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

}