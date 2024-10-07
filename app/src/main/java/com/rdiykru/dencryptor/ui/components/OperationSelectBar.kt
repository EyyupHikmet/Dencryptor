package com.rdiykru.dencryptor.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rdiykru.dencryptor.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationSelectionBar(
	onEncryptClicked: () -> Unit,
	onDecryptClicked: () -> Unit
) {
	val tabTitles = listOf("Şifrele", "Şifre Çöz")
	var selectedTab by remember { mutableIntStateOf(0) }
	val coroutineScope = rememberCoroutineScope()

	SecondaryTabRow(
		selectedTabIndex = selectedTab,
		indicator = {
			FancyIndicator(
				MaterialTheme.colorScheme.secondary,
				Modifier.tabIndicatorOffset(selectedTab)
			)
		},
		modifier = Modifier
			.padding(vertical = 16.dp, horizontal = 4.dp)
			.fillMaxWidth()
	) {
		tabTitles.forEachIndexed { index, title ->
			val isSelected = selectedTab == index
			val color =
				if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
			val icon = if (index == 0) R.drawable.locked else R.drawable.unlocked
			val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold

			Tab(
				selected = isSelected,
				onClick = {
					coroutineScope.launch {
						selectedTab = index
						if (index == 0) {
							onEncryptClicked()
						} else {
							onDecryptClicked()
						}
					}
				},
				text = {
					Text(
						color = color,
						fontWeight = fontWeight,
						text = title,
						style = androidx.compose.ui.text.TextStyle(
							fontSize = if (isSelected) 18.sp else 16.sp
						)
					)
				},
				icon = {
					Icon(
						tint = color,
						painter = painterResource(id = icon),
						contentDescription = title,
						modifier = Modifier
							.size(if (isSelected) 24.dp else 20.dp)
					)
				}
			)
		}
	}
}

@Composable
fun FancyIndicator(color: Color, modifier: Modifier = Modifier) {
	Box(
		modifier
			.padding(5.dp)
			.fillMaxSize()
			.border(BorderStroke(2.dp, color), RoundedCornerShape(5.dp))
	)
}

@Preview(showBackground = true)
@Composable
fun OperationSelectionBarPreview() {
	OperationSelectionBar(
		onEncryptClicked = { },
		onDecryptClicked = { }
	)
}
