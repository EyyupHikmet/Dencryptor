package com.rdiykru.dencryptor.presentation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rdiykru.dencryptor.R
import com.rdiykru.dencryptor.ui.components.FileContentDisplay
import com.rdiykru.dencryptor.ui.components.OperationSelectionBar
import com.rdiykru.dencryptor.ui.components.SelectFileInfo

@Composable
fun HomeScreen(
	openFilePicker: () -> Unit,
	homeState: HomeState,
	resetState: () -> Unit,
	onEncryptClicked: () -> Unit,
	onDecryptClicked: () -> Unit
) {
	val bottomPaddingInPixels = WindowInsets.navigationBars.getBottom(LocalDensity.current)
	val bottomPaddingInDp = with(LocalDensity.current) { bottomPaddingInPixels.toDp() }

	Scaffold(
		modifier = Modifier.fillMaxSize(),
		content = { paddingValues ->
			if (homeState.fileContent.isEmpty()) {
				Column(
					modifier = Modifier.fillMaxSize(),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.Center
				) {
					SelectFileInfo {
						openFilePicker()
					}
				}
			} else {
				Column(
					modifier = Modifier
						.fillMaxSize()
						.padding(paddingValues)
						.padding(16.dp)
						.verticalScroll(rememberScrollState())
				) {

					TopButtons(
						onFirstButtonClicked = openFilePicker,
						onSecondButtonClicked = resetState
					)
					FileContentDisplay(
						content = homeState.fileContent,
						fileSize = homeState.fileSize,
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
							enabled = homeState.encryptedContent.isNotEmpty()
						) {
							Text("Decrypt File")
						}
					}

					if (homeState.encryptedContent.isNotEmpty() && homeState.keyPairDisplay.isNotEmpty()) {
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
									text = homeState.keyPairDisplay,
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
									text = homeState.encryptedContent,
									style = MaterialTheme.typography.bodyMedium,
									overflow = TextOverflow.Ellipsis
								)
							}
						}

						if (homeState.decryptedContent.isNotEmpty()) {
							Column(
								modifier = Modifier.padding(top = 8.dp)
							) {
								Text(
									text = "Decrypted Content:",
									style = MaterialTheme.typography.bodyLarge
								)
								Text(
									text = homeState.decryptedContent,
									style = MaterialTheme.typography.bodyMedium,
									overflow = TextOverflow.Ellipsis
								)
							}
						}
					}
				}
			}
		},
		bottomBar = {
			if (homeState.fileContent.isNotEmpty()) {
				OperationSelectionBar(
					paddingValues = bottomPaddingInDp,
					onEncryptClicked = onEncryptClicked,
					onDecryptClicked = onDecryptClicked
				)
			}
		}
	)
}

@Composable
fun TopButtons(
	onFirstButtonClicked: () -> Unit,
	onSecondButtonClicked: () -> Unit,
) {
	Row(modifier = Modifier.fillMaxWidth()) {
		Button(modifier = Modifier.weight(1f),
			onClick = { onFirstButtonClicked() }) {
			Text(stringResource(R.string.select_new_text_file))
		}
		Button(modifier = Modifier.weight(1f),
			onClick = { onSecondButtonClicked() }) {
			Text(stringResource(R.string.clear_selected_content))
		}
	}
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TopButtonsPreview(
) {
	Row {

	}
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
	HomeScreen(
		openFilePicker = {},
		homeState = HomeState(),
		resetState = {},
		onEncryptClicked = {},
		onDecryptClicked = {}
	)
}