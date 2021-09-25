package com.example.withrun

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_new_member4.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_revise_profile.*
import kotlinx.android.synthetic.main.activity_revise_profile.gender
import kotlinx.android.synthetic.main.activity_revise_profile.view.*
import kotlinx.android.synthetic.main.activity_revise_profile.view.man
import kotlinx.android.synthetic.main.activity_revise_profile.view.woman
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.security.DigestException
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class ReviseProfile : AppCompatActivity() {

    val TAG : String = "ReviseProfile"

    lateinit var datePicker : DatePicker
    lateinit var profileimg: CircleImageView

    lateinit var imagePath: String
    var imageFileName: String? = null

    //Our constants
    private val OPERATION_CAPTURE_PHOTO = 1
    private val OPERATION_CHOOSE_PHOTO = 2

    var day: String? = "0000"
    var month: String? = "00"
    var year: String? = "00"

    var height_fix: Int? = 0
    var weight_fix: Int? = 0

    var gender_fix: String? = "else"

    var isChangeProfile = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_revise_profile)

        val tealColor = ContextCompat.getColor(this, R.color.teal_700)

        datePicker = findViewById<DatePicker>(R.id.dataPicker) // DatePicker 초기화
        profileimg = findViewById<CircleImageView>(R.id.profileimg) // DatePicker 초기화

        setProfile () // 현재 로그인 중인 유저프로필 정보 set

        // profile 화면으로 돌아가는 back 버튼
        back_Profile.setOnClickListener(View.OnClickListener { view ->
            goProfile ()
        })

        proheight.setOnClickListener {
            val dialog = AlertDialog.Builder(this).create()
            val edialog : LayoutInflater = LayoutInflater.from(this)
            val mView : View = edialog.inflate(R.layout.number_picker,null)

            val numberPicker : NumberPicker = mView.findViewById(R.id.numberPicker)
            val cancel : Button = mView.findViewById(R.id.cancel)
            val save : Button = mView.findViewById(R.id.confirm)
            val unit : TextView = mView.findViewById(R.id.unit)
            val title_dialog : TextView = mView.findViewById(R.id.title_dialog)

            numberPicker.wrapSelectorWheel = false

            numberPicker.minValue = 100
            numberPicker.maxValue = 250
            numberPicker.value = height_fix!!.toInt()
            unit.text = "cm"
            title_dialog.text = "자신의 키"

            cancel.setOnClickListener(View.OnClickListener { view ->
                dialog.dismiss()
                dialog.cancel()
            })

            save.setOnClickListener(View.OnClickListener { view ->

                height_fix = numberPicker.value
                proheight.setText(height_fix.toString()+" cm")

                setUseableTextView(updateBt, true)          // 수정버튼 활성화

                dialog.dismiss()
                dialog.cancel()

            })

            dialog.setView(mView)
            dialog.create()
            dialog.show()
        }
        proweight.setOnClickListener{

            val dialog1 = AlertDialog.Builder(this).create()
            val edialog1 : LayoutInflater = LayoutInflater.from(this)
            val mView1 : View = edialog1.inflate(R.layout.number_picker,null)

            val numberPicker1 : NumberPicker = mView1.findViewById(R.id.numberPicker)
            val cancel1 : Button = mView1.findViewById(R.id.cancel)
            val save1 : Button = mView1.findViewById(R.id.confirm)
            val unit1 : TextView = mView1.findViewById(R.id.unit)
            val title_dialog1 : TextView = mView1.findViewById(R.id.title_dialog)

            numberPicker1.wrapSelectorWheel = false

            numberPicker1.minValue = 25
            numberPicker1.maxValue = 250
            numberPicker1.value = weight_fix!!.toInt()
            unit1.text = "kg"
            title_dialog1.text = "자신의 몸무게"

            cancel1.setOnClickListener(View.OnClickListener { view ->
                dialog1.dismiss()
                dialog1.cancel()
            })

            save1.setOnClickListener(View.OnClickListener { view ->

                weight_fix = numberPicker1.value
                proweight.setText(weight_fix.toString()+" kg")

                setUseableTextView(updateBt, true)          // 수정버튼 활성화

                dialog1.dismiss()
                dialog1.cancel()

            })

            dialog1.setView(mView1)
            dialog1.create()
            dialog1.show()
        }

        gender.setOnCheckedChangeListener { radioGroup, optionId ->
            run {
                when (optionId) {
                    R.id.man -> {
                        gender_fix = "man"
                        Log.d(TAG, "man")
                    }
                    R.id.woman -> {
                        gender_fix = "woman"
                        Log.d(TAG, "woman")
                    }
                    else ->  {
                        gender_fix = "else"
                        Log.d(TAG, "else")
                    }
                }
                setUseableTextView(updateBt, true)          // 수정버튼 활성화
            }
        }

      updateNickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
