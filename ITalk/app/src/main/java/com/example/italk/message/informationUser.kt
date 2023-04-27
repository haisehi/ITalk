package com.example.italk.message

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.italk.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_information_user.*
import kotlinx.android.synthetic.main.activity_information_user.bottomNavigationView
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*
import java.util.zip.Inflater

class informationUser : AppCompatActivity() {
    companion object{
        var infoUser:User?=null
        val TAG = "infoUser"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information_user)

        var userNameBar = intent.getStringExtra("userName")
        supportActionBar?.title=userNameBar.toString()
        //the function show data of user
        setValueToView()
        //sign out user
        button_signout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            var intent = Intent(this, LoginActivity::class.java)
            intent.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        //button update data
        button_update_data_info_user.setOnClickListener {
            var intent = Intent(this@informationUser,UpdateActivity::class.java)
            intent.putExtra("userNameUD",userNameBar)
            startActivity(intent)
        }
        bottomMenu()
    }


    private fun setValueToView() {
        val uid = FirebaseAuth.getInstance().uid ?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                infoUser=snapshot.getValue(User::class.java)
                infoUserName.text= infoUser?.username
                infoUserDate.text= infoUser?.birthDate
                infoUserPhone.text= infoUser?.phone
                Picasso.get().load(infoUser?.profileImageUri).into(infoUserImg)
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun bottomMenu() {
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.menu_home_bottom ->{
                    var intent = Intent(this@informationUser, LatestMessagesActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_information_user_bottom ->{
                    var intent = Intent(this@informationUser,informationUser::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_new_message_bottom ->{
                    var intent = Intent(this@informationUser,NewMessageActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }
    }
}