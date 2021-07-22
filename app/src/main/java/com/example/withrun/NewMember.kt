package com.example.withrun

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.example.withrun.R.color

class NewMember : AppCompatActivity() {

    var terms_use:  Boolean = false
    var terms_person:  Boolean = false
//    var terms_marketing:  Boolean = false

    companion object {
        var marketing_noti : Int? = 0
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_member)

        // 뒤로가기 버튼 클릭시 login 화면으로 돌아감
        val back_login = findViewById<ImageView>(R.id.back_login)
        back_login.setOnClickListener(View.OnClickListener { view ->
            val dlg: AlertDialog.Builder = AlertDialog.Builder(this,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
            dlg.setMessage("뒤로가기 시 작성된 모든 내용은 저장되지 않습니다. 계속 하시겠습니까?") // 메시지
            dlg.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                val intent1 = Intent(this, MainActivity::class.java)
                startActivity(intent1)
                finish()
            })
            dlg.setNegativeButton("취소", null)
            dlg.show()
        })

        // textview를 클릭시 약관을 보여주는 activity로 이동
        val terms_use_agree = findViewById<TextView>(R.id.terms_use_agree)
        val terms_person_agree = findViewById<TextView>(R.id.terms_person_agree)

        // 약관동의 checkbox
        val terms_all_agree = findViewById<CheckBox>(R.id.terms_all_agree)         // 모두동의
        val terms_use_ck = findViewById<CheckBox>(R.id.terms_use_ck)               // 이용약관동의
        val terms_person_ck = findViewById<CheckBox>(R.id.terms_person_ck)         // 개인정보 수집동의
        val terms_marketing_ck = findViewById<CheckBox>(R.id.terms_marketing_ck)   // 마케팅 알림 수신동의

        // 다음페이지로 이동하는 버튼
        val next_newmember_page = findViewById<Button>(R.id.next_newmember_page)

        next_newmember_page.setOnClickListener(View.OnClickListener { view ->
            val intent = Intent(this, NewMember2::class.java)
            startActivity(intent)
        })

        // 약관 txt 파일을 보여주는 activity 이동
        terms_use_agree.setOnClickListener(View.OnClickListener { View ->
            val intent = Intent(this, NewMemberTerms::class.java)
            intent.putExtra("location","NewMember")
            startActivity(intent)
        })
        terms_person_agree.setOnClickListener(View.OnClickListener { View ->
            val intent = Intent(this, NewMemberTerms1::class.java)
            intent.putExtra("location","NewMember")
            startActivity(intent)
        })

        // 이용약관 모두동의 클릭이벤트
        terms_all_agree.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                terms_use_ck.isChecked = true
                terms_person_ck.isChecked = true
                terms_marketing_ck.isChecked = true
                next_newmember_page.isEnabled = true
                next_newmember_page.setBackgroundColor(color.ultimategray)
            }
            else {
                terms_use_ck.isChecked = false
                terms_person_ck.isChecked = false
                terms_marketing_ck.isChecked = false
                next_newmember_page.isEnabled = false
                next_newmember_page.setBackgroundColor(color.lightgrey1)
            }
        }
        
        terms_use_ck.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                terms_use = true

                // 만약 이용약관과 개인정보수집동의를 체크한 경우 버튼 활성화 + 컬러변경
                if (terms_use && terms_person) {
                    next_newmember_page.isEnabled = true
                    next_newmember_page.setBackgroundColor(color.ultimategray)
                }
                else {
                    next_newmember_page.isEnabled = false
                    next_newmember_page.setBackgroundColor(color.lightgrey1)
                }
            }
            else {
                terms_use = false

                // 만약 이용약관과 개인정보수집동의를 체크한 경우 버튼 활성화 + 컬러변경
                if (terms_use && terms_person) {
                    next_newmember_page.isEnabled = true
                    next_newmember_page.setBackgroundColor(color.ultimategray)
                }
                else {
                    next_newmember_page.isEnabled = false
                    next_newmember_page.setBackgroundColor(color.lightgrey1)
                }
            }
        }

        terms_person_ck.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                terms_person = true

                // 만약 이용약관과 개인정보수집동의를 체크한 경우 버튼 활성화 + 컬러변경
                if (terms_use && terms_person) {
                    next_newmember_page.isEnabled = true
                    next_newmember_page.setBackgroundColor(color.ultimategray)
                }
                else {
                    next_newmember_page.isEnabled = false
                    next_newmember_page.setBackgroundColor(color.lightgrey1)
                }
            }
            else {

                terms_person = false
                // 만약 이용약관과 개인정보수집동의를 체크한 경우 버튼 활성화 + 컬러변경
                if (terms_use && terms_person) {
                    next_newmember_page.isEnabled = true
                    next_newmember_page.setBackgroundColor(color.ultimategray)
                }
                else {
                    next_newmember_page.isEnabled = false
                    next_newmember_page.setBackgroundColor(color.lightgrey1)
                }
            }
        }
        terms_marketing_ck.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked)
                marketing_noti = 1
            else
                marketing_noti = 0
        }

    }




}