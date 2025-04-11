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

    private fun loginUser(email: String, password: String) {
        progressBar.visibility = View.VISIBLE

        val loginRequest = LoginRequest(email, password)

        RetrofitClient.apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        val sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                        sharedPreferences.edit().putString("token", loginResponse.token).apply()

                        Toast.makeText(this@MainActivity, "Connexion réussie", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@MainActivity, HomeUserActivity::class.java)
                        startActivity(intent)
                        finish()
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
}