package com.rdiykru.dencryptor.presentation

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rdiykru.dencryptor.core.encryption.rsa.RSA
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
	var homeState = MutableStateFlow(HomeState())
		private set

	fun handleSelectedFile(uri: Uri, contentResolver: ContentResolver) {
		viewModelScope.launch(Dispatchers.IO) {
			homeState.update { it.copy(dencrypting = true) }

			val cursor = contentResolver.query(uri, null, null, null, null)
			cursor?.use { cursorHandle ->
				val sizeIndex = cursorHandle.getColumnIndex(OpenableColumns.SIZE)
				cursorHandle.moveToFirst()
				homeState.update { it.copy(fileSize = cursorHandle.getInt(sizeIndex)) }
			}

			contentResolver.openInputStream(uri)?.use { inputStream ->
				val byteArray = inputStream.readBytes()
				homeState.update { it.copy(fileContent = String(byteArray)) }
			} ?: run {
				homeState.update { it.copy(fileContent = "Failed to open file") }
			}

			homeState.update { it.copy(dencrypting = false) }
		}
	}

	fun createDynamicSizedKeypair() {
		viewModelScope.launch(Dispatchers.IO) {
			homeState.update { it.copy(dencrypting = true) }

			val fileSizeInBits = homeState.value.fileSize * 8
			val keySize = when {
				fileSizeInBits < 2048 -> 2048
				fileSizeInBits < 4096 -> 4096
				else -> fileSizeInBits + 20 // margin of error, not calculated
			}
			val keyPair = RSA.generateKeyPair(keySize)

			homeState.update { it.copy(rsaKeyPair = keyPair) }
			homeState.update { it.copy(dencrypting = false) }
		}
	}

	// Encrypt the file content
	fun encryptFileContent() {
		viewModelScope.launch {
			homeState.update { it.copy(dencrypting = true) }

			val keypair = homeState.value.rsaKeyPair
			if (keypair != null) {
				val publicKey = keypair.publicKey
				val message = BigInteger(homeState.value.fileContent.toByteArray())

				if (message >= publicKey.n) {
					homeState.update { it.copy(encryptedContent = "File is too large to encrypt directly with RSA.") }
					homeState.update { it.copy(dencrypting = false) }
					return@launch
				}

				val encryptedMessage = RSA.encrypt(message, publicKey)
				homeState.update {
					it.copy(encryptedContent = encryptedMessage.toString(16))
				}
			}

			homeState.update { it.copy(dencrypting = false) }
		}
	}

	// Decrypt the file content
	fun decryptFileContent() {
		viewModelScope.launch {
			homeState.update { it.copy(dencrypting = true) }

			val keypair = homeState.value.rsaKeyPair
			if (keypair != null) {
				try {
					val encryptedMessage = BigInteger(homeState.value.encryptedContent, 16)
					val decryptedMessage = RSA.decrypt(encryptedMessage, keypair.privateKey)
					homeState.update { it.copy(decryptedContent = String(decryptedMessage.toByteArray())) }
				} catch (e: NumberFormatException) {
					homeState.update { it.copy(decryptedContent = "Failed to decrypt: Invalid encrypted data format") }
				}
			}

			homeState.update { it.copy(dencrypting = false) }
		}
	}

	// Reset the state
	fun resetState() {
		homeState.update { HomeState() }
	}
}

data class HomeState(
	val dencrypting: Boolean = false,
	val fileContent: String = "",
	val fileSize: Int = 0,
	val encryptedContent: String = "",
	val decryptedContent: String = "",
	val rsaKeyPair: RSA.KeyPair? = null
)