//                Log.d(TAG, "after : " + upsfedateNickname.text.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                Log.d(TAG, "before : " + updateNickname.text.toString())
            }

            @SuppressLint("ResourceAsColor")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                Log.d(TAG, "sha256 test : " + hashSHA256(updateNickname.text.toString()))

                notiNickname.visibility = View.GONE
                if (updateNickname.text.toString() == "") {
                    nicknameNoti.visibility = View.VISIBLE
                    ckNickname.visibility = View.GONE

                    setUseableTextView(updateBt, false)          // 수정버튼 비활성화

                } else if(updateNickname.text.toString() == MainActivity.loginNickname) {
                    nicknameNoti.visibility = View.GONE
                    ckNickname.setBackgroundResource(R.drawable.count_bg_seethrough)
                    ckNickname.isEnabled = false
                    setUseableTextView(updateBt, false)           // 수정버튼 비활성화


                } else {
                    nicknameNoti.visibility = View.GONE
                    ckNickname.visibility = View.VISIBLE
                    ckNickname.isEnabled = true
                    ckNickname.setBackgroundResource(R.drawable.count_bg_visible_)

                    setUseableTextView(updateBt, false)          // 수정버튼 비활성화


                    // 중복선택 버튼 클릭시 서버와 통신 후 닉네임 중복인 경우 1을 return, 중복이 아닐 시 0을 return
                    ckNickname.setOnClickListener(View.OnClickListener { view ->
                        Log.d(TAG,"클릭리스너 클릭 시 edittext 의 값 : " + updateNickname.text.toString())

                        coroutine(updateNickname.text.toString())

                    })
                }
            }
        })

        // 프로필 사진 변경 시 permission 확인 과
        val choose_photo = arrayOf("사진 찍기" , "갤러리에서 가져오기" , "기본 이미지로 변경")
        profileimg.setOnClickListener(View.OnClickListener { view ->

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                } else {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                }
            }

            val builder = AlertDialog.Builder(this)
            builder.setTitle("프로필 사진 선택")
                .setItems(choose_photo) { dialog, which->
                    when ( which ) {
                        0 -> { // 사진 찍기
                            capturePhoto()
                        }
                        1 -> { // 갤러리에서 가져오기
                            openGallery()
                        }
                        2 -> { // 기본이미지로 선택
                            profileimg.setImageResource(R.drawable.avatar)
                            imagePath = "defaultImg"

                        }
                    }
                }
            val dialog = builder.create()
            dialog.show()
        })

        // 수정완료버튼
        updateBt.setOnClickListener{

                updateProfileCoroutine ()

        }
    }

    fun setUseableTextView(tv: TextView, useable: Boolean) {
        tv.isClickable = useable
        tv.isEnabled = useable
        tv.isFocusable = useable
        tv.isFocusableInTouchMode = useable
        if (useable)  tv.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_700))
        else tv.setBackgroundColor(ContextCompat.getColor(this, R.color.lightgrey1))

    }

    fun coroutine (nickname:String) {

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                // network
                getHtml(nickname)
            }.await()
            Log.d(TAG,"html 값 " + html)
            changeUi(html)

        }
    }

    fun getHtml(nickname: String) : String {
        val client = OkHttpClient.Builder().build()
        val req = okhttp3.Request.Builder().url("http://ec2-13-209-169-218.ap-northeast-2.compute.amazonaws.com:12888/users/ckNickname/$nickname").build()
        client.newCall(req).execute().use {
                response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }

    fun changeUi (html:String) {
        if (html == "1") {

            notiNickname.text = "이미 사용중인 닉네임 입니다."
            notiNickname.setTextColor(ContextCompat.getColor(this, R.color.RED))
            notiNickname.visibility = View.VISIBLE
            ckNickname.setBackgroundResource(R.drawable.count_bg_visible_)

            setUseableTextView(updateBt, false)          // 수정버튼 비활성화

        } else {

            notiNickname.text = "사용 가능한 닉네임 입니다."
            notiNickname.setTextColor(ContextCompat.getColor(this, R.color.frenchblue))
            notiNickname.visibility = View.VISIBLE
            ckNickname.setBackgroundResource(R.drawable.count_bg_seethrough)

            setUseableTextView(updateBt, true)          // 수정버튼 활성화


        }
    }

    fun updateProfileCoroutine () {
        Log.d(TAG,"updateProfileCoroutine 지나감 ")

        val progressDialog = ProgressDialog(this)
        showProgressBar (progressDialog)

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                // network
                getHtml()
            }.await()
            Log.d(TAG,html)

            val jsonArray = JSONObject(html).getJSONArray("result").getJSONObject(0)


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

            MainActivity.loginId = jsonArray.getInt("id")
            MainActivity.loginEmail = jsonArray.getString("email")
            MainActivity.loginMarketing_noti = jsonArray.getString("marketing_noti")
            MainActivity.loginNickname = jsonArray.getString("nickname")
            MainActivity.loginGender = jsonArray.getString("gender")
            MainActivity.loginBirth = jsonArray.getString("birth")
            MainActivity.loginHeight = jsonArray.getString("height")
            MainActivity.loginWeight = jsonArray.getString("weight")
            MainActivity.loginProfileImgPath = jsonArray.getString("profileImgPath")

            dismissProgressBar(progressDialog)
            goProfile ()
        }
    }

    fun getHtml() : String {

        // 이미지 선택 안할 경우 imagePath 변수안에 else 선언
        // 이미지 선택 한 경우 imagePath엔 이미 파일경로 저장됨, encoding file 변수안에 담음
        if (this::imagePath.isInitialized) {
        } else {
            imagePath = "else"
        }
        Log.d(TAG, "서버 발송 path value : $imagePath")
        Log.d(TAG, "서버 발송 데이터 : birth $year-$month-$day")
        Log.d(TAG, "서버 발송 데이터 : gender $gender_fix")
        Log.d(TAG, "서버 발송 데이터 : height $height_fix")
        Log.d(TAG, "서버 발송 데이터 : weight $weight_fix")

        val jsonObject = JSONObject()
        jsonObject.put("id", MainActivity.loginId)
        jsonObject.put("nickname", updateNickname.text.toString())
        jsonObject.putOpt("imagepathname", imagePath)
        jsonObject.putOpt("birth", year+"-"+month+"-"+day)
        jsonObject.putOpt("gender", gender_fix)
        jsonObject.putOpt("height", height_fix)
        jsonObject.putOpt("weight", weight_fix)

        if (imagePath == "else" || imagePath == "defaultImg") { // else -> 프로필 이미지를 변경하지 않은경우,defaultImg -> 기본이미지로 프로필img를 변경한 경우
        } else {
            jsonObject.putOpt("file", encoder(imagePath))
        }

        val okHttpClient = OkHttpClient()
        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .method("POST", requestBody)
            .url("http://ec2-13-209-169-218.ap-northeast-2.compute.amazonaws.com:12888/users/usersUpdate")
            .build()

        okHttpClient.newCall(request).execute().use { response -> return if(response.body != null) {
            response.body!!.string()
        }
        else {
            "body null"
        }
        }
    }

    // Encode File/Image to Base64
    private fun encoder(filePath: String): String{
        val bytes = File(filePath).readBytes()
        val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
        return base64
    }

    private fun capturePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            if (takePictureIntent.resolveActivity(this.packageManager) != null) {
                // 찍은 사진을 그림파일로 만들기
                val photoFile: File? =
                    try {
                        createImageFile()
                    } catch (ex: IOException) {
                        Log.d("TAG", "그림파일 만드는도중 에러생김")
                        null
                    }

                // 그림파일을 성공적으로 만들었다면 onActivityForResult로 보내기
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this, "com.example.withrun.fileprovider", it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, OPERATION_CAPTURE_PHOTO)
                }
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "PHOTO_${timeStamp}", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            imagePath = absolutePath
        }
    }

    private fun openGallery(){
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        startActivityForResult(intent, OPERATION_CHOOSE_PHOTO)
    }

    private fun handleImageOnKitkat(data: Intent?) {

        val uri = data!!.data
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        imageFileName = "${timeStamp}.jpg"

        //DocumentsContract defines the contract between a documents provider and the platform.
        if (DocumentsContract.isDocumentUri(this, uri)){
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri.authority){
                val id = docId.split(":")[1]
                val selsetion = MediaStore.Images.Media._ID + "=" + id
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    selsetion)
            }
            else if ("com.android.providers.downloads.documents" == uri.authority){
                val contentUri = ContentUris.withAppendedId(Uri.parse(
                    "content://downloads/public_downloads"), java.lang.Long.valueOf(docId))
                imagePath = getImagePath(contentUri, null)
            }
        }
        else if ("content".equals(uri.scheme, ignoreCase = true)){
            imagePath = getImagePath(uri, null)
        }
        else if ("file".equals(uri.scheme, ignoreCase = true)){
            imagePath = uri.path
        }
        renderImage(imagePath)
    }

    private fun getImagePath(uri: Uri?, selection: String?): String {
        var path: String? = null
        val cursor = contentResolver.query(uri, null, selection, null, null )
        if (cursor != null){
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path!!
    }

    private fun renderImage(imagePath: String?){
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            profileimg?.setImageBitmap(rotateImageIfRequired(imagePath))
        }
        else {
//            show("ImagePath is null")
        }
    }

    private fun rotateImageIfRequired(imagePath: String): Bitmap? {
        var degrees = 0
        try {
            val exif = ExifInterface(imagePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degrees = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degrees = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degrees = 270
            }
        } catch (e: IOException) {
            Log.e("ImageError", "Error in reading Exif data of $imagePath", e)
        }

        val decodeBounds: BitmapFactory.Options = BitmapFactory.Options()
        decodeBounds.inJustDecodeBounds = true
        var bitmap: Bitmap? = BitmapFactory.decodeFile(imagePath, decodeBounds)
        val numPixels: Int = decodeBounds.outWidth * decodeBounds.outHeight
        val maxPixels = 2048 * 1536 // requires 12 MB heap
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inSampleSize = if (numPixels > maxPixels) 2 else 1
        bitmap = BitmapFactory.decodeFile(imagePath, options)
        if (bitmap == null) {
            return null
        }

        val matrix = Matrix()
        matrix.setRotate(degrees.toFloat())
        bitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width,
            bitmap.height, matrix, true
        )
        return bitmap
    }

    // 로그인 중인 유저의 프로필데이터 set
    fun setProfile () {

        // 프로필 이미지 set
        if (MainActivity.loginProfileImgPath == "else") {
            profileimg.setImageResource(R.drawable.avatar)

        } else {
            Glide.with(this)
                    .load(Constants.URL+MainActivity.loginProfileImgPath)
                    .into(profileimg)
        }

        // Nickname, email 은 null or "" 이 존재하지 않음
        updateNickname.setText(MainActivity.loginNickname)
        proemail.setText(MainActivity.loginEmail)

        // gender 경우 설정한 경우 "man" or "woman" / 설정하지 않은 경우 "else"
        if (MainActivity.loginGender == "man") {
            gender.man.isChecked = true
            gender_fix = "man"
        } else if (MainActivity.loginGender == "woman") {
            gender.woman.isChecked = true
            gender_fix = "woman"
        }

        // 생년월일의 경우 1992-09-06T00:00:00.000Z 로 데이터 받음 / 유저가 설정하지 않은 경우 0000-00-00T00:00:00.000Z
        val splitBirth : String = MainActivity.loginBirth
        val splitArray = splitBirth.split("-")

        val dateformat = Calendar.getInstance()
        datePicker.maxDate = dateformat.timeInMillis // 미래가 표시되지 않도록 설정정

        // if -> 유저가 생년월일을 설정하지 않은 경우 1990.01.01 로 set
        // else -> 유저가 설정 한 생년월일로 설정
        if (splitArray[0] == "0000") {
            datePicker.init(1990,1,1,null) // 1990.01.01 로 초기값 세팅
            day = "00"
            month = "00"
            year = "0000"
        } else {

            day = splitArray[2].substring(0,2)
            month = splitArray[1]
            year = splitArray[0]
            datePicker.init(splitArray[0].toInt(),(splitArray[1].toInt())-1,splitArray[2].substring(0,2).toInt(), DatePicker.OnDateChangedListener { datePicker, ck_year, ck_month, ck_day ->

                year = ck_year.toString()
                month = if (ck_month + 1 < 10) "0" + (ck_month + 1) else (ck_month + 1).toString()
                day = if (ck_day < 10) "0" + ck_day.toString() else ck_day.toString()

                Log.d(TAG, year+month+day)

                setUseableTextView(updateBt, true)          // 수정버튼 활성화
            })
        }

        proheight.setText(MainActivity.loginHeight+" cm")
        proweight.setText(MainActivity.loginWeight+" kg")

        height_fix = MainActivity.loginHeight.toInt()
        weight_fix = MainActivity.loginWeight.toInt()

    }

    fun goProfile () {

        val intent = Intent(this, Profile::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            OPERATION_CAPTURE_PHOTO ->

                if (resultCode == Activity.RESULT_OK) {

                    // 카메라로부터 받은 데이터가 있을경우에만
                    val file = File(imagePath)
                    if (Build.VERSION.SDK_INT < 28) {
                        val bitmap = MediaStore.Images.Media
                            .getBitmap(contentResolver, Uri.fromFile(file))  //Deprecated
                        profileimg!!.setImageBitmap(rotateImageIfRequired(file.path))
                    }
                    else{
                        val decode = ImageDecoder.createSource(this.contentResolver,
                            Uri.fromFile(file))
                        val bitmap = ImageDecoder.decodeBitmap(decode)
                        profileimg!!.setImageBitmap(rotateImageIfRequired(file.path))
                    }
                }

            OPERATION_CHOOSE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {

                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitkat(data)
                    }
                }
        }
    }

    private fun showProgressBar(progressDialog: ProgressDialog) {
        progressDialog.setTitle("프로필 수정")
        progressDialog.setMessage("프로필 저장 중 입니다.")
        progressDialog.show()
    }

    private fun dismissProgressBar(progressDialog: ProgressDialog) {
        progressDialog.dismiss()
    }
}