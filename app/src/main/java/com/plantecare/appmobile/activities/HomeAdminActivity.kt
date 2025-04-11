package com.plantecare.appmobile.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.plantecare.appmobile.R
import com.plantecare.appmobile.adapters.PotAdapter
import com.plantecare.appmobile.api.RetrofitClient
import com.plantecare.appmobile.models.PotResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeAdminActivity : AppCompatActivity() {

    private lateinit var listViewPots: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var potAdapter: PotAdapter
    private var potsList: MutableList<PotResponse> = mutableListOf()

    // Pour la gestion des API
    private val apiService by lazy {
        RetrofitClient.apiService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_admin)

        // Initialisation des vues
        initViews()

        // Initialisation des données administrateur
        setupAdminInfo()

        // Chargement de tous les pots de la base de données
        loadAllPots()

        // Configuration des événements
        setupEvents()
    }

    private fun initViews() {
        listViewPots = findViewById(R.id.listViewPlantPots)
        progressBar = findViewById(R.id.progressBarPots)

        // Initialisation de l'adaptateur avec une liste vide
        potAdapter = PotAdapter(this, potsList) { selectedPot ->
            onPotSelected(selectedPot)
        }
        listViewPots.adapter = potAdapter
    }

    private fun setupAdminInfo() {
        val sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "Administrateur")

        val welcomeTextView = findViewById<TextView>(R.id.textViewUsername)
        welcomeTextView.text = "Bonjour, $username"
    }

    private fun loadAllPots() {
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

        // Appel à l'API pour récupérer tous les pots
        apiService.getAllPots("Bearer $token").enqueue(object : Callback<List<PotResponse>> {
            override fun onResponse(call: Call<List<PotResponse>>, response: Response<List<PotResponse>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val pots = response.body()
                    if (pots != null) {
                        potsList.clear()
                        potsList.addAll(pots)
                        potAdapter.notifyDataSetChanged()

                        // Si aucun pot n'est trouvé
                        if (pots.isEmpty()) {
                            Toast.makeText(this@HomeAdminActivity, "Aucun pot enregistré dans le système", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Gérer les erreurs de réponse
                    Log.e("HomeAdminActivity", "Error code: ${response.code()}")
                    Toast.makeText(
                        this@HomeAdminActivity,
                        "Échec de récupération des pots: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<PotResponse>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e("HomeAdminActivity", "Request failed: ${t.message}", t)
                Toast.makeText(
                    this@HomeAdminActivity,
                    "Erreur de connexion: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun logout() {
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
    }


    private fun onPotSelected(pot: PotResponse) {
        // Affichage simple d'un toast avec les informations du pot
        Toast.makeText(
            this,
            "Pot: ${pot.name} - MAC: ${pot.macAddress}",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Rafraîchir la liste à la reprise de l'activité
    override fun onResume() {
        super.onResume()
        loadAllPots()
    }
}