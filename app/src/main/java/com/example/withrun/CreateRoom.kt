package com.example.withrun

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
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
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_create_room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class CreateRoom : AppCompatActivity() {

    val TAG : String = "CreateRoom"

    var year: String? = ""
    var month: String? = ""
    var day: String? = ""

    var startTimeString: String? = null
    var finishTimeString: String = ""
    var runDistanceText_fix : String = ""
    var choiceGender_fix : String = "모두"
    var level_fix : String = "모두"
    private var runAlone: Boolean? = false

    lateinit var imagePath: String
    var imageFileName: String? = null

    //Our constants
    private val OPERATION_CAPTURE_PHOTO = 1
    private val OPERATION_CHOOSE_PHOTO = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        // 메인 창으로 이동
        back_home.setOnClickListener(View.OnClickListener { view ->
            val intent = Intent(this, Home::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        })

        // image 삽입부분
        val choose_photo = arrayOf("사진 찍기" , "갤러리에서 가져오기" , "취소")
        imageArea.setOnClickListener {
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
        }

        // 오늘 날자 삽입
        val today = todayDate()
        runDate.text = todayDate()

        // 오늘이 아닌 다른날자에 러닝 시작 선택
        selectDiffDate.setOnClickListener {
            showDatePicker()
        }

        // 러닝 시작 시간 선택, 목표거리를 입력해야 실행
        runStartTime.setOnClickListener {
            if (runDistanceText_fix == "") {
                show("러닝 목표 거리를 우선 입력해 주세요")
                return@setOnClickListener
            }
            showTimePicker()
        }

        runStartTimeText.setOnClickListener {
            if (runDistanceText_fix == "") {
                show("러닝 목표 거리를 우선 입력해 주세요")
                return@setOnClickListener
            }
            showTimePicker()
        }

        //  러닝 거리 선택
        runDistance.setOnClickListener {
            numberPicker()
        }
        runDistanceText.setOnClickListener {
            numberPicker()
        }


//        //난이도 지정 스위치버튼
//        specifyLevel.setOnCheckedChangeListener { buttonView, isChecked ->
//            runAlone = isChecked
//        }



        genderSetting.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) { // 아무것도 선택하지 않은 경우
                Log.d(TAG, "gender 스피너 아무것도 선택되지 않은 상태")
            }

            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) { // 아이템 선택 한 경우
                if ("$position" == "0") {
                    choiceGender_fix = "모두"
                } else if ("$position" == "1") {
                    choiceGender_fix = "남자만"
                } else if ("$position" == "2") {
                    choiceGender_fix = "여자만"
                }
            }
        }

        level.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) { // 아무것도 선택하지 않은 경우
                Log.d(TAG, "gender 스피너 아무것도 선택되지 않은 상태")
            }

            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) { // 아이템 선택 한 경우
                if ("$position" == "0") {
                    level_fix = "모두"
                } else if ("$position" == "1") {
                    level_fix = "런린이"
                } else if ("$position" == "2") {
                    level_fix = "중급러너"
                } else if ("$position" == "3") {
                    level_fix = "프로러너"
                }
            }
        }

        // 룸 개설하기 버튼
        // 필수 입력 사항 - 룸이름, 날자, 시간, 거리
        createRoom.setOnClickListener {

            if (roomTitle.text == null || roomTitle.text.toString() == "") {
                roomTitle.setSelection(0)
                show("러닝 룸 이름을 입력해주세요.\n\n"+
                        "*필수입력사항 : 룸 이름, 러닝 날자, 러닝 시간, 러닝 거리")
                return@setOnClickListener
            }

            if (runDistanceText_fix == "") {
                show("러닝 거리를 입력해주세요.\n\n" +
                        "*필수입력사항 : 룸 이름, 러닝 날자, 러닝 시간, 러닝 거리")
                return@setOnClickListener
            }

            Log.d(TAG, "서버 발송할 데이터 확인 : "+year+month+day+ roomTitle.text.toString()+startTimeString+runDistanceText_fix+choiceGender_fix+level_fix)

            coroutine () // 생성하는 룸 저장 로직
        }
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

    private fun renderImage(imagePath: String?){
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)

            roomPhoto?.setImageBitmap(rotateImageIfRequired(imagePath))
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
                        roomPhoto!!.setImageBitmap(rotateImageIfRequired(file.path))
                    }
                    else{
                        val decode = ImageDecoder.createSource(this.contentResolver,
                            Uri.fromFile(file))
                        val bitmap = ImageDecoder.decodeBitmap(decode)
                        roomPhoto!!.setImageBitmap(rotateImageIfRequired(file.path))
                    }
                    textView36.visibility = View.GONE
                    imageView9.visibility = View.GONE
                    roomPhoto.visibility = View.VISIBLE
                }
            OPERATION_CHOOSE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {

                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitkat(data)
                        textView36.visibility = View.GONE
                        imageView9.visibility = View.GONE
                        roomPhoto.visibility = View.VISIBLE
                    }
                }
        }
    }

    fun goIntoRoom (roomNo: Int) {
        Log.d(TAG, "goIntoRoom 에서 loanStartTime 값 확인 : "+year+month+day+startTimeString+":00")

        val intent = Intent(this, Home::class.java)
        intent.putExtra("roomNo",roomNo)
        intent.putExtra("longStartTime",year+"-"+month+"-"+day+" "+startTimeString+":00")
        intent.putExtra("location", "CreateRoom")
        startActivity(intent)
        finish()
    }

    fun coroutine () {

        val progressDialog = ProgressDialog(this)
        showProgressBar (progressDialog)

        CoroutineScope(Dispatchers.Main).launch { this
            val html = CoroutineScope(Dispatchers.Default).async { this
                // network
                getHtml()
            }.await()
            Log.d(TAG,"http 통신 후 데이터 값 : "+html)

            dismissProgressBar(progressDialog)

            goIntoRoom (html.toInt())

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

        // 혼자뛰기 모드인 경우 1, 함께 뛰기 모드인 경우 0
//        var aloneMode:Int
//
//        if (runAlone!!) aloneMode = 1
//        else aloneMode = 0

        val jsonObject = JSONObject()
        jsonObject.putOpt("imagepathname", imagePath)
        jsonObject.putOpt("startDate", year+"-"+month+"-"+day)
        jsonObject.putOpt("roomTitle", roomTitle.text.toString())
        jsonObject.putOpt("startTime", startTimeString)
        jsonObject.putOpt("finishTime", startTimeString)
        jsonObject.putOpt("distance", runDistanceText_fix)
        jsonObject.putOpt("sortGender", choiceGender_fix)
        jsonObject.putOpt("sortLevel", level_fix)
        jsonObject.putOpt("roomManager", MainActivity.loginId)

        if (imagePath != "else") { // 프로필 image를 변경한 경우 image path값이 변경되어 있다.
            jsonObject.putOpt("file", encoder(imagePath))
        }

        val okHttpClient = OkHttpClient()
        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .method("POST", requestBody)
            .url("http://ec2-13-209-169-218.ap-northeast-2.compute.amazonaws.com:12888/rooms/createRoom")
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


    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, DatePickerDialog.OnDateSetListener { datePicker, y, m, d->
            year = y.toString()
            month = if (m + 1 < 10) "0" + (m + 1) else (m + 1).toString()
            day = if (d < 10) "0" + d.toString() else d.toString()

            runDate.text = month+"/"+day
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE)).apply {
            datePicker.minDate = System.currentTimeMillis() // 이전시간 선택 안되도록 설정
            datePicker.init(year!!.toInt(),month!!.toInt()-1,day!!.toInt(),null) // 유저가 선택한 날자로 초기값 세팅
        }.show()



    }

