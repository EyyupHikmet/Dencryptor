package com.rdiykru.dencryptor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rdiykru.dencryptor.R
import com.rdiykru.dencryptor.ui.theme.DencryptorTheme

@Composable
fun SelectFileInfo(onSelectFileClicked: () -> Unit) {
	val shape = RoundedCornerShape(12.dp)
	Column(
		modifier = Modifier
			.clip(shape)
			.background(
				color = MaterialTheme.colorScheme.secondaryContainer,
				shape = shape
			)
			.border(
				width = 1.dp,
				color = MaterialTheme.colorScheme.secondary,
				shape = shape
			)
			.height(200.dp)
			.width(300.dp)
			.clickable(onClick = onSelectFileClicked),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.SpaceBetween
	) {
		Text(
			modifier = Modifier
				.fillMaxWidth()
				.background(
					shape = shape,
					color = MaterialTheme.colorScheme.secondaryContainer
				)
				.border(
					width = 1.dp,
					color = MaterialTheme.colorScheme.primary,
					shape = shape
				)
				.padding(vertical = 2.dp, horizontal = 16.dp),
			textAlign = TextAlign.Center,
			color = MaterialTheme.colorScheme.onSecondaryContainer,
			text = stringResource(R.string.open_file_selector),
			style = MaterialTheme.typography.bodyLarge,
			fontWeight = FontWeight.Bold
		)
		Icon(
			modifier = Modifier.size(100.dp),
			painter = painterResource(R.drawable.file_open),
			contentDescription = "Open File Selector"
		)
	}
}

@Preview
@Composable
fun SelectFileInfoPreview() {
	DencryptorTheme {
		SelectFileInfo {}
	}
}