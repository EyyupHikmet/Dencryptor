@file:OptIn(ExperimentalMaterial3Api::class)

package com.rdiykru.dencryptor.presentation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.rdiykru.dencryptor.ui.components.FileListComponent
import com.rdiykru.dencryptor.ui.components.FullScreenKeyCreationDialog
import com.rdiykru.dencryptor.ui.components.OperationSelectionBar
import com.rdiykru.dencryptor.ui.components.SelectFileInfo
import com.rdiykru.dencryptor.ui.components.SelectedKeyPair
import com.rdiykru.dencryptor.ui.theme.DencryptorTheme

@ExperimentalMaterial3Api
@Composable
fun HomeScreen(
	openFilePicker: () -> Unit,
	getKeyPairList: () -> Unit,
	selectKeyPairFile: (String) -> Unit,
	homeState: HomeState,
	resetState: () -> Unit,
	createKey: (Int, String) -> Unit,
	onEncryptClicked: () -> Unit,
	onDecryptClicked: () -> Unit
) {
	var openKeyCreatorDialog by rememberSaveable { mutableStateOf(false) }
	var selectedTab by remember { mutableIntStateOf(0) }

	var openBottomSheet by rememberSaveable { mutableStateOf(false) }
	val skipPartiallyExpanded by rememberSaveable { mutableStateOf(false) }
	val bottomSheetState =
		rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

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

						DoubleButtons(
							firstText = stringResource(R.string.select_new_text_file),
							onFirstButtonClicked = openFilePicker,
							secondText = stringResource(R.string.clear_selected_content),
							onSecondButtonClicked = resetState
						)
						FileContentDisplay(
							content = homeState.fileContent,
							fileSize = homeState.fileSize,
							fileType = "txt"
						)
						Row(modifier = Modifier.fillMaxWidth()) {

							Button(
								modifier = Modifier.weight(1f),
								onClick = { openKeyCreatorDialog = !openKeyCreatorDialog },
							) {
								Text(stringResource(R.string.create_keypair))
							}
							Spacer(modifier = Modifier.size(4.dp))
							Button(
								modifier = Modifier.weight(1f),
								onClick = {
									getKeyPairList()
									openBottomSheet = true
								},
								enabled = homeState.publicKey == null || homeState.privateKey == null
							) {
								Text("Anahtar Seç")
							}
						}
						Column {
							if (homeState.publicKey != null) {
								Column(
									modifier = Modifier
										.fillMaxWidth()
										.wrapContentHeight(),
									horizontalAlignment = Alignment.CenterHorizontally,
									verticalArrangement = Arrangement.Center
								) {
									SelectedKeyPair(
										keyPairName = homeState.keyPairName,
										keySize = homeState.publicKey.size()
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
							val encryptedContent = homeState.encryptedContent
							val decryptedContent = homeState.decryptedContent
							if (selectedTab == 0) {
								Button(
									onClick = { onEncryptClicked() },
									enabled = homeState.publicKey != null && encryptedContent.isEmpty()
								) {
									if (encryptedContent.isNotEmpty()) {
										Icon(Icons.Default.Check, "Tamamlandı")
									}
									Text(if (encryptedContent.isEmpty()) "İçeriği Şifrele" else "Şifreleme Tamamlandı")
								}
							} else {
								Button(
									onClick = { onDecryptClicked() },
									enabled = homeState.privateKey != null && decryptedContent.isEmpty()
								) {
									if (decryptedContent.isNotEmpty()) {
										Icon(Icons.Default.Check, "Tamamlandı")
									}
									Text(if (decryptedContent.isEmpty()) "İçeriği Çöz" else "Şifre Çözme Tamamlandı")
								}
							}
						}

						if (homeState.encryptedContent.isNotEmpty() && selectedTab == 0) {
							DencryptedContent(
								modifier = Modifier
									.fillMaxWidth()
									.padding(top = 8.dp),
								title = "Şifrelenmiş İçerik",
								content = homeState.encryptedContent
							)
						}

						if (homeState.decryptedContent.isNotEmpty() && selectedTab == 1) {
							DencryptedContent(
								modifier = Modifier
									.fillMaxWidth()
									.padding(top = 8.dp),
								title = "Şifresi Çözülmüş İçerik",
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

	if (homeState.fileList != null) {
		FileListComponent(
			openBottomSheet = openBottomSheet,
			bottomSheetState = bottomSheetState,
			fileList = homeState.fileList,
			onItemClicked = { selectedFileName ->
				selectKeyPairFile(selectedFileName)
			},
			onDismiss = {
				openBottomSheet = false
			}
		)
	}

	FullScreenKeyCreationDialog(
		openDialog = openKeyCreatorDialog,
		onCreateClicked = createKey,
		homeState = homeState,
		onDismiss = { openKeyCreatorDialog = false }
	)
}

@Composable
fun DoubleButtons(
	firstText: String,
	onFirstButtonClicked: () -> Unit,
	secondText: String,
	onSecondButtonClicked: () -> Unit,
) {
	Row(modifier = Modifier.fillMaxWidth()) {
		Button(modifier = Modifier.weight(1f),
			onClick = { onFirstButtonClicked() }) {
			Text(firstText)
		}
		Spacer(modifier = Modifier.size(4.dp))
		Button(modifier = Modifier.weight(1f),
			onClick = { onSecondButtonClicked() }) {
			Text(secondText)
		}
	}
}


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DoubleButtonsPreview(
) {
	DencryptorTheme {
		DoubleButtons(
			stringResource(R.string.select_new_text_file),
			{},
			stringResource(R.string.clear_selected_content),
			{})
	}
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
	HomeScreen(
		openFilePicker = {},
		getKeyPairList = {},
		selectKeyPairFile = {},
		homeState = HomeState(fileContent = "deneme"),
		resetState = {},
		createKey = { keySize: Int, keyPairName: String ->
		},
		onEncryptClicked = {},
		onDecryptClicked = {}
	)
}