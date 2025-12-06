/**
 * Contains constants for database connection parameters.
 * <p>
 * Used to centralize the PostgreSQL JDBC connection URL, username, and password.
 * </p>
 */
public class SQLogin {
    /** JDBC connection URL for the PostgreSQL database with schema set to librarysystem. */
    public static final String URL = "jdbc:postgresql://localhost:5432/postgres?currentSchema=librarysystem";

    /** Username for connecting to the database. */
    public static final String USERNAME = "postgres";

    /** Password for connecting to the database. */
    public static final String PASSWORD = "GScholar3703";
}
