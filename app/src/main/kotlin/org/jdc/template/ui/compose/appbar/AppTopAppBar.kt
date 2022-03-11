package org.jdc.template.ui.compose.appbar

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jdc.template.R
import org.jdc.template.ui.compose.LocalNavController
import org.jdc.template.ui.theme.AppTheme

@Composable
internal fun AppTopAppBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: ImageVector = Icons.Filled.ArrowBack,
    onNavigationClick: (() -> Unit)? = null,
    appBarTextColor: Color? = null,
    appBarBackgroundColor: Color? = null,
    autoSizeTitle: Boolean = false,
    actions: @Composable (RowScope.() -> Unit)? = null,
) {
    AppTopAppBar(
        title = {
            AppBarTitle(
                title = title,
                subtitle = subtitle,
                color = appBarTextColor ?: Color.Unspecified,
                autoSizeTitle = autoSizeTitle
            )
        },
        navigationIcon = navigationIcon,
        onNavigationClick = onNavigationClick,
        appBarBackgroundColor = appBarBackgroundColor,
        actions = actions
    )
}

@Composable
internal fun AppTopAppBar(
    title: @Composable () -> Unit,
    navigationIcon: ImageVector = Icons.Filled.ArrowBack,
    onNavigationClick: (() -> Unit)? = null,
    appBarBackgroundColor: Color? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
) {
    val navController = LocalNavController.current
    val emptyBackStack = navController?.previousBackStackEntry == null

    TopAppBar(
        title = title,
        backgroundColor = appBarBackgroundColor ?: MaterialTheme.colors.primarySurface,
        navigationIcon = if (emptyBackStack) null else {
            {
                Icon(
                    modifier = Modifier
                        .clickable { onNavigationClick?.invoke() }
                        .padding(start = 12.dp),
                    imageVector = navigationIcon,
                    contentDescription = stringResource(id = R.string.back),
                )
            }
        },
        actions = {
            // Wrapping content so that the action icons have the same color as the navigation icon and title.
            if (actions != null) {
                CompositionLocalProvider(
                    LocalContentAlpha provides ContentAlpha.high,
                    content = { actions() }
                )
            }
        }
    )
}

@Preview(group = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Preview(group = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
private fun AboutTopAppBarPreview() {
    AppTheme {
        AppTopAppBar(
            title = "App Bar Title",
            subtitle = "Test",
            onNavigationClick = {},
            actions = {
                IconButton(onClick = { }) { Icon(imageVector = Icons.Outlined.Info, contentDescription = null) }
                IconButton(onClick = { }) { Icon(imageVector = Icons.Outlined.Settings, contentDescription = null) }
            }
        )
    }
}