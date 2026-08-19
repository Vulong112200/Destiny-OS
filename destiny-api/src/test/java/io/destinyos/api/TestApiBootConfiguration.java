package io.destinyos.api;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The nearest {@code @SpringBootConfiguration} above every {@code @WebMvcTest}
 * in this module, since {@code destiny-api} is a library module with no
 * {@code @SpringBootApplication} of its own — that only exists in
 * {@code destiny-app}, where concrete engines are assembled. Spring Boot's
 * test bootstrapper looks for this starting from the test class's own
 * package and walking upward, which is why this sits at the module's test
 * root ({@code io.destinyos.api}) rather than inside {@code controller}.
 *
 * <p>Must be {@code @SpringBootApplication}, not the bare
 * {@code @SpringBootConfiguration + @EnableAutoConfiguration} pair — a
 * {@code @WebMvcTest} slice discovers its controller only through
 * component-scanning from this class's package downward, and only
 * {@code @SpringBootApplication} carries the implicit {@code @ComponentScan}
 * that makes that scanning happen.
 */
@SpringBootApplication
public class TestApiBootConfiguration {
}
