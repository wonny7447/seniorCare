package com.example.seniorcare

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class FileActivity : AppCompatActivity() {

    // File 전송에 쓰이는 코드
    var FILE = 1
    var uploadFileUri : String ?= ""

    private val TAG = FileActivity::class.java.simpleName

    // 현재 접속중인 사용자 정보 불러오기
    var fbAuth: FirebaseAuth? = FirebaseAuth.getInstance()
    val uid = fbAuth?.uid.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file)
        setTitle("건강 데이터 업로드")

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

        // layout의 ListView와 연결 (1. ArrayList와 어댑터 연결, 2. 어댑터와 ListView 연결)
        val fileListView = findViewById<ListView>(R.id.fileListView)
        var fileList = ArrayList<String>()
        var fileAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fileList)

        // Firebase Storage 의 uid 폴더 조회
        val storage = Firebase.storage
        val listRef = storage.reference.child(uid)

        listRef.listAll()
            .addOnSuccessListener { listResult ->
                for (item in listResult.items) {
                    // 어댑터에 조회한 내용을 추가 (파일 이름만, 파일 url 불러오려면 item.downloadUrl 사용)
                    fileAdapter.add(item.name)
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "조회할 파일 목록 없음")
            }

        // 파일 목록을 전부 불러온 경우 ListView에 연결
        fileListView.adapter = fileAdapter
        fileListView.choiceMode = ListView.CHOICE_MODE_SINGLE

        // 첨부 파일 업로드 버튼 클릭 이벤트
        val postattach_button = findViewById<ImageButton>(R.id.postattach_button)
        postattach_button.setOnClickListener {
            openContent()
        }

    }

    // Firebase Storage에 파일 업로드
    private fun openContent() {
        var intent = Intent(Intent.ACTION_PICK)
        //intent.type = auth.uid.toString()+"/*"
        intent.type = "*/*"
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent, FILE)
    } //openContent

    // Firebase Storage에 파일 업로드
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == FILE) {
            var attachUri = data?.data!!
            var attachUriString = data?.data.toString()
            uploadFile(attachUri, attachUriString)
        }
    } //onActivityResult

    // Firebase Storage에 파일 업로드
    private fun uploadFile(attachUri: Uri, attachUriString: String) {
        var metaCursor = contentResolver.query(attachUri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)!!
        metaCursor.moveToFirst()
        var fileName = metaCursor.getString(0)
        metaCursor.close()
        
        // 사용자의 uid 경로에 파일 업로드
        var storageRef = FirebaseStorage.getInstance().reference.child(uid).child(fileName)
        storageRef.putFile(attachUri).addOnSuccessListener {
            uploadFileUri = fileName
            Log.e(TAG, "파일 업로드 성공 : " + uploadFileUri)
            // attachFileName.text = uploadFileUri
            startActivity(Intent(this, FileActivity::class.java))
        }
    } //uploadFile
}