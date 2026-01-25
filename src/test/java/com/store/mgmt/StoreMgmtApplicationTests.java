package com.store.mgmt;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@org.junit.jupiter.api.Disabled("Requires PostgreSQL for JSONB support - enable with Testcontainers")
class StoreMgmtApplicationTests {

	@Test
	void contextLoads() {
	}

}
