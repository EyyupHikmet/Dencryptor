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

class MainActivity : ComponentActivity() {

	// Mutable state to hold the file content and size
	private val fileContent = mutableStateOf("")
	private val fileSize = mutableStateOf(0L) // For file size in bytes

	private val requestFileLauncher =
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			handleActivityResult(result)
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
			type = "text/plain"  // Set MIME type to filter text files
		}
		requestFileLauncher.launch(intent)
	}

	private fun handleSelectedFile(uri: Uri) {
		// Retrieve file size
		val cursor = contentResolver.query(uri, null, null, null, null)
		cursor?.use {
			val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
			it.moveToFirst()
			fileSize.value = it.getLong(sizeIndex) // Set the file size
		}

		// Read file content as ByteArray
		contentResolver.openInputStream(uri)?.use { inputStream ->
			val byteArray = inputStream.readBytes() // Read file content as ByteArray
			performOperationsOnByteArray(byteArray) // Perform additional operations
		} ?: run {
			// Handle error in case the file stream couldn't be opened
			fileContent.value = "Failed to open file"
		}
	}

	// Perform other operations on the ByteArray
	private fun performOperationsOnByteArray(byteArray: ByteArray) {
		// Example operation: Convert ByteArray to String and set it to fileContent
		val textContent = String(byteArray)
		fileContent.value = textContent

		// Example: Log or display the size of the byte array (already set via fileSize)
		// You can also add other operations here if needed.
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			DencryptorTheme {
				// Scaffold for consistent theming and structure
				Scaffold(
					modifier = Modifier.fillMaxSize()
				) { paddingValues ->
					// UI with button to open file picker and display file content and size
					Column(
						modifier = Modifier
							.fillMaxSize()
							.padding(paddingValues)
							.padding(16.dp),
						verticalArrangement = Arrangement.spacedBy(16.dp)
					) {
						// Button to open file picker
						Button(onClick = { openFilePicker() }) {
							Text("Select Text File")
						}

						// Display the file content and size in a Text composable
						FileContentDisplay(
							content = fileContent.value,
							fileSize = fileSize.value
						)
					}
				}
			}
		}
	}
}

@Composable
fun FileContentDisplay(content: String, fileSize: Long, modifier: Modifier = Modifier) {
	// Display the file content and size in Text composables
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
