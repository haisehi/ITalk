package com.example.italk.view

import android.util.Log
import com.example.italk.R
import com.example.italk.User
import com.example.italk.model.chatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageRows(val chatMessage: chatMessage): Item<ViewHolder>(){
    var chatPartnerUser:User?=null

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_message_latest_row.text=chatMessage.text
        val chatPartnerId:String
        if (chatMessage.fromID== FirebaseAuth.getInstance().uid){
            chatPartnerId=chatMessage.toID
        } else {
            chatPartnerId=chatMessage.fromID
        }
        val ref= FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser =p0.getValue(User::class.java)
                viewHolder.itemView.user_textView_latest_message_row.text=chatPartnerUser?.username

                val targetImageView = viewHolder.itemView.imageView_latest_message_row
                Picasso.get().load(chatPartnerUser?.profileImageUri).into(targetImageView)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }
}