package com.example.italk

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBar.NavigationMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.UUID

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //click on register button
        button_edittext_register.setOnClickListener {
            performRegister()
        }
        //intent to login layout
        already_have_account_text_View.setOnClickListener {
            Log.d("Register","alreadt have account ?")
            var intent= Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }
        //cick on select photo
        select_photo_button_register.setOnClickListener {
            Log.d("Register","Try to show photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,0)
        }
    }
    var selectedPhotoUri:Uri?= null
    //go to the link and get the image from the link
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null ){
            //proceed and check what the selected image was...
            Log.d("Register","Photo was selected")
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            selectPhotoImageView_register.setImageBitmap(bitmap)

            select_photo_button_register.alpha=0f
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            select_photo_button_register.setBackgroundDrawable(bitmapDrawable)
        }
    }
    //this function is create a user with email and password use FirebaseAuth
    private fun performRegister() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        Log.d("Register","Email is: ${email}")
        Log.d("Register","Password is: ${password}")

        if(email.isEmpty()||password.isEmpty()){
            Toast.makeText(this,"Please enter email and password",Toast.LENGTH_SHORT).show()
            return
        }
        //firebase authentication to create a user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener
                //else if successful
                Log.d("Register","Successfuly created user with uid: ${it.result?.user?.uid}")
                upLoadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Log.d("Register","Failed create user: ${it.message}")
                Toast.makeText(this,"Failed create user: ${it.message}",Toast.LENGTH_SHORT).show()
            }
    }
    //this function is upload user image to Firebase Storage
    private fun upLoadImageToFirebaseStorage() {
        if(selectedPhotoUri==null) return

        val fileName = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/image/$fileName")

        ref.putFile(selectedPhotoUri!!)
            .addOnCompleteListener{
                Log.d("Register","Successfully uploaded image: ${it.result?.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("Register","File location: $it")
                    saveUserToFirebaseDatabase(it.toString())
                }
                ref.downloadUrl.addOnFailureListener {
                    Log.d("Register","Fail finded location file")
                }
            }
    }
    //this function is save user to firebase Database (real time database)
    private fun saveUserToFirebaseDatabase(profileImageUri: String) {
        val uid =FirebaseAuth.getInstance().uid ?:""
        val ref =FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user=User(uid,username_edittext_register.text.toString(),profileImageUri,"","")
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("Register","Successful , we saved the user to Firebase Database")
                Toast.makeText(this,"Register account successful",Toast.LENGTH_LONG).show()
//                var intent=Intent(this,LatestMessagesActivity::class.java)
//                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("Register","Fail save user to Firebase Database")
            }
    }
}

