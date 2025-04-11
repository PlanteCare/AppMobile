package com.plantecare.appmobile.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.plantecare.appmobile.R
import com.plantecare.appmobile.api.RetrofitClient
import com.plantecare.appmobile.models.LoginRequest
import com.plantecare.appmobile.models.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Vérifier si l'utilisateur est déjà connecté
        checkLoginStatus()

        val buttonLogin = findViewById<Button>(R.id.btnLogin)
        val editTextEmail = findViewById<EditText>(R.id.edtEmail)
        val editTextPassword = findViewById<EditText>(R.id.edtPassword)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (email.isEmpty()) {
                editTextEmail.error = "Email requis"
                editTextEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                editTextPassword.error = "Mot de passe requis"
                editTextPassword.requestFocus()
                return@setOnClickListener
            }

            loginUser(email, password)
        }
    }

    private fun checkLoginStatus() {
        val sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)
        val roleId = sharedPreferences.getInt("roleId", 0)

        if (token != null && roleId > 0) {
            navigateBasedOnRoleId(roleId)
        }
    }

    private fun loginUser(email: String, password: String) {
        progressBar.visibility = View.VISIBLE

        val loginRequest = LoginRequest(email, password)

        RetrofitClient.apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        // Sauvegarder les informations de connexion
                        saveLoginInfo(loginResponse)

                        Toast.makeText(
                            this@MainActivity,
                            "Connexion réussie",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Redirection basée sur le rôle
                        navigateBasedOnRoleId(loginResponse.role)
                    }
                } else {
                    Log.e("LoginError", "Error code: ${response.code()}")
                    Toast.makeText(
                        this@MainActivity,
                        "Échec de connexion: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                progressBar.visibility = View.GONE

                Log.e("LoginError", "Request failed: ${t.message}", t)
                Toast.makeText(
                    this@MainActivity,
                    "Erreur de connexion: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun saveLoginInfo(loginResponse: LoginResponse) {
        val sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("token", loginResponse.token)
            putString("username", loginResponse.username)
            putInt("roleId", loginResponse.role)  // Stocker le roleId en tant qu'entier
            apply()
        }
    }

    private fun navigateBasedOnRoleId(roleId: Int) {
        val intent = when (roleId) {
            2 -> Intent(this@MainActivity, HomeAdminActivity::class.java) // 2 pour admin
            1 -> Intent(this@MainActivity, HomeUserActivity::class.java)  // 1 pour user
            else -> {
                // En cas de roleId non reconnu, rediriger vers l'écran utilisateur par défaut
                Toast.makeText(this, "Rôle non reconnu, accès limité", Toast.LENGTH_SHORT).show()
                Intent(this@MainActivity, HomeUserActivity::class.java)
            }
        }
        startActivity(intent)
        finish()  // Fermer l'écran de connexion
    }
}