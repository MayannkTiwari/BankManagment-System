package dev.mayanktiwari.bank.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class LaunchChecksTest {

    private static AppProperties props(String url, String email) {
        return new AppProperties("Bank", "Operator", email, "Delhi", url, true, "Asia/Kolkata",
                new AppProperties.Security("key", "x".repeat(32), 5, 15),
                new AppProperties.Limits(new BigDecimal("100000.00")));
    }

    @Test
    void acceptsHttpsCustomDomain() {
        assertEquals(List.of(), LaunchChecks.problems(props("https://bank.example.com", "help@example.com")));
    }

    @Test
    void rejectsPlainHttp() {
        assertFalse(LaunchChecks.problems(props("http://bank.example.com", "help@example.com")).isEmpty());
    }

    @Test
    void rejectsLocalhostAndIpAddresses() {
        assertFalse(LaunchChecks.problems(props("https://localhost:8080", "help@example.com")).isEmpty());
        assertFalse(LaunchChecks.problems(props("https://192.168.1.10", "help@example.com")).isEmpty());
    }

    @Test
    void rejectsSharedHostingDomains() {
        for (String url : new String[] {"https://mybank.onrender.com", "https://mybank.vercel.app",
                "https://mybank.herokuapp.com", "https://mybank.up.railway.app", "https://x.github.io"}) {
            List<String> problems = LaunchChecks.problems(props(url, "help@example.com"));
            assertFalse(problems.isEmpty(), url);
        }
    }

    @Test
    void rejectsBadContactEmail() {
        assertTrue(LaunchChecks.problems(props("https://bank.example.com", "not-an-email")).size() == 1);
    }
}
