package com.example.italk

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

const val REQUEST_CODE_SIGN_IN=0
class LoginActivity : AppCompatActivity() {
    lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.title=null

        //login with Google
        auth= FirebaseAuth.getInstance()
        button_login_login_with_GG.setOnClickListener {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.webClient_id))
                .requestEmail()
                .build()
            val signInClient = GoogleSignIn.getClient(this, options)
            signInClient.signInIntent.also {
                startActivityForResult(it, REQUEST_CODE_SIGN_IN)
            }
        }

        //login with email and password
        button_edittext_login.setOnClickListener {
            var email = email_edittext_login.text.toString()
            var password = password_edittext_login.text.toString()

            Log.d("LoginActibity","Email : ${email}")
            Log.d("LoginActibity","password : ${password}")

            if(email.isEmpty()||password.isEmpty()){
                Toast.makeText(this,"Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    Log.d("LoginActibity","sign in successfull")
                    var intent= Intent(this,LatestMessagesActivity::class.java)
                    intent.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.d("LoginActibity","sign in fail ${it.message}")
                    Toast.makeText(this,
                    "Email or password is wrong!",Toast.LENGTH_SHORT).show()
                }
        }

        //intent to register
        back_to_register_text_View.setOnClickListener {
            var intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }
        //forgot password
        forgot_password.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_forgot,null)
            val userEmail = view.findViewById<EditText>(R.id.editBox)
            builder.setView(view)
            val dialog=builder.create()
            view.findViewById<Button>(R.id.buttonaReset).setOnClickListener {
                val email = userEmail.text.toString().trim()
                if(email.isEmpty()){
                    Toast.makeText(applicationContext,"Please enter your email",Toast.LENGTH_SHORT).show()
                }else{
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(applicationContext,"Email sent. Please check your email",Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }else{
                                Toast.makeText(applicationContext,"Failed to send email",Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
            view.findViewById<Button>(R.id.buttonCancel).setOnClickListener {
                dialog.dismiss()
            }
            if (dialog.window !=null){
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            dialog.show()
        }
    }

    //login with google
    private fun googleAuthForFirebase(account: GoogleSignInAccount) {
        val uid =FirebaseAuth.getInstance().uid ?:""
        val usersRef = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.signInWithCredential(credentials).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Successfully logged in", Toast.LENGTH_LONG).show()
                    // Get user information from Google account
                    val currentUser = auth.currentUser
                    val uid = currentUser?.uid ?: ""
                    val username = currentUser?.displayName ?: ""
                    val profileImageUri = currentUser?.photoUrl.toString()
                    // Create a new User object and save it to the database
                    val user = User(uid, username, profileImageUri,"","")
                    usersRef.child(uid).setValue(user)
                    Log.d("LoginGG","create database successful")
                    var intent= Intent(this@LoginActivity,LatestMessagesActivity::class.java)
                    intent.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            } catch(e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_SIGN_IN) {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
            account?.let {
                googleAuthForFirebase(it)
            }
        }
    }

}