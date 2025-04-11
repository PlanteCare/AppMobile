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

class HomeAdminActivity : AppCompatActivity() {

    private lateinit var listViewPots: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonAddPot: Button
    private lateinit var spinnerFilter: Spinner
    private lateinit var potAdapter: PotAdapter
    private var potsList: MutableList<PotResponse> = mutableListOf()
    private var allPotsList: MutableList<PotResponse> = mutableListOf() // Liste complète pour le filtrage

    // Pour la gestion des API
    private val apiService: ApiService by lazy {
        RetrofitClient.apiService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_admin)

        // Initialisation des vues
        initViews()

        // Initialisation des données utilisateur
        setupUserInfo()

        // Configuration du filtre
        setupFilter()

        // Chargement des pots de l'utilisateur
        loadAllPots()

        // Configuration des événements
        setupEvents()
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
        val username = sharedPreferences.getString("username", "Administrateur")

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

        // Appel à l'API pour récupérer tous les pots (admin a accès à tous)
        apiService.getAllPots("Bearer $token").enqueue(object : Callback<List<PotResponse>> {
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
                            Toast.makeText(this@HomeAdminActivity, "Aucun pot n'est enregistré", Toast.LENGTH_SHORT).show()
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

        // Pour l'admin, vous pourriez vouloir ajouter des options supplémentaires ici
        // comme modifier ou supprimer le pot
    }

    override fun onResume() {
        super.onResume()
        loadAllPots()
    }
}