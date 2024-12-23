package com.revakovskyi.core.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * A JUnit 5 extension to manage coroutine test dispatchers for unit tests.
 *
 * This extension sets a test dispatcher as the main dispatcher before each test and resets it
 * after all tests have run. It helps in controlling coroutine execution during testing,
 * making it deterministic and allowing the use of test dispatchers like `UnconfinedTestDispatcher`.
 *
 * @property testDispatcher The test dispatcher to be used as the main dispatcher.
 * Defaults to `UnconfinedTestDispatcher`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainCoroutineExtension(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : BeforeEachCallback, AfterAllCallback {

    override fun beforeEach(context: ExtensionContext?) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun afterAll(context: ExtensionContext?) {
        Dispatchers.resetMain()
    }

}