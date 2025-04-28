package com.example.artistshottest.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.artistshottest.ui.domain.model.Note
import com.example.artistshottest.ui.viewmodel.NotesViewModel
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val notesState by viewModel.notesState.collectAsState()
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var showCreateNote by remember { mutableStateOf(false) }
    val filteredNotes by remember { derivedStateOf { viewModel.filteredNotes } }
    val tagQuery by viewModel.tagQuery.collectAsState()

    // Load notes when screen first appears
    LaunchedEffect(Unit) {
        viewModel.loadNotes()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .systemBarsPadding()  // Adjust for system bars (status/navigation)
    ) {
        // Create Note Button (fixed at top)
        Button(
            onClick = { showCreateNote = !showCreateNote },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (showCreateNote)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Text(if (showCreateNote) "Cancel" else "Create Note")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Create Note Card (conditionally shown)
        AnimatedVisibility(
            visible = showCreateNote,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            CreateNoteScreen(
                onBack = { showCreateNote = false }
            )
        }

        // Filter Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Date Filter
            Text("Filter by:", modifier = Modifier.padding(end = 8.dp))

            val datePickerState = rememberDatePickerState()
            var showDatePicker by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showDatePicker = true }
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = selectedDate?.let {
                        SimpleDateFormat("MMM dd, yyyy").format(Date(it))
                    } ?: "Date",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    selectedDate = it
                                    viewModel.filterByDate(it, tagQuery) // <-- LLAMA AQUÍ
                                }
                                showDatePicker = false
                            }
                        ) { Text("OK") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))

            // Tag Filter
            Text("Tags:", modifier = Modifier.padding(end = 8.dp))

            OutlinedTextField(
                value = tagQuery,
                onValueChange = { newText ->
                    viewModel.onTagQueryChange(newText)
                },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search tags") },
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notes List Content
        when (notesState) {
            is NotesViewModel.NotesState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is NotesViewModel.NotesState.Success -> {
                val notes = (notesState as NotesViewModel.NotesState.Success).notes
                if (filteredNotes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No notes found")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = filteredNotes,
                            key = { note -> note.id }
                        ) { note ->
                            NoteItem(
                                note = note,
                                onDelete = { viewModel.deleteNote(note) },
                                onEdit = { updatedNote ->
                                    viewModel.editNote(
                                        noteId = updatedNote.id,
                                        newTitle = updatedNote.title,
                                        newBody = updatedNote.body,
                                        newTags = updatedNote.tags
                                    )
                                },
                                onClick = { /* Handle note click */ }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            is NotesViewModel.NotesState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No notes available")
                }
            }
            is NotesViewModel.NotesState.Error -> {
                val error = (notesState as NotesViewModel.NotesState.Error).message
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error loading notes", color = MaterialTheme.colorScheme.error)
                        Text(error)
                        Button(onClick = { viewModel.loadNotes() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteItem(
    note: Note,
    onDelete: () -> Unit,
    onEdit: (Note) -> Unit, // 👈 Nuevo callback
    onClick: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditNoteDialog(
            note = note,
            onDismiss = { showEditDialog = false },
            onSave = { updatedNote ->
                onEdit(updatedNote)
                showEditDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title and Date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy").format(Date(note.updatedAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Delete Button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete note",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            // Edit Button
            IconButton(onClick = { showEditDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Edit, // necesitas importar este icono
                    contentDescription = "Edit note",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Body (max 2 lines)
        Text(
            text = note.body,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Tags
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            note.tags.forEach { tag ->
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}