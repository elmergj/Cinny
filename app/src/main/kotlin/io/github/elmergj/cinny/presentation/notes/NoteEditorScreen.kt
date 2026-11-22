
package io.github.elmergj.cinny.presentation.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel = hiltViewModel()
) {
    val noteId by viewModel.noteId.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val content by viewModel.content.collectAsStateWithLifecycle()
    val isCreating by viewModel.isCreating.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var noteIdInput by rememberSaveable {
        mutableStateOf("")
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Cinny - Note Editor",
            style = MaterialTheme.typography.headlineMedium
        )

        if (noteId == null) {
            Button(
                onClick = viewModel::createNote,
                enabled = !isCreating
            ) {
                Text("Create Note")
            }

            OutlinedTextField(
                value = noteIdInput,
                onValueChange = { noteIdInput = it },
                label = { Text("Note ID") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

           Button(
                onClick = {
                    noteIdInput.toLongOrNull()?.let(viewModel::openNote)
                },
                enabled = !isCreating &&
                        noteIdInput.toLongOrNull()?.let { it > 0L } == true
            ) {
                Text("Load Note")
            }

            if (isCreating) {
                CircularProgressIndicator()
            }
        } else {
            Text("Note ID: $noteId")

            OutlinedTextField(
                value = title,
                onValueChange = viewModel::onTitleChanged,
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = viewModel::onContentChanged,
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // Test: REMOVE BEFORE GOING PROD.
            Button(
                onClick = viewModel::closeNote
            ) {
                Text("Back")
            }
        }

        if (error != null) {
            Text(
                text = error.orEmpty(),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
