package com.plantecare.appmobile.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.plantecare.appmobile.R
import com.plantecare.appmobile.adapters.PotAdapter
import com.plantecare.appmobile.models.PotResponse
import com.plantecare.appmobile.api.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.plantecare.appmobile.api.RetrofitClient
import com.plantecare.appmobile.mqtt.MqttHandler

class HomeUserActivity : AppCompatActivity() {

    private lateinit var listViewPots: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonAddPot: Button
    private lateinit var spinnerFilter: Spinner
    private lateinit var potAdapter: PotAdapter
    private var potsList: MutableList<PotResponse> = mutableListOf()
    private var allPotsList: MutableList<PotResponse> = mutableListOf() // Liste complète pour le filtrage
    private lateinit var mqttHandler: MqttHandler

    private val apiService: ApiService by lazy {
        RetrofitClient.apiService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_user)
        initViews()
        setupUserInfo()
        setupFilter()
        initMqtt()
        loadUserPots()
        setupEvents()
    }

    private fun initMqtt() {
        mqttHandler = MqttHandler(this)

        // Configurer le callback qui sera appelé quand un message de statut sera reçu
        mqttHandler.setConnectionCallback { success, errorMessage ->
            if (success) {
                // S'abonner au topic de statut une fois connecté
                mqttHandler.subscribe("plantecare/status") { messageContent ->
                    // Ce callback sera appelé quand un message arrive sur le topic
                    Log.d("HomeUserActivity", "Message MQTT reçu: $messageContent")
                }
            } else {
                Log.e("HomeUserActivity", "Échec de connexion MQTT: $errorMessage")
                runOnUiThread {
                    Toast.makeText(this, "Erreur de connexion au serveur MQTT", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Configurer le callback pour mettre à jour le statut des pots
        mqttHandler.onStatusUpdateCallback = { macAddress, isOk ->
            runOnUiThread {
                // Trouver le pot dans la liste
                val index = potsList.indexOfFirst { it.macAddress == macAddress }
                if (index != -1) {
                    // Mettre à jour le statut du pot
                    potsList[index].status = if (isOk) "ok" else "error"

                    // Notifier l'adaptateur du changement
                    potAdapter.notifyDataSetChanged()

                    Log.d("HomeUserActivity", "Statut du pot $macAddress mis à jour: ${potsList[index].status}")
                }
            }
        }

        // Se connecter au broker MQTT
        // Remplacer ces valeurs par vos paramètres réels
        val brokerUrl = "ssl://lyeshamrani.com:8883" // À remplacer par votre URL de broker
        val clientId = "PlanteApp_${System.currentTimeMillis()}"
        mqttHandler.connect(brokerUrl, clientId)
    }

    private fun initViews() {
        listViewPots = findViewById(R.id.listViewPlantPots)
        progressBar = findViewById(R.id.progressBarPots)
        buttonAddPot = findViewById(R.id.buttonAddPlantPot)
        spinnerFilter = findViewById(R.id.spinnerFilter)

        // Initialisation de l'adaptateur avec une liste vide
        potAdapter = PotAdapter(this, potsList) { selectedPot ->
            onPotSelected(selectedPot)
        }
        listViewPots.adapter = potAdapter
    }

    private fun setupUserInfo() {
        val sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "Utilisateur")

        val welcomeTextView = findViewById<TextView>(R.id.textViewUsername)
        welcomeTextView.text = "Bienvenue, $username!"
    }

    private fun setupFilter() {
        // Options de filtrage
        val filterOptions = arrayOf("Tous les pots", "Filtrer par nom", "Filtrer par adresse MAC")

        // Créer l'adaptateur pour le spinner
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = filterAdapter

        // Écouter les changements de sélection
        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> showAllPots() // Tous les pots
                    1 -> sortByName() // Tri par nom
                    2 -> sortByMacAddress() // Tri par adresse MAC
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                showAllPots() // Montrer tous les pots par défaut
            }
        }
    }

    private fun showAllPots() {
        potsList.clear()
        potsList.addAll(allPotsList)
        potAdapter.notifyDataSetChanged()
    }

    private fun sortByName() {
        potsList.clear()
        potsList.addAll(allPotsList.sortedBy { it.name })
        potAdapter.notifyDataSetChanged()
    }

    private fun sortByMacAddress() {
        potsList.clear()
        potsList.addAll(allPotsList.sortedBy { it.macAddress })
        potAdapter.notifyDataSetChanged()
    }

    private fun loadUserPots() {
        // Afficher le loader
        progressBar.visibility = View.VISIBLE

        // Récupérer le token d'authentification depuis les préférences
        val sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("token", "")

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Erreur d'authentification", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            return
        }

        // Appel à l'API pour récupérer les pots de l'utilisateur
        apiService.getPotsByUser("Bearer $token").enqueue(object : Callback<List<PotResponse>> {
            override fun onResponse(call: Call<List<PotResponse>>, response: Response<List<PotResponse>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val pots = response.body()
                    if (pots != null) {
                        // Stocker dans les deux listes
                        allPotsList.clear()
                        allPotsList.addAll(pots)

                        // Appliquer le filtre actuel
                        val currentPosition = spinnerFilter.selectedItemPosition
                        when (currentPosition) {
                            0 -> showAllPots()
                            1 -> sortByName()
                            2 -> sortByMacAddress()
                            else -> showAllPots()
                        }

                        // Si aucun pot n'est trouvé
                        if (pots.isEmpty()) {
                            Toast.makeText(this@HomeUserActivity, "Vous n'avez pas encore de pots", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Gérer les erreurs de réponse
                    Log.e("HomeUserActivity", "Error code: ${response.code()}")
                    Toast.makeText(
                        this@HomeUserActivity,
                        "Échec de récupération des pots: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<PotResponse>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e("HomeUserActivity", "Request failed: ${t.message}", t)
                Toast.makeText(
                    this@HomeUserActivity,
                    "Erreur de connexion: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun logout() {
        // Déconnexion MQTT
        mqttHandler.disconnect()

        // Suppression des infos stockées
        val sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Redirection vers la page de login
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // empêche de revenir en arrière
        startActivity(intent)
        finish()
    }

    private fun setupEvents() {
        val logoutButton = findViewById<View>(R.id.buttonLogout)
        logoutButton.setOnClickListener {
            logout()
        }

        buttonAddPot.setOnClickListener {
            // Ajouter votre code pour l'ajout d'un pot ici
            Toast.makeText(this, "Fonctionnalité d'ajout de pot à implémenter", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onPotSelected(pot: PotResponse) {
        // Affichage simple toast
        Toast.makeText(
            this,
            "Pot sélectionné: ${pot.name}",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onResume() {
        super.onResume()
        loadUserPots()
    }

    override fun onDestroy() {
        super.onDestroy()
        // S'assurer de déconnecter le client MQTT
        mqttHandler.disconnect()
    }
}