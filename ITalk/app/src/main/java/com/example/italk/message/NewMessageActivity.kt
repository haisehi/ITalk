package com.example.italk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.italk.message.ChatLogActivity
import com.example.italk.message.informationUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.activity_new_message.bottomNavigationView
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title= "Friends List"

        recyclerview_newmessage.layoutManager = LinearLayoutManager(this)
        bottomMenu()
        fetchUsers()
    }
    companion object{
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<com.xwray.groupie.ViewHolder>()
                p0.children.forEach{
                    Log.d("NewMessage",it.toString())
                    val user=it.getValue(User::class.java)
                    if (user!=null){
                        adapter.add(UserItem(user))
                    }
                }
                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem
                    val intent = Intent(view.context,ChatLogActivity::class.java)
//                    intent.putExtra(USER_KEY,userItem.user.username)
                    intent.putExtra(USER_KEY,userItem.user)
                    startActivity(intent)
                    finish()
                }
                recyclerview_newmessage.adapter=adapter

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
                    var intent = Intent(this@NewMessageActivity,LatestMessagesActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_information_user_bottom ->{
                    var intent = Intent(this@NewMessageActivity, informationUser::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_new_message_bottom ->{
                    var intent = Intent(this@NewMessageActivity,NewMessageActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }
    }
}

class UserItem(val user: User):Item<com.xwray.groupie.ViewHolder>(){
    override fun bind(viewHolder: com.xwray.groupie.ViewHolder, position: Int) {
        //will be called in our list for each user object later on...
        viewHolder.itemView.username_textview_message_row.text = user.username
        Picasso.get().load(user.profileImageUri).into(viewHolder.itemView.imageView_new_message_row)

    }
    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }
}