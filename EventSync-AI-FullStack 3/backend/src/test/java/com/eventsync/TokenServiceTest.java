package com.eventsync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {
    private TokenService tokens;

    @BeforeEach
    void setUp() {
        tokens = new TokenService();
        ReflectionTestUtils.setField(tokens, "secret", "test-only-signing-secret-that-is-long-enough-1234567890");
    }

    @Test
    void issuedTokenVerifiesToExpectedIdentityAndRole() {
        AppUser user = new AppUser();
        user.email = "volunteer@example.com";
        user.role = "VOLUNTEER";

        String token = tokens.issue(user);
        String[] claims = tokens.verify(token);

        assertNotNull(claims);
        assertEquals("volunteer@example.com", claims[0]);
        assertEquals("VOLUNTEER", claims[1]);
    }

    @Test
    void modifiedTokenIsRejected() {
        AppUser user = new AppUser();
        user.email = "admin@example.com";
        user.role = "ADMIN";

        String token = tokens.issue(user);
        int dot = token.indexOf('.');
        String changed = token.charAt(dot + 1) == 'A' ? "B" : "A";
        String tampered = token.substring(0, dot + 1) + changed + token.substring(dot + 2);

        assertNull(tokens.verify(tampered));
    }

    @Test
    void malformedTokenIsRejected() {
        assertNull(tokens.verify("not-a-token"));
        assertNull(tokens.verify("a.b.c"));
    }
}
