/**
 * AuthenticationException.java
 * Checked exception thrown when a login attempt fails (unknown
 * username, or username/password mismatch). Being a checked
 * exception, every caller is forced by the compiler to either catch
 * it or declare it - appropriate for a security-relevant failure
 * that must never be silently ignored.
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}
