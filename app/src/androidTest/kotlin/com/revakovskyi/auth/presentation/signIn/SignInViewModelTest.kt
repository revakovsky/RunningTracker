package com.revakovskyi.auth.presentation.signIn

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.revakovskyi.auth.data.AuthRepositoryImpl
import com.revakovskyi.auth.data.EmailPatternValidator
import com.revakovskyi.auth.data.SignInRequest
import com.revakovskyi.auth.domain.UserDataValidator
import com.revakovskyi.core.android_test.SessionStorageFake
import com.revakovskyi.core.android_test.TestMockEngine
import com.revakovskyi.core.android_test.loginResponseStub
import com.revakovskyi.core.data.network.HttpClientFactory
import com.revakovskyi.core.test.MainCoroutineExtension
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SignInViewModelTest {

    private lateinit var viewModel: SignInViewModel
    private lateinit var repository: AuthRepositoryImpl
    private lateinit var sessionStorageFake: SessionStorageFake
    private lateinit var mockEngine: TestMockEngine

    companion object {
        @JvmField
        @RegisterExtension
        val mainCoroutineExtension = MainCoroutineExtension()
    }


    @BeforeEach
    fun setUp() {
        sessionStorageFake = SessionStorageFake()

        val mockEngineConfig = MockEngineConfig().apply {
            requestHandlers.add { request ->
                val relativeUrl = request.url.encodedPath

                if (relativeUrl == "/login") {
                    respond(
                        content = ByteReadChannel(
                            text = Json.encodeToString(loginResponseStub)
                        ),
                        headers = headers {
                            set("Content-Type", "application/json")
                        }
                    )
                } else {
                    respond(
                        content = byteArrayOf(),
                        status = HttpStatusCode.InternalServerError,
                    )
                }
            }
        }

        mockEngine = TestMockEngine(
            dispatcher = mainCoroutineExtension.testDispatcher,
            mockEngineConfig = mockEngineConfig
        )

        val httpClient = HttpClientFactory(sessionStorage = sessionStorageFake).build(mockEngine)

        repository = AuthRepositoryImpl(
            httpClient = httpClient,
            sessionStorage = sessionStorageFake,
        )

        viewModel = SignInViewModel(
            authRepository = repository,
            userDataValidator = UserDataValidator(validator = EmailPatternValidator)
        )
    }


    @Test
    fun testSignIn() = runTest {
        val state = viewModel.state
        assertThat(state.canSignIn).isFalse()

        viewModel.setStateForTest(state.copy(email = "test@test.com", password = "Test123456"))
        viewModel.onAction(SignInAction.OnSignInClick)

        assertThat(viewModel.state.isSigningIn).isFalse()
        assertThat(viewModel.state.email).isEqualTo("test@test.com")
        assertThat(viewModel.state.password).isEqualTo("Test123456")

        val signInRequest =
            mockEngine.mockEngine.requestHistory.find { it.url.encodedPath == "/login" }
        assertThat(signInRequest).isNotNull()
        assertThat(signInRequest!!.headers.contains("x-api-key")).isTrue()

        val signInBody = Json.decodeFromString<SignInRequest>(
            signInRequest.body.toByteArray().decodeToString()
        )
        assertThat(signInBody.email).isEqualTo("test@test.com")
        assertThat(signInBody.password).isEqualTo("Test123456")

        val session = sessionStorageFake.get()
        assertThat(session?.userId).isEqualTo(loginResponseStub.userId)
        assertThat(session?.accessToken).isEqualTo(loginResponseStub.accessToken)
        assertThat(session?.refreshToken).isEqualTo(loginResponseStub.refreshToken)
    }

}