package com.rdiykru.dencryptor.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.rdiykru.dencryptor.core.encryption.rsa.RSA
import com.rdiykru.dencryptor.core.extensions.Formatters.size
import com.rdiykru.dencryptor.ui.theme.DencryptorTheme
import java.math.BigInteger.ONE

@Composable
fun KeyPair(keyPair: RSA.KeyPair) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(250.dp)
	) {
		KeyBody(
			modifier = Modifier.weight(1f),
			key = keyPair.publicKey.toString(),
			type = stringResource(R.string.public_key),
			keySize = keyPair.publicKey.size()
		)
		Spacer(modifier = Modifier.size(4.dp))
		KeyBody(
			modifier = Modifier.weight(1f),
			key = keyPair.privateKey.toString(),
			type = stringResource(R.string.private_key),
			keySize = keyPair.privateKey.size()
		)
	}
}

@Composable
private fun KeyBody(modifier: Modifier, type: String, key: String, keySize: Int) {
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
			text = "Anahtar Boyutu: $keySize byte",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSecondaryContainer,
			modifier = Modifier.padding(4.dp)
		)

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
			text = type,
			style = MaterialTheme.typography.bodyLarge,
			fontWeight = FontWeight.SemiBold
		)
		Text(
			text = key,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSecondaryContainer,
		)
	}
}


@Composable
fun SelectedKeyPair(keyPairName: String, keySize: Int) {
	val shape = RoundedCornerShape(12.dp)
	Row(
		modifier = Modifier
			.background(
				color = MaterialTheme.colorScheme.surface,
				shape = shape
			)
			.border(
				width = 1.dp,
				color = MaterialTheme.colorScheme.primary,
				shape = RoundedCornerShape(12.dp)
			)
			.wrapContentHeight(),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Center
	) {
		DefaultTextCard(modifier = Modifier.weight(1f), "Anahtar Adı", keyPairName)
		Spacer(modifier = Modifier.size(4.dp))
		DefaultTextCard(modifier = Modifier.weight(1f), "Anahtar Boyutu", "$keySize byte")
	}
}

@Preview(showBackground = true)
@Composable
private fun SelectedKeyPairPreview() {
	DencryptorTheme {
		SelectedKeyPair(
			keyPairName = "Sample Key Pair",
			keySize = 2048
		)
	}
}

@Preview(showBackground = true)
@Composable
private fun KeyPairPreview() {
	DencryptorTheme {
		KeyPair(
			RSA.KeyPair(
				publicKey = RSA.PublicKey(e = ONE, n = ONE),
				privateKey = RSA.PrivateKey(d = ONE, n = ONE),
			)
		)
	}
}