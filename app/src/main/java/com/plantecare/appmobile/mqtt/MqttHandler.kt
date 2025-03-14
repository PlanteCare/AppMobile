package com.plantecare.appmobile.mqtt

import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttHandler {
    private var client: MqttClient? = null

    fun connect(brokerUrl: String, clientId: String) {
        try {
            // Configurer la couche de persistance
            val persistence = MemoryPersistence()
            // Initialiser le client MQTT
            client = MqttClient(brokerUrl, clientId, persistence)
            // Configurer les options de connexion
            val connectOptions = MqttConnectOptions()
            connectOptions.isCleanSession = true
            // Se connecter au broker
            client?.connect(connectOptions)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            client?.let {
                if (it.isConnected) {
                    it.disconnect()
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(topic: String, message: String) {
        try {
            val mqttMessage = MqttMessage(message.toByteArray())
            client?.publish(topic, mqttMessage)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribe(topic: String, messageHandler: (String) -> Unit) {
        try {
            client?.subscribe(topic)
            client?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    // Gérer la perte de connexion
                    println("Connexion perdue: ${cause?.message}")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    // Conversion du message en String et envoi au handler
                    message?.let {
                        val messageContent = String(it.payload)
                        messageHandler(messageContent)
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    // Confirmation de livraison
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}