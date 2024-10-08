package com.rdiykru.dencryptor.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rdiykru.dencryptor.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListComponent(
	openBottomSheet: Boolean,
	bottomSheetState: SheetState,
	fileList: List<String>,
	onItemClicked: (String) -> Unit,
	onDismiss: () -> Unit
) {
	if (openBottomSheet) {
		ModalBottomSheet(
			onDismissRequest = { onDismiss() },
			sheetState = bottomSheetState,
			modifier = Modifier
				.fillMaxWidth()
				.wrapContentHeight(),
			containerColor = MaterialTheme.colorScheme.surface
		) {
			LazyColumn {
				items(fileList) { fileName ->
					ListItem(
						headlineContent = { Text(fileName) },
						leadingContent = {
							Icon(
								painterResource(R.drawable.file_open),
								contentDescription = "Localized description"
							)
						},
						modifier = Modifier
							.clickable {
								onItemClicked(fileName)
								onDismiss()
							}
							.border(width = 1.dp, color = MaterialTheme.colorScheme.onSurface),
						colors = ListItemDefaults.colors(
							containerColor = MaterialTheme.colorScheme.surfaceContainerLow
						),
					)
				}
			}
		}
	}
}
