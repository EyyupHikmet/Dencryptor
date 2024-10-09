package com.rdiykru.dencryptor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.rdiykru.dencryptor.ui.theme.DencryptorTheme

@Composable
fun TitleAndBodyPopup(
	showPopup: Boolean,
	onDismiss: () -> Unit,
	title: String,
	body: String
) {
	val shape = RoundedCornerShape(8.dp)
	if (showPopup) {
		Popup(
			alignment = Alignment.Center,
			onDismissRequest = { onDismiss() }
		) {
			Surface(
				modifier = Modifier
					.padding(48.dp)
					.background(
						MaterialTheme.colorScheme.surfaceDim,
						shape
					)
					.padding(12.dp),
				shape = shape
			) {
				Column(
					modifier = Modifier
						.height(300.dp)
						.background(
							color = MaterialTheme.colorScheme.surface,
							shape = shape
						)
						.border(
							width = 1.dp,
							color = MaterialTheme.colorScheme.onSecondaryContainer,
							shape = shape
						),
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
						modifier = Modifier
							.padding(12.dp)
							.verticalScroll(rememberScrollState()),
						text = body,
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onSurface,
					)
				}
			}
		}
	}
}

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
				shape = shape
			),
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

@Composable
fun CustomSnackbar(snackbarData: SnackbarData, isError: Boolean) {
	val snackbarColor: Color =
		if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer
	val textColor: Color =
		if (isError) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer
	val shape = RoundedCornerShape(8.dp)
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp)
			.wrapContentHeight()
			.background(snackbarColor, shape)
			.border(
				width = 1.dp,
				color = textColor,
				shape = shape
			)
			.padding(16.dp)
	) {
		Text(
			modifier = Modifier.basicMarquee(),
			maxLines = 1,
			text = snackbarData.visuals.message,
			color = textColor
		)
	}
}

@Preview(
	showBackground = true,
	device = Devices.PIXEL_7_PRO,
	showSystemUi = true
)
@Composable
private fun TitleAndBodyPopupPreview() {
	var showPopup by remember { mutableStateOf(true) }
	DencryptorTheme {
		TitleAndBodyPopup(
			showPopup = showPopup,
			onDismiss = { showPopup = false },
			title = "Sample Title",
			body = "This is a sample body text to demonstrate the TitleAndBodyPopup composable."
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

@Preview(showBackground = true)
@Composable
private fun CustomSnackbarPreview() {
	DencryptorTheme {
		val snackbarData = object : SnackbarData {
			override val visuals: SnackbarVisuals = object : SnackbarVisuals {
				override val message: String = "This is a sample snackbar message"
				override val actionLabel: String? = null
				override val duration: SnackbarDuration = SnackbarDuration.Short
				override val withDismissAction: Boolean = false
			}

			override fun dismiss() {}
			override fun performAction() {}
		}

		Column {
			CustomSnackbar(snackbarData = snackbarData, isError = false)
			CustomSnackbar(snackbarData = snackbarData, isError = true)
		}
	}
}
