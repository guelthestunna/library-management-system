import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Vector;

/**
 * ╔══════════════════════════════════════════╗
 * ║    LIBRARY MANAGEMENT SYSTEM v1.0       ║
 * ║    Java Swing + MySQL (JDBC)            ║
 * ╚══════════════════════════════════════════╝
 *
 * HOW TO COMPILE & RUN:
 *   1. Download MySQL Connector/J from https://dev.mysql.com/downloads/connector/j/
 *   2. Place mysql-connector-j-X.X.X.jar in the same folder as this file
 *   3. Compile:  javac -cp .;mysql-connector-j-*.jar LibraryApp.java   (Windows)
 *                javac -cp .:mysql-connector-j-*.jar LibraryApp.java   (Mac/Linux)
 *   4. Run:      java  -cp .;mysql-connector-j-*.jar LibraryApp        (Windows)
 *                java  -cp .:mysql-connector-j-*.jar LibraryApp        (Mac/Linux)
 */
public class LibraryApp extends JFrame {

    // ── DB Config ────────────────────────────────────────────────────────────
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/library_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "12345678";

    // ── Theme ─────────────────────────────────────────────────────────────────
    private static final Color BG_DARK    = new Color(13,  17,  23);
    private static final Color BG_PANEL   = new Color(22,  27,  34);
    private static final Color BG_CARD    = new Color(30,  37,  46);
    private static final Color ACCENT     = new Color(0,   229, 255);
    private static final Color ACCENT2    = new Color(0,   184, 204);
    private static final Color TEXT_WHITE = new Color(230, 237, 243);
    private static final Color TEXT_GRAY  = new Color(139, 148, 158);
    private static final Color SUCCESS    = new Color(63,  185, 80);
    private static final Color DANGER     = new Color(248, 81,  73);
    private static final Color WARNING    = new Color(210, 153, 34);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  14);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 12);

    private Connection conn;
    private JPanel     contentPanel;
    private CardLayout cardLayout;

    // ── Panels ────────────────────────────────────────────────────────────────
    private BooksPanel     booksPanel;
    private MembersPanel   membersPanel;
    private CheckoutsPanel checkoutsPanel;
    private ReportsPanel   reportsPanel;

    // ── Constructor ───────────────────────────────────────────────────────────
    public LibraryApp() {
        setTitle("Library Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        if (!connectDB()) {
            JOptionPane.showMessageDialog(null,
                "Cannot connect to MySQL.\n\nMake sure:\n" +
                "• MySQL is running\n• library_db exists (run library_db.sql)\n" +
                "• mysql-connector-j-*.jar is in the same folder",
                "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        buildUI();
        setVisible(true);
    }

    // ── DB Connection ─────────────────────────────────────────────────────────
    private boolean connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── UI Builder ────────────────────────────────────────────────────────────
    private void buildUI() {
        setLayout(new BorderLayout());

        // Content must be created BEFORE sidebar so cardLayout is not null
        // when makeNavButton fires setSelected(true)
        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_DARK);

        booksPanel     = new BooksPanel();
        membersPanel   = new MembersPanel();
        checkoutsPanel = new CheckoutsPanel();
        reportsPanel   = new ReportsPanel();

        contentPanel.add(booksPanel,     "books");
        contentPanel.add(membersPanel,   "members");
        contentPanel.add(checkoutsPanel, "checkouts");
        contentPanel.add(reportsPanel,   "reports");

        // Sidebar built AFTER cardLayout is ready
        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        cardLayout.show(contentPanel, "books");
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_PANEL);
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BG_CARD));

        // Logo area
        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(BG_PANEL);
        logoPanel.setMaximumSize(new Dimension(200, 80));
        logoPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel logo = new JLabel("📚 LibraSys");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(ACCENT);
        logoPanel.add(logo, BorderLayout.CENTER);
        sidebar.add(logoPanel);

        // Divider
        sidebar.add(makeDivider());

        // Nav buttons
        String[][] navItems = {
            {"📖  Books",     "books"},
            {"👥  Members",   "members"},
            {"🔄  Checkouts", "checkouts"},
            {"📊  Reports",   "reports"},
        };

        ButtonGroup group = new ButtonGroup();
        for (String[] item : navItems) {
            JToggleButton btn = makeNavButton(item[0], item[1]);
            group.add(btn);
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        sidebar.add(Box.createVerticalGlue());

        // Footer
        JLabel footer = new JLabel("  v1.0 — Carlos San Diego");
        footer.setFont(FONT_SMALL);
        footer.setForeground(TEXT_GRAY);
        footer.setBorder(new EmptyBorder(10, 10, 15, 10));
        sidebar.add(footer);

        return sidebar;
    }

    private JToggleButton makeNavButton(String text, String card) {
        JToggleButton btn = new JToggleButton(text);
        btn.setFont(FONT_BODY);
        btn.setForeground(TEXT_GRAY);
        btn.setBackground(BG_PANEL);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 20, 12, 20));
        btn.setMaximumSize(new Dimension(200, 45));
        btn.setMinimumSize(new Dimension(200, 45));

        btn.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                btn.setForeground(ACCENT);
                btn.setBackground(new Color(0, 229, 255, 20));
                if (cardLayout != null && contentPanel != null) {
                    cardLayout.show(contentPanel, card);
                    refreshPanel(card);
                }
            } else {
                btn.setForeground(TEXT_GRAY);
                btn.setBackground(BG_PANEL);
            }
        });

        if (card.equals("books")) btn.setSelected(true);
        return btn;
    }

    private void refreshPanel(String card) {
        switch (card) {
            case "books":     booksPanel.loadTable();     break;
            case "members":   membersPanel.loadTable();   break;
            case "checkouts": checkoutsPanel.loadTable(); break;
            case "reports":   reportsPanel.refresh();     break;
        }
    }

    private JSeparator makeDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(BG_CARD);
        sep.setBackground(BG_CARD);
        sep.setMaximumSize(new Dimension(200, 1));
        return sep;
    }

    // ── Shared Helpers ────────────────────────────────────────────────────────
    private JPanel makeHeader(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(24, 28, 16, 28));
        JLabel t = new JLabel(title);
        t.setFont(FONT_TITLE);
        t.setForeground(TEXT_WHITE);
        JLabel s = new JLabel(subtitle);
        s.setFont(FONT_SMALL);
        s.setForeground(TEXT_GRAY);
        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBackground(BG_DARK);
        text.add(t);
        text.add(Box.createRigidArea(new Dimension(0, 2)));
        text.add(s);
        p.add(text, BorderLayout.CENTER);
        return p;
    }

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    private JTextField makeField(int cols) {
        JTextField f = new JTextField(cols);
        f.setBackground(BG_CARD);
        f.setForeground(TEXT_WHITE);
        f.setCaretColor(ACCENT);
        f.setFont(FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(48, 54, 61)),
            new EmptyBorder(6, 10, 6, 10)));
        return f;
    }

    private JComboBox<String> makeCombo(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(BG_CARD);
        cb.setForeground(TEXT_WHITE);
        cb.setFont(FONT_BODY);
        ((JLabel) cb.getRenderer()).setBackground(BG_CARD);
        return cb;
    }

    private JTable makeTable(String[] cols) {
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setBackground(BG_PANEL);
        table.setForeground(TEXT_WHITE);
        table.setFont(FONT_BODY);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0, 229, 255, 40));
        table.setSelectionForeground(TEXT_WHITE);
        table.getTableHeader().setBackground(BG_CARD);
        table.getTableHeader().setForeground(TEXT_GRAY);
        table.getTableHeader().setFont(FONT_SMALL);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BG_DARK));
        return table;
    }

    private JScrollPane makeScroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(BG_PANEL);
        sp.getViewport().setBackground(BG_PANEL);
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY);
        l.setForeground(TEXT_GRAY);
        return l;
    }

    // ── BOOKS PANEL ───────────────────────────────────────────────────────────
    class BooksPanel extends JPanel {
        private JTable     table;
        private JTextField searchField;

        BooksPanel() {
            setLayout(new BorderLayout());
            setBackground(BG_DARK);

            JPanel header = makeHeader("📖 Books", "Manage your book inventory");

            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            toolbar.setBackground(BG_DARK);
            toolbar.setBorder(new EmptyBorder(0, 28, 12, 28));
            searchField = makeField(20);
            searchField.putClientProperty("JTextField.placeholderText", "Search title or author...");
            JButton searchBtn = makeButton("Search", ACCENT2);
            JButton addBtn    = makeButton("+ Add Book", SUCCESS);
            JButton editBtn   = makeButton("Edit", WARNING);
            JButton deleteBtn = makeButton("Delete", DANGER);
            JButton clearBtn  = makeButton("Show All", BG_CARD);

            toolbar.add(searchField);
            toolbar.add(searchBtn);
            toolbar.add(Box.createHorizontalStrut(12));
            toolbar.add(addBtn);
            toolbar.add(editBtn);
            toolbar.add(deleteBtn);
            toolbar.add(clearBtn);

            String[] cols = {"ID", "Title", "Author", "Genre", "ISBN", "Total", "Available", "Added"};
            table = makeTable(cols);
            table.getColumnModel().getColumn(0).setMaxWidth(50);
            table.getColumnModel().getColumn(5).setMaxWidth(60);
            table.getColumnModel().getColumn(6).setMaxWidth(80);

            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.setBackground(BG_DARK);
            tablePanel.setBorder(new EmptyBorder(0, 28, 28, 28));
            tablePanel.add(makeScroll(table), BorderLayout.CENTER);

            JPanel top = new JPanel(new BorderLayout());
            top.setBackground(BG_DARK);
            top.add(header, BorderLayout.NORTH);
            top.add(toolbar, BorderLayout.SOUTH);
            add(top, BorderLayout.NORTH);
            add(tablePanel, BorderLayout.CENTER);

            loadTable();

            // Actions
            searchBtn.addActionListener(e -> searchBooks(searchField.getText()));
            clearBtn.addActionListener(e -> { searchField.setText(""); loadTable(); });
            addBtn.addActionListener(e -> showBookDialog(null));
            editBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) { showMsg("Select a book to edit.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
                showBookDialog(row);
            });
            deleteBtn.addActionListener(e -> deleteBook());
        }

        void loadTable() {
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            m.setRowCount(0);
            try {
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM books ORDER BY title");
                while (rs.next()) {
                    int avail = rs.getInt("available_copies");
                    m.addRow(new Object[]{
                        rs.getInt("book_id"), rs.getString("title"), rs.getString("author"),
                        rs.getString("genre"), rs.getString("isbn"),
                        rs.getInt("total_copies"), avail, rs.getString("added_date")
                    });
                }
            } catch (SQLException ex) { showError(ex); }
        }

        void searchBooks(String q) {
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            m.setRowCount(0);
            try {
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? ORDER BY title");
                ps.setString(1, "%" + q + "%");
                ps.setString(2, "%" + q + "%");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    m.addRow(new Object[]{
                        rs.getInt("book_id"), rs.getString("title"), rs.getString("author"),
                        rs.getString("genre"), rs.getString("isbn"),
                        rs.getInt("total_copies"), rs.getInt("available_copies"), rs.getString("added_date")
                    });
                }
            } catch (SQLException ex) { showError(ex); }
        }

        void showBookDialog(Integer row) {
            JDialog dlg = new JDialog(LibraryApp.this, row == null ? "Add Book" : "Edit Book", true);
            dlg.setSize(420, 380);
            dlg.setLocationRelativeTo(LibraryApp.this);
            dlg.getContentPane().setBackground(BG_PANEL);

            JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
            form.setBackground(BG_PANEL);
            form.setBorder(new EmptyBorder(20, 20, 10, 20));

            JTextField tfTitle  = makeField(15);
            JTextField tfAuthor = makeField(15);
            JTextField tfGenre  = makeField(15);
            JTextField tfISBN   = makeField(15);
            JTextField tfTotal  = makeField(5);

            int bookId = -1;
            if (row != null) {
                bookId = (int) table.getValueAt(row, 0);
                tfTitle.setText((String) table.getValueAt(row, 1));
                tfAuthor.setText((String) table.getValueAt(row, 2));
                tfGenre.setText((String) table.getValueAt(row, 3));
                tfISBN.setText((String) table.getValueAt(row, 4));
                tfTotal.setText(String.valueOf(table.getValueAt(row, 5)));
            }

            form.add(makeLabel("Title *"));        form.add(tfTitle);
            form.add(makeLabel("Author *"));       form.add(tfAuthor);
            form.add(makeLabel("Genre"));          form.add(tfGenre);
            form.add(makeLabel("ISBN"));           form.add(tfISBN);
            form.add(makeLabel("Total Copies *")); form.add(tfTotal);

            JButton saveBtn   = makeButton(row == null ? "Add Book" : "Save Changes", SUCCESS);
            JButton cancelBtn = makeButton("Cancel", BG_CARD);
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnPanel.setBackground(BG_PANEL);
            btnPanel.add(cancelBtn); btnPanel.add(saveBtn);

            dlg.add(form, BorderLayout.CENTER);
            dlg.add(btnPanel, BorderLayout.SOUTH);

            final int fBookId = bookId;
            saveBtn.addActionListener(e -> {
                String title  = tfTitle.getText().trim();
                String author = tfAuthor.getText().trim();
                if (title.isEmpty() || author.isEmpty() || tfTotal.getText().trim().isEmpty()) {
                    showMsg("Title, Author, and Total Copies are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int total;
                try { total = Integer.parseInt(tfTotal.getText().trim()); }
                catch (NumberFormatException ex) { showMsg("Total copies must be a number.", "Error", JOptionPane.ERROR_MESSAGE); return; }

                try {
                    if (fBookId == -1) {
                        PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO books (title,author,genre,isbn,total_copies,available_copies,added_date) VALUES (?,?,?,?,?,?,?)");
                        ps.setString(1, title); ps.setString(2, author);
                        ps.setString(3, tfGenre.getText().trim()); ps.setString(4, tfISBN.getText().trim());
                        ps.setInt(5, total); ps.setInt(6, total);
                        ps.setString(7, LocalDate.now().toString());
                        ps.executeUpdate();
                    } else {
                        PreparedStatement ps = conn.prepareStatement(
                            "UPDATE books SET title=?,author=?,genre=?,isbn=?,total_copies=? WHERE book_id=?");
                        ps.setString(1, title); ps.setString(2, author);
                        ps.setString(3, tfGenre.getText().trim()); ps.setString(4, tfISBN.getText().trim());
                        ps.setInt(5, total); ps.setInt(6, fBookId);
                        ps.executeUpdate();
                    }
                    dlg.dispose();
                    loadTable();
                } catch (SQLException ex) { showError(ex); }
            });
            cancelBtn.addActionListener(e -> dlg.dispose());
            dlg.setVisible(true);
        }

        void deleteBook() {
            int row = table.getSelectedRow();
            if (row < 0) { showMsg("Select a book to delete.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
            int id    = (int) table.getValueAt(row, 0);
            String title = (String) table.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(LibraryApp.this,
                "Delete \"" + title + "\"? This cannot be undone.", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
            try {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE book_id=?");
                ps.setInt(1, id);
                ps.executeUpdate();
                loadTable();
            } catch (SQLException ex) { showError(ex); }
        }
    }

    // ── MEMBERS PANEL ─────────────────────────────────────────────────────────
    class MembersPanel extends JPanel {
        private JTable     table;
        private JTextField searchField;

        MembersPanel() {
            setLayout(new BorderLayout());
            setBackground(BG_DARK);

            JPanel header = makeHeader("👥 Members", "Manage library members");

            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            toolbar.setBackground(BG_DARK);
            toolbar.setBorder(new EmptyBorder(0, 28, 12, 28));
            searchField = makeField(20);
            JButton searchBtn = makeButton("Search", ACCENT2);
            JButton addBtn    = makeButton("+ Add Member", SUCCESS);
            JButton editBtn   = makeButton("Edit", WARNING);
            JButton deleteBtn = makeButton("Delete", DANGER);
            JButton clearBtn  = makeButton("Show All", BG_CARD);
            toolbar.add(searchField); toolbar.add(searchBtn);
            toolbar.add(Box.createHorizontalStrut(12));
            toolbar.add(addBtn); toolbar.add(editBtn); toolbar.add(deleteBtn); toolbar.add(clearBtn);

            String[] cols = {"ID", "Full Name", "Email", "Phone", "Address", "Registered"};
            table = makeTable(cols);
            table.getColumnModel().getColumn(0).setMaxWidth(50);

            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.setBackground(BG_DARK);
            tablePanel.setBorder(new EmptyBorder(0, 28, 28, 28));
            tablePanel.add(makeScroll(table), BorderLayout.CENTER);

            JPanel top = new JPanel(new BorderLayout());
            top.setBackground(BG_DARK);
            top.add(header, BorderLayout.NORTH);
            top.add(toolbar, BorderLayout.SOUTH);
            add(top, BorderLayout.NORTH);
            add(tablePanel, BorderLayout.CENTER);

            loadTable();

            searchBtn.addActionListener(e -> searchMembers(searchField.getText()));
            clearBtn.addActionListener(e -> { searchField.setText(""); loadTable(); });
            addBtn.addActionListener(e -> showMemberDialog(null));
            editBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) { showMsg("Select a member to edit.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
                showMemberDialog(row);
            });
            deleteBtn.addActionListener(e -> deleteMember());
        }

        void loadTable() {
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            m.setRowCount(0);
            try {
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM members ORDER BY full_name");
                while (rs.next()) {
                    m.addRow(new Object[]{
                        rs.getInt("member_id"), rs.getString("full_name"), rs.getString("email"),
                        rs.getString("phone"), rs.getString("address"), rs.getString("registered_date")
                    });
                }
            } catch (SQLException ex) { showError(ex); }
        }

        void searchMembers(String q) {
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            m.setRowCount(0);
            try {
                PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM members WHERE full_name LIKE ? OR email LIKE ? ORDER BY full_name");
                ps.setString(1, "%" + q + "%"); ps.setString(2, "%" + q + "%");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    m.addRow(new Object[]{
                        rs.getInt("member_id"), rs.getString("full_name"), rs.getString("email"),
                        rs.getString("phone"), rs.getString("address"), rs.getString("registered_date")
                    });
                }
            } catch (SQLException ex) { showError(ex); }
        }

        void showMemberDialog(Integer row) {
            JDialog dlg = new JDialog(LibraryApp.this, row == null ? "Add Member" : "Edit Member", true);
            dlg.setSize(400, 340);
            dlg.setLocationRelativeTo(LibraryApp.this);
            dlg.getContentPane().setBackground(BG_PANEL);

            JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
            form.setBackground(BG_PANEL);
            form.setBorder(new EmptyBorder(20, 20, 10, 20));

            JTextField tfName    = makeField(15);
            JTextField tfEmail   = makeField(15);
            JTextField tfPhone   = makeField(15);
            JTextField tfAddress = makeField(15);

            int memberId = -1;
            if (row != null) {
                memberId = (int) table.getValueAt(row, 0);
                tfName.setText((String) table.getValueAt(row, 1));
                tfEmail.setText((String) table.getValueAt(row, 2));
                tfPhone.setText((String) table.getValueAt(row, 3));
                tfAddress.setText((String) table.getValueAt(row, 4));
            }

            form.add(makeLabel("Full Name *")); form.add(tfName);
            form.add(makeLabel("Email"));       form.add(tfEmail);
            form.add(makeLabel("Phone"));       form.add(tfPhone);
            form.add(makeLabel("Address"));     form.add(tfAddress);

            JButton saveBtn   = makeButton(row == null ? "Add Member" : "Save Changes", SUCCESS);
            JButton cancelBtn = makeButton("Cancel", BG_CARD);
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnPanel.setBackground(BG_PANEL);
            btnPanel.add(cancelBtn); btnPanel.add(saveBtn);
            dlg.add(form, BorderLayout.CENTER);
            dlg.add(btnPanel, BorderLayout.SOUTH);

            final int fId = memberId;
            saveBtn.addActionListener(e -> {
                String name = tfName.getText().trim();
                if (name.isEmpty()) { showMsg("Full name is required.", "Validation", JOptionPane.WARNING_MESSAGE); return; }
                try {
                    if (fId == -1) {
                        PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO members (full_name,email,phone,address,registered_date) VALUES (?,?,?,?,?)");
                        ps.setString(1, name); ps.setString(2, tfEmail.getText().trim());
                        ps.setString(3, tfPhone.getText().trim()); ps.setString(4, tfAddress.getText().trim());
                        ps.setString(5, LocalDate.now().toString());
                        ps.executeUpdate();
                    } else {
                        PreparedStatement ps = conn.prepareStatement(
                            "UPDATE members SET full_name=?,email=?,phone=?,address=? WHERE member_id=?");
                        ps.setString(1, name); ps.setString(2, tfEmail.getText().trim());
                        ps.setString(3, tfPhone.getText().trim()); ps.setString(4, tfAddress.getText().trim());
                        ps.setInt(5, fId);
                        ps.executeUpdate();
                    }
                    dlg.dispose(); loadTable();
                } catch (SQLException ex) { showError(ex); }
            });
            cancelBtn.addActionListener(e -> dlg.dispose());
            dlg.setVisible(true);
        }

        void deleteMember() {
            int row = table.getSelectedRow();
            if (row < 0) { showMsg("Select a member to delete.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
            int id = (int) table.getValueAt(row, 0);
            String name = (String) table.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(LibraryApp.this,
                "Delete member \"" + name + "\"?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
            try {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM members WHERE member_id=?");
                ps.setInt(1, id); ps.executeUpdate();
                loadTable();
            } catch (SQLException ex) { showError(ex); }
        }
    }

    // ── CHECKOUTS PANEL ───────────────────────────────────────────────────────
    class CheckoutsPanel extends JPanel {
        private JTable table;

        CheckoutsPanel() {
            setLayout(new BorderLayout());
            setBackground(BG_DARK);

            JPanel header = makeHeader("🔄 Checkouts", "Manage book borrowing and returns");

            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            toolbar.setBackground(BG_DARK);
            toolbar.setBorder(new EmptyBorder(0, 28, 12, 28));
            JButton borrowBtn  = makeButton("📤 Borrow Book", SUCCESS);
            JButton returnBtn  = makeButton("📥 Return Book", ACCENT2);
            JButton activeBtn  = makeButton("Active Only", WARNING);
            JButton allBtn     = makeButton("Show All", BG_CARD);
            toolbar.add(borrowBtn); toolbar.add(returnBtn);
            toolbar.add(Box.createHorizontalStrut(12));
            toolbar.add(activeBtn); toolbar.add(allBtn);

            String[] cols = {"ID", "Book Title", "Member Name", "Checkout Date", "Due Date", "Return Date", "Status"};
            table = makeTable(cols);
            table.getColumnModel().getColumn(0).setMaxWidth(50);

            // Color status column
            table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    setBackground(sel ? new Color(0, 229, 255, 40) : BG_PANEL);
                    String s = String.valueOf(v);
                    setForeground(s.equals("RETURNED") ? SUCCESS : s.equals("OVERDUE") ? DANGER : WARNING);
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    return this;
                }
            });

            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.setBackground(BG_DARK);
            tablePanel.setBorder(new EmptyBorder(0, 28, 28, 28));
            tablePanel.add(makeScroll(table), BorderLayout.CENTER);

            JPanel top = new JPanel(new BorderLayout());
            top.setBackground(BG_DARK);
            top.add(header, BorderLayout.NORTH);
            top.add(toolbar, BorderLayout.SOUTH);
            add(top, BorderLayout.NORTH);
            add(tablePanel, BorderLayout.CENTER);

            loadTable();
            updateOverdueStatus();

            borrowBtn.addActionListener(e -> showBorrowDialog());
            returnBtn.addActionListener(e -> returnBook());
            activeBtn.addActionListener(e -> loadActiveCheckouts());
            allBtn.addActionListener(e -> loadTable());
        }

        void loadTable() {
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            m.setRowCount(0);
            try {
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(
                    "SELECT c.checkout_id, b.title, mem.full_name, " +
                    "c.checkout_date, c.due_date, c.return_date, c.status " +
                    "FROM checkouts c " +
                    "JOIN books b ON c.book_id=b.book_id " +
                    "JOIN members mem ON c.member_id=mem.member_id " +
                    "ORDER BY c.checkout_date DESC");
                while (rs.next()) {
                    m.addRow(new Object[]{
                        rs.getInt("checkout_id"), rs.getString("title"), rs.getString("full_name"),
                        rs.getString("checkout_date"), rs.getString("due_date"),
                        rs.getString("return_date") != null ? rs.getString("return_date") : "—",
                        rs.getString("status")
                    });
                }
            } catch (SQLException ex) { showError(ex); }
        }

        void loadActiveCheckouts() {
            DefaultTableModel m = (DefaultTableModel) table.getModel();
            m.setRowCount(0);
            try {
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(
                    "SELECT c.checkout_id, b.title, mem.full_name, " +
                    "c.checkout_date, c.due_date, c.return_date, c.status " +
                    "FROM checkouts c " +
                    "JOIN books b ON c.book_id=b.book_id " +
                    "JOIN members mem ON c.member_id=mem.member_id " +
                    "WHERE c.status != 'RETURNED' ORDER BY c.due_date ASC");
                while (rs.next()) {
                    m.addRow(new Object[]{
                        rs.getInt("checkout_id"), rs.getString("title"), rs.getString("full_name"),
                        rs.getString("checkout_date"), rs.getString("due_date"), "—",
                        rs.getString("status")
                    });
                }
            } catch (SQLException ex) { showError(ex); }
        }

        void updateOverdueStatus() {
            try {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE checkouts SET status='OVERDUE' WHERE due_date < ? AND status='BORROWED'");
                ps.setString(1, LocalDate.now().toString());
                ps.executeUpdate();
            } catch (SQLException ex) { showError(ex); }
        }

        void showBorrowDialog() {
            JDialog dlg = new JDialog(LibraryApp.this, "Borrow a Book", true);
            dlg.setSize(420, 260);
            dlg.setLocationRelativeTo(LibraryApp.this);
            dlg.getContentPane().setBackground(BG_PANEL);

            JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
            form.setBackground(BG_PANEL);
            form.setBorder(new EmptyBorder(20, 20, 10, 20));

            Vector<String> bookItems   = new Vector<>();
            Vector<Integer> bookIds    = new Vector<>();
            Vector<String> memberItems = new Vector<>();
            Vector<Integer> memberIds  = new Vector<>();

            try {
                ResultSet rb = conn.createStatement().executeQuery(
                    "SELECT book_id, title, available_copies FROM books WHERE available_copies > 0 ORDER BY title");
                while (rb.next()) {
                    bookItems.add(rb.getString("title") + " (" + rb.getInt("available_copies") + " avail)");
                    bookIds.add(rb.getInt("book_id"));
                }
                ResultSet rm = conn.createStatement().executeQuery(
                    "SELECT member_id, full_name FROM members ORDER BY full_name");
                while (rm.next()) {
                    memberItems.add(rm.getString("full_name"));
                    memberIds.add(rm.getInt("member_id"));
                }
            } catch (SQLException ex) { showError(ex); return; }

            if (bookItems.isEmpty()) { showMsg("No books available to borrow.", "Info", JOptionPane.INFORMATION_MESSAGE); return; }
            if (memberItems.isEmpty()) { showMsg("No members registered.", "Info", JOptionPane.INFORMATION_MESSAGE); return; }

            JComboBox<String> bookCombo   = new JComboBox<>(bookItems);
            JComboBox<String> memberCombo = new JComboBox<>(memberItems);
            bookCombo.setBackground(BG_CARD); bookCombo.setForeground(TEXT_WHITE); bookCombo.setFont(FONT_BODY);
            memberCombo.setBackground(BG_CARD); memberCombo.setForeground(TEXT_WHITE); memberCombo.setFont(FONT_BODY);

            JTextField tfDue = makeField(10);
            tfDue.setText(LocalDate.now().plusDays(14).toString());

            form.add(makeLabel("Book *"));       form.add(bookCombo);
            form.add(makeLabel("Member *"));     form.add(memberCombo);
            form.add(makeLabel("Due Date *"));   form.add(tfDue);
            form.add(makeLabel("(YYYY-MM-DD)")); form.add(new JLabel());

            JButton confirmBtn = makeButton("Confirm Borrow", SUCCESS);
            JButton cancelBtn  = makeButton("Cancel", BG_CARD);
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnPanel.setBackground(BG_PANEL);
            btnPanel.add(cancelBtn); btnPanel.add(confirmBtn);
            dlg.add(form, BorderLayout.CENTER);
            dlg.add(btnPanel, BorderLayout.SOUTH);

            confirmBtn.addActionListener(e -> {
                int bookId   = bookIds.get(bookCombo.getSelectedIndex());
                int memberId = memberIds.get(memberCombo.getSelectedIndex());
                String due   = tfDue.getText().trim();
                try {
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO checkouts (book_id,member_id,checkout_date,due_date,status) VALUES (?,?,?,?,'BORROWED')");
                    ps.setInt(1, bookId); ps.setInt(2, memberId);
                    ps.setString(3, LocalDate.now().toString()); ps.setString(4, due);
                    ps.executeUpdate();
                    PreparedStatement up = conn.prepareStatement(
                        "UPDATE books SET available_copies=available_copies-1 WHERE book_id=?");
                    up.setInt(1, bookId); up.executeUpdate();
                    dlg.dispose();
                    loadTable();
                    showMsg("Book borrowed successfully! Due: " + due, "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) { showError(ex); }
            });
            cancelBtn.addActionListener(e -> dlg.dispose());
            dlg.setVisible(true);
        }

        void returnBook() {
            int row = table.getSelectedRow();
            if (row < 0) { showMsg("Select a checkout record to return.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
            String status = (String) table.getValueAt(row, 6);
            if (status.equals("RETURNED")) { showMsg("This book is already returned.", "Info", JOptionPane.INFORMATION_MESSAGE); return; }
            int checkoutId = (int) table.getValueAt(row, 0);
            String title   = (String) table.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(LibraryApp.this,
                "Mark \"" + title + "\" as returned?", "Confirm Return",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
            try {
                PreparedStatement gs = conn.prepareStatement("SELECT book_id FROM checkouts WHERE checkout_id=?");
                gs.setInt(1, checkoutId);
                ResultSet rs = gs.executeQuery();
                if (rs.next()) {
                    int bookId = rs.getInt("book_id");
                    PreparedStatement upC = conn.prepareStatement(
                        "UPDATE checkouts SET return_date=?, status='RETURNED' WHERE checkout_id=?");
                    upC.setString(1, LocalDate.now().toString()); upC.setInt(2, checkoutId);
                    upC.executeUpdate();
                    PreparedStatement upB = conn.prepareStatement(
                        "UPDATE books SET available_copies=available_copies+1 WHERE book_id=?");
                    upB.setInt(1, bookId); upB.executeUpdate();
                }
                loadTable();
                showMsg("Book returned successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) { showError(ex); }
        }
    }

    // ── REPORTS PANEL ─────────────────────────────────────────────────────────
    class ReportsPanel extends JPanel {
        private JLabel lbTotalBooks, lbTotalMembers, lbActiveBorrows, lbOverdue;
        private JTable overdueTable;

        ReportsPanel() {
            setLayout(new BorderLayout());
            setBackground(BG_DARK);

            JPanel header = makeHeader("📊 Reports", "Library statistics and overdue tracking");

            JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
            statsRow.setBackground(BG_DARK);
            statsRow.setBorder(new EmptyBorder(0, 28, 20, 28));

            lbTotalBooks    = new JLabel("—");
            lbTotalMembers  = new JLabel("—");
            lbActiveBorrows = new JLabel("—");
            lbOverdue       = new JLabel("—");

            statsRow.add(makeStatCard("📚 Total Books",    lbTotalBooks,    ACCENT));
            statsRow.add(makeStatCard("👥 Members",        lbTotalMembers,  SUCCESS));
            statsRow.add(makeStatCard("🔄 Active Borrows", lbActiveBorrows, WARNING));
            statsRow.add(makeStatCard("⚠ Overdue",         lbOverdue,       DANGER));

            JPanel overdueSection = new JPanel(new BorderLayout());
            overdueSection.setBackground(BG_DARK);
            overdueSection.setBorder(new EmptyBorder(0, 28, 28, 28));
            JLabel overdueTitle = new JLabel("Overdue Books");
            overdueTitle.setFont(FONT_HEADER);
            overdueTitle.setForeground(DANGER);
            overdueTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

            String[] cols = {"Book Title", "Member", "Due Date", "Days Overdue"};
            overdueTable = makeTable(cols);
            overdueSection.add(overdueTitle, BorderLayout.NORTH);
            overdueSection.add(makeScroll(overdueTable), BorderLayout.CENTER);

            JPanel top = new JPanel(new BorderLayout());
            top.setBackground(BG_DARK);
            top.add(header, BorderLayout.NORTH);
            top.add(statsRow, BorderLayout.SOUTH);
            add(top, BorderLayout.NORTH);
            add(overdueSection, BorderLayout.CENTER);

            refresh();
        }

        JPanel makeStatCard(String label, JLabel valueLabel, Color accent) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(BG_CARD);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(48, 54, 61)),
                new EmptyBorder(16, 20, 16, 20)));
            JLabel lbl = new JLabel(label);
            lbl.setFont(FONT_SMALL);
            lbl.setForeground(TEXT_GRAY);
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
            valueLabel.setForeground(accent);
            card.add(lbl, BorderLayout.NORTH);
            card.add(valueLabel, BorderLayout.CENTER);
            return card;
        }

        void refresh() {
            try {
                ResultSet r1 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM books");
                if (r1.next()) lbTotalBooks.setText(String.valueOf(r1.getInt(1)));

                ResultSet r2 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM members");
                if (r2.next()) lbTotalMembers.setText(String.valueOf(r2.getInt(1)));

                ResultSet r3 = conn.createStatement().executeQuery(
                    "SELECT COUNT(*) FROM checkouts WHERE status='BORROWED' OR status='OVERDUE'");
                if (r3.next()) lbActiveBorrows.setText(String.valueOf(r3.getInt(1)));

                ResultSet r4 = conn.createStatement().executeQuery(
                    "SELECT COUNT(*) FROM checkouts WHERE status='OVERDUE'");
                if (r4.next()) lbOverdue.setText(String.valueOf(r4.getInt(1)));

                DefaultTableModel m = (DefaultTableModel) overdueTable.getModel();
                m.setRowCount(0);
                ResultSet ro = conn.createStatement().executeQuery(
                    "SELECT b.title, mem.full_name, c.due_date FROM checkouts c " +
                    "JOIN books b ON c.book_id=b.book_id " +
                    "JOIN members mem ON c.member_id=mem.member_id " +
                    "WHERE c.status='OVERDUE' ORDER BY c.due_date ASC");
                while (ro.next()) {
                    LocalDate due = LocalDate.parse(ro.getString("due_date"));
                    long days = ChronoUnit.DAYS.between(due, LocalDate.now());
                    m.addRow(new Object[]{
                        ro.getString("title"), ro.getString("full_name"),
                        ro.getString("due_date"), days + " days"
                    });
                }
            } catch (SQLException ex) { showError(ex); }
        }
    }

    // ── Shared Error Helpers ──────────────────────────────────────────────────
    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void showMsg(String msg, String title, int type) {
        JOptionPane.showMessageDialog(this, msg, title, type);
    }

    // ── Main ──────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(LibraryApp::new);
    }
}
