package com.example.myapplication.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(navController: NavController, productId: Int) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Cargar datos del producto
    LaunchedEffect(productId) {
        try {
            Log.d("EditProductScreen", "Iniciando carga del producto ID: $productId")
            val product = fetchProductById(productId)
            if (product != null) {
                Log.d("EditProductScreen", "Producto cargado exitosamente: ${product.nombre}")
                nombre = product.nombre
                descripcion = product.descripcion
                precio = product.precio
            } else {
                Log.e("EditProductScreen", "No se pudo cargar el producto")
                showErrorDialog = true
            }
        } catch (e: Exception) {
            Log.e("EditProductScreen", "Error al cargar el producto", e)
            showErrorDialog = true
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Editar Producto") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del producto") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    isError = nombre.isEmpty()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    isError = descripcion.isEmpty()
                )

                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    isError = precio.isEmpty()
                )

                Button(
                    onClick = {
                        if (validateFields(nombre, descripcion, precio)) {
                            coroutineScope.launch {
                                val success = updateProduct(
                                    id = productId,
                                    nombre = nombre,
                                    descripcion = descripcion,
                                    precio = precio
                                )
                                if (success) {
                                    showSuccessDialog = true
                                } else {
                                    showErrorDialog = true
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text("Guardar Cambios")
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Éxito") },
            text = { Text("Producto actualizado correctamente") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text("No se pudo actualizar el producto") },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}

private fun validateFields(nombre: String, descripcion: String, precio: String): Boolean {
    return nombre.isNotEmpty() && descripcion.isNotEmpty() && precio.isNotEmpty()
}

suspend fun updateProduct(
    id: Int,
    nombre: String,
    descripcion: String,
    precio: String
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            Log.d("EditProductScreen", "Iniciando actualización del producto ID: $id")
            val client = OkHttpClient()
            
            val json = JSONObject().apply {
                put("id", id)
                put("nombre", nombre)
                put("descripcion", descripcion)
                put("precio", precio)
            }

            val requestBody = json.toString()
                .toRequestBody("application/json".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("http://10.0.2.2:3000/api/products")
                .put(requestBody)
                .build()

            val response = client.newCall(request).execute()
            Log.d("EditProductScreen", "Respuesta de actualización: ${response.code}")
            
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("EditProductScreen", "Error al actualizar el producto", e)
            false
        }
    }
}

suspend fun fetchProductById(id: Int): Product? {
    return withContext(Dispatchers.IO) {
        try {
            Log.d("EditProductScreen", "Iniciando petición HTTP para producto ID: $id")
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("http://10.0.2.2:3000/api/products/$id")
                .build()

            val response = client.newCall(request).execute()
            Log.d("EditProductScreen", "Respuesta recibida: ${response.code}")
            
            if (response.isSuccessful) {
                val jsonData = response.body?.string()
                Log.d("EditProductScreen", "Datos recibidos: $jsonData")
                
                if (jsonData != null) {
                    val jsonObject = JSONObject(jsonData)
                    Product(
                        id = jsonObject.getInt("id"),
                        nombre = jsonObject.getString("nombre"),
                        descripcion = jsonObject.getString("descripcion"),
                        precio = jsonObject.getString("precio"),
                        stock = jsonObject.getInt("stock"),
                        imagenUrl = jsonObject.getString("imagenUrl")
                    )
                } else {
                    Log.e("EditProductScreen", "El cuerpo de la respuesta está vacío")
                    null
                }
            } else {
                Log.e("EditProductScreen", "Error en la respuesta: ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.e("EditProductScreen", "Error al obtener el producto", e)
            null
        }
    }
} 