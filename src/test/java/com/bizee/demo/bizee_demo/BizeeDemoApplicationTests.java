package com.bizee.demo.bizee_demo;

import com.bizee.demo.bizee_demo.support.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class BizeeDemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
