import java.util.List;

/**
 * Dao.java
 * Generic Data Access Object contract implemented by every concrete
 * DAO class (UserDao, QuizDao, QuestionDao, ...). Using a generic
 * interface here means the same two method signatures - save(T) and
 * findAll() - are reused across every entity type, with each DAO
 * supplying its own JDBC implementation (an example of interfaces +
 * generics + polymorphism working together: code that holds a
 * `Dao<Quiz>` reference does not need to know whether it is really
 * talking to a QuizDao or a mock implementation).
 *
 * @param <T> the entity type this DAO manages (Quiz, User, Result, ...)
 */
public interface Dao<T> {
    /** Inserts or updates one record. */
    void save(T item) throws DataAccessException;

    /** Returns every record of this type. */
    List<T> findAll() throws DataAccessException;
}
