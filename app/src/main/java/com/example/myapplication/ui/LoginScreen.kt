package com.example.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var snackbarVisible by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Contenedor principal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    val client = OkHttpClient()
                    val requestBody = """{"email": "$email", "password": "$password"}"""
                    val request = Request.Builder()
                        .url("http://10.0.2.2:3000/api/login")
                        .post(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody))
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            snackbarMessage = "Error de conexión: ${e.message}"
                            snackbarVisible = true
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (response.isSuccessful) {
                                coroutineScope.launch {
                                    navController.navigate("home")
                                }
                            } else {
                                val errorMessage = response.body?.string() ?: "Error desconocido"
                                snackbarMessage = "Error: $errorMessage"
                                snackbarVisible = true
                            }
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar Sesión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { navController.navigate("register") }) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }

    // Snackbar para mostrar mensajes
    if (snackbarVisible) {
        Snackbar(
            action = {
                Button(onClick = { snackbarVisible = false }) {
                    Text("Cerrar")
                }
            }
        ) {
            Text(snackbarMessage)
        }
    }
}
