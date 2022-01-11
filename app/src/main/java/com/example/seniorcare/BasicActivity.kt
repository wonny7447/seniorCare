package com.example.seniorcare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent as Intent

class BasicActivity : AppCompatActivity() {

    //로그 변수
    private val TAG = BasicActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic)

        var birth_editText = findViewById<EditText>(R.id.birth).text
        var height_editText = findViewById<EditText>(R.id.height).text
        var weight_editText = findViewById<EditText>(R.id.weight).text
        var sex_group = findViewById<RadioGroup>(R.id.sex_Radio_Group)
        val submit = findViewById<Button>(R.id.submit)

        submit.setOnClickListener() {

            var fbAuth: FirebaseAuth? = null
            fbAuth = FirebaseAuth.getInstance()
            var uid = fbAuth.uid.toString()

            var sex = sex_group.checkedRadioButtonId
            var sex_data = findViewById<Button>(sex).text.toString()

            //sendData(uid.toString(), birth, height, weight, sex_data.text)
            Log.e(TAG, "uid value : $uid")
            var height : String = height_editText.toString()
            var weight : String = weight_editText.toString()
            var birth : String = birth_editText.toString()
            Log.e(TAG, "sex 데이터 : $sex_data")
            Log.e(TAG, "height 데이터 : $height")
            Log.e(TAG, "weight 데이터 : $weight")
            Log.e(TAG, "birth 데이터 : $birth")

            val db = FirebaseFirestore.getInstance().collection("loginUserData")
            db.document(uid)
                .update("weight", weight,"height", height, "birth", birth, "sex", sex_data)
                .addOnCompleteListener {
                    Log.e(TAG, "기존 로그인 :: DB에 로그인 시간 갱신 성공")
                    // 건강 상태 등록 화면으로 이동
                    startActivity(Intent(this, CheckActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Log.e(TAG, "기존 로그인 :: DB에 로그인 시간 갱신 실패")
                }
        }

    } //onCreate
}