package com.example.italk.message

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.italk.*
import com.example.italk.model.ChatFromItem
import com.example.italk.model.ChatToItem
import com.example.italk.model.chatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {
    companion object{
        val TAG ="Chatlog"
    }
    val adapter=GroupAdapter<ViewHolder>()
    var toUser:User?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerView_chat_log.adapter=adapter

//        supportActionBar?.title="Chat Log"
//        val username =intent.getStringExtra(NewMessageActivity.USER_KEY)
        //toUser is the name of chat user
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title=toUser?.username

        listenForMessages()
        //click on send button at chat-log-activity layout
        send_button_chat_log.setOnClickListener {
            Log.d(TAG,"Attempt to send message...")
            if(edittext_chat_log.length() !=0){
                perFormSendMessage()
            }
            edittext_chat_log.setText("")
        }
    }
    //this function is listen for message and save data to user-message
    private fun listenForMessages() {
        val fromID = FirebaseAuth.getInstance().uid
        val user = toUser
        val toID = user?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-message/$fromID/$toID")
        ref.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, previousChildName: String?) {
                val chatMessage = p0.getValue(chatMessage::class.java)
                if (chatMessage != null){
                    Log.d(TAG,chatMessage.text)
                    if (chatMessage.fromID==FirebaseAuth.getInstance().uid){
                        val currentUSer=LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text,currentUSer))
                    }else{
                        adapter.add(ChatToItem(chatMessage.text,toUser!!))
                    }
                }
                recyclerView_chat_log.scrollToPosition(adapter.itemCount-1)
            }

            override fun onChildChanged(p0: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
            override fun onChildMoved(p0: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun perFormSendMessage() {
        //how to we actually send a message to FiseBase
        val text = edittext_chat_log.text.toString()
        val fromID = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toID = user?.uid
        val timestamp = System.currentTimeMillis()/1000
        if (fromID==null) return

//        val reference = FirebaseDatabase.getInstance().getReference("/message").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-message/$fromID/$toID").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-message/$toID/$fromID").push()

        val chatMessage= chatMessage(reference.key!!,text,fromID,toID!!,timestamp)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG,"saved our chat message :${reference.key}")
            }
        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromID/$toID")
        latestMessageRef.setValue(chatMessage)
        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toID/$fromID")
        latestMessageToRef.setValue(chatMessage)
    }
    //menu options
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId){
            R.id.menu_information ->{
                var intent = Intent(this,informationChatuser::class.java)
                intent.putExtra("toUserName",toUser?.username)
                intent.putExtra("toUserNameUID",toUser?.uid)
                intent.putExtra("date",toUser?.birthDate)
                intent.putExtra("phone",toUser?.phone)
                intent.putExtra("img",toUser?.profileImageUri)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_of_chat_log,menu)
        return super.onCreateOptionsMenu(menu)
    }
}


