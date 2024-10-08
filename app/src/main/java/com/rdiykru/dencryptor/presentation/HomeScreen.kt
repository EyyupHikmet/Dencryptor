@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rdiykru.dencryptor.R
import com.rdiykru.dencryptor.core.extensions.Formatters.size
import com.rdiykru.dencryptor.ui.components.DencryptedContent
import com.rdiykru.dencryptor.ui.components.FileContentDisplay
import com.rdiykru.dencryptor.ui.components.KeyCreationBottomSheet
import com.rdiykru.dencryptor.ui.components.OperationSelectionBar
import com.rdiykru.dencryptor.ui.components.SelectFileInfo
import com.rdiykru.dencryptor.ui.components.SelectedKeyPair
import com.rdiykru.dencryptor.ui.theme.DencryptorTheme

@ExperimentalMaterial3Api
@Composable
fun HomeScreen(
	openFilePicker: () -> Unit,
	homeState: HomeState,
	resetState: () -> Unit,
	createKey: (Int, String) -> Unit,
	onEncryptClicked: () -> Unit,
	onDecryptClicked: () -> Unit
) {
	var openBottomSheet by rememberSaveable { mutableStateOf(false) }
	var selectedTab by remember { mutableIntStateOf(0) }

	val bottomSheetState =
		rememberStandardBottomSheetState(initialValue = SheetValue.Expanded)

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
							onClick = { openBottomSheet = !openBottomSheet },
							enabled = homeState.rsaKeyPair == null
						) {
							Text(stringResource(R.string.create_keypair))
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
									SelectedKeyPair(
										keyPairName = homeState.keyPairName,
										keySize = homeState.rsaKeyPair.publicKey.size()
									)
								}
							}
						}

						Row(
							horizontalArrangement = Arrangement.SpaceEvenly,
							modifier = Modifier
								.fillMaxWidth()
								.padding(top = 16.dp),
						) {
							if (selectedTab == 0) {
								Button(
									onClick = { onEncryptClicked() },
									enabled = homeState.rsaKeyPair != null
								) {
									Text("Encrypt File")
								}
							} else {
								Button(
									onClick = { onDecryptClicked() },
									enabled = homeState.rsaKeyPair != null
								) {
									Text("Decrypt File")
								}
							}
						}

						if (homeState.encryptedContent.isNotEmpty() && selectedTab == 0) {
							DencryptedContent(
								modifier = Modifier
									.fillMaxWidth()
									.padding(top = 8.dp),
								title = "Encrypted Content:",
								content = homeState.encryptedContent
							)
						}

						if (homeState.decryptedContent.isNotEmpty() && selectedTab == 1) {
							DencryptedContent(
								modifier = Modifier
									.fillMaxWidth()
									.padding(top = 8.dp),
								title = "Decrypted Content:",
								content = homeState.decryptedContent
							)
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
					selectedTab = selectedTab,
					onTabSelected = { selectedTab = it }
				)
			}
		}
	)

	KeyCreationBottomSheet(
		openBottomSheet = openBottomSheet,
		bottomSheetState = bottomSheetState,
		onCreateClicked = createKey,
		homeState = homeState,
		onDismiss = { openBottomSheet = false }
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
		createKey = { keySize: Int, keyPairName: String ->

		},
		onEncryptClicked = {},
		onDecryptClicked = {}
	)
}