//    private fun showTimePicker() {
//        val dialog = AlertDialog.Builder(this).create()
//        val edialog : LayoutInflater = LayoutInflater.from(this)
//        val mView : View = edialog.inflate(R.layout.create_room_time_picker,null)
//
//        val timePickerStart : TimePicker = mView.findViewById(R.id.timePickerStart)
//        val timePickerFinish : TimePicker = mView.findViewById(R.id.timePickerFinish)
//        val cancel : Button = mView.findViewById(R.id.cancel)
//        val save : Button = mView.findViewById(R.id.confirm)
//        val pace : TextView = mView.findViewById(R.id.pace)
//        val goalDistance : TextView = mView.findViewById(R.id.goalDistance)
//
//        goalDistance.text = "목표 거리 $runDistanceText_fix km" // 설정한 목표거리 보여줌
//
//        // 종료시간을 시작시간의 10분 후로 미리 세팅함
//        timePickerFinish.minute = timePickerStart.minute + 10
//
////        if ( timePickerFinish.minute > 49 ) {
////
////            if ( timePickerFinish.hour > 22 ) {
////                timePickerFinish.hour = 0
////            } else {
////                timePickerFinish.hour = timePickerStart.hour + 1
////            }
////        } else {
////
////        }
//
//
//        var startTime: Int = timePickerStart.hour * (60 * 60) + timePickerStart.minute * 60
//        var finishTime: Int = timePickerFinish.hour * (60 * 60) + timePickerFinish.minute * 60
//        var gap: Int
//
//        startTimeString = oftenUseMethod.formatTime(timePickerStart.hour, timePickerStart.minute)!!
//        finishTimeString = oftenUseMethod.formatTime(timePickerFinish.hour, timePickerFinish.minute)!!
//
//        timePickerStart.setOnTimeChangedListener { timePicker, hour, min ->
//            startTime = hour * (60 * 60) + min * 60
//
//            Log.d(TAG, "startdatelong $startTime " + timePickerStart.hour + " "+ timePickerStart.minute+" "+ hour + " "+ min)
//
//            if (startTime > finishTime) {
//                gap = (24 * 60 * 60 ) - startTime + finishTime
//
//            } else if (finishTime > startTime) {
//                gap = finishTime - startTime
//
//            } else {
//                gap = 0
//            }
//            pace.text = oftenUseMethod.secondsToTime(gap/runDistanceText_fix.toInt().toLong()) +" 분/km"
//
//            startTimeString = oftenUseMethod.formatTime(timePickerStart.hour, timePickerStart.minute)!!
//        }
//
//        timePickerFinish.setOnTimeChangedListener { timePicker, hour, min ->
//            finishTime = hour * (60 * 60) + min * 60
//
//                if (startTime > finishTime) {
//                    gap = (24 * 60 * 60 ) - startTime + finishTime
//
//                } else if (finishTime > startTime) {
//                    gap = finishTime - startTime
//
//                } else {
//                    gap = 0
//                }
//                pace.text = oftenUseMethod.secondsToTime(gap/runDistanceText_fix.toInt().toLong()) +" 분/km"
//
//            finishTimeString = oftenUseMethod.formatTime(timePickerFinish.hour, timePickerFinish.minute)!!
//        }
//
//        cancel.setOnClickListener(View.OnClickListener { view ->
//            dialog.dismiss()
//            dialog.cancel()
//        })
//
//        save.setOnClickListener(View.OnClickListener { view ->
//
//            startTimeString = oftenUseMethod.formatTime(timePickerStart.hour, timePickerStart.minute)!!
//            finishTimeString = oftenUseMethod.formatTime(timePickerFinish.hour, timePickerFinish.minute)!!
//
//            runStartTime.visibility = View.GONE
//            runStartTimeText.setText("$startTimeString ~ $finishTimeString")
//            runStartTimeText.visibility = View.VISIBLE
//
//            dialog.dismiss()
//            dialog.cancel()
//
//        })
//
//        dialog.setView(mView)
//        dialog.create()
//        dialog.show()
//    }

    private fun showTimePicker() {
        val dialog = AlertDialog.Builder(this).create()
        val edialog : LayoutInflater = LayoutInflater.from(this)
        val mView : View = edialog.inflate(R.layout.create_room_time_picker,null)

        val timePickerStart : TimePicker = mView.findViewById(R.id.timePickerStart)
        val cancel : Button = mView.findViewById(R.id.cancel)
        val save : Button = mView.findViewById(R.id.confirm)

        if ( startTimeString != null) {
            timePickerStart.hour = startTimeString!!.substring(0,2).toInt()
            timePickerStart.minute = startTimeString!!.substring(3,4).toInt()
        }

        setTimePickerInterval(timePickerStart)
        timePickerStart.setOnTimeChangedListener { timePicker, hour, min ->

            startTimeString = timePickerStart.hour.toString() +":"+ timePickerStart.minute.toString() + "0"

            Log.d(TAG, "timepicker 시간 출력 test " + timePickerStart.hour + " "+ timePickerStart.minute+" "+ hour + " "+ min+ " "+startTimeString)

        }

        cancel.setOnClickListener(View.OnClickListener { view ->
            dialog.dismiss()
            dialog.cancel()
        })

        save.setOnClickListener(View.OnClickListener { view ->

            runStartTime.visibility = View.GONE
            runStartTimeText.setText("$startTimeString")
            runStartTimeText.visibility = View.VISIBLE

            dialog.dismiss()
            dialog.cancel()

        })

        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }

    private fun setTimePickerInterval(timePicker: TimePicker) {

        val TIME_PICKER_INTERVAL = 10
        try {
            val minutePicker = timePicker.findViewById(
                Resources.getSystem().getIdentifier(
                    "minute", "id", "android"
                )
            ) as NumberPicker
            minutePicker.minValue = 0
            minutePicker.maxValue = 60 / TIME_PICKER_INTERVAL - 1
            var displayedValues: ArrayList<String> = ArrayList()
            var i = 0
            while (i < 60) {
                displayedValues.add(String.format("%02d", i))
                i += TIME_PICKER_INTERVAL
            }
            minutePicker.displayedValues = displayedValues.toArray(arrayOfNulls<String>(0))
        } catch (e: Exception) {
            Log.e(TAG, "Exception: $e")
        }
    }



    private fun numberPicker() {

        val dialog = AlertDialog.Builder(this).create()
        val edialog : LayoutInflater = LayoutInflater.from(this)
        val mView : View = edialog.inflate(R.layout.number_picker,null)

        val numberPicker : NumberPicker = mView.findViewById(R.id.numberPicker)
        val cancel : Button = mView.findViewById(R.id.cancel)
        val save : Button = mView.findViewById(R.id.confirm)
        val unit : TextView = mView.findViewById(R.id.unit)
        val title_dialog : TextView = mView.findViewById(R.id.title_dialog)

        numberPicker.wrapSelectorWheel = false

        numberPicker.minValue = 1
        numberPicker.maxValue = 100
        numberPicker.value = 5
        unit.text = "KM"
        title_dialog.text = "목표 러닝 거리"

        cancel.setOnClickListener(View.OnClickListener { view ->
            dialog.dismiss()
            dialog.cancel()
        })

        save.setOnClickListener(View.OnClickListener { view ->

            runDistance.visibility = View.GONE
            runDistanceText_fix = numberPicker.value.toString()
            runDistanceText.setText(runDistanceText_fix + " KM")
            runDistanceText.visibility = View.VISIBLE

            dialog.dismiss()
            dialog.cancel()

        })

        dialog.setView(mView)
        dialog.create()
        dialog.show()
    }


    fun todayDate() : String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("MM/dd")
        val formatted = current.format(formatter)

        val splitArray = current.toString().split("-")
        day = splitArray[2].substring(0,2)
        month = splitArray[1]
        year = splitArray[0]

        Log.d(TAG, year+month+day)

        return formatted
    }

    private fun show(message: String) {
        Toast.makeText(this,message,Toast.LENGTH_LONG).show()
    }

    private fun showProgressBar(progressDialog: ProgressDialog) {
        progressDialog.setTitle("러닝 룸 생성")
        progressDialog.setMessage("신규 룸을 생성하는 중 입니다.")
        progressDialog.show()
    }

    private fun dismissProgressBar(progressDialog: ProgressDialog) {
        progressDialog.dismiss()
    }
}