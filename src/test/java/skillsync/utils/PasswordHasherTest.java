package skillsync.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {
    @Test void hashesAndVerifiesPasswords() {
        String encoded = PasswordHasher.hash("correct-password".toCharArray());
        assertTrue(PasswordHasher.matches("correct-password".toCharArray(), encoded));
        assertFalse(PasswordHasher.matches("wrong-password".toCharArray(), encoded));
    }
}
