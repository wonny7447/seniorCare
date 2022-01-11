package com.example.seniorcare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.seniorcare.Model.CheckData
import com.example.seniorcare.Model.LoginUserData
import com.google.firebase.firestore.FirebaseFirestore

class MyseniorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mysenior)

        val uid = intent.getStringExtra("uid").toString()
        val relationUid = intent.getStringExtra("relationUid").toString()

        var button_file = findViewById<Button>(R.id.button_file)
        var button_daylog = findViewById<Button>(R.id.button_daylog)
        var button_mypage = findViewById<Button>(R.id.button_mypage)
        var button_senior = findViewById<Button>(R.id.button_senior)

        button_file.setOnClickListener {
            val intent = Intent(this, FileProtectorActivity::class.java)
            intent.putExtra("uid", uid)
            intent.putExtra("relationUid", relationUid)
            startActivity(intent)
            finish()
        }
        button_daylog.setOnClickListener {
            val intent = Intent(this, ConfirmProtectorActivity::class.java)
            intent.putExtra("uid", uid)
            intent.putExtra("relationUid", relationUid)
            startActivity(intent)
            finish()
        }
        button_mypage.setOnClickListener {
            startActivity(Intent(this, LogoutActivity::class.java))
            finish()
        }
        button_senior.setOnClickListener {
            val intent = Intent(this, MyseniorActivity::class.java)
            intent.putExtra("uid", uid)
            intent.putExtra("relationUid", relationUid)
            startActivity(intent)
            finish()
        }


        var name = findViewById<TextView>(R.id.name)
        var birth = findViewById<TextView>(R.id.birth)
        var sex = findViewById<TextView>(R.id.sex)
        var height = findViewById<TextView>(R.id.height)
        var weight = findViewById<TextView>(R.id.weight)


        FirebaseFirestore.getInstance()
            .collection("loginUserData")
            .document(relationUid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var document = task.result.toObject(LoginUserData::class.java)

                    if(document?.uid != "") {
                        name.setText(document?.userName)
                        birth.setText(document?.birth)
                        sex.setText(document?.sex)
                        height.setText(document?.height)
                        weight.setText(document?.weight)
                    }
                }
            }
        // 조회 끝
    }
}