@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.rdiykru.dencryptor.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.rdiykru.dencryptor.ui.theme.DencryptorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

	private val viewModel: MainViewModel by viewModels()

	private val requestFileLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			handleActivityResult(result)
		}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()

		setContent {
			val homeState by viewModel.homeState.collectAsState()

			DencryptorTheme {
				HomeScreen(
					openFilePicker = {
						openFilePicker()
						viewModel.resetState()
					},
					getKeyPairList = {
						viewModel.getKeyPairFileList()
					},
					selectKeyPairFile = {
						viewModel.getKeyFile(it)
					},
					homeState = homeState,
					homeEvents = viewModel.homeEvents,
					createKey = { keySize: Int, keyPairName: String ->
						viewModel.createKeyPair(keySize, keyPairName)
					},
					getLocalDecryptedList = { viewModel.getDecryptedFileList() },
					getLocalEncryptedList = { viewModel.getEncryptedFileList() },
					selectLocalEncryptedFile = { viewModel.getEncryptFile(it) },
					selectLocalDecryptedFile = { viewModel.getDecryptFile(it) },
					saveEncryptedFile = { viewModel.saveEncryptedFile(it) },
					saveDecryptedFile = { viewModel.saveDecryptedFile(it) },
					onEncryptClicked = { viewModel.encryptFileContent() },
					onDecryptClicked = { viewModel.decryptFileContent() }
				)
			}
		}
	}

	private fun handleActivityResult(result: ActivityResult) {
		if (result.resultCode == RESULT_OK) {
			result.data?.data?.let { uri ->
				viewModel.handleSelectedFile(uri, contentResolver)
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
}
