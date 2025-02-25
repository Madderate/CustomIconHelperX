package com.madderate.wukongdemo

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.madderate.wukongdemo.base.BaseActivity
import com.madderate.wukongdemo.ui.theme.WukongBasicTheme
import com.madderate.wukongdemo.ui.views.ErrorUi
import com.madderate.wukongdemo.ui.views.LoadingUi
import com.madderate.wukongdemo.viewmodel.MainViewModel
import com.madderate.wukongdemo.viewmodel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    companion object {
        const val GROUP_NAME = "MainActivity"
    }

    private val mViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WukongBasicTheme { MainContent(mViewModel) }
        }
    }

    @Composable
    private fun MainContent(vm: MainViewModel = viewModel()) {
        val uiState by vm.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        when (val current = uiState.current) {
            is UiState.Error ->
                ErrorUi(errMsg = current.errMsg)
            is UiState.Loading ->
                LoadingUi()
            is UiState.Success ->
                UserInterfaceArea(
                    userInput = current.result.searchKeyword,
                    index = current.result.index,
                    bitmap = current.result.bitmap,
                    onUiAction = vm::onUiAction,
                    onUiNav = vm::onUiNav,
                    snackbarHostState = snackbarHostState,
                    modifier = Modifier.fillMaxSize()
                )
        }
        SnackbarArea(snackbarHostState = snackbarHostState)
    }

    @Composable
    private fun UserInterfaceArea(
        userInput: String,
        index: Int,
        bitmap: Bitmap?,
        onUiAction: (MainViewModel.MainUiAction) -> Unit,
        onUiNav: (BaseActivity, MainViewModel.MainUiNav) -> Unit,
        snackbarHostState: SnackbarHostState,
        modifier: Modifier = Modifier,
        scope: CoroutineScope = rememberCoroutineScope(),
        parentArrangement: Arrangement.Vertical = Arrangement.Center,
        parentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = parentArrangement,
            horizontalAlignment = parentAlignment
        ) {
            Button(
                onClick = { onUiNav(this@MainActivity, MainViewModel.ToIconSelect) },
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(text = stringResource(id = R.string.button_to_icon_select))
            }
            UserInput(
                userInput,
                onUserInputChanged = onUiAction,
                onSearch = { content ->
                    scope.launch { snackbarHostState.showSnackbar(content) }
                }
            )
            TextAndButton(index, onIndexChange = onUiAction)
            BitmapArea(bitmap, onUiAction)
        }
    }

    @Composable
    private fun UserInput(
        input: String,
        onUserInputChanged: (MainViewModel.Search) -> Unit,
        onSearch: (String) -> Unit
    ) {
        TextField(
            value = input,
            onValueChange = { onUserInputChanged(MainViewModel.Search(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 16.dp),
            label = { Text(stringResource(R.string.user_input)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = {
                onSearch(input)
            })
        )
    }

    @Composable
    private fun TextAndButton(index: Int, onIndexChange: (MainViewModel.Increase) -> Unit) {
        SelectionContainer(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp)
        ) {
            Text(
                stringResource(R.string.value_is, index),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        Button(onClick = { onIndexChange(MainViewModel.Increase(index + 1)) }) {
            Text(stringResource(id = R.string.add))
        }
    }

    @Composable
    private fun BitmapArea(
        bitmap: Bitmap?,
        onBitmapChanged: (MainViewModel.UpdateImage) -> Unit,
    ) {
        Button(
            onClick = { onBitmapChanged(MainViewModel.UpdateImage) },
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(text = stringResource(id = R.string.image_button))
        }
        if (bitmap == null || bitmap.isRecycled) return
        Image(
            modifier = Modifier
                .size(width = 100.dp, height = 150.dp)
                .padding(vertical = 16.dp)
                .border(3.dp, Color.Cyan),
            bitmap = bitmap.asImageBitmap(),
            contentDescription = stringResource(id = R.string.image_content_description)
        )
    }

    @Composable
    private fun SnackbarArea(snackbarHostState: SnackbarHostState) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) { SnackbarHost(hostState = snackbarHostState) }
    }

    //region Previews
    @Preview(
        name = "Total Preview",
        group = GROUP_NAME,
        showSystemUi = true,
        device = Devices.PIXEL_2
    )
    @Composable
    private fun TotalPreview() {
        WukongBasicTheme {
            val snackbarHostState = remember { SnackbarHostState() }
            UserInterfaceArea(
                userInput = "你好",
                index = 32,
                bitmap = null,
                onUiAction = {},
                onUiNav = { _, _ -> },
                snackbarHostState = snackbarHostState
            )
            SnackbarArea(snackbarHostState = snackbarHostState)
        }
    }
    //endregion
}
