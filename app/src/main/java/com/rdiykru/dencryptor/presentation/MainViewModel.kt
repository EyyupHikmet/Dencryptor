package com.rdiykru.dencryptor.presentation

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rdiykru.dencryptor.core.encryption.rsa.RSA
import com.rdiykru.dencryptor.core.extensions.Formatters.toPrivateKey
import com.rdiykru.dencryptor.core.extensions.Formatters.toPublicKey
import com.rdiykru.dencryptor.core.extensions.Formatters.toStringFormat
import com.rdiykru.dencryptor.core.file.FileOperationsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	private val fileOperationsManager: FileOperationsManager
) : ViewModel() {
	var homeState = MutableStateFlow(HomeState())
		private set

	private val TAG = "MainViewModel"

	fun getKeyFile(fileName: String) {
		Log.d(TAG, "getKeyFile: fileName = $fileName")
		if (fileName.contains("Public")) {
			fileOperationsManager.readKeyPairFile(fileName)?.let { publicKey ->
				homeState.update { it.copy(publicKey = publicKey.toPublicKey()) }
				Log.d(TAG, "getKeyFile: Public key retrieved")
			}
		} else if (fileName.contains("Private")) {
			fileOperationsManager.readKeyPairFile(fileName)?.let { private ->
				homeState.update { it.copy(privateKey = private.toPrivateKey()) }
				Log.d(TAG, "getKeyFile: Private key retrieved")
			}
		}
	}

	fun getKeyPairFileList() {
		Log.d(TAG, "getKeyPairFileList")
		homeState.update { it.copy(fileList = fileOperationsManager.getKeyPairList()) }
	}

	fun getEncryptedFileList() {
		Log.d(TAG, "getEncryptedFileList")
		homeState.update { it.copy(fileList = fileOperationsManager.getEncryptedFileList()) }
	}

	fun getDecryptedFileList() {
		Log.d(TAG, "getDecryptedFileList")
		homeState.update { it.copy(fileList = fileOperationsManager.getDecryptedFileList()) }
	}

	fun saveDecryptedFile(fileName: String, text: String) {
		Log.d(TAG, "saveDecryptedFile: fileName = $fileName")
		fileOperationsManager.saveDecryptedFile(fileName, text)
	}

	fun saveEncryptedFile(fileName: String, text: String) {
		Log.d(TAG, "saveEncryptedFile: fileName = $fileName")
		fileOperationsManager.saveEncryptedFile(fileName, text)
	}

	private fun saveKeyPairFile(fileName: String, keyPair: RSA.KeyPair) {
		Log.d(TAG, "saveKeyPairFile: fileName = $fileName")
		fileOperationsManager.saveKeyPairFile("${fileName}_Public", keyPair.publicKey.toStringFormat())
		fileOperationsManager.saveKeyPairFile("${fileName}_Private", keyPair.privateKey.toStringFormat())
	}

	fun handleSelectedFile(uri: Uri, contentResolver: ContentResolver) {
		Log.d(TAG, "handleSelectedFile: uri = $uri")
		viewModelScope.launch(Dispatchers.IO) {
			homeState.update { it.copy(dencrypting = true) }

			val cursor = contentResolver.query(uri, null, null, null, null)
			cursor?.use { cursorHandle ->
				val sizeIndex = cursorHandle.getColumnIndex(OpenableColumns.SIZE)
				cursorHandle.moveToFirst()
				val size = cursorHandle.getInt(sizeIndex)
				homeState.update { it.copy(fileSize = size) }
				Log.d(TAG, "handleSelectedFile: fileSize = $size")
			}

			contentResolver.openInputStream(uri)?.use { inputStream ->
				val byteArray = inputStream.readBytes()
				homeState.update { it.copy(fileContent = String(byteArray)) }
				Log.d(TAG, "handleSelectedFile: fileContent = ${homeState.value.fileContent.take(100)}") // Log first 100 chars
			} ?: run {
				homeState.update { it.copy(fileContent = "Failed to open file") }
				Log.e(TAG, "handleSelectedFile: Failed to open file")
			}

			homeState.update { it.copy(dencrypting = false) }
		}
	}

	fun createKeyPair(keySize: Int, keyPairName: String) {
		Log.d(TAG, "createKeyPair: keySize = $keySize, keyPairName = $keyPairName")
		homeState.update { it.copy(keyPairName = keyPairName) }
		if (keySize == homeState.value.fileSize) {
			createDynamicSizedKeypair()
		} else {
			createStaticSizedKeypair(keySize)
		}
	}

	private fun createStaticSizedKeypair(keySize: Int) {
		Log.d(TAG, "createStaticSizedKeypair: keySize = $keySize")
		viewModelScope.launch(Dispatchers.IO) {
			homeState.update { it.copy(dencrypting = true) }

			val keyPair = RSA.generateKeyPair(keySize * 8)
			saveKeyPairFile(homeState.value.keyPairName, keyPair)
			updateKeyPair(keyPair)

			homeState.update { it.copy(dencrypting = false) }
		}
	}

	private fun createDynamicSizedKeypair() {
		Log.d(TAG, "createDynamicSizedKeypair")
		viewModelScope.launch(Dispatchers.IO) {
			homeState.update { it.copy(dencrypting = true) }

			val fileSizeInBits = homeState.value.fileSize * 8
			val keySize = when {
				fileSizeInBits < 2048 -> 2048
				else -> fileSizeInBits + 24 // margin of error, not calculated
			}
			Log.d(TAG, "createDynamicSizedKeypair: keySize = $keySize")
			val keyPair = RSA.generateKeyPair(keySize)
			saveKeyPairFile(homeState.value.keyPairName, keyPair)
			updateKeyPair(keyPair)
			homeState.update { it.copy(dencrypting = false) }
		}
	}

	// Encrypt the file content
	fun encryptFileContent() {
		Log.d(TAG, "encryptFileContent")
		viewModelScope.launch {
			homeState.update { it.copy(dencrypting = true) }
			val publicKey = homeState.value.publicKey
			if (publicKey != null) {
				val message = BigInteger(homeState.value.fileContent.toByteArray())
				Log.d(TAG, "encryptFileContent: message = $message")

				if (message >= publicKey.n) {
					homeState.update { it.copy(encryptedContent = "File is too large to encrypt directly with RSA.") }
					homeState.update { it.copy(dencrypting = false) }
					return@launch
				}

				val encryptedMessage = RSA.encrypt(message, publicKey)
				homeState.update {
					it.copy(encryptedContent = encryptedMessage.toString(16))
				}
				Log.d(TAG, "encryptFileContent: encryptedContent = ${encryptedMessage.toString(16)}")
			}
			homeState.update { it.copy(dencrypting = false) }
		}
	}

	fun decryptFileContent() {
		Log.d(TAG, "decryptFileContent")
		viewModelScope.launch {
			homeState.update { it.copy(dencrypting = true) }
			val privateKey = homeState.value.privateKey
			if (privateKey != null) {
				try {
					val message = BigInteger(homeState.value.fileContent.toByteArray())
					val decryptedMessage = RSA.decrypt(message, privateKey)
					homeState.update { it.copy(decryptedContent = String(decryptedMessage.toByteArray())) }
					Log.d(TAG, "decryptFileContent: decryptedContent = ${homeState.value.decryptedContent}")
				} catch (e: NumberFormatException) {
					homeState.update { it.copy(decryptedContent = "Failed to decrypt: Invalid encrypted data format") }
					Log.e(TAG, "decryptFileContent: ${e.message}", e)
				}
			}
			homeState.update { it.copy(dencrypting = false) }
		}
	}

	private fun updateKeyPair(keyPair: RSA.KeyPair) {
		Log.d(TAG, "updateKeyPair")
		homeState.update { it.copy(privateKey = keyPair.privateKey) }
		homeState.update { it.copy(publicKey = keyPair.publicKey) }
	}

	fun resetFileList() {
		Log.d(TAG, "resetFileList")
		homeState.update { it.copy(fileList = null) }
	}

	fun resetState() {
		Log.d(TAG, "resetState")
		homeState.update { HomeState() }
	}
}

data class HomeState(
	val dencrypting: Boolean = false,
	val fileContent: String = "",
	val fileList: List<String>? = listOf(),
	val fileSize: Int = 0,
	val encryptedContent: String = "",
	val decryptedContent: String = "",
	val publicKey: RSA.PublicKey? = null,
	val privateKey: RSA.PrivateKey? = null,
	val keyPairName: String = ""
)
