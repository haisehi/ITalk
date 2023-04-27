package com.example.italk.message

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.italk.R
import com.example.italk.User
import com.example.italk.databinding.ActivityUpdateBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_update.*
import java.util.*
import kotlin.collections.HashMap

class UpdateActivity : AppCompatActivity() {
    companion object{
        var infoUserUD:User?=null
        val TAG = "UpdateUser"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        var userNameBarUD = intent.getStringExtra("userNameUD")
//        supportActionBar?.title=userNameBarUD.toString()
        //button update data
        button_update_data_user.setOnClickListener {
            updateDataUser()
        }
        //click select photo
        infoUserImgUpdate.setOnClickListener {
            Log.d(TAG,"Try to show photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,0)
        }
    }

    var selectedPhotoUri: Uri?= null
    //go to the link and get the image from the link
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null ){
            //proceed and check what the selected image was...
            Log.d(TAG,"Photo was selected")
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            selectPhotoImageView_update.setImageBitmap(bitmap)

            infoUserImgUpdate.alpha=0f
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            select_photo_button_register.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun upLoadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        // Upload image to Firebase Storage
        val fileName = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/image/$fileName")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")
                // Get image download URL
                ref.downloadUrl.addOnSuccessListener { uri ->
                    Log.d(TAG, "File location: $uri")
                    // Update user profile image in Realtime Database
                    val uid = FirebaseAuth.getInstance().uid ?: ""
                    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
                    val imgUpdate = infoUserUD?.profileImageUri
                    if (imgUpdate != ""){
                        ref.child("profileImageUri").setValue(uri.toString())
                            .addOnSuccessListener {
                                Log.d(TAG, "Profile image URL successfully updated in Realtime Database")
                                Toast.makeText(this, "Profile image successfully updated", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Log.e(TAG, "Failed to update profile image URL in Realtime Database", it)
                            }
                    }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to upload image to Firebase Storage", it)
            }
    }

    private fun updateDataUser() {
        val uid = FirebaseAuth.getInstance().uid ?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        var username = update_name.text.toString()
        var phone = update_phone.text.toString()
        var birthdate = update_date.text.toString()

            // Tạo một map để lưu trữ các giá trị cần cập nhật
            val updateData = HashMap<String, Any>()
        if (username.length !=0 || phone.length != 0 || birthdate.length !=0) {
            updateData["username"] = username
            updateData["phone"] = phone
            updateData["birthDate"] = birthdate
        }
            // Thực hiện cập nhật dữ liệu
            ref.updateChildren(updateData)
                .addOnSuccessListener {
                    Log.d(TAG, "Update user information successfully.")
                    Toast.makeText(
                        this,
                        "Update user information successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                    upLoadImageToFirebaseStorage()
                    finish()
                }
                .addOnFailureListener {
                    Log.e(TAG, "Update user information failed: ${it.message}")
                    Toast.makeText(
                        this,
                        "Update user information failed: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
