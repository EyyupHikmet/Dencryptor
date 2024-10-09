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
import com.rdiykru.dencryptor.core.extensions.sendEvent
import com.rdiykru.dencryptor.core.file.FileOperationsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
	private val fileOperationsManager: FileOperationsManager
) : ViewModel() {

	private val _homeEvents = Channel<HomeEvent>(Channel.BUFFERED)
	val homeEvents: Flow<HomeEvent> = _homeEvents.receiveAsFlow()

	var homeState = MutableStateFlow(HomeState())
		private set

	fun getKeyFile(fileName: String) {
		Log.d(TAG, "getKeyFile: fileName = $fileName")
		if (fileName.contains("Public")) {
			fileOperationsManager.readKeyPairFile(fileName)?.let { publicKey ->
				homeState.update { it.copy(publicKey = publicKey.toPublicKey()) }
				homeState.update { it.copy(publicKeyName = fileName) }
				Log.d(TAG, "getKeyFile: Public key retrieved")
				viewModelScope.launch {
					sendHomeEvent(HomeEvent.ShowSuccessMessage("Genel anahtar başarıyla alındı."))
				}
			}
		} else if (fileName.contains("Private")) {
			fileOperationsManager.readKeyPairFile(fileName)?.let { private ->
				homeState.update { it.copy(privateKey = private.toPrivateKey()) }
				homeState.update { it.copy(privateKeyName = fileName) }
				Log.d(TAG, "getKeyFile: Private key retrieved")
				viewModelScope.launch {
					sendHomeEvent(HomeEvent.ShowSuccessMessage("Özel anahtar başarıyla alındı."))
				}
			}
		} else {
			viewModelScope.launch {
				sendHomeEvent(HomeEvent.ShowErrorMessage("Anahtar dosyası alınamadı."))
			}
		}
		resetFileList()
	}

	fun getEncryptFile(fileName: String) {
		Log.d(TAG, "getEncryptFile: fileName = $fileName")
		fileOperationsManager.readEncryptedFile(fileName)?.let { fileContent ->
			homeState.update { it.copy(fileContent = fileContent) }
			homeState.update { it.copy(fileSize = fileContent.toByteArray().size) }
		}
		resetFileList()
	}

	fun getDecryptFile(fileName: String) {
		Log.d(TAG, "getDecryptFile: fileName = $fileName")
		fileOperationsManager.readDecryptedFile(fileName)?.let { fileContent ->
			homeState.update { it.copy(fileContent = fileContent) }
			homeState.update { it.copy(fileSize = fileContent.toByteArray().size) }
			Log.d(TAG, "getDecryptFile: Public key retrieved")
		}
		resetFileList()
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

	fun saveDecryptedFile(fileName: String) {
		Log.d(TAG, "saveDecryptedFile: fileName = $fileName")
		fileOperationsManager.saveDecryptedFile(
			"${fileName}_Decrypted",
			homeState.value.decryptedContent
		)
		viewModelScope.launch {
			sendHomeEvent(HomeEvent.ShowSuccessMessage("Şifre çözülmüş dosya başarıyla kaydedildi."))
		}
	}

	fun saveEncryptedFile(fileName: String) {
		Log.d(TAG, "saveEncryptedFile: fileName = $fileName")
		fileOperationsManager.saveEncryptedFile(
			"${fileName}_Encrypted",
			homeState.value.encryptedContent
		)
		viewModelScope.launch {
			sendHomeEvent(HomeEvent.ShowSuccessMessage("Şifrelenmiş dosya başarıyla kaydedildi."))
		}
	}

	private fun saveKeyPairFile(fileName: String, keyPair: RSA.KeyPair) {
		Log.d(TAG, "saveKeyPairFile: fileName = $fileName")
		fileOperationsManager.saveKeyPairFile(
			"${fileName}_Public",
			keyPair.publicKey.toStringFormat()
		)
		fileOperationsManager.saveKeyPairFile(
			"${fileName}_Private",
			keyPair.privateKey.toStringFormat()
		)
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
				Log.d(
					TAG,
					"handleSelectedFile: fileContent = ${homeState.value.fileContent.take(100)}"
				) // Log first 100 chars
			} ?: run {
				homeState.update { it.copy(fileContent = "Dosya açılamadı") }
				Log.e(TAG, "handleSelectedFile: Dosya açılamadı")
			}

			homeState.update { it.copy(dencrypting = false) }
		}
	}

	fun createKeyPair(keySize: Int, keyPairName: String) {
		Log.d(TAG, "createKeyPair: keySize = $keySize, keyPairName = $keyPairName")

		viewModelScope.launch {
			updateKeyPairName(keyPairName)
			try {
				if (keySize == homeState.value.fileSize) {
					createDynamicSizedKeypair(keyPairName = keyPairName)
				} else if (keySize > (1024 * 8)) {
					sendHomeEvent(HomeEvent.ShowErrorMessage("Oluşturulmak istenen anahtar boyutu fazla büyük."))
				} else {
					createStaticSizedKeypair(keyPairName = keyPairName, keySize = keySize)
				}
			} catch (e: Exception) {
				Log.e(TAG, "createKeyPair: Failed to create key pair: ${e.message}", e)
				sendHomeEvent(HomeEvent.ShowErrorMessage("Anahtar çifti oluşturulamadı: ${e.message}"))
			}
		}
	}

	private fun createStaticSizedKeypair(keySize: Int, keyPairName: String) {
		Log.d(TAG, "createStaticSizedKeypair: keySize = $keySize")
		viewModelScope.launch(Dispatchers.IO) {
			homeState.update { it.copy(dencrypting = true) }
			try {
				// Enforce key size limit of 1024 * 8 bits
				if (keySize > 1024 * 8) {
					sendHomeEvent(HomeEvent.ShowErrorMessage("Oluşturulmak istenen anahtar boyutu fazla büyük."))
					return@launch
				}

				// Set a timeout of 15 seconds for key pair generation
				withTimeout(15000) {
					val keyPair = RSA.generateKeyPair(keySize * 8)
					saveKeyPairFile(keyPairName, keyPair)
					updateKeyPair(keyPair)
					sendHomeEvent(HomeEvent.ShowSuccessMessage("Anahtar çifti başarıyla oluşturuldu."))
				}
			} catch (e: TimeoutCancellationException) {
				Log.e(TAG, "createStaticSizedKeypair: Key pair generation timed out", e)
				sendHomeEvent(HomeEvent.ShowErrorMessage("Statik boyutlu anahtar çifti oluşturma süresi aşıldı."))
			} catch (e: Exception) {
				Log.e(TAG, "createStaticSizedKeypair: Failed to generate key pair: ${e.message}", e)
				sendHomeEvent(HomeEvent.ShowErrorMessage("Statik boyutlu anahtar çifti oluşturulamadı: ${e.message}"))
			} finally {
				homeState.update { it.copy(dencrypting = false) }
			}
		}
	}

	private fun createDynamicSizedKeypair(keyPairName: String) {
		Log.d(TAG, "createDynamicSizedKeypair")
		viewModelScope.launch(Dispatchers.IO) {
			homeState.update { it.copy(dencrypting = true) }
			try {
				// Set a timeout of 15 seconds for key pair generation
				withTimeout(15000) {
					val fileSizeInBits = homeState.value.fileSize * 8
					val keySize = when {
						fileSizeInBits < 2048 -> 2048
						fileSizeInBits > (1024 * 8) -> {
							sendHomeEvent(HomeEvent.ShowErrorMessage("Anahtar boyutu 1024 byte'tan fazla olamaz."))
							return@withTimeout
						}

						else -> fileSizeInBits + 24 // margin of error, not calculated
					}
					Log.d(TAG, "createDynamicSizedKeypair: keySize = $keySize")
					val keyPair = RSA.generateKeyPair(keySize)
					saveKeyPairFile(keyPairName, keyPair)
					updateKeyPair(keyPair)
					sendHomeEvent(HomeEvent.ShowSuccessMessage("Dinamik boyutlu anahtar çifti başarıyla oluşturuldu."))
				}
			} catch (e: TimeoutCancellationException) {
				Log.e(TAG, "createDynamicSizedKeypair: Key pair generation timed out", e)
				sendHomeEvent(HomeEvent.ShowErrorMessage("Dinamik boyutlu anahtar çifti oluşturma süresi aşıldı."))
			} catch (e: Exception) {
				Log.e(
					TAG,
					"createDynamicSizedKeypair: Failed to generate dynamic key pair: ${e.message}",
					e
				)
				sendHomeEvent(HomeEvent.ShowErrorMessage("Dinamik boyutlu anahtar çifti oluşturulamadı: ${e.message}"))
			} finally {
				homeState.update { it.copy(dencrypting = false) }
			}
		}
	}

	fun encryptFileContent() {
		Log.d(TAG, "encryptFileContent")
		viewModelScope.launch {
			homeState.update { it.copy(dencrypting = true) }
			val publicKey = homeState.value.publicKey
			if (publicKey != null) {
				val message = homeState.value.fileContent
				Log.d(TAG, "encryptFileContent: message = $message")

				if (BigInteger(message.toByteArray()) >= publicKey.n) {
					homeState.update { it.copy(encryptedContent = "") }
					homeState.update { it.copy(dencrypting = false) }
					sendHomeEvent(HomeEvent.ShowErrorMessage("Şifreleme başarısız: mesaj anahtar boyutuna çok büyük."))
					return@launch
				}

				val encryptedMessage = RSA.encrypt(message, publicKey)
				homeState.update { it.copy(encryptedContent = encryptedMessage) }
				sendHomeEvent(HomeEvent.ShowSuccessMessage("Dosya başarıyla şifrelendi."))
			} else {
				sendHomeEvent(HomeEvent.ShowErrorMessage("Şifreleme için genel anahtar mevcut değil."))
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
					val message = homeState.value.fileContent
					val decryptedMessage = RSA.decrypt(message, privateKey)
					homeState.update { it.copy(decryptedContent = decryptedMessage) }
					Log.d(
						TAG,
						"decryptFileContent: decryptedContent = ${homeState.value.decryptedContent}"
					)
					sendHomeEvent(HomeEvent.ShowSuccessMessage("Dosya başarıyla çözüldü."))
				} catch (e: NumberFormatException) {
					sendHomeEvent(HomeEvent.ShowErrorMessage("Şifre çözme başarısız: Geçersiz şifrelenmiş veri formatı."))
					homeState.update { it.copy(decryptedContent = "") }
					Log.e(TAG, "decryptFileContent: ${e.message}", e)
				} catch (e: IllegalArgumentException) {
					sendHomeEvent(HomeEvent.ShowErrorMessage("Şifre çözme başarısız: ${e.message}"))
					homeState.update { it.copy(decryptedContent = "") }
					Log.e(TAG, "decryptFileContent: ${e.message}", e)
				} catch (e: Exception) {
					sendHomeEvent(HomeEvent.ShowErrorMessage("Şifre çözme başarısız: Beklenmeyen bir hata oluştu."))
					homeState.update { it.copy(decryptedContent = "") }
					Log.e(TAG, "decryptFileContent: ${e.message}", e)
				}
			} else {
				sendHomeEvent(HomeEvent.ShowErrorMessage("Şifre çözme için özel anahtar mevcut değil."))
			}
			homeState.update { it.copy(dencrypting = false) }
		}
	}

	private fun updateKeyPair(keyPair: RSA.KeyPair) {
		Log.d(TAG, "updateKeyPair")
		homeState.update { it.copy(privateKey = keyPair.privateKey) }
		homeState.update { it.copy(publicKey = keyPair.publicKey) }
	}

	private fun updateKeyPairName(keyPairName: String) {
		Log.d(TAG, "updateKeyPairName")
		homeState.update { it.copy(privateKeyName = "${keyPairName}_Private") }
		homeState.update { it.copy(publicKeyName = "${keyPairName}_Public") }
	}


	private fun sendHomeEvent(event: HomeEvent) {
		viewModelScope.launch {
			sendEvent(_homeEvents, formatMessage(event), TAG)
		}
	}

	private fun resetFileList() {
		Log.d(TAG, "resetFileList")
		homeState.update { it.copy(fileList = null) }
	}

	fun resetState() {
		Log.d(TAG, "resetState")
		homeState.update { HomeState() }
	}

	private fun formatMessage(event: HomeEvent): HomeEvent {
		return when (event) {
			is HomeEvent.ShowErrorMessage -> HomeEvent.ShowErrorMessage("Hata: ${event.error}")
			is HomeEvent.ShowSuccessMessage -> HomeEvent.ShowSuccessMessage("Başarı: ${event.success}")
		}
	}

	companion object {
		private const val TAG = "MAIN_VIEW_MODEL"
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
	val publicKeyName: String = "",
	val privateKeyName: String = ""
)

sealed interface HomeEvent {
	data class ShowErrorMessage(val error: String) : HomeEvent
	data class ShowSuccessMessage(val success: String) : HomeEvent
}