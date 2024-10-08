package com.rdiykru.dencryptor.core.file

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class FileOperationsManager(private val context: Context) {

	companion object {
		private const val DECRYPTED_FOLDER = "Decrypted"
		private const val ENCRYPTED_FOLDER = "Encrypted"
		private const val KEY_PAIR_FOLDER = "KeyPair"
	}

	// Save methods
	fun saveDecryptedFile(fileName: String, text: String) {
		saveTextToFile(DECRYPTED_FOLDER, fileName, text)
	}

	fun saveEncryptedFile(fileName: String, text: String) {
		saveTextToFile(ENCRYPTED_FOLDER, fileName, text)
	}

	fun saveKeyPairFile(fileName: String, text: String) {
		saveTextToFile(KEY_PAIR_FOLDER, fileName, text)
	}

	// Read methods
	fun readDecryptedFile(fileName: String): String? {
		return readTextFromFile(DECRYPTED_FOLDER, fileName)
	}

	fun readEncryptedFile(fileName: String): String? {
		return readTextFromFile(ENCRYPTED_FOLDER, fileName)
	}

	fun readKeyPairFile(fileName: String): String? {
		return readTextFromFile(KEY_PAIR_FOLDER, fileName)
	}

	// list methods
	fun getKeyPairList(): List<String> {
		return getAllFilesInFolder(KEY_PAIR_FOLDER)
	}

	fun getEncryptedFileList(): List<String> {
		return getAllFilesInFolder(ENCRYPTED_FOLDER)
	}

	fun getDecryptedFileList(): List<String> {
		return getAllFilesInFolder(DECRYPTED_FOLDER)
	}

	// New method: Get all file names from a folder
	private fun getAllFilesInFolder(folderName: String): List<String> {
		val baseDir: File? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			context.getExternalFilesDir(null) // Scoped storage
		} else {
			Environment.getExternalStorageDirectory() // Legacy storage
		}

		val appFolder = File(baseDir, "Dencryptor/$folderName")

		return if (appFolder.exists()) {
			// Return a list of file names in the folder
			appFolder.listFiles()?.filter { it.isFile }?.map { it.nameWithoutExtension }
				?: emptyList()
		} else {
			Log.e("FileOperationsManager", "Folder does not exist: ${appFolder.absolutePath}")
			emptyList()
		}
	}

	// Private methods
	private fun saveTextToFile(folderName: String, fileName: String, text: String) {
		val baseDir: File? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			context.getExternalFilesDir(null) // Scoped storage
		} else {
			Environment.getExternalStorageDirectory() // Legacy storage
		}

		val appFolder = File(baseDir, "Dencryptor/$folderName")
		if (!appFolder.exists()) {
			if (appFolder.mkdirs()) {
				Log.d("FileOperationsManager", "Created directory: ${appFolder.absolutePath}")
			} else {
				Log.e(
					"FileOperationsManager",
					"Failed to create directory: ${appFolder.absolutePath}"
				)
			}
		}

		val textFile = File(appFolder, "$fileName.txt")
		try {
			FileOutputStream(textFile).use { output ->
				output.write(text.toByteArray())
			}
			Log.d("FileOperationsManager", "Saved file: ${textFile.absolutePath}")
		} catch (e: IOException) {
			Log.e("FileOperationsManager", "Error saving file: ${e.message}", e)
		}
	}

	private fun readTextFromFile(folderName: String, fileName: String): String? {
		val baseDir: File? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			context.getExternalFilesDir(null) // Scoped storage
		} else {
			Environment.getExternalStorageDirectory() // Legacy storage
		}

		val textFile = File(baseDir, "Dencryptor/$folderName/$fileName.txt")

		return if (textFile.exists()) {
			try {
				FileInputStream(textFile).bufferedReader().use { it.readText() }.also {
					Log.d("FileOperationsManager", "Read file: ${textFile.absolutePath}")
				}
			} catch (e: IOException) {
				Log.e("FileOperationsManager", "Error reading file: ${e.message}", e)
				null
			}
		} else {
			Log.e("FileOperationsManager", "File does not exist: ${textFile.absolutePath}")
			null
		}
	}
}
