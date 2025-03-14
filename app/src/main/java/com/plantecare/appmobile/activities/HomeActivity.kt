package com.plantecare.appmobile.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.plantecare.appmobile.mqtt.MqttHandler
import java.text.SimpleDateFormat
import java.util.*
import com.plantecare.appmobile.R


class HomeActivity : AppCompatActivity() {
    private var mqttHandler: MqttHandler? = null
    private lateinit var textViewMessages: TextView
    private lateinit var buttonClearMessages: Button
    private val messagesBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        textViewMessages = findViewById(R.id.textViewMessages)
        buttonClearMessages = findViewById(R.id.buttonClearMessages)

        buttonClearMessages.setOnClickListener {
            messagesBuilder.setLength(0)
            textViewMessages.text = ""
        }
        setupMqttConnection()
    }

    private fun setupMqttConnection() {
        mqttHandler = MqttHandler()
        mqttHandler?.connect("tcp://lyeshamrani.com:1883", "AndroidClient")

        mqttHandler?.subscribe("plantecare/message") { message ->
            runOnUiThread {
                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val currentTime = sdf.format(Date())

                messagesBuilder.append("[$currentTime] ")
                messagesBuilder.append(message).append("\n\n")

                textViewMessages.text = messagesBuilder.toString()

                val scrollView = textViewMessages.parent as ScrollView
                scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        }
    }

    override fun onDestroy() {
        mqttHandler?.disconnect()
        super.onDestroy()
    }
}