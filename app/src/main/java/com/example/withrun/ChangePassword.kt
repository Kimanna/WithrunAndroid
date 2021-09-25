package com.example.withrun

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_change_password.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.security.DigestException
import java.security.MessageDigest

class ChangePassword : AppCompatActivity() {

    val TAG:String = "ChangePassword"

    var tempTextString = ""
    var tempText:String = ""
    var passwordText:String = ""
    var passwordReText:String = ""

    var tempPass: Boolean = false
    var Pass: Boolean = false
    var PassRe: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val pw_pattern = "^(?=.*[a-zA-Z])((?=.*\\d)|(?=.*\\W)).{10,20}$".toRegex()

        tempPassword.setOnFocusChangeListener { view, b ->
            validate()

            if (b) {
                textInputLayout3.isErrorEnabled = false
            } else {
                tempText = tempPassword.text.toString().trim()
                if (tempText == "") {
                    textInputLayout3.error = "임시 비밀번호를 입력해 주세요"
                    return@setOnFocusChangeListener
                }

                tempPass = true
                validate()
            }
        }
        newPassword.setOnFocusChangeListener { view, b ->
            if (b) {
            } else {
                Log.d(TAG,"pass focus out")
                if (Pass && PassRe) {
                    Log.d(TAG,"pass 1st if문 ")
                    if (newPassword.text.toString().trim() != rePassword.text.toString().trim()) {
                        textInputLayout5.error = "비밀번호가 일치하지 않습니다."
                        PassRe = false
                        return@setOnFocusChangeListener
                    }
                    if (newPassword.text.toString().trim() == rePassword.text.toString().trim()) {
                        textInputLayout5.isErrorEnabled = false
                        PassRe = true
                        validate()
                    }
                }
            }
        }

        newPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                textInputLayout5.error = null

                if (newPassword.text.toString().trim() == "") {
                    textInputLayout4.error = "새로운 비밀번호를 입력해 주세요"
                    Pass = false
                    return
                }
                if (!pw_pattern.matches(newPassword.text.toString().trim())) {
                    textInputLayout4.error = "10~20자의 영문, 숫자 조합으로 설정 해 주세요"
                    Pass = false
                    return
                }

                textInputLayout4.isErrorEnabled = false
                passwordText = newPassword.text.toString().trim()
                Pass = true

                validate()

            }
        })

        rePassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (rePassword.text.toString().trim() == "") {
                    PassRe = false
                    return
                }
                if (newPassword.text.toString().trim() != rePassword.text.toString().trim()) {
                    textInputLayout5.error = "비밀번호가 일치하지 않습니다."
                    PassRe = false
                    return
                }

                textInputLayout5.isErrorEnabled = false
                passwordReText = rePassword.text.toString().trim()
                PassRe = true

                validate()

            }
        })

        resetComplete.setOnClickListener(View.OnClickListener { view ->

            coroutine (MainActivity.loginEmail)

        })
    }



    @SuppressLint("ResourceAsColor")
    fun validate() {

        Log.d(TAG, tempPass.toString()+" "+Pass.toString()+" "+PassRe.toString())
        resetComplete.isEnabled = false
        resetComplete.setBackgroundColor(R.color.ultimategray)
        if (!tempPass || !Pass || !PassRe) {
            return
        }
        resetComplete.isEnabled = true
        resetComplete.setBackgroundColor(R.color.purple_700)
    }

    fun coroutine (email:String) {
        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                // network
                getHtml(email, tempText, passwordText)
            }.await()

            Log.d(TAG,html)
            val jsonObject = JSONObject(html).getJSONObject("result")
            val jsonObject1 = jsonObject.getString("affectedRows")
            Log.d(TAG,jsonObject1.toString())

            if (jsonObject1.toInt() == 1) { // 비밀번호 변경 완료
                goProfile ("비밀번호 변경이 완료 되었습니다.",true)
            } else {
                goProfile ("임시비밀번호가 틀렸습니다. \n 다시 시도해 주세요",false)
            }
        }
    }

    fun getHtml(email: String, tempPw: String, password: String?) : String {
        Log.d(TAG,email)
        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url("http://ec2-13-209-169-218.ap-northeast-2.compute.amazonaws.com:12888/users/resetPw/changePw?email=$email&tempPw=$tempPw&password=$password").build()
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

    fun goProfile (msg: String, fail: Boolean) {

        if (fail) {
            val dlg: AlertDialog.Builder = AlertDialog.Builder(
                this,
                android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth
            )
            dlg.setMessage(msg) // 메시지
            dlg.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->

                val intent1 = Intent(this, Profile::class.java)
                intent1.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent1)
                finish()
            })
            dlg.show()
        }
        if (!fail) {
            val dlg: AlertDialog.Builder = AlertDialog.Builder(
                this,
                android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth
            )
            dlg.setMessage(msg) // 메시지
            dlg.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->

            })
            dlg.show()
        }

    }
}