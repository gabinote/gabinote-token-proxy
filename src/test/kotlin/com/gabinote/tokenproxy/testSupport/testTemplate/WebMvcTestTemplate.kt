package com.gabinote.tokenproxy.testSupport.testTemplate


import com.gabinote.coffeenote.testSupport.testConfig.jackson.UseJackson
import com.gabinote.tokenproxy.testSupport.testUtil.time.TestTimeProvider
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext

@UseJackson
@AutoConfigureMockMvc
@EnableAspectJAutoProxy
@AutoConfigureRestDocs
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(
    WebMvcTestTemplate.FilterConfig::class,
    TestTimeProvider::class,
)
@ExtendWith(MockKExtension::class)
abstract class WebMvcTestTemplate : DescribeSpec() {

    @TestConfiguration
    class FilterConfig

}