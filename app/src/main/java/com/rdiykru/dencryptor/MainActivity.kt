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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rdiykru.dencryptor.ui.theme.DencryptorTheme
import dagger.hilt.android.AndroidEntryPoint
import com.rdiykru.dencryptor.core.encryption.rsa.RSA
import java.math.BigInteger
import javax.inject.Inject

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow

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
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Button(onClick = { openFilePicker() }) {
                            Text("Select Text File")
                        }

                        FileContentDisplay(
                            content = fileContent.value,
                            fileSize = fileSize.value
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Button(onClick = { encryptFileContent() }) {
                                Text("Encrypt File")
                            }

                            Button(
                                onClick = { decryptFileContent() },
                                enabled = encryptedContent.value.isNotEmpty()
                            ) {
                                Text("Decrypt File")
                            }
                        }

                        if (encryptedContent.value.isNotEmpty() && keyPairDisplay.value.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp)
                                ) {
                                    Text("Key Pair:", style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        text = keyPairDisplay.value,
                                        style = MaterialTheme.typography.bodyMedium,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        "Encrypted Content:",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = encryptedContent.value,
                                        style = MaterialTheme.typography.bodyMedium,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (decryptedContent.value.isNotEmpty()) {
                                Column(
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text(
                                        text = "Decrypted Content:",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = decryptedContent.value,
                                        style = MaterialTheme.typography.bodyMedium,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                }
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

        // Decrypt the encrypted content
        val decryptedMessage = RSA.decrypt(encryptedMessage, privateKey)
        decryptedContent.value = String(decryptedMessage.toByteArray())
    }
}

@Composable
fun FileContentDisplay(content: String, fileSize: Long, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "File Size: $fileSize bytes",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "File Content:",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
