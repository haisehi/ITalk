package com.example.italk

import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.italk.message.ChatLogActivity
import com.example.italk.message.informationUser
import com.example.italk.model.chatMessage
import com.example.italk.view.LatestMessageRows
import com.google.android.material.search.SearchView
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessagesActivity : AppCompatActivity() {
    companion object{
        var currentUser:User?=null
        val TAG ="latestMessages"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        recyclerView_lastest_messages.adapter=adapter
        recyclerView_lastest_messages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        //set item click listener on your adapter
        adapter.setOnItemClickListener { item, view ->
            Log.d(TAG,"123")
            val intent = Intent(this,ChatLogActivity::class.java)

            val row = item as LatestMessageRows
            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartnerUser)
            Log.d(TAG,"this is put extra : ${row.chatPartnerUser?.username}")
            startActivity(intent)
        }

        listenForLatestMessages()
        fetchCurrentUser()
        verifyUserIsLoggedIn()
        bottomMenu()
    }

    val adapter =GroupAdapter<ViewHolder>()
//    bottom menu
    private fun bottomMenu() {
        Log.d(TAG,"bottom menu")
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.menu_home_bottom ->{
                    var intent = Intent(this@LatestMessagesActivity,LatestMessagesActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_information_user_bottom ->{
                    var intent = Intent(this@LatestMessagesActivity,informationUser::class.java)
                    intent.putExtra("userName",currentUser?.username)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_new_message_bottom ->{
                    var intent = Intent(this@LatestMessagesActivity,NewMessageActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                else -> return@setOnNavigationItemSelectedListener false
            }
        }

    }

    val latestMessageMap = HashMap<String,chatMessage>()

    private fun listenForLatestMessages() {
        val fromID = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromID")
        ref.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, previousChildName: String?) {
                val chatMessage = p0.getValue(chatMessage::class.java) ?: return
                latestMessageMap[p0.key!!]=chatMessage
                refreshRecyclerViewMessages()
            }
            override fun onChildChanged(p0: DataSnapshot, previousChildName: String?) {
                val chatMessage = p0.getValue(chatMessage::class.java) ?: return
                latestMessageMap[p0.key!!]=chatMessage
                refreshRecyclerViewMessages()
            }
            override fun onChildRemoved(p0: DataSnapshot) {}
            override fun onChildMoved(p0: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    //This function has the function of receiving and displaying the most recently sent messages
    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessageMap.values.forEach {
            adapter.add(LatestMessageRows(it))
        }
    }


    //get information of user
    private fun fetchCurrentUser() {
        var uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                currentUser=p0.getValue(User::class.java)
                Log.d(TAG,"current user ${currentUser?.username}")
            }
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    //If the user has not logged in, the application will return to the login page to perform the login action
    private fun verifyUserIsLoggedIn() {
        var uid = FirebaseAuth.getInstance().uid
        if (uid==null){
            var intent = Intent(this,LoginActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}