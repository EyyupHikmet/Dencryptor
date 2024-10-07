package com.rdiykru.dencryptor.presentation

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rdiykru.dencryptor.core.encryption.rsa.RSA
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	private val rsaKeyPair: RSA.KeyPair
) : ViewModel() {
	var homeState = MutableStateFlow(HomeState())
		private set

	fun handleSelectedFile(uri: Uri, contentResolver: ContentResolver) {
		viewModelScope.launch {
			val cursor = contentResolver.query(uri, null, null, null, null)
			cursor?.use { cursorHandle ->
				val sizeIndex = cursorHandle.getColumnIndex(OpenableColumns.SIZE)
				cursorHandle.moveToFirst()
				homeState.update { it.copy(fileSize = cursorHandle.getLong(sizeIndex)) }
			}

			contentResolver.openInputStream(uri)?.use { inputStream ->
				val byteArray = inputStream.readBytes()
				homeState.update { it.copy(fileContent = String(byteArray)) }
			} ?: run {
				homeState.update { it.copy(fileContent = "Failed to open file") }
			}
		}
	}

	fun encryptFileContent() {
		viewModelScope.launch {
			val publicKey = rsaKeyPair.publicKey
			val message = BigInteger(homeState.value.fileContent.toByteArray())

			if (message >= publicKey.n) {
				homeState.update { it.copy(encryptedContent = "File is too large to encrypt directly with RSA.") }
				return@launch
			}

			val encryptedMessage = RSA.encrypt(message, publicKey)
			homeState.update { it.copy(encryptedContent = encryptedMessage.toString(16)) }
			homeState.update {
				it.copy(
					keyPairDisplay =
					"Public Key: ${publicKey.e}, ${publicKey.n} \nPrivate Key: ${rsaKeyPair.privateKey.d}, ${rsaKeyPair.privateKey.n}"
				)
			}
		}
	}

	fun decryptFileContent() {
		viewModelScope.launch {
			val privateKey = rsaKeyPair.privateKey
			val encryptedMessage = BigInteger(homeState.value.encryptedContent, 16)

			val decryptedMessage = RSA.decrypt(encryptedMessage, privateKey)
			homeState.update { it.copy(decryptedContent = String(decryptedMessage.toByteArray())) }
		}
	}

	fun resetState(){
		homeState.update { HomeState() }
	}
}

data class HomeState(
	val fileContent: String = "",
	val fileSize: Long = 0L,
	val encryptedContent: String = "",
	val decryptedContent: String = "",
	val keyPairDisplay: String = "",
)
