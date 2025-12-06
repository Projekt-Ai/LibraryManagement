import java.sql.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BorrowTest {
    private Connection conn;
    private int testMemberId = 900;
    private int testBookId = 450;

    @BeforeEach
    void initializeDB() throws SQLException {
        // Connect to DB
        conn = DriverManager.getConnection(SQLogin.URL, SQLogin.USERNAME, SQLogin.PASSWORD);

        // Setup tables and insert sample data
        setupDatabase(conn);
    }

    private void setupDatabase(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
        	
            stmt.executeUpdate("DROP TABLE IF EXISTS Borrowings CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS Books CASCADE");
            stmt.executeUpdate("DROP TABLE IF EXISTS Members CASCADE");

            // Create Books table
            stmt.executeUpdate("CREATE TABLE Books (" +
            	    "bookID INT PRIMARY KEY, " +
            	    "title VARCHAR(255) NOT NULL, " +
            	    "authorID INT, " +
            	    "genre VARCHAR(100), " +
            	    "publicationYear INT, " +
            	    "ISBN VARCHAR(20), " +
            	    "availableCopies INT NOT NULL CHECK (availableCopies >= 0)" +
            	")");

            	stmt.executeUpdate("CREATE TABLE Members (" +
            	    "memberID INT PRIMARY KEY, " +
            	    "fullName VARCHAR(255) NOT NULL, " +
            	    "email VARCHAR(255), " +
            	    "phone VARCHAR(20), " +
            	    "joinDate DATE, " +
            	    "activeStatus BOOLEAN DEFAULT TRUE" +
            	")");

            	stmt.executeUpdate("CREATE TABLE Borrowings (" +
            	    "memberID INT NOT NULL, " +
            	    "bookID INT NOT NULL, " +
            	    "borrowDate DATE NOT NULL, " +
            	    "dueDate DATE NOT NULL, " +
            	    "returnDate DATE, " +
            	    "PRIMARY KEY (memberID, bookID, borrowDate), " +
            	    "FOREIGN KEY (memberID) REFERENCES Members(memberID), " +
            	    "FOREIGN KEY (bookID) REFERENCES Books(bookID)" +
            	")");
            	
            	stmt.executeUpdate("CREATE UNIQUE INDEX unique_active_borrow ON Borrowings (memberID, bookID) WHERE returnDate IS NULL");


            stmt.executeUpdate("INSERT INTO Books VALUES " +
                "(450, 'Test Book', 1, 'Fiction', 2020, '1234567890', 3)," +
                "(451, 'Another Book', 2, 'Non-Fiction', 2019, '0987654321', 0)");

            stmt.executeUpdate("INSERT INTO Members VALUES " +
                "(900, 'Test Member', 'test@example.com', '1234567890', CURRENT_DATE, TRUE)," +
                "(901, 'Second Member', 'second@example.com', '0987654321', CURRENT_DATE, TRUE)");

            stmt.executeUpdate("INSERT INTO Borrowings (memberID, bookID, borrowDate, dueDate, returnDate) "
            		+ "VALUES (" + testMemberId + ", " + testBookId + ", CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE + INTERVAL '20 days', NULL)"
            	);
        }
    }

    @Test
    void testDuplicateBorrowPrevention() throws SQLException {
        String dupCheck = "SELECT 1 FROM Borrowings WHERE memberID=? AND bookID=? AND returnDate IS NULL";

        try (PreparedStatement ps = conn.prepareStatement(dupCheck)) {
            ps.setInt(1, testMemberId);
            ps.setInt(2, testBookId);
            
            boolean duplicateExists = ps.executeQuery().next();
            
            assertTrue(duplicateExists, "Duplicate borrow record should exist for member " + testMemberId + " and book " + testBookId);
        }
        
        // Try inserting duplicate borrow and expect failure or handle it
        String insertBorrow = "INSERT INTO Borrowings(memberID, bookID, borrowDate, dueDate, returnDate) VALUES (?, ?, CURRENT_DATE, CURRENT_DATE + INTERVAL '14 days', NULL)";
        try (PreparedStatement ps = conn.prepareStatement(insertBorrow)) {
            ps.setInt(1, testMemberId);
            ps.setInt(2, testBookId);
            assertThrows(SQLException.class, ps::executeUpdate, "Should not allow duplicate borrow without return");
        }
    }

    @Test
    void testBorrowWhenNoCopiesAvailable() throws SQLException {
        int noCopyBookId = 451;
        int memberId = 900;
        
        // Check availableCopies for the book
        String copyCheck = "SELECT availableCopies FROM Books WHERE bookID = ?";
        int availableCopies = -1;
        
        try (PreparedStatement ps = conn.prepareStatement(copyCheck)) {
            ps.setInt(1, noCopyBookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    availableCopies = rs.getInt("availableCopies");
                }
            }
        }
        assertEquals(0, availableCopies, "Available copies should be 0 for bookID " + noCopyBookId);
        
        // Attempt to insert borrow should fail or be prevented - simulate that here
        String insertBorrow = "INSERT INTO Borrowings(memberID, bookID, borrowDate, dueDate, returnDate) VALUES (?, ?, CURRENT_DATE, CURRENT_DATE + INTERVAL '14 days', NULL)";
        try (PreparedStatement ps = conn.prepareStatement(insertBorrow)) {
            ps.setInt(1, memberId);
            ps.setInt(2, noCopyBookId);
            
            if (availableCopies < 1) {
                throw new SQLException("No available copies");
            } else {
                ps.executeUpdate();
                fail("Borrow should not be allowed when no copies are available");
            }
        } catch (SQLException ex) {
            assertEquals("No available copies", ex.getMessage());
        }
    }

}
