package com.rdiykru.dencryptor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rdiykru.dencryptor.presentation.HomeState
import com.rdiykru.dencryptor.ui.theme.DencryptorTheme

@Composable
fun FullScreenKeyCreationDialog(
	openDialog: Boolean,
	homeState: HomeState,
	onCreateClicked: (Int, String) -> Unit,
	onDismiss: () -> Unit
) {
	if (openDialog) {
		var manualKeySize by remember { mutableStateOf("") }
		var keyPairName by remember { mutableStateOf("") }
		var errorMessage by remember { mutableStateOf("") }

		val tailoredSize = if (homeState.fileSize * 8 < 2048) 2048 / 8 else (homeState.fileSize + 3)

		Dialog(onDismissRequest = { onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
			Column(
				modifier = Modifier
					.wrapContentHeight()
					.fillMaxWidth()
					.padding(vertical = 16.dp, horizontal = 12.dp)
					.background(
						color = MaterialTheme.colorScheme.surface,
						shape = RoundedCornerShape(12.dp)
					)
					.padding(8.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center
			) {
				TitleText("Anahtar Oluşturma")

				OutlinedTextField(
					label = { Text("Otomatik Anahtar Boyutu") },
					value = "$tailoredSize byte",
					modifier = Modifier.padding(16.dp),
					onValueChange = {},
					enabled = false
				)

				KeySizeInput(
					label = "Manuel Anahtar Boyutu",
					value = manualKeySize,
					onValueChange = { newValue ->
						if (newValue.all { it.isDigit() } && newValue.length <= 4) {
							manualKeySize = newValue
						}
					}
				)

				OutlinedTextField(
					label = { Text("Anahtar Çifti Adı") },
					value = keyPairName,
					modifier = Modifier.padding(16.dp),
					onValueChange = { keyPairName = it },
				)

				Text(
					text = errorMessage,
					color = MaterialTheme.colorScheme.error,
					style = MaterialTheme.typography.bodySmall,
					modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
				)

				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 8.dp)
				) {
					Button(
						modifier = Modifier
							.weight(1f)
							.padding(horizontal = 12.dp),
						onClick = { onDismiss() },
						colors = ButtonDefaults.buttonColors(
							containerColor = Color.Transparent
						),
						border = null
					) {
						Text(
							color = MaterialTheme.colorScheme.onSurface,
							text = "İptal"
						)
					}
					Button(
						modifier = Modifier
							.weight(1f)
							.padding(horizontal = 12.dp),
						onClick = {
							val keySize =
								if (manualKeySize.isEmpty()) tailoredSize else manualKeySize.toIntOrNull()
							val isKeySizeValid = keySize != null && keySize <= 1024
							val isKeyPairNameValid = keyPairName.isNotEmpty()

							// Validate input
							if (!isKeyPairNameValid) {
								errorMessage = "Anahtar Çifti Adı Boş Bırakılamaz!"
							} else if (manualKeySize.isNotEmpty() && !isKeySizeValid) {
								errorMessage = "Anahtar Boyutu 1024 byte'dan Fazla Olamaz!"
							} else if (keySize == null) {
								errorMessage = "Manuel Anahtar İçin Girilen Değer Geçersiz"
							} else {
								onCreateClicked(
									if (manualKeySize.isEmpty()) homeState.fileSize else (keySize),
									keyPairName
								)
								onDismiss()
							}
						},
						enabled = true
					) {
						Text("Oluştur")
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyCreationBottomSheet(
	openBottomSheet: Boolean,
	bottomSheetState: SheetState,
	homeState: HomeState,
	onCreateClicked: (Int, String) -> Unit,
	onDismiss: () -> Unit
) {
	if (openBottomSheet) {
		ModalBottomSheet(
			onDismissRequest = { onDismiss() },
			sheetState = bottomSheetState,
			modifier = Modifier.fillMaxSize()
		) {
			Column(
				modifier = Modifier.fillMaxSize(),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Top
			) {
				var manualKeySize by remember { mutableStateOf("") }
				var keyPairName by remember { mutableStateOf("") }
				var errorMessage by remember { mutableStateOf("") }

				val tailoredSize =
					if (homeState.fileSize * 8 < 2048) 2048 / 8 else (homeState.fileSize + 3)

				TitleText("Anahtar Oluşturma")
				OutlinedTextField(
					label = { Text("Oto. Hesaplanmış Anahtar Boyutu") },
					value = "$tailoredSize byte",
					modifier = Modifier.padding(16.dp),
					onValueChange = {},
					enabled = false
				)

				KeySizeInput(
					label = "Manuel Anahtar Boyutu",
					value = manualKeySize,
					onValueChange = { newValue ->
						if (newValue.all { it.isDigit() } && newValue.length <= 4) {
							manualKeySize = newValue
						}
					}
				)

				OutlinedTextField(
					label = { Text("Anahtar Çifti Adı") },
					value = keyPairName,
					modifier = Modifier.padding(16.dp),
					onValueChange = { keyPairName = it },
				)

				Text(
					text = errorMessage,
					color = MaterialTheme.colorScheme.error,
					style = MaterialTheme.typography.bodySmall,
					modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
				)

				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 24.dp)
				) {
					Button(
						modifier = Modifier
							.weight(1f)
							.padding(horizontal = 12.dp),
						onClick = { onDismiss() },
						colors = ButtonDefaults.buttonColors(
							containerColor = Color.Transparent
						),
						border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
					) {
						Text(
							color = MaterialTheme.colorScheme.onSurface,
							text = "İptal"
						)
					}
					Button(
						modifier = Modifier
							.weight(1f)
							.padding(horizontal = 12.dp),
						onClick = {
							val keySize =
								if (manualKeySize.isEmpty()) tailoredSize else manualKeySize.toIntOrNull()
							val isKeySizeValid = keySize != null && keySize <= 1024
							val isKeyPairNameValid = keyPairName.isNotEmpty()

							// Validate input
							if (!isKeyPairNameValid) {
								errorMessage = "Anahtar Çifti Adı Boş Bırakılamaz!"
							} else if (manualKeySize.isNotEmpty() && !isKeySizeValid) {
								errorMessage = "Anahtar Boyutu 1024 byte'dan Fazla Olamaz!"
							} else if (keySize == null) {
								errorMessage = "Manuel Anahtar İçin Girilen Değer Geçersiz"
							} else {
								onCreateClicked(
									if (manualKeySize.isEmpty()) homeState.fileSize else (keySize),
									keyPairName
								)
								onDismiss()
							}
						},
						enabled = true
					) {
						Text("Oluştur")
					}
				}
			}
		}
	}
}

@Composable
fun KeySizeInput(
	label: String,
	value: String,
	onValueChange: (String) -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.wrapContentHeight(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		OutlinedTextField(
			value = value,
			modifier = Modifier.padding(16.dp),
			onValueChange = { newValue -> onValueChange(newValue) },
			supportingText = {
				Row(verticalAlignment = Alignment.CenterVertically) {
					Icon(
						imageVector = Icons.Default.Info,
						contentDescription = "Bilgilendirme"
					)
					Text(
						modifier = Modifier.padding(start = 4.dp),
						text = "Bu alan boş bırakılırsa otomatik değer seçilecektir."
					)
				}
			},
			label = { Text(text = label) },
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
		)
	}
}

@Composable
fun TitleText(title: String) {
	Text(
		modifier = Modifier.padding(16.dp),
		text = title,
		textAlign = TextAlign.Center,
		fontWeight = FontWeight.SemiBold,
		style = MaterialTheme.typography.headlineMedium,
		color = MaterialTheme.colorScheme.onSurface
	)
}

@Preview(showBackground = true)
@Composable
fun FullScreenKeyCreationDialogPreview() {
	val openBottomSheet by rememberSaveable { mutableStateOf(true) }

	DencryptorTheme {
		FullScreenKeyCreationDialog(
			openDialog = openBottomSheet,
			homeState = HomeState(fileContent = "deneme", fileSize = 123123),
			onCreateClicked = { _: Int, _: String -> },
			onDismiss = {}
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun KeyCreationBottomSheetPreview() {
	val openBottomSheet by rememberSaveable { mutableStateOf(true) }
	val bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded)

	DencryptorTheme {
		KeyCreationBottomSheet(
			openBottomSheet = openBottomSheet,
			bottomSheetState = bottomSheetState,
			homeState = HomeState(fileContent = "deneme", fileSize = 123123),
			onCreateClicked = { _: Int, _: String -> },
			onDismiss = {}
		)
	}
}
