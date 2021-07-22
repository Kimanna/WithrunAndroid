package com.example.withrun

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class NewMemberTerms : AppCompatActivity() {

    private var location = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_member_terms)


        var intent = intent
        if (!TextUtils.isEmpty(intent.getStringExtra("location"))) { // --------------------백버튼, 뒤로가기버튼 컨트롤
            location = intent.getStringExtra("location")
        }


        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener(View.OnClickListener { view ->

            goActivity ()

        })

        val terms = findViewById<TextView>(R.id.terms_use)

        var string: String? = ""
        val stringBuilder = StringBuilder()
        val `is`: InputStream = this.resources.openRawResource(R.raw.terms)
        val reader = BufferedReader(InputStreamReader(`is`))
        while (true) {
            try {
                if (reader.readLine().also { string = it } == null) break
            } catch (e: IOException) {
                e.printStackTrace()
            }
            stringBuilder.append(string).append("\n")
            terms.text = stringBuilder
        }
        `is`.close()
//        Toast.makeText(baseContext, stringBuilder.toString(), Toast.LENGTH_LONG).show()
    }

    fun goActivity () {

        when(location){
            "Settings" -> {
                val intent = Intent(this, Settings::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
                finish()
            }
            "NewMember" -> {
                val intent = Intent(this, NewMember::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
                finish()
            }
        }
    }

}