package com.it.mytest.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.it.mytest.MainActivity
import com.it.mytest.R


class SplashActivity : AppCompatActivity() {


    /** Duration of wait  */
    private val SPLASH_DISPLAY_LENGTH = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        Handler(Looper.myLooper()!!).postDelayed(Runnable { /* Create an Intent that will start the Menu-Activity. */
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
        }, SPLASH_DISPLAY_LENGTH)

    }
}