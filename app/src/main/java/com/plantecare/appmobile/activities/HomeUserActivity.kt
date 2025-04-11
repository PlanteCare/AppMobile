package com.plantecare.appmobile.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.plantecare.appmobile.mqtt.MqttHandler
import java.text.SimpleDateFormat
import java.util.*
import com.plantecare.appmobile.R


class HomeUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_user)
    }
}