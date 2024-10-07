package com.rdiykru.dencryptor.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.rdiykru.dencryptor.ui.components.FileContentDisplay
import com.rdiykru.dencryptor.ui.components.OperationSelectionBar

@Composable
fun HomeScreen(
	openFilePicker: () -> Unit,
	fileContent: String,
	fileSize: Long,
	encryptedContent: String,
	decryptedContent: String,
	keyPairDisplay: String,
	onEncryptClicked: () -> Unit,
	onDecryptClicked: () -> Unit
) {
	val bottomPaddingInPixels = WindowInsets.navigationBars.getBottom(LocalDensity.current)
	val bottomPaddingInDp = with(LocalDensity.current) { bottomPaddingInPixels.toDp() }

	Scaffold(
		modifier = Modifier.fillMaxSize(),
		content = { paddingValues ->
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(paddingValues)
					.padding(16.dp)
					.verticalScroll(rememberScrollState())
			) {
				Button(onClick = { openFilePicker() }) {
					Text("Select Text File")
				}

				FileContentDisplay(
					content = fileContent,
					fileSize = fileSize,
					fileType = "txt"
				)

				Row(
					horizontalArrangement = Arrangement.spacedBy(16.dp),
					modifier = Modifier.padding(top = 16.dp)
				) {
					Button(onClick = { onEncryptClicked() }) {
						Text("Encrypt File")
					}

					Button(
						onClick = { onDecryptClicked() },
						enabled = encryptedContent.isNotEmpty()
					) {
						Text("Decrypt File")
					}
				}

				if (encryptedContent.isNotEmpty() && keyPairDisplay.isNotEmpty()) {
					Row(
						modifier = Modifier
							.fillMaxSize()
							.padding(top = 16.dp),
						horizontalArrangement = Arrangement.spacedBy(16.dp)
					) {
						Column(
							modifier = Modifier
								.weight(1f)
								.padding(8.dp)
						) {
							Text("Key Pair:", style = MaterialTheme.typography.bodyLarge)
							Text(
								text = keyPairDisplay,
								style = MaterialTheme.typography.bodyMedium,
								overflow = TextOverflow.Ellipsis
							)
						}

						Column(
							modifier = Modifier
								.weight(1f)
								.padding(8.dp)
						) {
							Text(
								"Encrypted Content:",
								style = MaterialTheme.typography.bodyLarge
							)
							Text(
								text = encryptedContent,
								style = MaterialTheme.typography.bodyMedium,
								overflow = TextOverflow.Ellipsis
							)
						}
					}

					if (decryptedContent.isNotEmpty()) {
						Column(
							modifier = Modifier.padding(top = 8.dp)
						) {
							Text(
								text = "Decrypted Content:",
								style = MaterialTheme.typography.bodyLarge
							)
							Text(
								text = decryptedContent,
								style = MaterialTheme.typography.bodyMedium,
								overflow = TextOverflow.Ellipsis
							)
						}
					}
				}
			}
		},
		bottomBar = {
			OperationSelectionBar(
				paddingValues =bottomPaddingInDp,
				onEncryptClicked = onEncryptClicked,
				onDecryptClicked = onDecryptClicked
			)
		}
	)
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
	HomeScreen(
		openFilePicker = {},
		fileContent = "This is a sample text file content for preview.",
		fileSize = 1024L,
		encryptedContent = "SampleEncryptedContent123",
		decryptedContent = "SampleDecryptedContent123",
		keyPairDisplay = "SampleKeyPair123",
		onEncryptClicked = {},
		onDecryptClicked = {}
	)
}
