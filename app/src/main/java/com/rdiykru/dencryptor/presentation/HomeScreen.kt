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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rdiykru.dencryptor.R
import com.rdiykru.dencryptor.core.encryption.rsa.RSA
import com.rdiykru.dencryptor.core.extensions.Formatters.size
import com.rdiykru.dencryptor.ui.components.CustomSnackbar
import com.rdiykru.dencryptor.ui.components.DencryptedContent
import com.rdiykru.dencryptor.ui.components.FileContentDisplay
import com.rdiykru.dencryptor.ui.components.FileListComponent
import com.rdiykru.dencryptor.ui.components.FullScreenKeyCreationDialog
import com.rdiykru.dencryptor.ui.components.InputDialog
import com.rdiykru.dencryptor.ui.components.OperationSelectionBar
import com.rdiykru.dencryptor.ui.components.SelectFileInfo
import com.rdiykru.dencryptor.ui.components.SelectedKeyPair
import com.rdiykru.dencryptor.ui.components.TitleAndBodyPopup
import com.rdiykru.dencryptor.ui.theme.DencryptorTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.math.BigInteger

@ExperimentalMaterial3Api
@Composable
fun HomeScreen(
	openFilePicker: () -> Unit,
	getKeyPairList: () -> Unit,
	getLocalEncryptedList: () -> Unit,
	getLocalDecryptedList: () -> Unit,
	selectKeyPairFile: (String) -> Unit,
	selectLocalEncryptedFile: (String) -> Unit,
	selectLocalDecryptedFile: (String) -> Unit,
	homeState: HomeState,
	homeEvents: Flow<HomeEvent>,
	createKey: (Int, String) -> Unit,
	saveEncryptedFile: (String) -> Unit,
	saveDecryptedFile: (String) -> Unit,
	onEncryptClicked: () -> Unit,
	onDecryptClicked: () -> Unit
) {
	val snackbarHostState = remember { SnackbarHostState() }

	LaunchedEffect(Unit) {
		homeEvents.collect { event ->
			when (event) {
				is HomeEvent.ShowErrorMessage -> {
					snackbarHostState.showSnackbar(
						message = event.error,
						duration = SnackbarDuration.Short
					)
					snackbarHostState.currentSnackbarData?.dismiss()
				}

				is HomeEvent.ShowSuccessMessage -> {
					snackbarHostState.showSnackbar(
						message = event.success,
						duration = SnackbarDuration.Short
					)
					snackbarHostState.currentSnackbarData?.dismiss()
				}
			}
		}
	}

	var openKeyCreatorDialog by rememberSaveable { mutableStateOf(false) }
	var openFileCreatorDialog by remember { mutableStateOf(false) }

	var selectedTab by remember { mutableIntStateOf(0) }

	var openKeyBottomSheet by rememberSaveable { mutableStateOf(false) }
	var openEncryptedBottomSheet by rememberSaveable { mutableStateOf(false) }
	var openDecryptedBottomSheet by rememberSaveable { mutableStateOf(false) }
	val skipPartiallyExpanded by rememberSaveable { mutableStateOf(false) }
	val bottomSheetState =
		rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

	var showPopup by remember { mutableStateOf(false) }

	val bottomPaddingInPixels = WindowInsets.navigationBars.getBottom(LocalDensity.current)
	val bottomPaddingInDp = with(LocalDensity.current) { bottomPaddingInPixels.toDp() }

	Scaffold(
		snackbarHost = {
			SnackbarHost(
				hostState = snackbarHostState,
				snackbar = { snackbarData ->
					val isError = snackbarData.visuals.message.contains("Hata:", ignoreCase = true)
					CustomSnackbar(snackbarData, isError)
				}
			)
		},
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
							firstText = stringResource(R.string.select_external_text_file),
							onFirstButtonClicked = openFilePicker,
							secondText = stringResource(R.string.select_local_text_file),
							onSecondButtonClicked = {
								if (selectedTab == 0) {
									getLocalDecryptedList()
									openDecryptedBottomSheet = true
								} else {
									getLocalEncryptedList()
									openEncryptedBottomSheet = true
								}
							}
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
									openKeyBottomSheet = true
								},
							) {
								Text("Yerel Anahtar Seç")
							}
						}
						Column {
							if (homeState.publicKey != null) {
								Column(
									modifier = Modifier
										.fillMaxWidth()
										.wrapContentHeight()
										.clickable { showPopup = true },
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
									enabled = homeState.publicKey != null
								) {
									Text("İçeriği Şifrele")
								}
								if (encryptedContent.isNotEmpty()) {
									Button(
										onClick = { openFileCreatorDialog = true },
									) {
										Icon(painterResource(R.drawable.save), "Tamamlandı")

										Text("Şifreli İçeriği Kaydet")
									}
								}
							} else {
								Button(
									onClick = { onDecryptClicked() },
									enabled = homeState.privateKey != null
								) {
									Text("İçeriği Çöz")
								}
								if (decryptedContent.isNotEmpty()) {
									Button(
										onClick = { openFileCreatorDialog = true },
									) {
										Icon(painterResource(R.drawable.save), "Tamamlandı")

										Text("Çözülmüş İçeriği Kaydet")
									}
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
			openBottomSheet = openKeyBottomSheet,
			bottomSheetState = bottomSheetState,
			fileList = homeState.fileList,
			onItemClicked = { selectedFileName ->
				selectKeyPairFile(selectedFileName)
			},
			onDismiss = {
				openKeyBottomSheet = false
			}
		)
		FileListComponent(
			openBottomSheet = openEncryptedBottomSheet,
			bottomSheetState = bottomSheetState,
			fileList = homeState.fileList,
			onItemClicked = { selectedFileName ->
				selectLocalEncryptedFile(selectedFileName)
			},
			onDismiss = {
				openEncryptedBottomSheet = false
			}
		)
		FileListComponent(
			openBottomSheet = openDecryptedBottomSheet,
			bottomSheetState = bottomSheetState,
			fileList = homeState.fileList,
			onItemClicked = { selectedFileName ->
				selectLocalDecryptedFile(selectedFileName)
			},
			onDismiss = {
				openDecryptedBottomSheet = false
			}
		)
	}

	TitleAndBodyPopup(
		showPopup = showPopup,
		onDismiss = { showPopup = false },
		title = if (selectedTab == 0) "Genel Anahtar" else "Özel Anahtar",
		body = if (selectedTab == 0) "${homeState.publicKey}" else "${homeState.privateKey}"
	)

	FullScreenKeyCreationDialog(
		openDialog = openKeyCreatorDialog,
		onCreateClicked = createKey,
		homeState = homeState,
		onDismiss = { openKeyCreatorDialog = false }
	)

	InputDialog(
		openDialog = openFileCreatorDialog,
		onDismiss = { openFileCreatorDialog = false },
		onSave = { input ->
			if (selectedTab == 0) {
				saveEncryptedFile(input)
			} else {
				saveDecryptedFile(input)
			}
			openFileCreatorDialog = false
		}
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
			stringResource(R.string.select_external_text_file),
			{},
			stringResource(R.string.select_local_text_file),
			{})
	}
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
	val byteArrayExample = "Bu bir bytearray örneğidir".toByteArray()
	val byteArrayExample2 = "Bu farklı bir bytearray örneğidir".toByteArray()
	HomeScreen(
		openFilePicker = {},
		getKeyPairList = {},
		selectKeyPairFile = {},
		homeState = HomeState(
			fileContent = "deneme",
			privateKey = RSA.PrivateKey(
				d = BigInteger(byteArrayExample),
				n = BigInteger(byteArrayExample2)
			),
			publicKey = RSA.PublicKey(
				e = BigInteger(byteArrayExample),
				n = BigInteger(byteArrayExample2)
			),
			keyPairName = "Test Key"
		),
		createKey = { keySize: Int, keyPairName: String ->
		},
		selectLocalDecryptedFile = {},
		selectLocalEncryptedFile = {},
		getLocalDecryptedList = {},
		getLocalEncryptedList = {},
		saveDecryptedFile = {},
		saveEncryptedFile = {},
		onEncryptClicked = {},
		onDecryptClicked = {},
		homeEvents = emptyFlow(),
	)
}