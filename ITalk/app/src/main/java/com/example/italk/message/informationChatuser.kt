package com.example.italk.message

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.italk.NewMessageActivity
import com.example.italk.R
import com.example.italk.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_information_chatuser.*
import kotlinx.android.synthetic.main.activity_information_user.*

class informationChatuser : AppCompatActivity() {
    companion object{
        var infoChatUser:User?=null
        val TAG = "infoChatUser"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information_chatuser)
        var infoChatUserUID=intent.getStringExtra("toUserNameUID")
        var toUserName=intent.getStringExtra("toUserName")
        supportActionBar?.title= toUserName.toString()
        //the function show data of user
        setValueToView()

        val uid = FirebaseAuth.getInstance().uid ?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$infoChatUserUID")
        val user= User()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                infoChatUser =snapshot.getValue(User::class.java)
                infoChatUserName.text= infoChatUser?.username
                infoChatUserDate.text= infoChatUser?.birthDate
                infoChatUserPhone.text= infoChatUser?.phone
                Picasso.get().load(infoChatUser?.profileImageUri).into(infoChatUserImg)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    private fun setValueToView() {

    }
}