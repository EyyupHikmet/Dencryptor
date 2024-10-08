package com.rdiykru.dencryptor.ui.components

import android.content.res.Configuration
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rdiykru.dencryptor.R
import com.rdiykru.dencryptor.ui.theme.DencryptorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationSelectionBar(
	paddingValues: Dp,
	selectedTab: Int,
	onTabSelected: (Int) -> Unit,
) {
	val tabTitles = listOf("Şifrele", "Şifre Çöz")

	SecondaryTabRow(
		selectedTabIndex = selectedTab,
		indicator = {
			FancyIndicator(
				MaterialTheme.colorScheme.onSurface,
				Modifier.tabIndicatorOffset(selectedTab)
			)
		},
		modifier = Modifier
			.padding(bottom = paddingValues)
			.fillMaxWidth()
	) {
		tabTitles.forEachIndexed { index, title ->
			val isSelected = selectedTab == index
			val color =
				if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
			val icon = if (index == 0) R.drawable.locked else R.drawable.unlocked
			val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold

			Tab(
				selected = isSelected,
				onClick = {
					onTabSelected(index)
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

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OperationSelectionBarPreview() {
	DencryptorTheme {
		OperationSelectionBar(
			2.dp,
			onTabSelected = {},
			selectedTab = 0
		)
	}
}
