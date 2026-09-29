package com.stencilla.app.ui.stylenotes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stencilla.app.data.local.db.StyleNoteEntity
import com.stencilla.app.ui.theme.*

@Composable
fun StyleNotesScreen(
    onBack: () -> Unit,
    viewModel: StyleNotesViewModel = hiltViewModel(),
) {
    val notes by viewModel.notes.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<StyleNoteEntity?>(null) }

    Scaffold(
        containerColor = StencillaOffWhite,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = StencillaDarkGray)
                }
                Spacer(Modifier.width(8.dp))
                Text("Style Notes", style = MaterialTheme.typography.titleLarge, color = StencillaDarkGray)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showAddSheet = true }) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add note", tint = StencillaTerracotta)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = StencillaTerracotta,
                contentColor = StencillaCardWhite,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = "New Note")
            }
        }
    ) { padding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.StickyNote2, contentDescription = null,
                        tint = StencillaLightGray, modifier = Modifier.size(64.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No style notes yet", style = MaterialTheme.typography.titleMedium, color = StencillaGray)
                    Text(
                        "Tap + to jot down outfit ideas, inspiration, or style rules.",
                        style = MaterialTheme.typography.bodySmall,
                        color = StencillaGray,
                        modifier = Modifier.padding(top = 6.dp, start = 32.dp, end = 32.dp),
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(notes, key = { it.id }) { note ->
                    StyleNoteCard(
                        note = note,
                        onClick = { editingNote = note },
                        onDelete = { viewModel.delete(note) },
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        NoteEditorSheet(
            initial = null,
            onSave = { title, body, tags ->
                viewModel.create(title, body, tags)
                showAddSheet = false
            },
            onDismiss = { showAddSheet = false },
        )
    }

    editingNote?.let { note ->
        NoteEditorSheet(
            initial = note,
            onSave = { title, body, tags ->
                viewModel.update(note, title, body, tags)
                editingNote = null
            },
            onDismiss = { editingNote = null },
        )
    }
}

@Composable
private fun StyleNoteCard(
    note: StyleNoteEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StencillaCardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(note.title, style = MaterialTheme.typography.titleSmall, color = StencillaDarkGray)
                    note.body?.takeIf { it.isNotBlank() }?.let { body ->
                        Text(
                            body, style = MaterialTheme.typography.bodySmall,
                            color = StencillaGray, maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "More", tint = StencillaGray)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { showMenu = false; onClick() },
                            leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; onDelete() },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        )
                    }
                }
            }
            // Tags
            note.tags?.split(",")?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }?.let { tagList ->
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tagList.take(4).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(StencillaTerracottaLight)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(tag.trim(), style = MaterialTheme.typography.labelSmall, color = StencillaTerracotta)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteEditorSheet(
    initial: StyleNoteEntity?,
    onSave: (String, String?, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var body by remember { mutableStateOf(initial?.body ?: "") }
    var tags by remember { mutableStateOf(initial?.tags ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = StencillaCardWhite) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(
                if (initial == null) "New Style Note" else "Edit Note",
                style = MaterialTheme.typography.titleMedium,
                color = StencillaDarkGray,
            )
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Title") }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StencillaTerracotta,
                    focusedLabelColor = StencillaTerracotta,
                ),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = body, onValueChange = { body = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 6, shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StencillaTerracotta,
                    focusedLabelColor = StencillaTerracotta,
                ),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = tags, onValueChange = { tags = it },
                label = { Text("Tags (comma-separated, e.g. minimal,summer)") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StencillaTerracotta,
                    focusedLabelColor = StencillaTerracotta,
                ),
            )
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(),
                ) { Text("Cancel", color = StencillaGray) }
                Button(
                    onClick = { if (title.isNotBlank()) onSave(title, body.takeIf { it.isNotBlank() }, tags.takeIf { it.isNotBlank() }) },
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StencillaTerracotta),
                ) { Text("Save", color = StencillaCardWhite) }
            }
        }
    }
}
