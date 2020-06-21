package com.infinty8.flashcall.sharedpref

import android.content.Context
import android.content.SharedPreferences

class SharedPrefData(context: Context) {

    var mySharedPref: SharedPreferences? = context.getSharedPreferences("filename1", Context.MODE_PRIVATE)

    fun setName(state: String?) {
        val editor = mySharedPref!!.edit()
        editor.putString("name", state)
        editor.commit()
    }

    fun getName(): String? {
        return mySharedPref!!.getString("name", "")
    }

    fun setEmail(state: String?) {
        val editor = mySharedPref!!.edit()
        editor.putString("email", state)
        editor.commit()
    }

    fun getEmail(): String? {
        return mySharedPref!!.getString("email", "")
    }

    fun setAuthId(state: String?) {
        val editor = mySharedPref!!.edit()
        editor.putString("authId", state)
        editor.commit()
    }

    fun getAuthId(): String? {
        return mySharedPref!!.getString("authId", "")
    }

    fun setImage(state: String?) {
        val editor = mySharedPref!!.edit()
        editor.putString("image", state)
        editor.commit()
    }

    fun getImage(): String? {
        return mySharedPref!!.getString("image", "")
    }

    fun setSkip(state: String?) {
        val editor = mySharedPref!!.edit()
        editor.putString("skip", state)
        editor.commit()
    }

    fun getSkip(): String? {
        return mySharedPref!!.getString("skip", "")
    }


}