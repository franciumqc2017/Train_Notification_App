package com.trainnotify.app

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        val settings = Settings(this)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    SetupScreen(settings)
                }
            }
        }
    }

    @Composable
    private fun SetupScreen(settings: Settings) {
        var trains by remember { mutableStateOf(settings.trainNumbers.joinToString(", ")) }
        var station by remember { mutableStateOf(settings.stationCode) }
        var poll by remember { mutableStateOf(settings.pollSeconds.toString()) }
        var monitoring by remember { mutableStateOf(settings.monitoring) }

        Column(
            Modifier.padding(24.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Train departure alerts", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = trains,
                onValueChange = { trains = it },
                label = { Text("Train numbers (comma separated)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                enabled = !monitoring,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = station,
                onValueChange = { station = it },
                label = { Text("Station code, e.g. PHL") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                enabled = !monitoring,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = poll,
                onValueChange = { poll = it },
                label = { Text("Check every (seconds, min 30)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !monitoring,
                modifier = Modifier.fillMaxWidth(),
            )

            if (monitoring) {
                OutlinedButton(
                    onClick = {
                        MonitorService.stop(this@MainActivity)
                        monitoring = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Stop monitoring") }
            } else {
                Button(
                    onClick = {
                        settings.trainNumbers = trains.split(",")
                        settings.stationCode = station
                        settings.pollSeconds = poll.toIntOrNull() ?: 60
                        MonitorService.start(this@MainActivity)
                        monitoring = true
                    },
                    enabled = trains.isNotBlank() && station.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Start monitoring") }
            }

            // Amtraker's terms require visible attribution.
            TextButton(onClick = {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://amtraker.com")))
            }) { Text("Train data from Amtraker") }
        }
    }
}
