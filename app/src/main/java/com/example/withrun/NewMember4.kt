package com.example.withrun

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_new_member2.*
import kotlinx.android.synthetic.main.activity_new_member3.*
import kotlinx.android.synthetic.main.activity_new_member4.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.security.DigestException
import java.security.MessageDigest
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*

class NewMember4 : AppCompatActivity() {

    val TAG:String = "NewMember4"

    //Our variables
    private var mImageView: CircleImageView? = null
//    private var mUri: Uri? = null
//    var m_imageFile: File? = null

    lateinit var imagePath: String
    var imageFileName: String? = null

    //Our constants
    private val OPERATION_CAPTURE_PHOTO = 1
    private val OPERATION_CHOOSE_PHOTO = 2

    var day: String? = "00"
    var month: String? = "00"
    var year: String? = "0000"

    var height_fix: Int? = 0
    var weight_fix: Int? = 0

    var gender_fix: String? = "else"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_member4)

        // 뒤로가기 버튼 클릭시 NewMember2 화면으로 돌아감
        back_Newmember3.setOnClickListener(View.OnClickListener { view ->
            val intent1 = Intent(this, NewMember3::class.java)
            startActivity(intent1)
            finish()
        })

        val choose_photo = arrayOf("사진 찍기" , "갤러리에서 가져오기" , "취소")
        nickname.setText(getRandomString(5)) // 닉네임 임의 설정된 값 세팅
        mImageView = findViewById(R.id.profileimg) // 이미지 적용부분 초기화

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
                        2 -> { // 취소
                        }
                    }
                }
            val dialog = builder.create()
            dialog.show()
        })


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
            }
        }


        // 생년월일 변수안에 기본값으로 (today) 설정, 사용자가 변경할 시 변경된 값으로 변수 재 적용
        val datePicker = findViewById<DatePicker>(R.id.dataPicker)

        val dateformat = Calendar.getInstance()
        dataPicker.maxDate = dateformat.timeInMillis // 미래가 표시되지 않도록 설정정
        datePicker.init(1990,1,1,null) // 1990.01.01 로 초기값 세팅

       datePicker.setOnDateChangedListener { datePicker, ck_year, ck_month, ck_day ->
            year = ck_year.toString()
            month = if (ck_month + 1 < 10) "0" + (ck_month + 1) else (ck_month + 1).toString()
            day = if (ck_day < 10) "0" + ck_day.toString() else ck_day.toString()

           Log.d(TAG, year+month+day)
        }

        height.setOnClickListener( View.OnClickListener { view ->

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
            numberPicker.value = 167
            unit.text = "cm"
            title_dialog.text = "자신의 키"

            cancel.setOnClickListener(View.OnClickListener { view ->
                dialog.dismiss()
                dialog.cancel()
            })

            save.setOnClickListener(View.OnClickListener { view ->

                height_fix = numberPicker.value
                height.setText(height_fix.toString()+" cm")
                Log.d(TAG,height_fix.toString())

                dialog.dismiss()
                dialog.cancel()

            })

            dialog.setView(mView)
            dialog.create()
            dialog.show()

        })

        weight.setOnClickListener( View.OnClickListener { view ->

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
            numberPicker1.value = 62
            unit1.text = "kg"
            title_dialog1.text = "자신의 몸무게"

            cancel1.setOnClickListener(View.OnClickListener { view ->
                dialog1.dismiss()
                dialog1.cancel()
            })

            save1.setOnClickListener(View.OnClickListener { view ->

                weight_fix = numberPicker1.value
                weight.setText(weight_fix.toString()+" kg")
                Log.d(TAG,weight_fix.toString())

                dialog1.dismiss()
                dialog1.cancel()

            })

            dialog1.setView(mView1)
            dialog1.create()
            dialog1.show()

        })
        join_bt.setOnClickListener( View.OnClickListener { view ->

            coroutine ()
            showDialog()

        })

    }

    fun String.convertDateToTimestamp(): Long =
            SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).parse(this).time


    fun showDialog() {
        val dlg: android.support.v7.app.AlertDialog.Builder = android.support.v7.app.AlertDialog.Builder(this,  android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
        dlg.setMessage("회원가입이 완료 되었습니다.") // 메시지
        dlg.setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        })
        dlg.show()

    }

    // Encode File/Image to Base64
    private fun encoder(filePath: String): String{
        val bytes = File(filePath).readBytes()
        val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
        return base64
    }

    fun coroutine () {
        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                // network
                getHtml()
            }.await()

            Log.d(TAG,html.toString())
        }
    }

    fun getHtml() : String {

        // 이미지 선택 안할 경우 imagePath 변수안에 else 선언
        // 이미지 선택 한 경우 imagePath엔 이미 파일경로 저장됨, encoding file 변수안에 담음
        if (this::imagePath.isInitialized) {
        } else {
            imagePath = "else"
        }
        Log.d(TAG, "서버 발송 path value : "+imagePath)
        Log.d(TAG, "비밀번호 sha256 : "+hashSHA256(NewMember3.pw))


        val jsonObject = JSONObject()
        jsonObject.put("email", NewMember2.email)
        jsonObject.put("marketing_noti", NewMember.marketing_noti)
        jsonObject.put("password", hashSHA256(NewMember3.pw))
        jsonObject.put("nickname", nickname.text.toString())
        jsonObject.putOpt("imagepathname", imagePath)
        jsonObject.putOpt("birth", year+"-"+month+"-"+day)
        jsonObject.putOpt("gender", gender_fix)
        jsonObject.putOpt("height", height_fix)
        jsonObject.putOpt("weight", weight_fix)

        if (imagePath != "else") { // 프로필 image를 변경한 경우 image path값이 변경되어 있다.
            jsonObject.putOpt("file", encoder(imagePath))
        }

        val okHttpClient = OkHttpClient()
        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .method("POST", requestBody)
            .url("http://ec2-13-209-169-218.ap-northeast-2.compute.amazonaws.com:12888/users/usersJoin")
            .build()

        okHttpClient.newCall(request).execute().use { response -> return if(response.body != null) {
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

    // 닉네임 랜덤 생성 함수
    fun getRandomString (length: Int) :String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString ("")
    }

    private fun show(message: String) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
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
//    private fun capturePhoto(){
//
//        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            createImageFile()?.let {
//                mUri = FileProvider.getUriForFile(this,
//                    "com.example.withrun.fileprovider", it)
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
//                startActivityForResult(takePictureIntent, OPERATION_CAPTURE_PHOTO)
//                m_imageFile = it
//            }
//
//        }
//        Log.d(TAG,mUri.toString())
//        Log.d(TAG,m_imageFile.toString())
//
//    }

//    private fun createImageFile(): File {
//        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        imageFileName = "PHOTO_${timeStamp}.jpg"
//        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        return File(storageDir, imageFileName)
//    }

    private fun openGallery(){
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        startActivityForResult(intent, OPERATION_CHOOSE_PHOTO)
    }

    private fun renderImage(imagePath: String?){
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)

            mImageView?.setImageBitmap(rotateImageIfRequired(imagePath))
        }
        else {
            show("ImagePath is null")
        }
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

    @TargetApi(19)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>
                                            , grantedResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantedResults)
        when(requestCode){
            1 ->
                if (grantedResults.isNotEmpty() && grantedResults.get(0) ==
                    PackageManager.PERMISSION_GRANTED){
                    openGallery()
                }else {
                    show("Unfortunately You are Denied Permission to Perform this Operataion.")
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            OPERATION_CAPTURE_PHOTO ->

                if (resultCode == Activity.RESULT_OK) {

                    // 카메라로부터 받은 데이터가 있을경우에만
                    val file = File(imagePath)
                    if (Build.VERSION.SDK_INT < 28) {
                        val bitmap = MediaStore.Images.Media
                            .getBitmap(contentResolver, Uri.fromFile(file))  //Deprecated
                        mImageView!!.setImageBitmap(rotateImageIfRequired(file.path))
                    }
                    else{
                        val decode = ImageDecoder.createSource(this.contentResolver,
                            Uri.fromFile(file))
                        val bitmap = ImageDecoder.decodeBitmap(decode)
                        mImageView!!.setImageBitmap(rotateImageIfRequired(file.path))
                    }

//                    Log.d(TAG,encoder(imagePath))

//                    m_imageFile?.let {
//                        MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(it))?.let {
//                            mImageView!!.setImageBitmap(it)
//                        }
//                    }
                }
            OPERATION_CHOOSE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {

                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitkat(data)
                    }
                }
        }
    }


}