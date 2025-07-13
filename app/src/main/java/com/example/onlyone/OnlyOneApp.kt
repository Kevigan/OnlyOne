package com.example.onlyone

import android.app.Application

import com.google.firebase.FirebaseApp

class OnlyOneApp: Application() {
    override fun onCreate(){
        super.onCreate()
        //FirebaseApp.initializeApp(this)
    }
}