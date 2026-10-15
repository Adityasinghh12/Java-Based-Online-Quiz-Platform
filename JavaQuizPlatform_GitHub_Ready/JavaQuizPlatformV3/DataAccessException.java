/**
 * DataAccessException.java
 * Unchecked exception thrown by the DAO layer when a database
 * operation fails (e.g. a SQLException from JDBC). Wrapping the raw
 * SQLException in our own exception type means the UI layer only
 * ever has to deal with one well-defined failure type, instead of
 * reacting to a low-level JDBC exception directly.
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
