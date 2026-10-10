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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
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
        val watches = remember { settings.watches.toMutableStateList() }
        var train by remember { mutableStateOf("") }
        var station by remember { mutableStateOf("") }
        var poll by remember { mutableStateOf(settings.pollSeconds.toString()) }
        var monitoring by remember { mutableStateOf(settings.monitoring) }
        var loudAlert by remember { mutableStateOf(settings.loudAlert) }

        Column(
            Modifier.padding(24.dp).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Train departure alerts", style = MaterialTheme.typography.headlineSmall)

            if (watches.isEmpty()) {
                Text("No trains yet. Add a train and the station to watch it leave.")
            }
            watches.forEachIndexed { index, watch ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Train ${watch.trainNum} leaving ${watch.stationCode}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    if (!monitoring) {
                        TextButton(onClick = {
                            watches.removeAt(index)
                            settings.watches = watches.toList()
                        }) { Text("Remove") }
                    }
                }
            }

            if (!monitoring) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = train,
                        onValueChange = { train = it.trim() },
                        label = { Text("Train") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = station,
                        onValueChange = { station = it.trim().uppercase() },
                        label = { Text("Station, e.g. PHL") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedButton(
                    onClick = {
                        val watch = Watch(train, station)
                        if (watch !in watches) watches += watch
                        settings.watches = watches.toList()
                        train = ""
                        station = ""
                    },
                    enabled = train.isNotBlank() && station.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Add train") }

                OutlinedTextField(
                    value = poll,
                    onValueChange = { poll = it },
                    label = { Text("Check every (seconds, min 30)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Can be changed while monitoring: the service reads it when a train departs.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = loudAlert,
                    onCheckedChange = {
                        loudAlert = it
                        settings.loudAlert = it
                    },
                )
                Text("Play alarm sound, even on silent or vibrate")
            }

            if (monitoring) {
                OutlinedButton(
                    onClick = {
                        MonitorService.stop(this@MainActivity)
                        AlarmSound.stop()
                        monitoring = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Stop monitoring") }
            } else {
                Button(
                    onClick = {
                        settings.watches = watches.toList()
                        settings.pollSeconds = poll.toIntOrNull() ?: 60
                        MonitorService.start(this@MainActivity)
                        monitoring = true
                    },
                    enabled = watches.isNotEmpty(),
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
