package com.rdiykru.dencryptor.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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

@Preview(showBackground = true)
@Composable
fun FileContentDisplayPreview() {
	FileContentDisplay(
		content = "This is a preview of the file content. The file may contain text data.",
		fileSize = 1024L
	)
}
