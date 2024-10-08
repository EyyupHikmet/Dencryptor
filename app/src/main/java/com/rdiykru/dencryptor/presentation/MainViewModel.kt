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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
            fileName,
            homeState.value.decryptedContent
        )
    }

    fun saveEncryptedFile(fileName: String) {
        Log.d(TAG, "saveEncryptedFile: fileName = $fileName")
        fileOperationsManager.saveEncryptedFile(
            fileName,
            homeState.value.encryptedContent
        )

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
                val message = homeState.value.fileContent
                Log.d(TAG, "encryptFileContent: message = $message")

                if (BigInteger(message.toByteArray()) >= publicKey.n) {
                    homeState.update { it.copy(encryptedContent = "") }
                    homeState.update { it.copy(dencrypting = false) }
                    return@launch
                }

                val encryptedMessage = RSA.encrypt(message, publicKey)
                homeState.update {
                    it.copy(encryptedContent = encryptedMessage)
                }
                Log.d(
                    TAG,
                    "encryptFileContent: encryptedContent = $encryptedMessage"
                )
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
                    // Convert file content to a BigInteger
                    val message = homeState.value.fileContent

                    // Decrypt the message
                    val decryptedMessage = RSA.decrypt(message, privateKey)

                    // Check if decryption was successful
                    homeState.update { it.copy(decryptedContent = decryptedMessage) }
                    Log.d(
                        TAG,
                        "decryptFileContent: decryptedContent = ${homeState.value.decryptedContent}"
                    )
                } catch (e: NumberFormatException) {
                    sendHomeEvent(
                        HomeEvent.ShowErrorMessage(
                            "Şifre Çözme Başarısız: Geçersiz Şifreli Veri Formatı"
                        )
                    )
                    homeState.update { it.copy(decryptedContent = "") }
                    Log.e(TAG, "decryptFileContent: ${e.message}", e)
                } catch (e: IllegalArgumentException) {
                    // Handle case where ciphertext is too large
                    sendHomeEvent(
                        HomeEvent.ShowErrorMessage(
                            "Şifre Çözme Başarısız: ${e.message}"
                        )
                    )
                    homeState.update { it.copy(decryptedContent = "") }
                    Log.e(TAG, "decryptFileContent: ${e.message}", e)
                } catch (e: Exception) {
                    // Handle any other exceptions that might occur
                    sendHomeEvent(
                        HomeEvent.ShowErrorMessage(
                            "Şifre Çözme Başarısız: Beklenmedik bir hata oluştu."
                        )
                    )
                    homeState.update { it.copy(decryptedContent = "") }
                    Log.e(TAG, "decryptFileContent: ${e.message}", e)
                }
            } else {
                sendHomeEvent(
                    HomeEvent.ShowErrorMessage(
                        "Özel anahtar mevcut değil."
                    )
                )
            }
            homeState.update { it.copy(dencrypting = false) }
        }
    }

    private fun updateKeyPair(keyPair: RSA.KeyPair) {
        Log.d(TAG, "updateKeyPair")
        homeState.update { it.copy(privateKey = keyPair.privateKey) }
        homeState.update { it.copy(publicKey = keyPair.publicKey) }
    }


    suspend fun sendHomeEvent(event: HomeEvent) {
        sendEvent(_homeEvents, event, TAG)
    }

    private fun resetFileList() {
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

sealed interface HomeEvent {
    data class ShowErrorMessage(val error: String) : HomeEvent
    data class ShowSuccessMessage(val success: String) : HomeEvent
}