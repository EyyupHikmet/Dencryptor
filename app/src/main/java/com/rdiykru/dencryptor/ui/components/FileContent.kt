package com.rdiykru.dencryptor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rdiykru.dencryptor.R
import com.rdiykru.dencryptor.ui.theme.DencryptorTheme

@Composable
fun FileContentDisplay(
	content: String,
	fileType: String,
	fileSize: Long,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.wrapContentHeight()
			.fillMaxWidth()
	) {
		val columnShape = RoundedCornerShape(12.dp)

		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.Center,
		) {
			Column(
				modifier = Modifier
					.background(
						color = MaterialTheme.colorScheme.secondaryContainer,
						shape = columnShape
					)
					.border(
						width = 1.dp,
						color = MaterialTheme.colorScheme.primary,
						shape = columnShape
					)
					.wrapContentHeight()
					.weight(4f),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text(
					modifier = modifier
						.fillMaxWidth()
						.background(
							shape = RoundedCornerShape(8.dp),
							color = MaterialTheme.colorScheme.primary
						)
						.padding(2.dp),
					textAlign = TextAlign.Center,
					color = MaterialTheme.colorScheme.onPrimary,
					text = stringResource(R.string.file_size),
					style = MaterialTheme.typography.bodyLarge,
					fontWeight = FontWeight.SemiBold
				)
				Text(
					modifier = Modifier
						.basicMarquee(),
					text = stringResource(R.string.file_size_byte_value, fileSize),
					color = MaterialTheme.colorScheme.onSecondaryContainer,
					maxLines = 1,
					style = MaterialTheme.typography.bodyLarge
				)
			}

			Spacer(Modifier.size(4.dp))
			Column(
				modifier = Modifier
					.background(
						color = MaterialTheme.colorScheme.secondaryContainer,
						shape = columnShape
					)
					.border(
						width = 1.dp,
						color = MaterialTheme.colorScheme.primary,
						shape = RoundedCornerShape(12.dp)
					)
					.wrapContentHeight()
					.weight(2f),
				horizontalAlignment = Alignment.CenterHorizontally
			) {

				Text(
					modifier = modifier
						.fillMaxWidth()
						.background(
							shape = RoundedCornerShape(8.dp),
							color = MaterialTheme.colorScheme.primary
						)
						.padding(2.dp),
					textAlign = TextAlign.Center,
					color = MaterialTheme.colorScheme.onPrimary,
					text = stringResource(R.string.file_type),
					style = MaterialTheme.typography.bodyLarge,
					fontWeight = FontWeight.SemiBold
				)
				Text(
					text = fileType,
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSecondaryContainer,
				)
			}
		}
		Spacer(modifier = Modifier.height(16.dp))
		Column(
			modifier = Modifier
				.background(
					color = MaterialTheme.colorScheme.secondaryContainer,
					shape = columnShape
				)
				.border(
					width = 1.dp,
					color = MaterialTheme.colorScheme.secondary,
					shape = RoundedCornerShape(12.dp)
				)
				.height(200.dp),
			horizontalAlignment = Alignment.Start,
			verticalArrangement = Arrangement.Top
		) {
			Text(
				modifier = modifier
					.fillMaxWidth()
					.background(
						shape = RoundedCornerShape(8.dp),
						color = MaterialTheme.colorScheme.primary
					)
					.padding(vertical = 2.dp, horizontal = 16.dp),
				textAlign = TextAlign.Start,
				color = MaterialTheme.colorScheme.onPrimary,
				text = stringResource(R.string.file_content),
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.SemiBold
			)
			Text(
				modifier = Modifier
					.verticalScroll(rememberScrollState())
					.padding(12.dp),
				text = content,
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSecondaryContainer,
			)
		}
	}
}

@Preview(showBackground = true)
@Composable
fun FileContentDisplayPreview() {
	DencryptorTheme {
		FileContentDisplay(
			content = "This is a preview of the file content. The file may contain text data that is long enough to require scrolling.",
			fileSize = 230L,
			fileType = "txt"
		)
	}
}
