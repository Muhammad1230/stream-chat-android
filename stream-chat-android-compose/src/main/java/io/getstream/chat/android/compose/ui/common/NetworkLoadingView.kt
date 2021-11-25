package io.getstream.chat.android.compose.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents the default network loading view for the header, in case the network is down.
 *
 * @param modifier Styling for the [Row].
 * @param spinnerSize The size of the spinner.
 * @param textStyle The text style of the inner text.
 * @param textColor The text color of the inner text.
 */
@Composable
public fun NetworkLoadingView(
    modifier: Modifier = Modifier,
    spinnerSize: Dp = 18.dp,
    textStyle: TextStyle = ChatTheme.typography.title3Bold,
    textColor: Color = ChatTheme.colors.textHighEmphasis,
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(spinnerSize),
            strokeWidth = 2.dp,
            color = ChatTheme.colors.primaryAccent,
        )

        Text(
            text = stringResource(id = R.string.stream_compose_waiting_for_network),
            style = textStyle,
            color = textColor,
        )
    }
}
