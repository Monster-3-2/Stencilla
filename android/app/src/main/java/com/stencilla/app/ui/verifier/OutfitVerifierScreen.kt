package com.stencilla.app.ui.verifier

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutfitVerifierScreen(onBack: () -> Unit, viewModel: OutfitVerifierViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.selectImage(context, it, context.contentResolver.getType(it)) }
    }
    val cameraUri = androidx.compose.runtime.remember { mutableListOf<Uri>() }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        cameraUri.removeLastOrNull()?.let { uri ->
            if (success) viewModel.selectImage(context, uri, "image/jpeg") else context.contentResolver.delete(uri, null, null)
        }
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = com.stencilla.app.util.ImageFileUtil.createCaptureUri(context)
            cameraUri.add(uri)
            cameraLauncher.launch(uri)
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("AI Outfit Verifier") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Optional, one-time feedback", style = MaterialTheme.typography.headlineSmall)
            Text("Your photo is sent only after you opt in. It is used for fit, color, and proportion feedback, not identity or health judgments. The server does not store it.")
            OutlinedButton(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text("Choose a photo") }
            OutlinedButton(onClick = { cameraPermission.launch(Manifest.permission.CAMERA) }, modifier = Modifier.fillMaxWidth()) { Text("Take a photo") }
            if (state.selectedUri != null) {
                OutlinedButton(onClick = { viewModel.clearSelection(context) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Delete selected photo")
                }
            }
            RowWithCheckbox("I agree to send this photo for this analysis", state.consentGiven, viewModel::setConsent)
            RowWithCheckbox("Keep the photo on this device after analysis", state.keepOnDevice, viewModel::setKeepOnDevice)
            Text("The photo is uploaded only for this analysis and is never retained on the server. It is deleted from the app cache after analysis unless you opt to keep it on this device. Analysis text is not saved to history.", style = MaterialTheme.typography.bodySmall)
            Button(onClick = { viewModel.analyze(context) }, enabled = state.selectedUri != null && state.consentGiven && !state.isAnalyzing, modifier = Modifier.fillMaxWidth()) {
                if (state.isAnalyzing) CircularProgressIndicator(modifier = Modifier.padding(2.dp), strokeWidth = 2.dp) else Text("Analyze my outfit")
            }
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.result?.let { result ->
                Card(modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Feedback", style = MaterialTheme.typography.titleLarge)
                    Text("Fit", style = MaterialTheme.typography.titleMedium); Text(result.fitFeedback)
                    Text("Color", style = MaterialTheme.typography.titleMedium); Text(result.colorFeedback)
                    Text("Proportion", style = MaterialTheme.typography.titleMedium); Text(result.proportionFeedback)
                    Text(result.overallFeedback)
                    result.cautions.forEach { Text("Note: $it", style = MaterialTheme.typography.bodySmall) }
                    if (state.retainedFilePath != null) OutlinedButton(onClick = viewModel::deleteRetainedImage) { Text("Delete retained photo") }
                } }
            }
            OutlinedButton(onClick = { viewModel.clearSelection(context); onBack() }, modifier = Modifier.fillMaxWidth()) { Text("Done") }
        }
    }
}

@Composable
private fun RowWithCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}
