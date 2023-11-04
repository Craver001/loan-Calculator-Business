package dev.gameplay.loancal

import android.app.Application
import com.google.firebase.FirebaseApp

class InitializedApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}
