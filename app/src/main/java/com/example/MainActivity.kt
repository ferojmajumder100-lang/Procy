package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val vpnPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.resultCode == RESULT_OK) {
      startService(Intent(this, ProxyVpnService::class.java))
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          ProxyConfigScreen(
            onConnect = {
              val intent = android.net.VpnService.prepare(this@MainActivity)
              if (intent != null) {
                vpnPermissionLauncher.launch(intent)
              } else {
                startService(Intent(this@MainActivity, ProxyVpnService::class.java))
              }
            },
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}

@Composable
fun ProxyConfigScreen(onConnect: () -> Unit, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val prefs = remember { context.getSharedPreferences("proxy_prefs", Context.MODE_PRIVATE) }

  var host by remember { mutableStateOf(prefs.getString("host", "") ?: "") }
  var port by remember { mutableStateOf(prefs.getString("port", "") ?: "") }
  var username by remember { mutableStateOf(prefs.getString("username", "") ?: "") }
  var password by remember { mutableStateOf(prefs.getString("password", "") ?: "") }

  Column(
    modifier = modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(text = "Proxy Configuration", style = MaterialTheme.typography.headlineMedium)
    OutlinedTextField(value = host, onValueChange = { host = it }, label = { Text("Server Host") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = port, onValueChange = { port = it }, label = { Text("Port") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
    
    Button(
      onClick = {
        prefs.edit().putString("host", host).putString("port", port).putString("username", username).putString("password", password).apply()
        onConnect()
      },
      modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
    ) {
      Text("Connect")
    }
  }
}
