package com.example.seniorcare

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import com.example.seniorcare.Model.CheckData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ConfirmActivity : AppCompatActivity() {

    //로그 변수
    private val TAG = ConfirmActivity::class.java.simpleName
    var uid = FirebaseAuth.getInstance().uid.toString()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm)

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

        var time_RG = findViewById<RadioGroup>(R.id.time_RG)

        var first_question_layout = findViewById<LinearLayout>(R.id.first_question_layout)
        var second_question_layout = findViewById<LinearLayout>(R.id.second_question_layout)
        var third_question_layout = findViewById<LinearLayout>(R.id.third_question_layout)
        var fourth_question_layout = findViewById<LinearLayout>(R.id.fourth_question_layout)
        var fifth_question_layout = findViewById<LinearLayout>(R.id.fifth_question_layout)
        var fifth_answer_layout = findViewById<LinearLayout>(R.id.fifth_answer_layout)
        var first_answer = findViewById<TextView>(R.id.first_answer)
        var second_answer = findViewById<TextView>(R.id.second_answer)
        var third_answer = findViewById<TextView>(R.id.third_answer)
        var fourth_answer = findViewById<TextView>(R.id.fourth_answer)
        var fifth_answer = findViewById<TextView>(R.id.fifth_answer)
        var noresult = findViewById<TextView>(R.id.noresult)

        var today_date = ""
        var calendar = findViewById<CalendarView>(R.id.calendarView)
        calendar.maxDate = System.currentTimeMillis()
        calendar.setOnDateChangeListener { calendarView, y, mm, d ->

            first_question_layout.setVisibility(View.INVISIBLE)
            second_question_layout.setVisibility(View.INVISIBLE)
            third_question_layout.setVisibility(View.INVISIBLE)
            fourth_question_layout.setVisibility(View.INVISIBLE)
            fifth_question_layout.setVisibility(View.INVISIBLE)
            fifth_answer_layout.setVisibility(View.INVISIBLE)
            noresult.setVisibility(View.INVISIBLE)
            time_RG.setVisibility(View.VISIBLE)

            var month = mm + 1
            var month_String = ""
            if (month < 10)
                month_String = "0" + month.toString()
            else
                month_String = month.toString()

            today_date = "$y$month_String$d"
        }

        var time : String = ""

        time_RG.setOnCheckedChangeListener { radioGroup, i ->
            when(i) {
                R.id.time_1 -> time = "아침"
                R.id.time_2 -> time = "점심"
                R.id.time_3 -> time = "저녁"
            }

            first_question_layout.setVisibility(View.INVISIBLE)
            second_question_layout.setVisibility(View.INVISIBLE)
            third_question_layout.setVisibility(View.INVISIBLE)
            fourth_question_layout.setVisibility(View.INVISIBLE)
            fifth_question_layout.setVisibility(View.INVISIBLE)
            fifth_answer_layout.setVisibility(View.INVISIBLE)
            noresult.setVisibility(View.INVISIBLE)

            FirebaseFirestore.getInstance()
                .collection("loginUserData")
                .document(uid)
                .collection("healthCheckData")
                .document(today_date)
                .collection(time)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var documents = task.result.toObjects(CheckData::class.java)
                        Log.e(TAG, "document 크기 ${documents.size}, today_date : $today_date, time : $time")
                        if(documents.isEmpty()) {
                            noresult.setVisibility(View.VISIBLE)
                            noresult.setText("조회된 데이터가 없습니다.")
                        }
                        else {
                            var checkData = documents.get(0)

                            noresult.setVisibility(View.INVISIBLE)
                            first_question_layout.setVisibility(View.VISIBLE)
                            second_question_layout.setVisibility(View.VISIBLE)
                            third_question_layout.setVisibility(View.VISIBLE)
                            fourth_question_layout.setVisibility(View.VISIBLE)
                            fifth_question_layout.setVisibility(View.VISIBLE)
                            fifth_answer_layout.setVisibility(View.VISIBLE)

                            first_answer.setText(checkData.first)
                            second_answer.setText(checkData.second)
                            third_answer.setText(checkData.third)
                            fourth_answer.setText(checkData.fourth)
                            fifth_answer.setText(checkData.fifth)
                        }

                    }
                }
            // 조회 끝
        }
    }
}