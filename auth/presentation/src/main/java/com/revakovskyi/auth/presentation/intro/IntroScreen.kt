package com.revakovskyi.auth.presentation.intro

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.revakovskyi.auth.presentation.R
import com.revakovskyi.core.presentation.designsystem.LogoIcon
import com.revakovskyi.core.presentation.designsystem.components.ActionButton
import com.revakovskyi.core.presentation.designsystem.components.GradientBackground
import com.revakovskyi.core.presentation.designsystem.components.OutlinedActionButton

@Composable
fun IntroScreenRoute(
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
) {
    IntroScreen(
        onAction = { action ->
            when (action) {
                IntroAction.OnSignInClick -> onSignInClick()
                IntroAction.OnSignUpClick -> onSignUpClick()
            }
        }
    )
}


@Composable
fun IntroScreen(
    onAction: (action: IntroAction) -> Unit,
) {

    GradientBackground {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            LogoVertical()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = stringResource(R.string.welcome_to_the_running_tracker),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.app_description),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedActionButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.sign_in),
                isLoading = false,
                onClick = { onAction(IntroAction.OnSignInClick) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ActionButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.sign_up),
                isLoading = false,
                onClick = { onAction(IntroAction.OnSignUpClick) }
            )

        }

    }

}


@Composable
private fun LogoVertical(
    modifier: Modifier = Modifier,
) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = LogoIcon,
            contentDescription = stringResource(R.string.app_logo),
            tint = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.running_tracker),
            style = MaterialTheme.typography.headlineLarge
        )

    }

}
