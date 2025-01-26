package com.example.myapplication.ui
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

@Composable
fun HomeScreen() {
    var products by remember { mutableStateOf(listOf<Product>()) }
   // val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        products = fetchProducts()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        if (products.isEmpty()) {
            Text("No hay productos disponibles")
        } else {
            LazyColumn {
                items(products) { product ->
                    ProductItem(product)
                }
            }
        }
    }
}

@Composable
fun ProductItem(product: Product) {
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

data class Product(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: String,
    val stock: Int,
    val imagenUrl: String
)