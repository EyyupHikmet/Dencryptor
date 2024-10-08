package com.rdiykru.dencryptor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rdiykru.dencryptor.ui.theme.DencryptorTheme

@Composable
fun DefaultTextCard(modifier: Modifier, title: String, body: String) {
	val shape = RoundedCornerShape(12.dp)

	Column(
		modifier = modifier
			.background(
				color = MaterialTheme.colorScheme.secondaryContainer,
				shape = shape
			)
			.border(
				width = 1.dp,
				color = MaterialTheme.colorScheme.primary,
				shape = RoundedCornerShape(12.dp)
			)
			.wrapContentHeight(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			modifier = Modifier
				.fillMaxWidth()
				.background(
					shape = RoundedCornerShape(8.dp),
					color = MaterialTheme.colorScheme.primary
				)
				.padding(2.dp),
			textAlign = TextAlign.Center,
			color = MaterialTheme.colorScheme.onPrimary,
			text = title,
			style = MaterialTheme.typography.bodyLarge,
			fontWeight = FontWeight.SemiBold
		)
		Text(
			text = body,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSecondaryContainer,
		)
	}
}

@Preview(showBackground = true)
@Composable
private fun DefaultTextCardPreview() {
	DencryptorTheme {
		DefaultTextCard(
			modifier = Modifier.padding(16.dp),
			title = "Sample Title",
			body = "This is a sample body text to demonstrate the DefaultTextCard composable."
		)
	}
}