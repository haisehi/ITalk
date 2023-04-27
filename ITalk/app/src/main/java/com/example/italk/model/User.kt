package com.example.italk

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val uid:String,
    val username:String,
    val profileImageUri:String,
    val birthDate:String,
    val phone:String):Parcelable {
    constructor():this("","","","","")
}