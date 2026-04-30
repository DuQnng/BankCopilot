package com.idea;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class BankCopilotApplicationTests {

    @Test
    void applicationClassCanBeCreatedWithoutStartingExternalServices() {
        assertDoesNotThrow(BankCopilotApplication::new);
    }
}
