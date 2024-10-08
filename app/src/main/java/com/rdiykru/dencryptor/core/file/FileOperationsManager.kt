package com.rdiykru.dencryptor.core.file

import android.content.Context
import android.os.Build
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class FileOperationsManager(private val context: Context) {

	private val decryptedFolder = "Decrypted"
	private val encryptedFolder = "Encrypted"
	private val keyPairFolder = "KeyPair"

	fun saveDecryptedFile(fileName: String, text: String) {
		saveTextToFile(decryptedFolder, fileName, text)
	}

	fun saveEncryptedFile(fileName: String, text: String) {
		saveTextToFile(encryptedFolder, fileName, text)
	}

	fun saveKeyPairFile(fileName: String, text: String) {
		saveTextToFile(keyPairFolder, fileName, text)
	}

	fun readDecryptedFile(fileName: String): String? {
		return readTextFromFile(decryptedFolder, fileName)
	}

	fun readEncryptedFile(fileName: String): String? {
		return readTextFromFile(encryptedFolder, fileName)
	}

	fun readKeyPairFile(fileName: String): String? {
		return readTextFromFile(keyPairFolder, fileName)
	}

	private fun saveTextToFile(folderName: String, fileName: String, text: String) {
		val baseDir: File? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			context.getExternalFilesDir(null) // Scoped storage
		} else {
			Environment.getExternalStorageDirectory() // Legacy storage
		}

		val appFolder = File(baseDir, "Dencryptor/$folderName")
		if (!appFolder.exists()) {
			appFolder.mkdirs()
		}

		val textFile = File(appFolder, "$fileName.txt")
		try {
			FileOutputStream(textFile).use { output ->
				output.write(text.toByteArray())
			}
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	private fun readTextFromFile(folderName: String, fileName: String): String? {
		val baseDir: File? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			context.getExternalFilesDir(null) // Scoped storage
		} else {
			Environment.getExternalStorageDirectory() // Legacy storage
		}

		val textFile = File(baseDir, "Dencryptor/$folderName/$fileName.txt")

		return try {
			FileInputStream(textFile).bufferedReader().use { it.readText() }
		} catch (e: IOException) {
			e.printStackTrace()
			null
		}
	}
}
