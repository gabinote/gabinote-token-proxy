package com.gabinote.tokenproxy.testSupport.testTemplate


import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.annotation.DirtiesContext

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(MockKExtension::class)
abstract class ServiceTestTemplate : DescribeSpec() {
    init {
        isolationMode = IsolationMode.InstancePerTest
    }
}
