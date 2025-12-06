import java.awt.*;
import java.sql.*;
import javax.swing.*;

/**
 * Main window and controller for the Library Management System GUI.
 * 
 * This class manages the database connection, user interface setup,
 * and navigation between different panels such as catalogue search,
 * borrowing history, payments, and administrative functions.
 * 
 * @author Kailani Thomas
 */
public class LibraryManager extends JFrame {
    private static final long serialVersionUID = 1L;
    
    /** Database connection instance */
    Connection conn;
    
    /** Layout manager for switching between views */
    private CardLayout cardLayout;
    
    /** Main container panel holding different views */
    private JPanel main;
    
    /** Label displaying welcome message */
    private JLabel welcome;
    
    /** Current logged-in member ID */
    private int memberId;
    
    /**
     * Constructs the LibraryManager frame.
     * Initializes the database connection and user interface.
     * 
     * @param memberName the name of the logged-in user to display in the welcome message
     * @param isAdmin flag indicating whether the user has administrator privileges
     * @param memberId the ID of the logged-in member
     */
    public LibraryManager(String memberName, boolean isAdmin, int memberId) {
        this.memberId = memberId;
        InitializeDB();
        SetupUI(memberName, isAdmin);
    }
    
    /**
     * Initializes the PostgreSQL database connection.
     * Prints stack trace on failure.
     */
    private void InitializeDB() {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(SQLogin.URL, SQLogin.USERNAME, SQLogin.PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sets up the main user interface, including navigation buttons and different panels.
     * Adds different views to the card layout and sets action listeners for navigation.
     * 
     * @param memberName the name to show in the welcome label
     * @param isAdmin whether the user has admin privileges to show admin buttons
     */
    public void SetupUI(String memberName, boolean isAdmin) {
        setTitle("Library Management System");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        cardLayout = new CardLayout();
        main = new JPanel(cardLayout);
        setVisible(true);
        
        JPanel home = new JPanel(new BorderLayout());
        welcome = new JLabel("Welcome, " + memberName + "!");
        welcome.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        home.add(welcome, BorderLayout.NORTH);
        
        getContentPane().add(main);
        
        JPanel container = new JPanel(new GridBagLayout());
        home.add(container, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        JButton librarySearch = new JButton("Library Catalogue");
        JButton adminSearch = new JButton("Search Catalogue");
        JButton historyButton = new JButton("Borrowing History");
        JButton payFine = new JButton("Pay a Fine");
        JButton returnBook = new JButton("Return a Book");
        
        JButton newMember = new JButton("Add Member");
        JButton newBook = new JButton("Add Books");
        
        if (!isAdmin) {
            buttonPanel.add(librarySearch);
            buttonPanel.add(historyButton);
            buttonPanel.add(payFine);
            buttonPanel.add(returnBook);
        }
        
        if (isAdmin) {
            buttonPanel.add(adminSearch);
            buttonPanel.add(historyButton);
            buttonPanel.add(newMember);
            buttonPanel.add(newBook);
        }
        
        main.add(home, "home");
        main.add(new LibraryCatalogue(conn, cardLayout, main, memberId), "library");
        main.add(new BorrowHistory(conn, cardLayout, main, isAdmin, memberId), "borrowing");
        main.add(new Payment(conn, main, cardLayout, memberId), "payment");
        main.add(new BookReturn(conn, main, cardLayout), "return");
        
        main.add(new AdminSearch(conn, cardLayout, main), "search");
        main.add(new AddMember(conn, main, cardLayout), "addMem");
        main.add(new AddBooks(conn, main, cardLayout), "addBook");
        
        container.add(buttonPanel, new GridBagConstraints());
        
        librarySearch.addActionListener(e -> cardLayout.show(main, "library"));
        historyButton.addActionListener(e -> cardLayout.show(main, "borrowing"));
        payFine.addActionListener(e -> cardLayout.show(main, "payment"));
        returnBook.addActionListener(e -> cardLayout.show(main, "return"));
        
        adminSearch.addActionListener(e -> cardLayout.show(main, "search"));
        newMember.addActionListener(e -> cardLayout.show(main, "addMem"));
        newBook.addActionListener(e -> cardLayout.show(main, "addBook"));
        
        cardLayout.show(main, "home");
        setVisible(true);
    }
    
    /**
     * Main entry point of the application.
     * Launches the LibraryManager GUI with a guest user.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryManager("Guest", false, 210));
    }
}
