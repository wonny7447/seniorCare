package com.example.seniorcare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.example.seniorcare.Model.LoginUserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FindfamilyActivity : AppCompatActivity() {

    //로그 변수
    private val TAG = FindfamilyActivity::class.java.simpleName
    var userData : LoginUserData? = null
    var relation_uid : String ?= null
    var uid = FirebaseAuth.getInstance().uid.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_findfamily)

        var name_editText = findViewById<EditText>(R.id.name).text
        var email_editText = findViewById<EditText>(R.id.email).text
        var result = findViewById<TextView>(R.id.result)
        var sex_group = findViewById<RadioGroup>(R.id.sex_Radio_Group)
        var submit = findViewById<Button>(R.id.submit)
        var add_family = findViewById<Button>(R.id.add_family)
        var spinner = findViewById<Spinner>(R.id.spinner)
        var sAdapter = ArrayAdapter.createFromResource(this, R.array.relation, android.R.layout.simple_spinner_dropdown_item)
        var spinner_data : String? = null
        spinner.setAdapter(sAdapter)

        submit.setOnClickListener() {

            var sex = sex_group.checkedRadioButtonId
            var sex_data = findViewById<Button>(sex).text.toString()

            Log.e(TAG, "uid value : $uid")
            var name : String = name_editText.toString()
            var email : String = email_editText.toString()
            Log.e(TAG, "sex 데이터 : $sex_data")
            Log.e(TAG, "name 데이터 : $name")
            Log.e(TAG, "email 데이터 : $email")

            FirebaseFirestore.getInstance()
                .collection("loginUserData")
                .whereEqualTo("userName", name)
                .whereEqualTo("userEmail", email)
                .whereEqualTo("sex", sex_data)
                .whereEqualTo("protect", "본인")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var documents = task.result.toObjects(LoginUserData::class.java)
                        if(documents.isEmpty()) {
                            add_family.setVisibility(View.INVISIBLE)
                            spinner.setVisibility(View.INVISIBLE)
                            result.setText("검색 결과가 없습니다.\n다시 조회하세요.")
                        }
                        else{
                            userData = documents.get(0)
                            relation_uid = userData?.uid.toString()
                            result.setText(userData?.userName.toString()+"님을 가족으로 등록하시겠습니까?")
                            add_family.setVisibility(View.VISIBLE)
                            spinner.setVisibility(View.VISIBLE)
                        }
                    }
                }
            // 조회 끝
        } // 검색 onclick

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                spinner_data = spinner.selectedItem.toString()
            }

        }

        add_family.setOnClickListener {
            Log.e(TAG, "가족등록 $relation_uid, $spinner_data")
            FirebaseFirestore.getInstance()
                .collection("loginUserData")
                .document(uid)
                .update("relation_uid", relation_uid, "relation", spinner_data)
                .addOnCompleteListener {
                    Log.e(TAG, "가족 등록 완료")
                }
                .addOnFailureListener {
                    Log.e(TAG, "가족 등록 실패")
                }
        } // 가족등록 onclick

    } //onCreate
} // class