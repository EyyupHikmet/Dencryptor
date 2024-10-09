package com.rdiykru.dencryptor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rdiykru.dencryptor.ui.theme.DencryptorTheme

@Composable
fun InputDialog(
	openDialog: Boolean,
	onDismiss: () -> Unit,
	onSave: (String) -> Unit
) {
	if (openDialog) {
		var inputText by remember { mutableStateOf("") }
		var errorMessage by remember { mutableStateOf("") }

		Dialog(
			onDismissRequest = { onDismiss() },
			properties = DialogProperties(usePlatformDefaultWidth = false)
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
					.background(
						color = MaterialTheme.colorScheme.surface,
						shape = RoundedCornerShape(12.dp)
					),
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text(
					text = "Bu Alanın Doldurulması Zorunludur.",
					fontWeight = FontWeight.Bold,
					textAlign = TextAlign.Center,
					modifier = Modifier.padding(bottom = 16.dp)
				)

				OutlinedTextField(
					value = inputText,
					onValueChange = {
						inputText = it
						errorMessage = "" // Clear error message when user types
					},
					label = { Text("Dosya Adını Giriniz") },
					modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
				)

				if (errorMessage.isNotEmpty()) {
					Text(
						text = errorMessage,
						color = MaterialTheme.colorScheme.error,
						modifier = Modifier.padding(bottom = 8.dp)
					)
				}

				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween
				) {
					Button(
						onClick = { onDismiss() },
						colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
					) {
						Text(text = "İptal")
					}
					Button(
						onClick = {
							if (inputText.isEmpty()) {
								errorMessage = "Bu Alan Boş Bırakılamaz"
							} else {
								onSave(inputText)
								onDismiss()
							}
						}
					) {
						Text(text = "Kaydet")
					}
				}
			}
		}
	}
}

@Preview(showBackground = true)
@Composable
fun InputDialogPreview() {
	DencryptorTheme {
		InputDialog(
			openDialog = true,
			onDismiss = {},
			onSave = { input -> println("Saved input: $input") }
		)
	}
}
