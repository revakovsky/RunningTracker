@file:OptIn(ExperimentalFoundationApi::class)

package com.revakovskyi.auth.presentation.signUp

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import com.revakovskyi.auth.domain.UserDataValidator
import com.revakovskyi.auth.presentation.R
import com.revakovskyi.core.peresentation.ui.ObserveAsEvents
import com.revakovskyi.core.peresentation.ui.rememberImeState
import com.revakovskyi.core.presentation.designsystem.CheckIcon
import com.revakovskyi.core.presentation.designsystem.CrossIcon
import com.revakovskyi.core.presentation.designsystem.EmailIcon
import com.revakovskyi.core.presentation.designsystem.Poppins
import com.revakovskyi.core.presentation.designsystem.TrackerDarkRed
import com.revakovskyi.core.presentation.designsystem.TrackerGreen
import com.revakovskyi.core.presentation.designsystem.components.ActionButton
import com.revakovskyi.core.presentation.designsystem.components.GradientBackground
import com.revakovskyi.core.presentation.designsystem.components.TrackerPasswordTextField
import com.revakovskyi.core.presentation.designsystem.components.TrackerTextField
import org.koin.androidx.compose.koinViewModel

private const val TAG = "clickable_text"

@Composable
fun SignUpScreenRoot(
    viewModel: SignUpViewModel = koinViewModel(),
    onSignInClick: () -> Unit,
    onSuccessfulRegistration: () -> Unit,
) {
    val context = LocalContext.current
    val keyBoardController = LocalSoftwareKeyboardController.current

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is SignUpEvent.Error -> {
                Toast.makeText(context, event.error.asString(context), Toast.LENGTH_LONG).show()
            }

            SignUpEvent.RegistrationSuccess -> {
                Toast.makeText(context, R.string.registration_successful, Toast.LENGTH_LONG).show()
                onSuccessfulRegistration()
            }
        }
    }

    SignUpScreen(
        state = viewModel.state,
        onAction = { action ->
            keyBoardController?.hide()

            when (action) {
                SignUpAction.OnSignInClick -> onSignInClick()
                else -> Unit
            }
            viewModel.onAction(action)
        }
    )

}


@Composable
private fun SignUpScreen(
    state: SignUpState,
    onAction: (SignUpAction) -> Unit,
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
                text = stringResource(R.string.create_an_account),
                style = MaterialTheme.typography.headlineMedium
            )

            val annotatedString = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontFamily = Poppins,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    append(stringResource(R.string.already_have_an_account) + "  ")
                    pushStringAnnotation(tag = TAG, annotation = stringResource(R.string.sign_in))

                    withStyle(
                        style = SpanStyle(
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        append(stringResource(R.string.sign_in))
                    }
                }
            }

            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(
                        tag = TAG,
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let {
                        onAction(SignUpAction.OnSignInClick)
                    }
                }
            )

            Spacer(modifier = Modifier.height(48.dp))

            TrackerTextField(
                state = state.email,
                startIcon = EmailIcon,
                endIcon = if (state.isValidEmail) CheckIcon else null,
                hint = stringResource(R.string.example_email),
                title = stringResource(R.string.email),
                additionalInfo = stringResource(R.string.must_be_a_valid_email),
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
                onTogglePasswordVisibility = { onAction(SignUpAction.OnTogglePasswordVisibilityClick) },
                onConfirm = { localFocusManager.clearFocus() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordRequirement(
                text = stringResource(
                    R.string.at_least_x_characters,
                    UserDataValidator.MIN_PASSWORD_LENGTH
                ),
                isValid = state.passwordValidationState.hasMinLength
            )

            Spacer(modifier = Modifier.height(4.dp))

            PasswordRequirement(
                text = stringResource(R.string.at_least_one_number),
                isValid = state.passwordValidationState.hasNumber
            )

            Spacer(modifier = Modifier.height(4.dp))

            PasswordRequirement(
                text = stringResource(R.string.contains_a_lower_character),
                isValid = state.passwordValidationState.hasLowerCaseCharacter
            )

            Spacer(modifier = Modifier.height(4.dp))

            PasswordRequirement(
                text = stringResource(R.string.contains_an_upper_case_character),
                isValid = state.passwordValidationState.hasUpperCaseCharacter
            )

            Spacer(modifier = Modifier.height(48.dp))

            ActionButton(
                text = stringResource(R.string.register),
                isLoading = state.isRegistering,
                enabled = state.canRegister,
                bringIntoViewRequester = bringIntoButtonViewRequester,
                onClick = { onAction(SignUpAction.OnRegisterClick) },
            )

        }

    }

}


@Composable
private fun PasswordRequirement(
    text: String,
    isValid: Boolean,
    modifier: Modifier = Modifier,
) {

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = if (isValid) CheckIcon else CrossIcon,
            contentDescription = null,
            tint = if (isValid) TrackerGreen else TrackerDarkRed
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium
        )

    }

}
