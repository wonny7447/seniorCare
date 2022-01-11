package com.example.seniorcare

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import com.example.seniorcare.Model.CheckData
import com.example.seniorcare.Model.pushDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CheckActivity : AppCompatActivity() {

    //로그 변수
    private val TAG = CheckActivity::class.java.simpleName
    var uid = FirebaseAuth.getInstance().uid.toString()


    // push 관련
    val JSON = MediaType.parse("application/json; charset=utf-8")//Post전송 JSON Type
    val url = "https://fcm.googleapis.com/fcm/send" //FCM HTTP를 호출하는 URL
    val serverKey =
        "AAAAHkmJ9Qg:APA91bH0mQIwuKWLGZP4tUHdDQT1IdD2VPdhhKraZwTJctVkyr6W8OaOaX0dFsFYOVZu6BiM73GKcnj_4PZTLhZpsAgKeL4VJhjZjYoL0SqSxqzwEizCBJU4lkrOwWcuH4gpstTymoPn"

    var token = "epgRS8TtRVW2-bCxIHOyRA:APA91bHU1i2JjgGW4o3Hm0S2KMt41gmpptQsjAhzDhC-jAZkS_ShmYixTlVhe6o7j_R8JL6dBVwRnLWr5jY2KDEJGZhLa2Jp2IaNyZ_2ee96jIszSA4WEFjXVHDjouiujhOfreXagte7"
    //Firebase에서 복사한 서버키
    var okHttpClient: OkHttpClient
    var gson: Gson

    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)

        // 하단 메뉴바
        var button_file = findViewById<Button>(R.id.button_file)
        var button_daylog = findViewById<Button>(R.id.button_daylog)
        var button_mypage = findViewById<Button>(R.id.button_mypage)
        var button_today = findViewById<Button>(R.id.button_today)

        button_file.setOnClickListener {
            startActivity(Intent(this, FileActivity::class.java))
            finish()
        }
        button_daylog.setOnClickListener {
            startActivity(Intent(this, ConfirmActivity::class.java))
            finish()
        }
        button_mypage.setOnClickListener {
            startActivity(Intent(this, LogoutActivity::class.java))
            finish()
        }
        button_today.setOnClickListener {
            startActivity(Intent(this, CheckActivity::class.java))
            finish()
        }



        var today = findViewById<TextView>(R.id.today)
        val current = LocalDateTime.now()
        // 2022 / 01 / 11 형식으로 날짜 출력
        val today_date_formatter = DateTimeFormatter.ofPattern("yyyy / MM /dd")
        val today_date = current.format(today_date_formatter)
        val today_date_formatter2 = DateTimeFormatter.ofPattern("yyyyMMdd")
        val today_date2 = current.format(today_date_formatter2)
        // 22 형식으로 시간 출력
        val now_formatter = DateTimeFormatter.ofPattern("HH")
        val now_time = current.format(now_formatter)
        var now : String ?= null
        if(now_time == "11" || now_time == "12" || now_time == "13" || now_time == "14" || now_time == "15") {
            now = "점심"   // 11시~16시
        } else if(now_time == "05" ||now_time == "06" || now_time == "07" || now_time == "08" || now_time == "09" || now_time == "10") {
            now = "아침"   // 5시~10시
        } else {
            now = "저녁"
        }

        today.setText(today_date.toString() + "  " + now + "  건강 상태 기록")

        var first_yn_gr = findViewById<RadioGroup>(R.id.first_yn)
        var second_yn_gr = findViewById<RadioGroup>(R.id.second_yn)
        var third_yn_gr = findViewById<RadioGroup>(R.id.third_yn)
        var fourth_yn_gr = findViewById<RadioGroup>(R.id.fourth_yn)
        var fifth_text = findViewById<EditText>(R.id.fifth_text)
        var submit = findViewById<Button>(R.id.submit)

        submit.setOnClickListener() {
            var first_yn = first_yn_gr.checkedRadioButtonId
            var first_data = findViewById<Button>(first_yn).text.toString()
            var second_yn = second_yn_gr.checkedRadioButtonId
            var second_data = findViewById<Button>(second_yn).text.toString()
            var third_yn_yn = third_yn_gr.checkedRadioButtonId
            var third_data = findViewById<Button>(third_yn_yn).text.toString()
            var fourth_yn = fourth_yn_gr.checkedRadioButtonId
            var fourth_data = findViewById<Button>(fourth_yn).text.toString()
            var checkData = CheckData(now, first_data, second_data, third_data, fourth_data, fifth_text.text.toString())

            Log.e(TAG, "now : $now, first_data : $first_data, second_data : $second_data, third_data : $third_data, fourth_data : $fourth_data, fifth_text : ${fifth_text.text.toString()}")

            FirebaseFirestore.getInstance()
                .collection("loginUserData")
                .document(uid)
                .collection("healthCheckData")
                .document(today_date2)
                .collection(now)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var documents = task.result.toObjects(CheckData::class.java)
                        if (documents.isEmpty()) {
                            FirebaseFirestore.getInstance()
                                .collection("loginUserData")
                                .document(uid)
                                .collection("healthCheckData")
                                .document(today_date2)
                                .collection(now)
                                .document()
                                .set(checkData)
                                .addOnCompleteListener {
                                    Log.e(TAG, "checkData에 데이터 insert 성공")


                                    sendMessage(token, "새로운 건강 데이터가 등록되었습니다. 바로 확인해주세요.", "[시니어 케어]")
                                    startActivity(Intent(this, ConfirmActivity::class.java))
                                    finish()
                                }





                        } else {
                            Toast.makeText(this, "오늘 "+ now +"에 이미 등록된 건강정보가 있습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

        }


    }//oncreate


    fun sendMessage(destinationUid: String, title: String, message: String) {
        Log.i("토큰정보", token)
        var noti = com.example.seniorcare.Model.pushDTO.Notification(title, message)
        var pushDTO = pushDTO(token, noti)

        var body = RequestBody.create(JSON, gson?.toJson(pushDTO)!!)
        var request = Request
            .Builder()
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "key=" + serverKey)
            .url(url)       //푸시 URL 세팅
            .post(body)     //pushDTO가 담긴 body 세팅
            .build()
        okHttpClient?.newCall(request)?.enqueue(object : Callback {//푸시 전송
            override fun onFailure(call: Call?, e: IOException?) {}

            override fun onResponse(call: Call?, response: Response?) {
                println(response?.body()?.string())  //요청이 성공했을 경우 결과값 출력
            }
        })
    } // 푸시 sendMessage
}