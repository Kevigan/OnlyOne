package com.example.onlyone

import android.app.Application
import android.content.Context

import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OnlyOneApp : Application() {
    override fun onCreate() {
        super.onCreate()
        //FirebaseApp.initializeApp(this)  // keep it here
    }
}

