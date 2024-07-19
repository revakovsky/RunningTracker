@file:OptIn(ExperimentalFoundationApi::class)

package com.revakovskyi.auth.presentation.signIn

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.revakovskyi.auth.presentation.R
import com.revakovskyi.core.peresentation.ui.ObserveAsEvents
import com.revakovskyi.core.peresentation.ui.rememberImeState
import com.revakovskyi.core.presentation.designsystem.EmailIcon
import com.revakovskyi.core.presentation.designsystem.Poppins
import com.revakovskyi.core.presentation.designsystem.components.ActionButton
import com.revakovskyi.core.presentation.designsystem.components.GradientBackground
import com.revakovskyi.core.presentation.designsystem.components.TrackerPasswordTextField
import com.revakovskyi.core.presentation.designsystem.components.TrackerTextField
import org.koin.androidx.compose.koinViewModel

private const val TAG = "clickable_text"

@Composable
fun SignInScreenRoot(
    viewModel: SignInViewModel = koinViewModel(),
    onSuccessfulSignIn: () -> Unit,
    onSignUpClick: () -> Unit,
) {
    val context = LocalContext.current
    val keyBoardController = LocalSoftwareKeyboardController.current

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is SignInEvent.Error -> {
                Toast.makeText(context, event.error.asString(context), Toast.LENGTH_LONG).show()
            }

            SignInEvent.SignInSuccess -> {
                Toast.makeText(context, R.string.successfully_signed_in, Toast.LENGTH_LONG).show()
                onSuccessfulSignIn()
            }
        }
    }

    SignInScreen(
        state = viewModel.state,
        onAction = { action ->
            keyBoardController?.hide()

            when (action) {
                SignInAction.OnSignUpClick -> onSignUpClick()
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )

}


@Composable
private fun SignInScreen(
    state: SignInState,
    onAction: (action: SignInAction) -> Unit,
) {
    val localFocusManager = LocalFocusManager.current
    val bringIntoButtonViewRequester = remember { BringIntoViewRequester() }
    val imeStateOpen by rememberImeState()

    LaunchedEffect(imeStateOpen) {
        if (!imeStateOpen) localFocusManager.clearFocus()
    }

    GradientBackground {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .padding(top = 16.dp)
        ) {

            Text(
                text = stringResource(R.string.hi_there),
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.welcome_to_your_ultimate_running_companion),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(48.dp))

            TrackerTextField(
                state = state.email,
                startIcon = EmailIcon,
                endIcon = null,
                hint = stringResource(R.string.example_email),
                title = stringResource(R.string.email),
                keyboardType = KeyboardType.Email,
                onNextClick = { localFocusManager.moveFocus(FocusDirection.Down) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TrackerPasswordTextField(
                state = state.password,
                isPasswordVisible = state.isPasswordVisible,
                hint = stringResource(R.string.password),
                title = stringResource(R.string.password),
                bringIntoViewRequester = bringIntoButtonViewRequester,
                onTogglePasswordVisibility = { onAction(SignInAction.OnTogglePasswordVisibility) },
                onConfirm = { localFocusManager.clearFocus() }
            )

            Spacer(modifier = Modifier.height(48.dp))

            ActionButton(
                text = stringResource(R.string.sign_in),
                isLoading = state.isSigningIn,
                enabled = state.canSignIn && !state.isSigningIn,
                bringIntoViewRequester = bringIntoButtonViewRequester,
                onClick = { onAction(SignInAction.OnSignInClick) },
            )

            val annotatedString = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontFamily = Poppins,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    append(stringResource(R.string.do_not_have_an_account) + "  ")
                    pushStringAnnotation(tag = TAG, annotation = stringResource(R.string.sign_up))

                    withStyle(
                        style = SpanStyle(
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        append(stringResource(R.string.sign_up))
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {

                ClickableText(
                    text = annotatedString,
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(
                            tag = TAG,
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let {
                            onAction(SignInAction.OnSignUpClick)
                        }
                    }
                )

            }

        }

    }

}
