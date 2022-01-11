package com.example.seniorcare

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import android.widget.Toolbar
import com.example.seniorcare.Model.LoginUserData
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {

    //로그 변수
    private val TAG = MainActivity::class.java.simpleName

    // firebase 인증을 위한 변수
    var auth : FirebaseAuth ? = null

    // 구글 로그인 연동에 필요한 변수
    var googleSignInClient : GoogleSignInClient ? = null
    var GOOGLE_LOGIN_CODE = 9001

    var protect_data : String = "본인"


    // onCreate는 Acitivity가 처음 실행 되는 상태에 제일 먼저 호출되는 메소드로 여기에 실행시 필요한 각종 초기화 작업을 적어줌
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 로그인 페이지가 첫화면임 (activity_main.xml의 레이아웃 사용)
        setContentView(R.layout.activity_main)


        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })

        // firebaseauth를 사용하기 위한 인스턴스 get
        auth = FirebaseAuth.getInstance()

        // xml에서 요소 가져오기
        var google_sign_in_button = findViewById<SignInButton>(R.id.google_sign_in_button)
        var protect_YN_GR = findViewById<RadioGroup>(R.id.protect_YN)

        // 구글 로그인 버튼 클릭 시 이벤트 : googleLogin function 실행
        google_sign_in_button.setOnClickListener {


            var protect_YN = protect_YN_GR.checkedRadioButtonId
            protect_data = findViewById<Button>(protect_YN).text.toString()

            FirebaseAuth.getInstance().signOut()
            googleSignInClient?.signOut()
            googleLogin()
        }

        // 구글 로그인을 위해 구성되어야 하는 코드 (Id, Email request)
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    } // onCreate

    private fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    } // googleLogin

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_LOGIN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
            if (result != null) {
                if(result.isSuccess) {
                    var account = result.signInAccount
                    firebaseAuthWithGoogle(account)
                }
            }
        } //if
    } // onActivityResult

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if(task.isSuccessful) {

                    Log.e(TAG, "보호자 여부 $protect_data")
                    // 로그인 성공 시
                    val isNew = task.result!!.additionalUserInfo!!.isNewUser

                    // 최초 로그인 유저인 경우 화면 이동
                    if(isNew){

                        // FireStore 데이터베이스에 로그인 사용자 정보 저장 (테이블 이름 : loginUserData)
                        val uid = auth?.uid.toString()
                        val userName = auth?.currentUser?.displayName.toString()
                        val email = auth?.currentUser?.email.toString()
                        val photoUrl = auth?.currentUser?.photoUrl.toString()
                        val loginTime = System.currentTimeMillis()
                        val loginUserData = LoginUserData(uid, "", "", userName, email, photoUrl, loginTime, "", "", "", "", protect_data)

                        val db = FirebaseFirestore.getInstance().collection("loginUserData")
                        db.document(uid)
                            .set(loginUserData)
                            .addOnCompleteListener {
                                Log.e(TAG, "신규 로그인 :: DB에 로그인 정보 생성 성공")
                            }
                            .addOnFailureListener {
                                Log.e(TAG, "신규 로그인 :: DB에 로그인 정보 생성 실패")
                            }

                        if(protect_data == "본인") {
                            // 유저의 기본 정보 입력 화면으로 이동
                            startActivity(Intent(this, BasicActivity::class.java))
                            // 로그인 화면 종료
                            finish()
                        }
                        else {
                            // 가족 검색 화면으로 이동
                            startActivity(Intent(this, FindfamilyActivity::class.java))
                            // 로그인 화면 종료
                            finish()
                        }
                    }
                    // 기존 로그인 유저인 경우
                    else{
                        // 로그인 시간 갱신
                        val loginTime = System.currentTimeMillis()
                        FirebaseFirestore.getInstance()
                            .collection("loginUserData")
                            .document(auth?.uid.toString())
                            .update("loginTime", loginTime)
                            .addOnCompleteListener {
                                Log.e(TAG, "기존 로그인 :: DB에 로그인 시간 갱신 성공")
                            }
                            .addOnFailureListener {
                                Log.e(TAG, "기존 로그인 :: DB에 로그인 시간 갱신 실패")
                            }

                        // 보호자 여부에 따른 인텐트 분기
                        FirebaseFirestore.getInstance()
                            .collection("loginUserData")
                            .document(auth?.uid.toString())
                            .get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    var userData = task.result?.toObject(LoginUserData::class.java)
                                    if (userData != null) {
                                        if(userData.protect == "본인")
                                            startActivity(Intent(this, CheckActivity::class.java))
                                        else {
                                            val intent = Intent(this, ConfirmProtectorActivity::class.java)
                                            intent.putExtra("uid", userData.uid)
                                            intent.putExtra("relationUid", userData.relation_uid)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                }
                            }
                    }
                } else {
                    // 로그인 실패 시
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    } //firebaseAuthWithGoogle
}