package com.example.withrun

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_new_member2.*
import kotlinx.android.synthetic.main.activity_new_member3.*
import java.security.DigestException
import java.security.MessageDigest
import java.util.regex.Pattern

class NewMember3 : AppCompatActivity() {

    val TAG:String = "NewMember3"

    companion object {
        var pw : String = ""
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_member3)

        // 뒤로가기 버튼 클릭시 NewMember2 화면으로 돌아감
        back_Newmember2.setOnClickListener(View.OnClickListener { view ->
            val intent1 = Intent(this, NewMember2::class.java)
            startActivity(intent1)
            finish()
        })

        next_newmember4_page.isEnabled = false

        // 이전페이지에서 인증한 email주소로 입력시켜줌
        var intent = getIntent()
        val cf_email = intent.getStringExtra("email")
        email.setText(intent.getStringExtra("email"))

        email.isClickable = false
        email.isEnabled = false
        email.isFocusable = false

        val pw_pattern = "^(?=.*[a-zA-Z])((?=.*\\d)|(?=.*\\W)).{10,20}$".toRegex()
        pw = input_pw.text.toString().trim()
        var pw_re = input_pw_re.text.toString().trim()

        input_pw.setOnFocusChangeListener{ view, b ->
            pw = input_pw.text.toString().trim()

            if (b) {
            } else { // 포커스 아웃일때 pw 정규식 체크
                if (pw_pattern.matches(pw)) {
                    pw_ckbox.isChecked = true
                    NotiPassword.visibility = View.GONE
                    nextPageBt()
                } else {
                    pw_ckbox.isChecked = false
                    NotiPassword.visibility = View.VISIBLE
                    nextPageBt()
                }
            }
        }

        input_pw_re.setOnKeyListener( View.OnKeyListener { view, i, keyEvent ->
            if ( i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP ) {
                pw_re = input_pw_re.text.toString().trim()
                if ( pw == pw_re ) {
                    pw_re_ckbox.isChecked = true
                    nextPageBt()
                } else {
                    pw_re_ckbox.isChecked = false
                    nextPageBt()
                }

            }
            false
        })

        next_newmember4_page.setOnClickListener(View.OnClickListener { view ->



            val intent = Intent(this, NewMember4::class.java)
            startActivity(intent)

        })

    }


    // 다음페이지로 이동하는 버튼 활성, 비활성화
    @SuppressLint("ResourceAsColor")
    fun nextPageBt() {

        if (!pw_ckbox.isChecked || !pw_re_ckbox.isChecked) {
            next_newmember4_page.isEnabled = false
            next_newmember4_page.setBackgroundColor(R.color.lightgrey1)

        } else {

            next_newmember4_page.isEnabled = true
            next_newmember4_page.setBackgroundColor(R.color.ultimategray)

        }

    }

}