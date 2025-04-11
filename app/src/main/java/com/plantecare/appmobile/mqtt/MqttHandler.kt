package com.plantecare.appmobile.mqtt

import android.content.Context
import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class MqttHandler(private val context: Context? = null) {
    private var client: MqttClient? = null
    private val TAG = "MqttHandler"
    private var connectionCallback: ((Boolean, String?) -> Unit)? = null

    fun setConnectionCallback(callback: (success: Boolean, errorMessage: String?) -> Unit) {
        this.connectionCallback = callback
    }

    fun connect(brokerUrl: String, clientId: String) {
        try {
            Log.d(TAG, "Tentative de connexion à $brokerUrl avec ID $clientId")

            val persistence = MemoryPersistence()
            client = MqttClient(brokerUrl, clientId, persistence)
            val connectOptions = MqttConnectOptions()
            connectOptions.isCleanSession = true

            // Configuration SSL/TLS avec un TrustManager qui accepte tous les certificats
            // ATTENTION: Cette configuration n'est pas sécurisée et ne devrait être utilisée que pour des tests
            if (brokerUrl.startsWith("ssl://") || brokerUrl.startsWith("mqtts://")) {
                Log.d(TAG, "Configuration SSL permissive pour connexion sécurisée")
                try {
                    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    })

                    val sslContext = SSLContext.getInstance("TLSv1.2")
                    sslContext.init(null, trustAllCerts, SecureRandom())
                    connectOptions.socketFactory = sslContext.socketFactory
                    Log.d(TAG, "Configuration SSL permissive réussie")
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de la configuration SSL", e)
                    connectionCallback?.invoke(false, "Erreur SSL: ${e.message}")
                    return
                }
            }

            // Définir un timeout plus long
            connectOptions.connectionTimeout = 60 // 60 secondes
            connectOptions.keepAliveInterval = 60

            // Définir le callback avant la connexion
            client?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e(TAG, "Connexion perdue", cause)
                    connectionCallback?.invoke(false, "Connexion perdue: ${cause?.message}")
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    message?.let {
                        val messageContent = String(it.payload)
                        Log.d(TAG, "Message reçu sur $topic: $messageContent")
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    // Rien à faire ici
                }
            })

            // Connexion synchrone
            client?.connect(connectOptions)
            Log.d(TAG, "Connexion MQTT réussie!")
            connectionCallback?.invoke(true, null)

        } catch (e: MqttException) {
            Log.e(TAG, "Exception MQTT (${e.reasonCode}): ${e.message}", e)
            connectionCallback?.invoke(false, "Exception MQTT (${e.reasonCode}): ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Exception générale", e)
            connectionCallback?.invoke(false, "Exception: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            client?.let {
                if (it.isConnected) {
                    it.disconnect()
                    Log.d(TAG, "Déconnexion réussie")
                }
            }
        } catch (e: MqttException) {
            Log.e(TAG, "Erreur lors de la déconnexion", e)
        }
    }

    fun publish(topic: String, message: String) {
        try {
            if (client?.isConnected != true) {
                Log.w(TAG, "Tentative de publication sur $topic alors que client non connecté")
                return
            }

            val mqttMessage = MqttMessage(message.toByteArray())
            client?.publish(topic, mqttMessage)
            Log.d(TAG, "Message publié sur $topic: $message")
        } catch (e: MqttException) {
            Log.e(TAG, "Erreur lors de la publication sur $topic", e)
        }
    }

    fun subscribe(topic: String, messageHandler: (String) -> Unit) {
        try {
            if (client?.isConnected != true) {
                Log.w(TAG, "Tentative d'abonnement à $topic alors que client non connecté")
                return
            }

            client?.subscribe(topic, 1) // QoS 1 pour garantir la livraison
            Log.d(TAG, "Abonnement réussi à $topic")

            // Mise à jour du callback pour traiter les messages
            client?.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    Log.e(TAG, "Connexion perdue", cause)
                    // Tentative de reconnexion
                    try {
                        client?.reconnect()
                        Log.d(TAG, "Tentative de reconnexion")
                    } catch (e: MqttException) {
                        Log.e(TAG, "Erreur lors de la tentative de reconnexion", e)
                    }
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    message?.let {
                        val messageContent = String(it.payload)
                        Log.d(TAG, "Message reçu sur $topic: $messageContent")
                        messageHandler(messageContent)
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    // Rien à faire ici
                }
            })
        } catch (e: MqttException) {
            Log.e(TAG, "Erreur lors de l'abonnement à $topic", e)
        }
    }
}