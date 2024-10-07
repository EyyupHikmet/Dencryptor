package com.rdiykru.dencryptor.presentation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import com.rdiykru.dencryptor.ui.components.KeyPair
import com.rdiykru.dencryptor.ui.components.OperationSelectionBar
import com.rdiykru.dencryptor.ui.components.SelectFileInfo
import com.rdiykru.dencryptor.ui.theme.DencryptorTheme

@Composable
fun HomeScreen(
	openFilePicker: () -> Unit,
	homeState: HomeState,
	resetState: () -> Unit,
	createKey: () -> Unit,
	onEncryptClicked: () -> Unit,
	onDecryptClicked: () -> Unit
) {
	val bottomPaddingInPixels = WindowInsets.navigationBars.getBottom(LocalDensity.current)
	val bottomPaddingInDp = with(LocalDensity.current) { bottomPaddingInPixels.toDp() }

	Scaffold(
		modifier = Modifier.fillMaxSize(),
		content = { paddingValues ->
			Box(modifier = Modifier.fillMaxSize()) {

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
							.verticalScroll(rememberScrollState()),
						horizontalAlignment = Alignment.CenterHorizontally
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
						Button(
							onClick = { createKey() },
							enabled = homeState.rsaKeyPair == null
						) {
							Text("Create Tailored Key")
						}
						Row(
							horizontalArrangement = Arrangement.SpaceEvenly,
							modifier = Modifier
								.fillMaxWidth()
								.padding(top = 16.dp),
						) {
							Button(
								onClick = { onEncryptClicked() },
								enabled = homeState.rsaKeyPair != null
							) {
								Text("Encrypt File")
							}

							Button(
								onClick = { onDecryptClicked() },
								enabled = homeState.encryptedContent.isNotEmpty()
							) {
								Text("Decrypt File")
							}
						}

						Column {
							if (homeState.rsaKeyPair != null) {
								Column(
									modifier = Modifier
										.fillMaxWidth()
										.wrapContentHeight(),
									horizontalAlignment = Alignment.CenterHorizontally,
									verticalArrangement = Arrangement.Center
								) {
									KeyPair(homeState.rsaKeyPair)
								}
							}
						}

						if (homeState.encryptedContent.isNotEmpty()) {
							Column(
								modifier = Modifier
									.fillMaxWidth()
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
								modifier = Modifier
									.fillMaxWidth()
									.padding(top = 8.dp)
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

				if (homeState.dencrypting) {
					Box(
						modifier = Modifier
							.fillMaxSize()
							.background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
							.clickable(enabled = false, onClick = {})
							.align(Alignment.Center)
					) {
						CircularProgressIndicator(
							modifier = Modifier.align(Alignment.Center)
						)
					}
				}
			}
		},
		bottomBar = {
			if (homeState.fileContent.isNotEmpty() && !homeState.dencrypting) {
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
	DencryptorTheme {
		TopButtons({}, {})
	}
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
	HomeScreen(
		openFilePicker = {},
		homeState = HomeState(fileContent = "deneme"),
		resetState = {},
		createKey = {},
		onEncryptClicked = {},
		onDecryptClicked = {}
	)
}