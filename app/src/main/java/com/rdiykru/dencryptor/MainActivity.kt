package com.rdiykru.dencryptor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import com.rdiykru.dencryptor.core.encryption.rsa.RSA
import com.rdiykru.dencryptor.presentation.HomeScreen
import com.rdiykru.dencryptor.ui.theme.DencryptorTheme
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigInteger
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	private val fileContent = mutableStateOf("")
	private val fileSize = mutableStateOf(0L)
	private val encryptedContent = mutableStateOf("")
	private val decryptedContent = mutableStateOf("")
	private val keyPairDisplay = mutableStateOf("")

	@Inject
	lateinit var rsaKeyPair: RSA.KeyPair

	private val requestFileLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			handleActivityResult(result)
		}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		setContent {
			DencryptorTheme {
				HomeScreen(
					openFilePicker = { openFilePicker() },
					fileContent = fileContent.value,
					fileSize = fileSize.value,
					encryptedContent = encryptedContent.value,
					decryptedContent = decryptedContent.value,
					keyPairDisplay = keyPairDisplay.value,
					onEncryptClicked = { },
					onDecryptClicked = { }
				)
			}
		}
	}

	private fun handleActivityResult(result: ActivityResult) {
		if (result.resultCode == RESULT_OK) {
			result.data?.data?.let { uri ->
				handleSelectedFile(uri)
			}
		}
	}

	private fun openFilePicker() {
		val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
			addCategory(Intent.CATEGORY_OPENABLE)
			type = "text/plain"
		}
		requestFileLauncher.launch(intent)
	}

	private fun handleSelectedFile(uri: Uri) {
		val cursor = contentResolver.query(uri, null, null, null, null)
		cursor?.use {
			val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
			it.moveToFirst()
			fileSize.value = it.getLong(sizeIndex)
		}

		contentResolver.openInputStream(uri)?.use { inputStream ->
			val byteArray = inputStream.readBytes()
			fileContent.value = String(byteArray)
		} ?: run {
			fileContent.value = "Failed to open file"
		}
	}

	private fun encryptFileContent() {
		val publicKey = rsaKeyPair.publicKey
		val message = BigInteger(fileContent.value.toByteArray())

		if (message >= publicKey.n) {
			encryptedContent.value = "File is too large to encrypt directly with RSA."
			return
		}

		val encryptedMessage = RSA.encrypt(message, publicKey)
		encryptedContent.value = encryptedMessage.toString(16)
		keyPairDisplay.value =
			"Public Key: ${publicKey.e}, ${publicKey.n} \nPrivate Key: ${rsaKeyPair.privateKey.d}, ${rsaKeyPair.privateKey.n}"
	}

	private fun decryptFileContent() {
		val privateKey = rsaKeyPair.privateKey
		val encryptedMessage = BigInteger(encryptedContent.value, 16)

		val decryptedMessage = RSA.decrypt(encryptedMessage, privateKey)
		decryptedContent.value = String(decryptedMessage.toByteArray())
	}
}
