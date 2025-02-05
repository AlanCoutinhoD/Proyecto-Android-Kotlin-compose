package com.example.myapplication.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.platform.LocalContext

@Composable
fun HomeScreen(navController: NavController) {
    var products by remember { mutableStateOf(listOf<Product>()) }

    LaunchedEffect(Unit) {
        products = fetchProducts()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            if (products.isEmpty()) {
                Text("No hay productos disponibles")
            } else {
                LazyColumn {
                    items(products) { product ->
                        ProductItem(
                            product = product,
                            navController = navController,
                            onProductDeleted = { deletedProductId ->
                                products = products.filter { it.id != deletedProductId }
                            }
                        )
                    }
                }
            }
        }
        
        // Botón flotante para añadir producto
        FloatingActionButton(
            onClick = { navController.navigate("add_product") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Añadir Producto"
            )
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    navController: NavController,
    onProductDeleted: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar este producto?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = deleteProduct(product.id)
                            if (success) {
                                onProductDeleted(product.id)
                            }
                        }
                        showDialog = false
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = product.imagenUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(bottom = 8.dp)
            )
            Text(text = product.nombre, style = MaterialTheme.typography.titleMedium)
            Text(text = product.descripcion, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Precio: ${product.precio}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Stock: ${product.stock}", style = MaterialTheme.typography.bodyMedium)
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
                
                Button(
                    onClick = { 
                        navController.navigate("edit_product/${product.id}")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text("Editar")
                }
            }
        }
    }
}

suspend fun fetchProducts(): List<Product> {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://10.0.2.2:3000/api/products")
        .build()

    return withContext(Dispatchers.IO) {
        val response = client.newCall(request).execute()
        val jsonData = response.body?.string()
        Log.d("HomeScreen", "Response: $jsonData")
        parseProducts(jsonData)
    }
}

fun parseProducts(jsonData: String?): List<Product> {
    val products = mutableListOf<Product>()
    jsonData?.let {
        val jsonArray = JSONArray(it)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val product = Product(
                id = jsonObject.getInt("id"),
                nombre = jsonObject.getString("nombre"),
                descripcion = jsonObject.getString("descripcion"),
                precio = jsonObject.getString("precio"),
                stock = jsonObject.getInt("stock"),
                imagenUrl = jsonObject.getString("imagenUrl")
            )
            products.add(product)
        }
    }
    return products
}

suspend fun deleteProduct(productId: Int): Boolean {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://10.0.2.2:3000/api/products/$productId")
        .delete()
        .build()

    return withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Log.d("HomeScreen", "Producto eliminado con éxito")
                true
            } else {
                Log.e("HomeScreen", "Error al eliminar el producto: ${response.code}")
                false
            }
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error al eliminar el producto", e)
            false
        }
    }
}

data class Product(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: String,
    val stock: Int,
    val imagenUrl: String
)