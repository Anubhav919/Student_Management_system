import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentDashboardGUI extends JFrame {

    // --- shared color palette (only 3 colors used across the whole app) ---
    private static final Color NAVY = new Color(0x2C, 0x3E, 0x50);
    private static final Color TEAL = new Color(0x1A, 0xBC, 0x9C);
    private static final Color LIGHT_GRAY = new Color(0xEC, 0xF0, 0xF1);
    private static final String[][] GRADE_SCALE = {
        {"90.00", "100.00", "O",  "Outstanding"},
        {"85.00", "89.99",  "A+", "Excellent"},
        {"80.00", "84.99",  "A",  "Very Good"},
        {"75.00", "79.99",  "B+", "Good"},
        {"65.00", "74.99",  "B",  "Above Average"},
        {"55.00", "64.99",  "C",  "Average"},
        {"50.00", "54.99",  "P",  "Pass"},
        {"0.00",  "49.99",  "F",  "Fail"},
};

    // TODO: replace these fields with a Student object once Student.java is finalized
    private final String studentName;
    private final String rollNo;
    private final String course;
    private final String department;

    private JPanel sidebar;
    private boolean sidebarVisible = true;
    private JPanel contentPanel;
    private CardLayout contentLayout;

    private static final String[] MENU_ITEMS = {
            "Admit Card", "Attendance", "LMS", "My Report Card", "Performance",
            "College Info", "Time Table", "Fees", "Feedback", "Notifications"
    };

    // Icons matching the screenshot design
    private static final String[] MENU_ICONS = {
            "\uD83C\uDF93", "\u2713", "\uD83D\uDCBB", "\uD83D\uDCC4", "\uD83D\uDCCA",
            "\uD83C\uDFEB", "\uD83D\uDCC5", "\uD83D\uDCB0", "\uD83D\uDCAC", "\uD83D\uDD14"
    };

    public StudentDashboardGUI(String studentName, String rollNo, String course, String department) {
        this.studentName = studentName;
        this.rollNo = rollNo;
        this.course = course;
        this.department = department;

        setTitle("SmartStudent - Dashboard");
        setSize(1100, 620);  // landscape-friendly window
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 520));

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildTopBar(), BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        sidebar = buildSidebar();
        centerWrapper.add(sidebar, BorderLayout.WEST);
        centerWrapper.add(buildContentArea(), BorderLayout.CENTER);

        root.add(centerWrapper, BorderLayout.CENTER);
        add(root);
    }

    // ---------------- Top bar: hamburger + title + profile ----------------
    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(NAVY);
        topBar.setPreferredSize(new Dimension(950, 60));
        topBar.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        JButton hamburgerBtn = new JButton("\u2630"); // ☰
        hamburgerBtn.setFont(new Font("SansSerif", Font.PLAIN, 20));
        hamburgerBtn.setForeground(Color.WHITE);
        hamburgerBtn.setBackground(NAVY);
        hamburgerBtn.setBorderPainted(false);
        hamburgerBtn.setContentAreaFilled(false);
        hamburgerBtn.setFocusPainted(false);
        hamburgerBtn.addActionListener(e -> toggleSidebar());
        topBar.add(hamburgerBtn, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("Student Management System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        titleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showContentCard("Welcome");
            }
        });
        topBar.add(titleLabel, BorderLayout.CENTER);

        JPanel profileWrapper = new JPanel(new GridBagLayout());
        profileWrapper.setOpaque(false);
        GridBagConstraints profileGbc = new GridBagConstraints();
        profileGbc.insets = new Insets(0, 0, 0, 10);
        profileWrapper.add(buildProfileButton(), profileGbc);
        topBar.add(profileWrapper, BorderLayout.EAST);

        return topBar;
    }

    private JButton buildProfileButton() {
        String initial = (studentName != null && !studentName.isEmpty())
                ? studentName.substring(0, 1).toUpperCase() : "?";

        JButton profileBtn = new JButton(initial) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEAL);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getAscent();
                g2.drawString(getText(), (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - 2);
                g2.dispose();
            }
        };
        profileBtn.setPreferredSize(new Dimension(32, 32));
        profileBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        profileBtn.setBorderPainted(false);
        profileBtn.setContentAreaFilled(false);
        profileBtn.setFocusPainted(false);

        JPopupMenu profileMenu = buildProfileMenu();
        profileBtn.addActionListener(e -> profileMenu.show(profileBtn, 0, profileBtn.getHeight()));

        return profileBtn;
    }

    private JPopupMenu buildProfileMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(NAVY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel nameLabel = new JLabel(studentName);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel courseLabel = new JLabel("Course: " + course);
        courseLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        courseLabel.setForeground(LIGHT_GRAY);
        courseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel deptLabel = new JLabel("Dept: " + department);
        deptLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        deptLabel.setForeground(LIGHT_GRAY);
        deptLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel rollLabel = new JLabel(rollNo);
        rollLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        rollLabel.setForeground(TEAL);
        rollLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerPanel.add(nameLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(courseLabel);
        headerPanel.add(deptLabel);
        headerPanel.add(rollLabel);

        menu.add(headerPanel);
        menu.addSeparator();

        JMenuItem profileItem = new JMenuItem("  Profile");
        JMenuItem resetPassItem = new JMenuItem("  Reset Password");
        JMenuItem logoutItem = new JMenuItem("  Logout");

        for (JMenuItem item : new JMenuItem[]{profileItem, resetPassItem, logoutItem}) {
            item.setFont(new Font("SansSerif", Font.PLAIN, 14));
            item.setBackground(Color.WHITE);
            item.setForeground(NAVY);
        }

        profileItem.addActionListener(e -> showContentCard("Profile"));
        resetPassItem.addActionListener(e -> new ForgotPasswordGUI().setVisible(true));
        logoutItem.addActionListener(e -> {
            dispose();
            new StudentLoginGUI().setVisible(true);
        });

        menu.add(profileItem);
        menu.add(resetPassItem);
        menu.add(logoutItem);

        return menu;
    }

    // ---------------- Sidebar ----------------
    private JPanel buildSidebar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(NAVY);
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        panel.add(buildSidebarHeader());

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(0x3D, 0x50, 0x66));
        separator.setMaximumSize(new Dimension(220, 1));
        panel.add(separator);
        panel.add(Box.createVerticalStrut(10));

        for (int i = 0; i < MENU_ITEMS.length; i++) {
            panel.add(buildSidebarButton(MENU_ITEMS[i], MENU_ICONS[i]));
        }

        return panel;
    }

    // Name / Course / Roll No block shown at the top of the sidebar, above the menu items
    private JPanel buildSidebarHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(24, 20, 20, 20));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);

        String initial = (studentName != null && !studentName.isEmpty())
                ? studentName.substring(0, 1).toUpperCase() : "?";
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEAL);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(initial);
                int textHeight = fm.getAscent();
                g2.drawString(initial, (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - 3);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(56, 56);
            }

            @Override
            public Dimension getMinimumSize() {
                return new Dimension(56, 56);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(56, 56);
            }
        };
        avatar.setOpaque(false);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(studentName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel courseLabel = new JLabel(course, SwingConstants.CENTER);
        courseLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        courseLabel.setForeground(LIGHT_GRAY);
        courseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel rollBadge = new JLabel(rollNo, SwingConstants.CENTER);
        rollBadge.setFont(new Font("SansSerif", Font.PLAIN, 11));
        rollBadge.setForeground(TEAL);
        rollBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        rollBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TEAL, 1, true),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));

        header.add(avatar);
        header.add(Box.createVerticalStrut(10));
        header.add(nameLabel);
        header.add(Box.createVerticalStrut(2));
        header.add(courseLabel);
        header.add(Box.createVerticalStrut(6));
        header.add(rollBadge);

        Dimension headerPreferred = header.getPreferredSize();
        header.setMaximumSize(new Dimension(220, headerPreferred.height));

        return header;
    }

    private JPanel buildSidebarButton(String label, String icon) {
        JPanel itemPanel = new JPanel(new BorderLayout(10, 0));
        itemPanel.setBackground(NAVY);
        itemPanel.setMaximumSize(new Dimension(220, 44));
        itemPanel.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 12));
        itemPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        iconLabel.setForeground(LIGHT_GRAY);

        JLabel itemLabel = new JLabel(label);
        itemLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        itemLabel.setForeground(LIGHT_GRAY);

        itemPanel.add(iconLabel, BorderLayout.WEST);
        itemPanel.add(itemLabel, BorderLayout.CENTER);

        itemPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                itemPanel.setBackground(TEAL);
                iconLabel.setForeground(Color.WHITE);
                itemLabel.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                itemPanel.setBackground(NAVY);
                iconLabel.setForeground(LIGHT_GRAY);
                itemLabel.setForeground(LIGHT_GRAY);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showContentCard(label);
            }
        });

        return itemPanel;
    }

    private void toggleSidebar() {
        sidebarVisible = !sidebarVisible;
        sidebar.setVisible(sidebarVisible);
    }

    // ---------------- Content area ----------------
    private JPanel buildContentArea() {
        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setBackground(LIGHT_GRAY);

        contentPanel.add(buildWelcomeCard(), "Welcome");

        for (String item : MENU_ITEMS) {
            if (item.equals("Attendance")) {
                contentPanel.add(buildAttendanceCard(), item);
            } else if (item.equals("Time Table")) {
                contentPanel.add(buildTimetableCard(), item);
            } else if (item.equals("Performance")) {
                contentPanel.add(buildPerformanceCard(), item);
            } else if (item.equals("Fees")) {
                contentPanel.add(buildFeesCard(), item);
            } else if (item.equals("College Info")) {
                contentPanel.add(buildCollegeInfoCard(), item);
            } else if (item.equals("Feedback")) {             
                contentPanel.add(buildFeedbackCard(), item);   
            } else if (item.equals("Admit Card")) {
                contentPanel.add(buildAdmitCardCard(), item);
            } else if (item.equals("My Report Card")) {
                contentPanel.add(buildReportCardCard(), item);
            } else if (item.equals("LMS")) {
                contentPanel.add(buildLmsCard(), item);
            } else if (item.equals("Notifications")) {
                contentPanel.add(buildNotificationsCard(), item);
            } else {
                contentPanel.add(buildPlaceholderCard(item), item);
            }
        }
        contentPanel.add(buildProfileCard(), "Profile");

        contentLayout.show(contentPanel, "Welcome");
        return contentPanel;
    }

    private JPanel buildWelcomeCard() {
        // Landscape home matching the provided screenshot design
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(LIGHT_GRAY);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel greeting = new JLabel("Welcome, " + studentName + "!");
        greeting.setFont(new Font("SansSerif", Font.BOLD, 24));
        greeting.setForeground(NAVY);
        greeting.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Here's what's happening in your academic journey.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(0x7F, 0x8C, 0x8D));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 22, 0));

        header.add(greeting);
        header.add(subtitle);
        panel.add(header, BorderLayout.NORTH);

        String[][] tiles = {
                {"Admit Card",     "\uD83C\uDF93"},
                {"Attendance",     "\u2713"},
                {"LMS",            "\uD83D\uDCBB"},
                {"My Report Card", "\uD83D\uDCC4"},
                {"Performance",    "\uD83D\uDCCA"},
                {"College Info",   "\uD83C\uDFEB"},
                {"Time Table",     "\uD83D\uDCC5"},
                {"Fees",           "\uD83D\uDCB0"},
                {"Feedback",       "\uD83D\uDCAC"},
                {"Notifications",  "\uD83D\uDD14"}
        };

        // Landscape: 5 columns × 2 rows
        JPanel grid = new JPanel(new GridLayout(2, 5, 18, 18));
        grid.setBackground(LIGHT_GRAY);
        grid.setOpaque(true);

        for (String[] tile : tiles) {
            grid.add(buildDashboardTile(tile[0], tile[1]));
        }

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setBackground(LIGHT_GRAY);
        centerWrap.add(grid, BorderLayout.CENTER);
        panel.add(centerWrap, BorderLayout.CENTER);

        return panel;
    }

    /** Landscape dashboard tile — uses app TEAL / NAVY only (no extra blues). */
    private JPanel buildDashboardTile(String label, String iconGlyph) {
        // Soft teal wash derived from TEAL palette
        final Color softTealBg = new Color(0xE8, 0xF8, 0xF3);
        final Color hoverBg = new Color(0xD5, 0xF5, 0xEC);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setOpaque(true);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1, true),
                BorderFactory.createEmptyBorder(16, 10, 14, 10)));

        JLabel icon = new JLabel(iconGlyph, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(softTealBg);
                g2.fillOval(4, 4, getWidth() - 8, getHeight() - 8);
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(TEAL);
                g2.drawOval(4, 4, getWidth() - 8, getHeight() - 8);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(56, 56);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(56, 56);
            }

            @Override
            public Dimension getMinimumSize() {
                return new Dimension(56, 56);
            }
        };
        icon.setFont(new Font("SansSerif", Font.PLAIN, 22));
        icon.setForeground(TEAL);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        icon.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel text = new JLabel(label, SwingConstants.CENTER);
        text.setFont(new Font("SansSerif", Font.PLAIN, 12));
        text.setForeground(NAVY);
        text.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(icon);
        card.add(Box.createVerticalStrut(10));
        card.add(text);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showContentCard(label);
            }
        });

        return card;
    }

    private JPanel buildProfileCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);

        JPanel detailArea = buildProfileDetailArea();
        JScrollPane scrollPane = new JScrollPane(detailArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(LIGHT_GRAY);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ---- Right side: Personal Details / Address / University Information cards ----
    private JPanel buildProfileDetailArea() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(LIGHT_GRAY);
        wrapper.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        String[] fields = fetchFullStudentRecord();
        // index mapping, see fetchFullStudentRecord() for order
        String fatherName = fields[0], motherName = fields[1], gender = fields[2],
                category = fields[3], religion = fields[4], studentPhone = fields[5],
                parentPhone = fields[6], tempAddress = fields[7], permAddress = fields[8],
                district = fields[9], state = fields[10], pincode = fields[11],
                applicationNumber = fields[12], currentSemester = fields[13], division = fields[14],
                email = fields[15];

        // Uniform 2x2 grid: all four cells get exactly the same width and height,
        // regardless of how much content each card has (4th cell intentionally left blank).
        JPanel grid = new JPanel(new GridLayout(2, 2, 20, 20));
        grid.setBackground(LIGHT_GRAY);

        grid.add(buildDetailCard("Personal Details", new String[][]{
                {"Name", studentName},
                {"Email", email},
                {"Father/Guardian Name", fatherName},
                {"Mother Name", motherName},
                {"Gender", gender},
                {"Category", category},
                {"Religion", religion},
                {"Student Phone", studentPhone},
                {"Parent Phone", parentPhone},
        }));

        grid.add(buildDetailCard("Address", new String[][]{
                {"Local / Present Address", tempAddress},
                {"Permanent Address", permAddress},
                {"District", district},
                {"State", state},
                {"Pincode", pincode},
        }));

        grid.add(buildDetailCard("University Information", new String[][]{
                {"Roll Number", rollNo},
                {"Application Number", applicationNumber},
                {"Course", course},
                {"Department", department},
                {"Current Semester", currentSemester},
                {"Division", division},
        }));

        grid.add(buildBlankCard()); // 4th cell intentionally empty, matches reference layout

        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildBlankCard() {
        JPanel card = new JPanel();
        card.setBackground(LIGHT_GRAY);
        card.setOpaque(true);
        return card;
    }

    /** Fetches the remaining student columns not already passed into this GUI. */
    private String[] fetchFullStudentRecord() {
        String[] result = new String[16];
        java.util.Arrays.fill(result, "N/A");

        String sql = "SELECT father_name, mother_name, gender, category, religion, student_phone, " +
                "parent_phone, temporary_address, permanent_address, district, state, pincode, " +
                "application_number, current_semester, division, email FROM students WHERE roll_no = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    result[0] = rs.getString("father_name");
                    result[1] = rs.getString("mother_name");
                    result[2] = rs.getString("gender");
                    result[3] = rs.getString("category");
                    result[4] = rs.getString("religion");
                    result[5] = rs.getString("student_phone");
                    result[6] = rs.getString("parent_phone");
                    result[7] = rs.getString("temporary_address");
                    result[8] = rs.getString("permanent_address");
                    result[9] = rs.getString("district");
                    result[10] = rs.getString("state");
                    result[11] = rs.getString("pincode");
                    result[12] = rs.getString("application_number");
                    result[13] = rs.getString("current_semester");
                    result[14] = String.valueOf(rs.getInt("division"));
                    String emailVal = rs.getString("email");
                    result[15] = (emailVal != null) ? emailVal : "N/A";
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private JPanel buildDetailCard(String title, String[][] rows) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLabel.setForeground(NAVY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(0xEE, 0xEE, 0xEE));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        card.add(separator);
        card.add(Box.createVerticalStrut(10));

        for (String[] row : rows) {
            JPanel rowPanel = new JPanel(new BorderLayout());
            rowPanel.setBackground(Color.WHITE);
            rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

            JLabel keyLabel = new JLabel(row[0]);
            keyLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            keyLabel.setForeground(NAVY);
            rowPanel.add(keyLabel, BorderLayout.WEST);

            JLabel valueLabel = new JLabel(row[1] != null ? row[1] : "N/A");
            valueLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            valueLabel.setForeground(new Color(0x7F, 0x8C, 0x8D));
            rowPanel.add(valueLabel, BorderLayout.EAST);

            card.add(rowPanel);
            card.add(Box.createVerticalStrut(6));
        }

        card.add(Box.createVerticalGlue());

        return card;
    }

    // ---------------- Attendance (Course Wise) ----------------
    private JPanel buildAttendanceCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("Course Wise Attendance");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(NAVY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(LIGHT_GRAY);

        // ---- filter bar ----
        JPanel filterCard = new JPanel();
        filterCard.setLayout(new BoxLayout(filterCard, BoxLayout.Y_AXIS));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        filterRow.setBackground(Color.WHITE);

        JLabel semLabel = new JLabel("Semester:");
        semLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        semLabel.setForeground(NAVY);

        String[] semesters = {"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"};
        JComboBox<String> semesterBox = new JComboBox<>(semesters);
        semesterBox.setFont(new Font("SansSerif", Font.PLAIN, 13));

        String defaultSemester = fetchCurrentSemester();
        if (defaultSemester != null) {
            semesterBox.setSelectedItem(defaultSemester);
        }

        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        searchBtn.setBackground(TEAL);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setBorderPainted(false);
        searchBtn.setOpaque(true);

        filterRow.add(semLabel);
        filterRow.add(semesterBox);
        filterRow.add(searchBtn);

        filterCard.add(filterRow);
        body.add(filterCard, BorderLayout.NORTH);

        // ---- results table ----
        String[] columns = {"Sr.No", "Course Name", "Course Short Name", "Attended/Delivered", "Percent"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(0xE8, 0xF8, 0xF3));
        table.setGridColor(new Color(0xEE, 0xEE, 0xEE));

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1));
        tableWrapper.add(table.getTableHeader(), BorderLayout.NORTH);
        tableWrapper.add(table, BorderLayout.CENTER);

        JLabel totalLabel = new JLabel("Total Percentage: N/A", SwingConstants.RIGHT);
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        totalLabel.setForeground(NAVY);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 10));

        JPanel tableSection = new JPanel(new BorderLayout());
        tableSection.setBackground(LIGHT_GRAY);
        tableSection.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        tableSection.add(tableWrapper, BorderLayout.CENTER);
        tableSection.add(totalLabel, BorderLayout.SOUTH);

        body.add(tableSection, BorderLayout.CENTER);
        panel.add(body, BorderLayout.CENTER);

        searchBtn.addActionListener(e -> loadAttendanceData(
                (String) semesterBox.getSelectedItem(), tableModel, totalLabel));

        // load once on first open using the default semester
        loadAttendanceData((String) semesterBox.getSelectedItem(), tableModel, totalLabel);

        return panel;
    }

    private String fetchCurrentSemester() {
        String sql = "SELECT current_semester FROM students WHERE roll_no = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("current_semester");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Note: our schema stores one attendance_status (Present/Absent) per subject per semester
     * in student_marks, not per individual lecture/date. So "Attended/Delivered" here means
     * 1/1 (Present) or 0/1 (Absent) per subject, not a running count across multiple classes.
     */
    private void loadAttendanceData(String semester, DefaultTableModel tableModel, JLabel totalLabel) {
        tableModel.setRowCount(0);

        String sql = "SELECT s.subject_name, s.subject_id, sm.attendance_status " +
                "FROM student_marks sm JOIN subjects s ON sm.subject_id = s.subject_id " +
                "WHERE sm.roll_no = ? AND sm.semester_recorded = ?";

        int presentCount = 0;
        int totalCount = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            stmt.setString(2, semester);

            try (ResultSet rs = stmt.executeQuery()) {
                int srNo = 1;
                while (rs.next()) {
                    String subjectName = rs.getString("subject_name");
                    String subjectId = rs.getString("subject_id");
                    boolean present = "Present".equalsIgnoreCase(rs.getString("attendance_status"));

                    String attendedDelivered = (present ? "1/1" : "0/1");
                    String percent = present ? "100" : "0";

                    tableModel.addRow(new Object[]{srNo++, subjectName, subjectId, attendedDelivered, percent});

                    totalCount++;
                    if (present) presentCount++;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (totalCount > 0) {
            double percentage = (presentCount * 100.0) / totalCount;
            totalLabel.setText(String.format("Total Percentage: %d/%d (%.1f%%)",
                    presentCount, totalCount, percentage));
        } else {
            totalLabel.setText("Total Percentage: No subjects found for this semester");
        }
    }

    // ---------------- Time Table (weekly grid with faculty names) ----------------
    private int timetableWeekOffset = 0;
    private JLabel timetableHeaderLabel;
    private JPanel timetableTableSlot;

    private JPanel buildTimetableCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // ---- filter bar: Academic Year + Search ----
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)));

        JLabel yearLabel = new JLabel("Academic Year:");
        yearLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        yearLabel.setForeground(NAVY);

        String[] years = {"2024-2025", "2025-2026", "2026-2027"};
        JComboBox<String> yearBox = new JComboBox<>(years);
        yearBox.setSelectedItem("2026-2027");
        yearBox.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        searchBtn.setBackground(TEAL);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBtn.setBorderPainted(false);
        searchBtn.setOpaque(true);

        JButton prevBtn = new JButton("\u2190 Previous");
        JButton nextBtn = new JButton("Next \u2192");
        for (JButton navBtn : new JButton[]{prevBtn, nextBtn}) {
            navBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
            navBtn.setBackground(new Color(0xE8, 0xE8, 0xE8));
            navBtn.setForeground(NAVY);
            navBtn.setFocusPainted(false);
        }

        filterCard.add(yearLabel);
        filterCard.add(yearBox);
        filterCard.add(searchBtn);
        filterCard.add(Box.createHorizontalStrut(30));
        filterCard.add(prevBtn);
        filterCard.add(nextBtn);

        panel.add(filterCard, BorderLayout.NORTH);

        // ---- header (course/dept/semester + date range) ----
        timetableHeaderLabel = new JLabel();
        timetableHeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        timetableHeaderLabel.setForeground(Color.WHITE);
        timetableHeaderLabel.setBackground(TEAL);
        timetableHeaderLabel.setOpaque(true);
        timetableHeaderLabel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        // ---- table slot (rebuilt on search/prev/next) ----
        timetableTableSlot = new JPanel(new BorderLayout());
        timetableTableSlot.setBackground(LIGHT_GRAY);
        timetableTableSlot.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBackground(LIGHT_GRAY);
        centerWrapper.add(timetableHeaderLabel, BorderLayout.NORTH);
        centerWrapper.add(timetableTableSlot, BorderLayout.CENTER);
        panel.add(centerWrapper, BorderLayout.CENTER);

        prevBtn.addActionListener(e -> {
            timetableWeekOffset--;
            refreshTimetable();
        });
        nextBtn.addActionListener(e -> {
            timetableWeekOffset++;
            refreshTimetable();
        });
        searchBtn.addActionListener(e -> refreshTimetable());

        refreshTimetable();

        return panel;
    }

    private void refreshTimetable() {
        String semester = fetchCurrentSemester();
        String weekRange = computeWeekRangeLabel(timetableWeekOffset);

        timetableHeaderLabel.setText("Timetable for " + course + " - " + department +
                " / Semester " + (semester != null ? semester : "N/A") + " / " + weekRange);

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        java.util.Map<String, java.util.Map<String, String>> schedule = fetchTimetable(semester, days);

        java.util.TreeSet<String> allSlots = new java.util.TreeSet<>();
        for (String day : days) {
            if (schedule.containsKey(day)) {
                allSlots.addAll(schedule.get(day).keySet());
            }
        }

        String[] columns = new String[days.length + 1];
        columns[0] = "Time Slots";
        System.arraycopy(days, 0, columns, 1, days.length);

        Object[][] data = new Object[allSlots.size()][columns.length];
        int r = 0;
        for (String slot : allSlots) {
            data[r][0] = slot;
            for (int d = 0; d < days.length; d++) {
                String cellText = "";
                if (schedule.containsKey(days[d]) && schedule.get(days[d]).containsKey(slot)) {
                    cellText = schedule.get(days[d]).get(slot);
                }
                data[r][d + 1] = cellText;
            }
            r++;
        }

        JTable table = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setRowHeight(64);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0xD9, 0xEA, 0xD3));
        table.getTableHeader().setForeground(NAVY);
        table.setGridColor(new Color(0xEE, 0xEE, 0xEE));
        table.setRowMargin(2);
        table.setIntercellSpacing(new Dimension(2, 2));

        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                JTextArea area = new JTextArea(value != null ? value.toString() : "");
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setEditable(false);
                area.setFont(new Font("SansSerif", Font.PLAIN, 12));
                area.setForeground(NAVY);
                area.setBackground(column == 0 ? new Color(0xF5, 0xF5, 0xF5) : Color.WHITE);
                area.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                if (column == 0) {
                    area.setFont(new Font("SansSerif", Font.BOLD, 13));
                }
                return area;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1));

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        timetableTableSlot.removeAll();
        timetableTableSlot.add(tableWrapper, BorderLayout.CENTER);
        timetableTableSlot.revalidate();
        timetableTableSlot.repaint();
    }

    /** Monday-Sunday date range label for the given week offset from the current week, e.g. "16 Sep 2026 To 22 Sep 2026". */
    private String computeWeekRangeLabel(int offset) {
        java.time.LocalDate monday = java.time.LocalDate.now()
                .with(java.time.DayOfWeek.MONDAY)
                .plusWeeks(offset);
        java.time.LocalDate sunday = monday.plusDays(6);
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy");
        return "Date: " + monday.format(fmt) + " To " + sunday.format(fmt);
    }

    /** Returns day -> (time_slot -> "Subject Name (CODE)\nFaculty: Name") for this student's course/dept/semester. */
    private java.util.Map<String, java.util.Map<String, String>> fetchTimetable(String semester, String[] days) {
        java.util.Map<String, java.util.Map<String, String>> schedule = new java.util.HashMap<>();
        for (String day : days) {
            schedule.put(day, new java.util.LinkedHashMap<>());
        }

        if (semester == null) return schedule;

        String sql = "SELECT tt.day_of_week, tt.time_slot, tt.faculty_name, tt.subject_id, s.subject_name " +
                "FROM timetable tt JOIN subjects s ON tt.subject_id = s.subject_id " +
                "WHERE tt.course = ? AND tt.department = ? AND tt.semester = ? " +
                "ORDER BY tt.time_slot";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course);
            stmt.setString(2, department);
            stmt.setString(3, semester);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String day = rs.getString("day_of_week");
                    String slot = rs.getString("time_slot");
                    String subjectName = rs.getString("subject_name");
                    String subjectCode = rs.getString("subject_id");
                    String faculty = rs.getString("faculty_name");

                    String cellText = subjectName + " (" + subjectCode + ")\nFaculty: " + faculty;
                    schedule.computeIfAbsent(day, k -> new java.util.LinkedHashMap<>()).put(slot, cellText);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return schedule;
    }

    // ---------------- Performance (Report Card) ----------------
    private JPanel performanceResultSlot;

    private JPanel buildPerformanceCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        String semester = fetchCurrentSemester();

        // ---- filter card ----
        JPanel filterCard = new JPanel();
        filterCard.setLayout(new BoxLayout(filterCard, BoxLayout.Y_AXIS));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JLabel cardTitle = new JLabel("Report Card");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        cardTitle.setForeground(NAVY);
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterCard.add(cardTitle);
        filterCard.add(Box.createVerticalStrut(12));

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        filterRow.setBackground(Color.WHITE);
        filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> yearBox = new JComboBox<>(new String[]{"2024-2025", "2025-2026", "2026-2027"});
        yearBox.setSelectedItem("2026-2027");

        JComboBox<String> examSessionBox = new JComboBox<>(new String[]{"Fall 2026-2027", "Spring 2026-2027"});

        String classLabel = course + " - " + department + " - Semester " + (semester != null ? semester : "N/A");
        JComboBox<String> classBox = new JComboBox<>(new String[]{classLabel});

        JComboBox<String> divisionBox = new JComboBox<>(new String[]{fetchDivision()});

        for (JComboBox<String> box : new JComboBox[]{yearBox, examSessionBox, classBox, divisionBox}) {
            box.setFont(new Font("SansSerif", Font.PLAIN, 13));
        }

        JButton submitBtn = new JButton("Submit");
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        submitBtn.setBackground(TEAL);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorderPainted(false);
        submitBtn.setOpaque(true);

        filterRow.add(labeledField("Academic Year:", yearBox));
        filterRow.add(labeledField("Exam Session:", examSessionBox));
        filterRow.add(labeledField("Class:", classBox));
        filterRow.add(labeledField("Division:", divisionBox));

        filterCard.add(filterRow);
        filterCard.add(Box.createVerticalStrut(10));

        JPanel submitRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        submitRow.setBackground(Color.WHITE);
        submitRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitRow.add(submitBtn);
        filterCard.add(submitRow);

        panel.add(filterCard, BorderLayout.NORTH);

        // ---- result slot, populated on Submit ----
        performanceResultSlot = new JPanel(new BorderLayout());
        performanceResultSlot.setBackground(LIGHT_GRAY);
        performanceResultSlot.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JScrollPane outerScroll = new JScrollPane(performanceResultSlot);
        outerScroll.setBorder(BorderFactory.createEmptyBorder());
        outerScroll.getViewport().setBackground(LIGHT_GRAY);
        panel.add(outerScroll, BorderLayout.CENTER);

        submitBtn.addActionListener(e -> loadPerformanceData(semester));

        return panel;
    }

    private JPanel labeledField(String labelText, JComboBox<String> box) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Color.WHITE);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(NAVY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(220, 28));

        wrapper.add(label);
        wrapper.add(Box.createVerticalStrut(4));
        wrapper.add(box);
        return wrapper;
    }

    private String fetchDivision() {
        String sql = "SELECT division FROM students WHERE roll_no = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return "Section-" + rs.getInt("division");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "N/A";
    }

    private void loadPerformanceData(String semester) {
        String[] columns = {"Sr. No", "Subject Code", "Subject Name", "Marks / Total", "Percent"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql = "SELECT s.subject_id, s.subject_name, sm.marks_obtained, sm.total_marks " +
                "FROM student_marks sm JOIN subjects s ON sm.subject_id = s.subject_id " +
                "WHERE sm.roll_no = ? AND sm.semester_recorded = ?";

        int sumObtained = 0;
        int sumTotal = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            stmt.setString(2, semester);

            try (ResultSet rs = stmt.executeQuery()) {
                int srNo = 1;
                while (rs.next()) {
                    String subjectId = rs.getString("subject_id");
                    String subjectName = rs.getString("subject_name");
                    int obtained = rs.getInt("marks_obtained");
                    int total = rs.getInt("total_marks");
                    String marksTotal = obtained + " / " + total;
                    String percent = total > 0 ? String.format("%.1f%%", (obtained * 100.0) / total) : "N/A";

                    tableModel.addRow(new Object[]{srNo++, subjectId, subjectName, marksTotal, percent});

                    sumObtained += obtained;
                    sumTotal += total;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        int overallRowIndex = tableModel.getRowCount();
        if (sumTotal > 0) {
            String overallMarksTotal = sumObtained + " / " + sumTotal;
            String overallPercent = String.format("%.1f%%", (sumObtained * 100.0) / sumTotal);
            tableModel.addRow(new Object[]{"", "", "Overall Total", overallMarksTotal, overallPercent});
        }

        JTable table = new JTable(tableModel);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(0xEE, 0xEE, 0xEE));

        final int totalRowIndex = overallRowIndex;
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (row == totalRowIndex) {
                    c.setFont(new Font("SansSerif", Font.BOLD, 13));
                    c.setBackground(new Color(0xE8, 0xF8, 0xF3));
                    setForeground(NAVY);
                } else {
                    c.setBackground(Color.WHITE);
                    setForeground(NAVY);
                }
                return c;
            }
        });

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1));
        tableWrapper.add(table.getTableHeader(), BorderLayout.NORTH);
        tableWrapper.add(table, BorderLayout.CENTER);

        JLabel cardLabel = new JLabel("Report Card");
        cardLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        cardLabel.setForeground(NAVY);
        cardLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(LIGHT_GRAY);
        wrapper.add(cardLabel, BorderLayout.NORTH);
        wrapper.add(tableWrapper, BorderLayout.CENTER);

        performanceResultSlot.removeAll();
        performanceResultSlot.add(wrapper, BorderLayout.CENTER);
        performanceResultSlot.revalidate();
        performanceResultSlot.repaint();
    }

    // ---------------- Fees ----------------
    private JPanel feesTableSlot;

    private JPanel buildFeesCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(LIGHT_GRAY);

        JPanel summaryRow = buildFeesSummaryRow();
        summaryRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.add(summaryRow);
        scrollContent.add(Box.createVerticalStrut(15));

        JPanel tabsRow = buildFeesCategoryTabs();
        tabsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.add(tabsRow);
        scrollContent.add(Box.createVerticalStrut(15));

        feesTableSlot = new JPanel(new BorderLayout());
        feesTableSlot.setBackground(LIGHT_GRAY);
        feesTableSlot.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.add(feesTableSlot);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(LIGHT_GRAY);
        panel.add(scrollPane, BorderLayout.CENTER);

        loadFeesTab("All", "All Fees (Include Misc)");

        return panel;
    }

    private JPanel buildFeesSummaryRow() {
        double[] amounts = fetchFeeSummary();
        String[] labels = {"Academic", "Academic Miscellaneous", "Hostel", "Hostel Miscellaneous",
                "Transport", "Transport Miscellaneous", "Total Outstanding Amount"};

        JPanel row = new JPanel(new GridLayout(1, labels.length, 12, 0));
        row.setBackground(LIGHT_GRAY);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        for (int i = 0; i < labels.length; i++) {
            boolean highlight = (i == labels.length - 1);
            row.add(buildFeesSummaryCard(labels[i], amounts[i], highlight));
        }
        return row;
    }

    private JPanel buildFeesSummaryCard(String label, double amount, boolean highlight) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(highlight ? TEAL : Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(highlight ? TEAL : new Color(0xDD, 0xDD, 0xDD), 1),
                BorderFactory.createEmptyBorder(14, 10, 14, 10)));

        JLabel valueLabel = new JLabel(String.format("%,.0f", amount), SwingConstants.CENTER);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        valueLabel.setForeground(highlight ? Color.WHITE : NAVY);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel nameLabel = new JLabel(label, SwingConstants.CENTER);
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        nameLabel.setForeground(highlight ? LIGHT_GRAY : new Color(0x7F, 0x8C, 0x8D));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(valueLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(nameLabel);

        return card;
    }

    /** Returns [academic, academicMisc, hostel, hostelMisc, transport, transportMisc, totalOutstanding]. */
    private double[] fetchFeeSummary() {
        double[] result = new double[7];

        String sql = "SELECT fee_category, fee_type, SUM(amount - amount_paid) AS outstanding " +
                "FROM fee_records WHERE roll_no = ? GROUP BY fee_category, fee_type";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("fee_category");
                    String type = rs.getString("fee_type");
                    double outstanding = rs.getDouble("outstanding");

                    if ("Academic".equalsIgnoreCase(category) && "Regular".equalsIgnoreCase(type)) {
                        result[0] = outstanding;
                    } else if ("Academic".equalsIgnoreCase(category) && "Miscellaneous".equalsIgnoreCase(type)) {
                        result[1] = outstanding;
                    } else if ("Hostel".equalsIgnoreCase(category) && "Regular".equalsIgnoreCase(type)) {
                        result[2] = outstanding;
                    } else if ("Hostel".equalsIgnoreCase(category) && "Miscellaneous".equalsIgnoreCase(type)) {
                        result[3] = outstanding;
                    } else if ("Transport".equalsIgnoreCase(category) && "Regular".equalsIgnoreCase(type)) {
                        result[4] = outstanding;
                    } else if ("Transport".equalsIgnoreCase(category) && "Miscellaneous".equalsIgnoreCase(type)) {
                        result[5] = outstanding;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        result[6] = result[0] + result[1] + result[2] + result[3] + result[4] + result[5];
        return result;
    }

    private JPanel buildFeesCategoryTabs() {
        JPanel row = new JPanel(new GridLayout(1, 5, 12, 0));
        row.setBackground(LIGHT_GRAY);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        row.add(buildFeesTabButton("All Fees (Include Misc)", "All"));
        row.add(buildFeesTabButton("Academic Fees", "Academic"));
        row.add(buildFeesTabButton("Hostel Fees", "Hostel"));
        row.add(buildFeesTabButton("Transport Fees", "Transport"));
        row.add(buildFeesTabButton("Miscellaneous Fees", "Miscellaneous"));

        return row;
    }

    private JPanel buildFeesTabButton(String label, String filterKey) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1),
                BorderFactory.createEmptyBorder(12, 8, 12, 8)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel textLabel = new JLabel(label, SwingConstants.CENTER);
        textLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        textLabel.setForeground(NAVY);
        card.add(textLabel);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(0xE8, 0xF8, 0xF3));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                loadFeesTab(filterKey, label);
            }
        });

        return card;
    }

    private void loadFeesTab(String filterKey, String title) {
        feesTableSlot.removeAll();

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(LIGHT_GRAY);

        JPanel feeSection = buildFeeRecordsSection(filterKey, title);
        feeSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(feeSection);
        wrapper.add(Box.createVerticalStrut(20));

        JPanel transactionsSection = buildFeeTransactionsSection();
        transactionsSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(transactionsSection);

        feesTableSlot.add(wrapper, BorderLayout.CENTER);
        feesTableSlot.revalidate();
        feesTableSlot.repaint();
    }

    private JPanel buildFeeRecordsSection(String filterKey, String title) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(LIGHT_GRAY);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(NAVY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        section.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Sr.No", "Session", "Fee Type", "Head Name", "Amount"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(0xEE, 0xEE, 0xEE));

        double totalToPay = loadFeeRecords(filterKey, tableModel);

        JLabel totalRow = new JLabel(String.format("Amount to Pay : %,.0f", totalToPay), SwingConstants.RIGHT);
        totalRow.setFont(new Font("SansSerif", Font.BOLD, 13));
        totalRow.setForeground(NAVY);
        totalRow.setOpaque(true);
        totalRow.setBackground(new Color(0xD4, 0xEE, 0xFA));
        totalRow.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 10));

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1));
        tableWrapper.add(table.getTableHeader(), BorderLayout.NORTH);
        tableWrapper.add(table, BorderLayout.CENTER);
        tableWrapper.add(totalRow, BorderLayout.SOUTH);

        section.add(tableWrapper, BorderLayout.CENTER);
        return section;
    }

    private double loadFeeRecords(String filterKey, DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        double total = 0;

        StringBuilder sql = new StringBuilder(
                "SELECT session, fee_category, fee_type, head_name, amount, amount_paid " +
                        "FROM fee_records WHERE roll_no = ?");

        if ("Academic".equals(filterKey)) {
            sql.append(" AND fee_category = 'Academic' AND fee_type = 'Regular'");
        } else if ("Hostel".equals(filterKey)) {
            sql.append(" AND fee_category = 'Hostel' AND fee_type = 'Regular'");
        } else if ("Transport".equals(filterKey)) {
            sql.append(" AND fee_category = 'Transport' AND fee_type = 'Regular'");
        } else if ("Miscellaneous".equals(filterKey)) {
            sql.append(" AND fee_type = 'Miscellaneous'");
        }
        sql.append(" ORDER BY session, id");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setString(1, rollNo);
            try (ResultSet rs = stmt.executeQuery()) {
                int srNo = 1;
                while (rs.next()) {
                    String session = rs.getString("session");
                    String category = rs.getString("fee_category");
                    String type = rs.getString("fee_type");
                    String headName = rs.getString("head_name");
                    double amount = rs.getDouble("amount");
                    double paid = rs.getDouble("amount_paid");
                    double outstanding = amount - paid;

                    String feeTypeDisplay = (category != null ? category : "") +
                            ("Miscellaneous".equalsIgnoreCase(type) ? " Fees (Misc)" : " Fees");

                    tableModel.addRow(new Object[]{srNo++, session, feeTypeDisplay, headName,
                            String.format("%,.0f", outstanding)});
                    total += outstanding;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return total;
    }

    private JPanel buildFeeTransactionsSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(LIGHT_GRAY);

        JLabel titleLabel = new JLabel("Transaction History");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(NAVY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        section.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"S.no", "Transaction Id", "Date of Deposit", "Voucher Type", "Transaction Type", "Amount"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(0xEE, 0xEE, 0xEE));

        loadFeeTransactions(tableModel);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1));
        tableWrapper.add(table.getTableHeader(), BorderLayout.NORTH);
        tableWrapper.add(table, BorderLayout.CENTER);

        section.add(tableWrapper, BorderLayout.CENTER);
        return section;
    }

    private void loadFeeTransactions(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);

        String sql = "SELECT transaction_id, deposit_date, voucher_type, transaction_type, amount " +
                "FROM fee_transactions WHERE roll_no = ? ORDER BY deposit_date";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            try (ResultSet rs = stmt.executeQuery()) {
                int srNo = 1;
                while (rs.next()) {
                    String transactionId = rs.getString("transaction_id");
                    java.sql.Date depositDate = rs.getDate("deposit_date");
                    String voucherType = rs.getString("voucher_type");
                    String transactionType = rs.getString("transaction_type");
                    double amount = rs.getDouble("amount");

                    String dateStr = depositDate != null
                            ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(depositDate)
                            : "N/A";

                    tableModel.addRow(new Object[]{srNo++, transactionId, dateStr, voucherType,
                            transactionType, String.format("%,.2f", amount)});
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ---------------- College Info ----------------
    private CardLayout collegeInfoLayout;
    private JPanel collegeInfoPanel;

    private static final String[] COLLEGE_INFO_TILES = {
            "Departments", "Perfo.Grades", "Programme", "Registration", "Calendar"
    };

    private JPanel buildCollegeInfoCard() {
        collegeInfoLayout = new CardLayout();
        collegeInfoPanel = new JPanel(collegeInfoLayout);
        collegeInfoPanel.setBackground(LIGHT_GRAY);

        collegeInfoPanel.add(buildCollegeInfoTileGrid(), "Grid");
        collegeInfoPanel.add(buildDepartmentsPage(), "Departments");
        collegeInfoPanel.add(buildGradesPage(), "Perfo.Grades");   
        collegeInfoPanel.add(buildProgrammePage(), "Programme");
        collegeInfoPanel.add(buildRegistrationPage(), "Registration");
        collegeInfoPanel.add(buildCalendarPage(), "Calendar");

        collegeInfoLayout.show(collegeInfoPanel, "Grid");
        return collegeInfoPanel;
    }

    private JPanel buildCollegeInfoTileGrid() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel grid = new JPanel(new GridLayout(2, 3, 20, 20));
        grid.setBackground(LIGHT_GRAY);

        // "Profile" tile jumps straight to the existing Profile page (outside this nested layout)
        grid.add(buildCollegeInfoTile("Profile", () -> showContentCard("Profile")));
        for (String tile : COLLEGE_INFO_TILES) {
            grid.add(buildCollegeInfoTile(tile, () -> collegeInfoLayout.show(collegeInfoPanel, tile)));
        }
        grid.add(new JPanel() {{ setOpaque(false); }}); // filler to keep the 2x3 grid balanced

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setBackground(LIGHT_GRAY);
        gridWrapper.add(grid, BorderLayout.NORTH);

        panel.add(gridWrapper, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildCollegeInfoTile(String label, Runnable onClick) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1),
                BorderFactory.createEmptyBorder(25, 10, 20, 10)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(180, 140));

        JLabel iconLabel = new JLabel(label.substring(0, 1), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEAL);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 20));
                FontMetrics fm = g2.getFontMetrics();
                int tw = fm.stringWidth(getText());
                int th = fm.getAscent();
                g2.drawString(getText(), (getWidth() - tw) / 2, (getHeight() + th) / 2 - 3);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(50, 50);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(50, 50);
            }
        };
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel textLabel = new JLabel(label, SwingConstants.CENTER);
        textLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        textLabel.setForeground(NAVY);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        card.add(iconLabel);
        card.add(textLabel);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(0xE8, 0xF8, 0xF3));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });

        return card;
    }

    private JButton buildBackToCollegeInfoButton() {
        JButton backBtn = new JButton("\u2190 Back");
        backBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        backBtn.setBackground(new Color(0xE8, 0xE8, 0xE8));
        backBtn.setForeground(NAVY);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> collegeInfoLayout.show(collegeInfoPanel, "Grid"));
        return backBtn;
    }

    // ---- Departments: static reference info about departments offered at the college ----
    private JPanel buildDepartmentsPage() {
        String[][] departments = {
                {"Computer Science Engineering", "Focuses on programming, data structures, algorithms, and software systems."},
                {"Information Technology", "Covers networking, databases, and IT infrastructure management."},
                {"Electronics and Communication Engineering", "Deals with circuits, signal processing, and communication systems."},
                {"Electrical Engineering", "Covers power systems, machines, and electrical circuit design."},
                {"Mechanical Engineering", "Focuses on machine design, thermodynamics, and manufacturing."},
                {"Civil Engineering", "Covers structural design, surveying, and construction management."},
                {"Computer Applications", "Focuses on software development and application programming."},
                {"Physics", "Studies mechanics, electromagnetism, and modern physics."},
                {"Chemistry", "Covers organic, inorganic, and physical chemistry."},
                {"Mathematics", "Focuses on calculus, algebra, and statistics."},
                {"Commerce", "Covers accounting, business studies, and economics."},
                {"Business Administration", "Focuses on management, marketing, and organizational behavior."},
                {"English", "Covers literature, linguistics, and communication skills."},
        };
        return buildCollegeInfoListPage("Departments", "Department", "Description", departments);
    }

        // ---- Perfo.Grades: card-grid grading pattern ----
    private JPanel buildGradesPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(LIGHT_GRAY);
        headerRow.add(buildBackToCollegeInfoButton(), BorderLayout.WEST);

        JLabel titleLabel = new JLabel("College Grading Pattern", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(NAVY);
        headerRow.add(titleLabel, BorderLayout.CENTER);
        panel.add(headerRow, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 3, 20, 20));
        grid.setBackground(LIGHT_GRAY);
        for (String[] g : GRADE_SCALE) {
            String range = g[0] + "-" + g[1];
            grid.add(buildGradeScaleCard(g[2], g[3], range));
        }

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setBackground(LIGHT_GRAY);
        gridWrapper.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        gridWrapper.add(grid, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(gridWrapper);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(LIGHT_GRAY);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /** One grading-scale card: ":" + range pill on top, letter + description below, teal underline. */
    private JPanel buildGradeScaleCard(String letter, String description, String range) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xEE, 0xEE, 0xEE), 1),
                BorderFactory.createEmptyBorder(18, 20, 0, 20)));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(Color.WHITE);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel colon = new JLabel(":", SwingConstants.CENTER);
        colon.setFont(new Font("SansSerif", Font.PLAIN, 14));
        colon.setForeground(new Color(0x95, 0xA5, 0xA6));
        topRow.add(colon, BorderLayout.CENTER);
        topRow.add(pillLabel(range, new Color(0xDC, 0xE9, 0xF2), NAVY), BorderLayout.EAST);
        card.add(topRow);

        card.add(Box.createVerticalStrut(24));

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setBackground(Color.WHITE);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel letterLabel = new JLabel(letter);
        letterLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        letterLabel.setForeground(NAVY);
        bottomRow.add(letterLabel, BorderLayout.WEST);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descLabel.setForeground(NAVY);
        bottomRow.add(descLabel, BorderLayout.EAST);
        card.add(bottomRow);

        card.add(Box.createVerticalStrut(14));

        JPanel underline = new JPanel();
        underline.setBackground(TEAL);
        underline.setAlignmentX(Component.LEFT_ALIGNMENT);
        underline.setPreferredSize(new Dimension(10, 3));
        underline.setMaximumSize(new Dimension(Integer.MAX_VALUE, 3));
        card.add(underline);

        return card;
    }

    /** Rounded "pill" badge, matching the light blue range badges in the screenshot. */
    private JComponent pillLabel(String text, Color background, Color foreground) {
        JLabel label = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setOpaque(false);
        label.setForeground(foreground);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        return label;
    }

    /** Modal popup version of the same grading pattern, callable from anywhere (e.g. Performance page). */
    private void showGradingPatternDialog() {
        JDialog dialog = new JDialog(this, "College Grading Pattern", true);
        dialog.setSize(920, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Color.WHITE);
        headerRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("College Grading Pattern");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(NAVY);
        headerRow.add(title, BorderLayout.WEST);

        JButton closeX = new JButton("\u2715");
        closeX.setFont(new Font("SansSerif", Font.PLAIN, 16));
        closeX.setForeground(new Color(0x7F, 0x8C, 0x8D));
        closeX.setBorderPainted(false);
        closeX.setContentAreaFilled(false);
        closeX.setFocusPainted(false);
        closeX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeX.addActionListener(e -> dialog.dispose());
        headerRow.add(closeX, BorderLayout.EAST);

        root.add(headerRow, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 3, 20, 20));
        grid.setBackground(Color.WHITE);
        for (String[] g : GRADE_SCALE) {
            String range = g[0] + "-" + g[1];
            grid.add(buildGradeScaleCard(g[2], g[3], range));
        }
        root.add(grid, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        closeBtn.setBackground(Color.WHITE);
        closeBtn.setForeground(NAVY);
        closeBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCC, 0xCC, 0xCC), 1, true),
                BorderFactory.createEmptyBorder(8, 28, 8, 28)));
        closeBtn.setFocusPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setOpaque(true);
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel closeBtnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        closeBtnWrapper.setBackground(Color.WHITE);
        closeBtnWrapper.setBorder(BorderFactory.createEmptyBorder(26, 0, 0, 0));
        closeBtnWrapper.add(closeBtn);
        root.add(closeBtnWrapper, BorderLayout.SOUTH);

        dialog.add(root, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // ---- Programme: which departments run under each course ----
    private JPanel buildProgrammePage() {
        String[][] programmes = {
                {"B.Tech", "Computer Science Engineering, Information Technology, Electronics and Communication Engineering, Electrical Engineering, Mechanical Engineering, Civil Engineering"},
                {"BCA / MCA", "Computer Applications"},
                {"B.Sc", "Physics, Chemistry, Mathematics, Zoology, Botany"},
                {"B.Com", "Commerce, Accounting and Finance"},
                {"BBA", "Business Administration"},
                {"BA", "English, History, Political Science, Economics"},
        };
        return buildCollegeInfoListPage("Programme", "Course", "Departments Offered", programmes);
    }

    /** Generic 2-column info list used by Departments/Grades/Programme pages. */
    private JPanel buildCollegeInfoListPage(String title, String col1, String col2, String[][] rows) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(LIGHT_GRAY);
        headerRow.add(buildBackToCollegeInfoButton(), BorderLayout.WEST);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(NAVY);
        headerRow.add(titleLabel, BorderLayout.CENTER);
        panel.add(headerRow, BorderLayout.NORTH);

        String[] columns = {col1, col2};
        JTable table = new JTable(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setRowHeight(40);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(0xEE, 0xEE, 0xEE));
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                             boolean hasFocus, int row, int column) {
                JTextArea area = new JTextArea(value != null ? value.toString() : "");
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setEditable(false);
                area.setFont(new Font("SansSerif", column == 0 ? Font.BOLD : Font.PLAIN, 13));
                area.setForeground(NAVY);
                area.setBackground(Color.WHITE);
                area.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                return area;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1));

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        panel.add(tableWrapper, BorderLayout.CENTER);
        return panel;
    }

    // ---- Registration: Backlog Registration form (only shown when backlogs exist) +
    // an always-available Next Semester Registration action ----
    private java.util.List<JCheckBox> backlogCheckBoxes = new java.util.ArrayList<>();

    private JPanel buildRegistrationPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(LIGHT_GRAY);
        headerRow.add(buildBackToCollegeInfoButton(), BorderLayout.WEST);

        JLabel titleLabel = new JLabel("Semester Registration", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(NAVY);
        headerRow.add(titleLabel, BorderLayout.CENTER);
        panel.add(headerRow, BorderLayout.NORTH);

        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(LIGHT_GRAY);
        scrollContent.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        java.util.List<String[]> backlogSubjects = new java.util.ArrayList<>();
        int backlogCount = loadBacklogSubjects(backlogSubjects);

        // ---- Backlog Registration form: only shown when backlogs exist ----
        if (backlogCount > 0) {
            JPanel backlogForm = buildBacklogRegistrationForm(backlogSubjects);
            backlogForm.setAlignmentX(Component.LEFT_ALIGNMENT);
            scrollContent.add(backlogForm);
            scrollContent.add(Box.createVerticalStrut(25));
        }

        // ---- Semester Registration: pick academic year + semester, then choose subjects ----
        JPanel semRegSection = buildSemesterRegistrationForm();
        semRegSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.add(semRegSection);
        scrollContent.add(Box.createVerticalStrut(25));

        // ---- My Registration Requests: shows Pending / Approved / Rejected status ----
        JPanel myRegSection = buildMyRegistrationsSection();
        myRegSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.add(myRegSection);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(LIGHT_GRAY);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ---------------- Admit Card ----------------
    private JPanel admitCardResultSlot;

    private JPanel buildAdmitCardCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("Admit Card List");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(NAVY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(LIGHT_GRAY);

        // ---- filter card ----
        JPanel filterCard = new JPanel();
        filterCard.setLayout(new BoxLayout(filterCard, BoxLayout.Y_AXIS));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        filterCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        String semester = fetchCurrentSemester();

        JComboBox<String> sessionBox = new JComboBox<>();
        sessionBox.addItem("Select Session");
        java.util.List<String[]> sessions = fetchExamSessions(semester); // {id, session_name, exam_type}
        for (String[] s : sessions) sessionBox.addItem(s[1] + " (" + s[2] + ")");

        JComboBox<String> semesterBox = new JComboBox<>(
                new String[]{"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"});
        if (semester != null) semesterBox.setSelectedItem(semester);

        JButton submitBtn = new JButton("Submit");
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        submitBtn.setBackground(TEAL);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorderPainted(false);
        submitBtn.setOpaque(true);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        filterRow.setBackground(Color.WHITE);
        filterRow.add(labeledField("Select Exam Session:", sessionBox));
        filterRow.add(labeledField("Select Semester:", semesterBox));
        filterCard.add(filterRow);

        JPanel submitRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        submitRow.setBackground(Color.WHITE);
        submitRow.add(submitBtn);
        filterCard.add(submitRow);

        scrollContent.add(filterCard);
        scrollContent.add(Box.createVerticalStrut(20));

        admitCardResultSlot = new JPanel(new BorderLayout());
        admitCardResultSlot.setBackground(LIGHT_GRAY);
        admitCardResultSlot.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.add(admitCardResultSlot);

        submitBtn.addActionListener(e -> {
            int idx = sessionBox.getSelectedIndex();
            if (idx <= 0) {
                JOptionPane.showMessageDialog(panel, "Please select an exam session.",
                        "Missing Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String[] chosen = sessions.get(idx - 1); // {id, session_name, exam_type}
            loadAdmitCardResult(chosen, (String) semesterBox.getSelectedItem());
        });

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(LIGHT_GRAY);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /** Sessions configured by admin for this student's course/department/semester. */
    private java.util.List<String[]> fetchExamSessions(String semester) {
        java.util.List<String[]> result = new java.util.ArrayList<>();
        if (semester == null) return result;

        String sql = "SELECT id, session_name, exam_type FROM exam_sessions " +
                "WHERE course = ? AND department = ? AND semester = ? ORDER BY exam_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course);
            stmt.setString(2, department);
            stmt.setString(3, semester);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new String[]{
                            String.valueOf(rs.getInt("id")),
                            rs.getString("session_name"),
                            rs.getString("exam_type")
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /** Checks attendance_percentage >= 75 for every subject in the semester; builds the result row. */
    private void loadAdmitCardResult(String[] session, String semester) {
        admitCardResultSlot.removeAll();

        java.util.List<String[]> shortfall = new java.util.ArrayList<>(); // {subjectName, percent}
        String sql = "SELECT s.subject_name, sm.attendance_percentage " +
                "FROM student_marks sm JOIN subjects s ON sm.subject_id = s.subject_id " +
                "WHERE sm.roll_no = ? AND sm.semester_recorded = ?";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            stmt.setString(2, semester);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double pct = rs.getDouble("attendance_percentage");
                    if (pct < 75.0) {
                        shortfall.add(new String[]{rs.getString("subject_name"), String.format("%.1f", pct)});
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        boolean eligible = shortfall.isEmpty();
        String classLabel = course + " " + department.substring(0, Math.min(3, department.length())) +
                " " + semester;

        String[] columns = {"Class", "Exam Component", "Eligibility", ""};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        model.addRow(new Object[]{classLabel, session[2], eligible ? "Eligible" : "Not Eligible", ""});

        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);

        // Print / status button in the last column
        table.getColumnModel().getColumn(3).setCellRenderer((t, value, isSelected, hasFocus, row, col) -> {
            JButton btn = new JButton(eligible ? "Print Now" : "View Shortfall");
            btn.setBackground(eligible ? TEAL : new Color(0xE0, 0x6C, 0x5B));
            btn.setForeground(Color.WHITE);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            return btn;
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (table.columnAtPoint(e.getPoint()) == 3) {
                    if (eligible) showHallTicketDialog(session, semester);
                    else showShortfallDialog(shortfall);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1));
        scrollPane.setPreferredSize(new Dimension(680, 80));

        admitCardResultSlot.add(scrollPane, BorderLayout.CENTER);
        admitCardResultSlot.revalidate();
        admitCardResultSlot.repaint();
    }

    private void showShortfallDialog(java.util.List<String[]> shortfall) {
        StringBuilder msg = new StringBuilder("Attendance below 75% in:\n\n");
        for (String[] row : shortfall) msg.append("\u2022 ").append(row[0]).append(": ").append(row[1]).append("%\n");
        JOptionPane.showMessageDialog(this, msg.toString(), "Not Eligible", JOptionPane.WARNING_MESSAGE);
    }

    /** Renders the printable hall ticket and lets the student print it via PrinterJob. */
    private void showHallTicketDialog(String[] session, String semester) {
        JDialog dialog = new JDialog(this, "Hall Ticket", true);
        dialog.setSize(650, 700);
        dialog.setLocationRelativeTo(this);

        JPanel ticket = new JPanel();
        ticket.setLayout(new BoxLayout(ticket, BoxLayout.Y_AXIS));
        ticket.setBackground(Color.WHITE);
        ticket.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TEAL, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel headerTitle = new JLabel("Hall Ticket", SwingConstants.CENTER);
        headerTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel headerSub = new JLabel(session[1], SwingConstants.CENTER);
        headerSub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        headerSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        ticket.add(headerTitle);
        ticket.add(headerSub);
        ticket.add(Box.createVerticalStrut(15));

        String[] fields = fetchFullStudentRecord();
        ticket.add(ticketRow("Roll Number:", rollNo));
        ticket.add(ticketRow("Application Number:", fields[12]));
        ticket.add(ticketRow("Name of Student:", studentName));
        ticket.add(ticketRow("Father's Name:", fields[0]));
        ticket.add(ticketRow("Mother's Name:", fields[1]));
        ticket.add(ticketRow("Program:", course + " - " + department));
        ticket.add(ticketRow("Semester:", semester));
        ticket.add(ticketRow("Exam Type:", session[2]));
        ticket.add(Box.createVerticalStrut(12));

        String[] cols = {"Course Code", "Course Name", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        String sql = "SELECT s.subject_id, s.subject_name FROM student_marks sm " +
                "JOIN subjects s ON sm.subject_id = s.subject_id " +
                "WHERE sm.roll_no = ? AND sm.semester_recorded = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            stmt.setString(2, semester);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getString("subject_id"), rs.getString("subject_name"), "Eligible"});
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        JTable subjectTable = new JTable(model);
        subjectTable.setRowHeight(26);
        JScrollPane subjScroll = new JScrollPane(subjectTable);
        subjScroll.setPreferredSize(new Dimension(560, 140));
        subjScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        ticket.add(subjScroll);

        ticket.add(Box.createVerticalStrut(20));
        JLabel note = new JLabel("Student must bring the College ID Card along with this Hall Ticket.");
        note.setFont(new Font("SansSerif", Font.ITALIC, 11));
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        ticket.add(note);

        JButton printBtn = new JButton("Print");
        printBtn.setBackground(TEAL);
        printBtn.setForeground(Color.WHITE);
        printBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        printBtn.addActionListener(e -> {
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) return java.awt.print.Printable.NO_SUCH_PAGE;
                Graphics2D g2 = (Graphics2D) graphics;
                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                ticket.printAll(g2);
                return java.awt.print.Printable.PAGE_EXISTS;
            });
            if (job.printDialog()) {
                try { job.print(); } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        JScrollPane outer = new JScrollPane(ticket);
        outer.setBorder(null);
        dialog.add(outer, BorderLayout.CENTER);
        dialog.add(printBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel ticketRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(600, 24));
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setPreferredSize(new Dimension(160, 20));
        JLabel v = new JLabel(value != null ? value : "-");
        v.setFont(new Font("SansSerif", Font.PLAIN, 12));
        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.CENTER);
        return row;
    }
    // ---------------- Feedback ----------------
    private DefaultTableModel myFeedbackTableModel;

    private JPanel buildFeedbackCard() {
        JPanel panel = new JPanel(new BorderLayout());      
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(LIGHT_GRAY);

        JPanel formSection = buildFeedbackFormSection();
        formSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.add(formSection);
        scrollContent.add(Box.createVerticalStrut(25));

        JPanel historySection = buildMyFeedbackHistorySection();
        historySection.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollContent.add(historySection);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(LIGHT_GRAY);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildFeedbackFormSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JLabel title = new JLabel("Submit Feedback");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(title);

        JLabel subtitle = new JLabel("Tell us about a teacher, the curriculum, holidays/calendar, facilities, or a general suggestion.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(0x7F, 0x8C, 0x8D));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 12, 0));
        section.add(subtitle);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterRow.setBackground(Color.WHITE);
        filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> categoryBox = new JComboBox<>(new String[]{
                "Teacher", "Curriculum", "Holiday / Calendar", "Facilities", "Suggestion", "Other"
        });
        categoryBox.setFont(new Font("SansSerif", Font.PLAIN, 13));

        JTextField relatedToField = new JTextField(18);
        relatedToField.setFont(new Font("SansSerif", Font.PLAIN, 13));

        filterRow.add(labeledField("Category:", categoryBox));

        JPanel relatedWrapper = new JPanel();
        relatedWrapper.setLayout(new BoxLayout(relatedWrapper, BoxLayout.Y_AXIS));
        relatedWrapper.setBackground(Color.WHITE);
        JLabel relatedLabel = new JLabel("Related To (e.g. teacher/subject name):");
        relatedLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        relatedLabel.setForeground(NAVY);
        relatedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        relatedToField.setAlignmentX(Component.LEFT_ALIGNMENT);
        relatedToField.setMaximumSize(new Dimension(260, 28));
        relatedWrapper.add(relatedLabel);
        relatedWrapper.add(Box.createVerticalStrut(4));
        relatedWrapper.add(relatedToField);
        filterRow.add(relatedWrapper);

        section.add(filterRow);
        section.add(Box.createVerticalStrut(12));

        JLabel messageLabel = new JLabel("Your Feedback:");
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        messageLabel.setForeground(NAVY);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(messageLabel);
        section.add(Box.createVerticalStrut(4));

        JTextArea messageArea = new JTextArea(4, 40);
        messageArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        messageScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        messageScroll.setBorder(BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1));
        section.add(messageScroll);
        section.add(Box.createVerticalStrut(12));

        JButton submitBtn = new JButton("Submit Feedback");
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        submitBtn.setBackground(TEAL);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorderPainted(false);
        submitBtn.setOpaque(true);
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        submitBtn.addActionListener(e -> {
            String category = (String) categoryBox.getSelectedItem();
            String relatedTo = relatedToField.getText().trim();
            String message = messageArea.getText().trim();

            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(section, "Please enter your feedback message.",
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = submitFeedback(category, relatedTo, message, section);
            if (ok) {
                relatedToField.setText("");
                messageArea.setText("");
                if (myFeedbackTableModel != null) loadMyFeedback(myFeedbackTableModel);
            }
        });
        section.add(submitBtn);

        return section;
    }

    private boolean submitFeedback(String category, String relatedTo, String message, Component parent) {
        String sql = "INSERT INTO feedback (roll_no, category, related_to, message) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            stmt.setString(2, category);
            stmt.setString(3, relatedTo.isEmpty() ? null : relatedTo);
            stmt.setString(4, message);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(parent, "Feedback submitted. Thank you!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Failed to submit feedback: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return false;
        }
    }

    private JPanel buildMyFeedbackHistorySection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(LIGHT_GRAY);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(LIGHT_GRAY);
        headerRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel titleLabel = new JLabel("My Feedback History");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(NAVY);
        headerRow.add(titleLabel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("\u21bb Refresh");
        refreshBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        refreshBtn.setBackground(new Color(0xE8, 0xE8, 0xE8));
        refreshBtn.setForeground(NAVY);
        refreshBtn.setFocusPainted(false);
        headerRow.add(refreshBtn, BorderLayout.EAST);

        section.add(headerRow, BorderLayout.NORTH);

        String[] columns = {"Category", "Related To", "Message", "Submitted On", "Status", "Admin Response"};
        myFeedbackTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(myFeedbackTableModel);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(0xEE, 0xEE, 0xEE));

        table.getColumnModel().getColumn(4).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                            boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                String status = value != null ? value.toString() : "";
                if ("Resolved".equalsIgnoreCase(status)) setForeground(new Color(0x27, 0xAE, 0x60));
                else if ("Rejected".equalsIgnoreCase(status)) setForeground(new Color(0xE0, 0x6C, 0x5B));
                else setForeground(new Color(0xE6, 0x7E, 0x22));
                setFont(new Font("SansSerif", Font.BOLD, 13));
                setBackground(Color.WHITE);
                return c;
            }
        });

        loadMyFeedback(myFeedbackTableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1));

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tableWrapper.add(scrollPane, BorderLayout.CENTER);
        tableWrapper.setPreferredSize(new Dimension(680, 220));

        section.add(tableWrapper, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> loadMyFeedback(myFeedbackTableModel));

        return section;
    }

    private void loadMyFeedback(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        String sql = "SELECT category, related_to, message, submitted_date, status, admin_response " +
                "FROM feedback WHERE roll_no = ? ORDER BY submitted_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    java.sql.Timestamp submitted = rs.getTimestamp("submitted_date");
                    String dateStr = submitted != null
                            ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(submitted)
                            : "N/A";
                    String status = rs.getString("status");
                    String response = rs.getString("admin_response");
                    tableModel.addRow(new Object[]{
                            rs.getString("category"),
                            rs.getString("related_to") != null ? rs.getString("related_to") : "-",
                            rs.getString("message"), dateStr,
                            status != null ? status : "Pending",
                            (response != null && !response.isEmpty()) ? response : "-"
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Backlog registration form: one checkbox per backlog subject + its own submit button. */
    private JPanel buildBacklogRegistrationForm(java.util.List<String[]> backlogSubjects) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE0, 0x6C, 0x5B), 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JLabel title = new JLabel("Backlog Registration");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(title);

        JLabel subtitle = new JLabel("Select the backlog subjects you want to register for this cycle:");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(0x7F, 0x8C, 0x8D));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 12, 0));
        section.add(subtitle);

        backlogCheckBoxes.clear();

        for (String[] subject : backlogSubjects) {
            String code = subject[0];
            String name = subject[1];
            String semester = subject[2];
            String status = subject[3];

            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(Color.WHITE);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

            JCheckBox checkBox = new JCheckBox(name + " (" + code + ")  \u2014 Sem " + semester + ", " + status);
            checkBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
            checkBox.setForeground(NAVY);
            checkBox.setBackground(Color.WHITE);
            checkBox.putClientProperty("subjectCode", code);
            checkBox.putClientProperty("subjectName", name);

            backlogCheckBoxes.add(checkBox);
            row.add(checkBox, BorderLayout.WEST);
            section.add(row);
        }

        section.add(Box.createVerticalStrut(12));

        JButton submitBtn = new JButton("Register Selected Backlog Subjects");
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        submitBtn.setBackground(new Color(0xE0, 0x6C, 0x5B));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorderPainted(false);
        submitBtn.setOpaque(true);
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        submitBtn.addActionListener(e -> {
            StringBuilder selected = new StringBuilder();
            int count = 0;
            for (JCheckBox cb : backlogCheckBoxes) {
                if (cb.isSelected()) {
                    selected.append("\u2022 ").append(cb.getClientProperty("subjectName")).append("\n");
                    count++;
                }
            }
            if (count == 0) {
                JOptionPane.showMessageDialog(section,
                        "Please select at least one backlog subject to register.",
                        "No Subjects Selected", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(section,
                        "Backlog registration submitted for:\n\n" + selected,
                        "Backlog Registered", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        section.add(submitBtn);

        return section;
    }

    /** Backlog = Absent, or Present with less than 40% marks. Fills `out` with
     *  {subjectCode, subjectName, semester, status} rows and returns the count. */
    private int loadBacklogSubjects(java.util.List<String[]> out) {
        String sql = "SELECT s.subject_id, s.subject_name, sm.semester_recorded, sm.marks_obtained, " +
                "sm.total_marks, sm.attendance_status " +
                "FROM student_marks sm JOIN subjects s ON sm.subject_id = s.subject_id " +
                "WHERE sm.roll_no = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    boolean absent = "Absent".equalsIgnoreCase(rs.getString("attendance_status"));
                    int obtained = rs.getInt("marks_obtained");
                    int total = rs.getInt("total_marks");
                    boolean failed = total > 0 && (obtained * 100.0 / total) < 40;

                    if (absent || failed) {
                        out.add(new String[]{
                                rs.getString("subject_id"), rs.getString("subject_name"),
                                rs.getString("semester_recorded"), absent ? "Absent" : "Failed"
                        });
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return out.size();
    }

    // ---- Semester Registration: Academic Year + Semester -> subject checklist -> Pending request ----
    private JPanel semesterSubjectListSlot;
    private final java.util.List<JCheckBox> semesterSubjectCheckBoxes = new java.util.ArrayList<>();
    private DefaultTableModel myRegistrationsTableModel;

    /** Academic Year + Semester selectors, followed by a checklist of that semester's subjects. */
    private JPanel buildSemesterRegistrationForm() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JLabel title = new JLabel("Semester Registration");
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(NAVY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(title);

        JLabel subtitle = new JLabel("Choose the academic year and semester, then select the subjects to register for.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(0x7F, 0x8C, 0x8D));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 12, 0));
        section.add(subtitle);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        filterRow.setBackground(Color.WHITE);
        filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> yearBox = new JComboBox<>(new String[]{"2024-2025", "2025-2026", "2026-2027"});
        yearBox.setSelectedItem("2026-2027");

        String[] semesters = {"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"};
        JComboBox<String> semesterBox = new JComboBox<>(semesters);
        String currentSemester = fetchCurrentSemester();
        // default to the semester *after* the current one, since this form registers for the upcoming term
        String defaultTarget = nextSemesterLabel(currentSemester, semesters);
        semesterBox.setSelectedItem(defaultTarget != null ? defaultTarget
                : (currentSemester != null ? currentSemester : semesters[0]));

        for (JComboBox<String> box : new JComboBox[]{yearBox, semesterBox}) {
            box.setFont(new Font("SansSerif", Font.PLAIN, 13));
        }

        filterRow.add(labeledField("Academic Year:", yearBox));
        filterRow.add(labeledField("Semester:", semesterBox));
        section.add(filterRow);
        section.add(Box.createVerticalStrut(14));

        JLabel subjectsLabel = new JLabel("Subjects Available for Registration");
        subjectsLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        subjectsLabel.setForeground(NAVY);
        subjectsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subjectsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        section.add(subjectsLabel);

        // ---- subject checklist slot, rebuilt whenever the semester dropdown changes ----
        semesterSubjectListSlot = new JPanel();
        semesterSubjectListSlot.setLayout(new BoxLayout(semesterSubjectListSlot, BoxLayout.Y_AXIS));
        semesterSubjectListSlot.setBackground(Color.WHITE);
        semesterSubjectListSlot.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(semesterSubjectListSlot);

        section.add(Box.createVerticalStrut(12));

        JButton submitBtn = new JButton("Submit Registration");
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        submitBtn.setBackground(TEAL);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorderPainted(false);
        submitBtn.setOpaque(true);
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.addActionListener(e -> submitSemesterRegistration(
                (String) yearBox.getSelectedItem(), (String) semesterBox.getSelectedItem(), section));
        section.add(submitBtn);

        semesterBox.addActionListener(e -> refreshSemesterSubjectList((String) semesterBox.getSelectedItem()));

        refreshSemesterSubjectList((String) semesterBox.getSelectedItem());

        return section;
    }

    /** Returns the semester right after `current` in the ordered list, or null if there isn't one. */
    private String nextSemesterLabel(String current, String[] semesters) {
        if (current == null) return null;
        for (int i = 0; i < semesters.length - 1; i++) {
            if (semesters[i].equalsIgnoreCase(current)) {
                return semesters[i + 1];
            }
        }
        return null;
    }

    /** Rebuilds the subject checklist for the chosen semester; subjects already requested (any status) are disabled. */
    private void refreshSemesterSubjectList(String semester) {
        semesterSubjectListSlot.removeAll();
        semesterSubjectCheckBoxes.clear();

        java.util.List<String[]> subjects = fetchSubjectsForSemester(semester);
        java.util.Set<String> alreadyRequested = fetchRegisteredSubjectIds(semester);

        if (subjects.isEmpty()) {
            JLabel emptyLabel = new JLabel("No subjects found for this semester yet.");
            emptyLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            emptyLabel.setForeground(new Color(0x7F, 0x8C, 0x8D));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            semesterSubjectListSlot.add(emptyLabel);
        } else {
            for (String[] subject : subjects) {
                String code = subject[0];
                String name = subject[1];
                boolean requested = alreadyRequested.contains(code);

                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(Color.WHITE);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

                JCheckBox checkBox = new JCheckBox(name + " (" + code + ")" +
                        (requested ? "  \u2014 already requested" : ""));
                checkBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
                checkBox.setForeground(requested ? new Color(0x9B, 0x9B, 0x9B) : NAVY);
                checkBox.setBackground(Color.WHITE);
                checkBox.setEnabled(!requested);
                checkBox.putClientProperty("subjectCode", code);
                checkBox.putClientProperty("subjectName", name);

                semesterSubjectCheckBoxes.add(checkBox);
                row.add(checkBox, BorderLayout.WEST);
                semesterSubjectListSlot.add(row);
            }
        }

        semesterSubjectListSlot.revalidate();
        semesterSubjectListSlot.repaint();
    }

    /** Distinct subjects taught for this student's course/department in the given semester,
     *  derived from the timetable (reuses existing schema instead of a separate curriculum table). */
    private java.util.List<String[]> fetchSubjectsForSemester(String semester) {
        java.util.List<String[]> result = new java.util.ArrayList<>();
        if (semester == null) return result;

        String sql = "SELECT DISTINCT s.subject_id, s.subject_name " +
                "FROM timetable tt JOIN subjects s ON tt.subject_id = s.subject_id " +
                "WHERE tt.course = ? AND tt.department = ? AND tt.semester = ? " +
                "ORDER BY s.subject_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course);
            stmt.setString(2, department);
            stmt.setString(3, semester);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new String[]{rs.getString("subject_id"), rs.getString("subject_name")});
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /** Subject IDs this student has already requested (any status) for the given semester. */
    private java.util.Set<String> fetchRegisteredSubjectIds(String semester) {
        java.util.Set<String> result = new java.util.HashSet<>();
        if (semester == null) return result;

        String sql = "SELECT subject_id FROM subject_registrations WHERE roll_no = ? AND semester = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            stmt.setString(2, semester);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("subject_id"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Inserts one 'Pending' row per selected subject into subject_registrations.
     * Nothing appears in Attendance / Performance / Time Table yet - those only reflect a
     * subject once an admin approves the request (see the note above loadMyRegistrations()).
     */
    private void submitSemesterRegistration(String academicYear, String semester, JPanel parent) {
        java.util.List<String> selectedCodes = new java.util.ArrayList<>();
        for (JCheckBox cb : semesterSubjectCheckBoxes) {
            if (cb.isSelected() && cb.isEnabled()) {
                selectedCodes.add((String) cb.getClientProperty("subjectCode"));
            }
        }

        if (selectedCodes.isEmpty()) {
            JOptionPane.showMessageDialog(parent,
                    "Please select at least one subject to register for.",
                    "No Subjects Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO subject_registrations " +
                "(roll_no, subject_id, academic_year, semester, status, requested_date) " +
                "VALUES (?, ?, ?, ?, 'Pending', CURRENT_TIMESTAMP)";

        int insertedCount = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (String code : selectedCodes) {
                stmt.setString(1, rollNo);
                stmt.setString(2, code);
                stmt.setString(3, academicYear);
                stmt.setString(4, semester);
                stmt.addBatch();
                insertedCount++;
            }
            stmt.executeBatch();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "Something went wrong submitting your registration. Please try again.",
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(parent,
                "Registration request submitted for " + insertedCount + " subject(s).\n" +
                        "Status: Pending admin approval.",
                "Registration Submitted", JOptionPane.INFORMATION_MESSAGE);

        refreshSemesterSubjectList(semester);
        if (myRegistrationsTableModel != null) {
            loadMyRegistrations(myRegistrationsTableModel);
        }
    }

    /**
     * Shows every registration request this student has made, with its live status
     * (Pending / Approved / Rejected) and a Refresh button, since approval happens on the
     * admin side, outside this window.
     *
     * APPROVAL FLOW (to implement on the admin/staff side, not in this file):
     *  1. Admin reviews subject_registrations WHERE status = 'Pending'.
     *  2. On approval: UPDATE subject_registrations SET status = 'Approved' WHERE id = ?;
     *     then INSERT INTO student_marks (roll_no, subject_id, semester_recorded,
     *     marks_obtained, total_marks, attendance_status) VALUES (roll_no, subject_id,
     *     semester, 0, 100, 'Absent') so the subject starts appearing automatically in
     *     this student's Attendance and Performance pages (both already query
     *     student_marks by roll_no + semester_recorded).
     *  3. Time Table already lists every subject taught for the student's course/department/
     *     semester regardless of registration status, so no extra step is needed there.
     *  4. On rejection: UPDATE subject_registrations SET status = 'Rejected' WHERE id = ?;
     *     (no student_marks row is created).
     */
    private JPanel buildMyRegistrationsSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(LIGHT_GRAY);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(LIGHT_GRAY);
        headerRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel titleLabel = new JLabel("My Registration Requests");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(NAVY);
        headerRow.add(titleLabel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("\u21bb Refresh");
        refreshBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        refreshBtn.setBackground(new Color(0xE8, 0xE8, 0xE8));
        refreshBtn.setForeground(NAVY);
        refreshBtn.setFocusPainted(false);
        headerRow.add(refreshBtn, BorderLayout.EAST);

        section.add(headerRow, BorderLayout.NORTH);

        String[] columns = {"Subject", "Code", "Academic Year", "Semester", "Requested On", "Status"};
        myRegistrationsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(myRegistrationsTableModel);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(0xEE, 0xEE, 0xEE));

        // Color the Status column: green = Approved, orange = Pending, red = Rejected.
        table.getColumnModel().getColumn(columns.length - 1).setCellRenderer(
                new javax.swing.table.DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                                     boolean hasFocus, int row, int column) {
                        Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                        String status = value != null ? value.toString() : "";
                        if ("Approved".equalsIgnoreCase(status)) {
                            setForeground(new Color(0x27, 0xAE, 0x60));
                        } else if ("Rejected".equalsIgnoreCase(status)) {
                            setForeground(new Color(0xE0, 0x6C, 0x5B));
                        } else {
                            setForeground(new Color(0xE6, 0x7E, 0x22));
                        }
                        setFont(new Font("SansSerif", Font.BOLD, 13));
                        setBackground(Color.WHITE);
                        return c;
                    }
                });

        loadMyRegistrations(myRegistrationsTableModel);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1));
        tableWrapper.add(table.getTableHeader(), BorderLayout.NORTH);
        tableWrapper.add(table, BorderLayout.CENTER);
        tableWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        section.add(tableWrapper, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> loadMyRegistrations(myRegistrationsTableModel));

        return section;
    }

    private void loadMyRegistrations(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);

        String sql = "SELECT s.subject_name, sr.subject_id, sr.academic_year, sr.semester, " +
                "sr.requested_date, sr.status " +
                "FROM subject_registrations sr JOIN subjects s ON sr.subject_id = s.subject_id " +
                "WHERE sr.roll_no = ? ORDER BY sr.requested_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    java.sql.Timestamp requested = rs.getTimestamp("requested_date");
                    String dateStr = requested != null
                            ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(requested)
                            : "N/A";
                    tableModel.addRow(new Object[]{
                            rs.getString("subject_name"), rs.getString("subject_id"),
                            rs.getString("academic_year"), rs.getString("semester"),
                            dateStr, rs.getString("status")
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ---- Calendar (College Info): list of public holidays ----
    private JPanel buildCalendarPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(LIGHT_GRAY);
        headerRow.add(buildBackToCollegeInfoButton(), BorderLayout.WEST);

        JLabel titleLabel = new JLabel("Public Holidays", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(NAVY);
        headerRow.add(titleLabel, BorderLayout.CENTER);
        panel.add(headerRow, BorderLayout.NORTH);

        java.util.List<String[]> holidayList = fetchHolidays();
        String[][] holidays = holidayList.toArray(new String[0][]);

        String[] columns = {"Date", "Holiday"};
        JTable table = new JTable(holidays, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(0xEE, 0xEE, 0xEE));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1));

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        panel.add(tableWrapper, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Loads holidays from DB if a `holidays` table exists (admin-managed).
     * Expected columns: holiday_date (DATE), holiday_name (VARCHAR).
     * Falls back to built-in sample list when the table is missing or empty.
     */
    private java.util.List<String[]> fetchHolidays() {
        java.util.List<String[]> list = new java.util.ArrayList<>();
        String sql = "SELECT holiday_date, holiday_name FROM holidays ORDER BY holiday_date";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("d MMM yyyy");
            while (rs.next()) {
                java.sql.Date d = rs.getDate("holiday_date");
                String name = rs.getString("holiday_name");
                list.add(new String[]{
                        d != null ? fmt.format(d) : "N/A",
                        name != null ? name : "Holiday"
                });
            }
        } catch (Exception ignored) {
            // table may not exist yet
        }
        if (list.isEmpty()) {
            // Sample fallback (admin can replace via holidays table)
            String[][] sample = {
                    {"26 Jan 2026", "Republic Day"},
                    {"4 Mar 2026", "Holi"},
                    {"14 Apr 2026", "Ambedkar Jayanti"},
                    {"15 Aug 2026", "Independence Day"},
                    {"2 Oct 2026", "Gandhi Jayanti"},
                    {"20 Oct 2026", "Dussehra"},
                    {"8 Nov 2026", "Diwali"},
                    {"25 Dec 2026", "Christmas"},
            };
            for (String[] row : sample) list.add(row);
        }
        return list;
    }

    /** Holiday dates as yyyy-MM-dd for calendar marking. */
    private java.util.Set<String> fetchHolidayDateKeys() {
        java.util.Set<String> keys = new java.util.HashSet<>();
        String sql = "SELECT holiday_date FROM holidays";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                java.sql.Date d = rs.getDate("holiday_date");
                if (d != null) keys.add(d.toString());
            }
        } catch (Exception ignored) {
        }
        if (keys.isEmpty()) {
            // Match sample holidays
            keys.add("2026-01-26");
            keys.add("2026-03-04");
            keys.add("2026-04-14");
            keys.add("2026-08-15");
            keys.add("2026-10-02");
            keys.add("2026-10-20");
            keys.add("2026-11-08");
            keys.add("2026-12-25");
        }
        return keys;
    }

    // ---------------- LMS ----------------
    private CardLayout lmsLayout;
    private JPanel lmsPanel;
    private JPanel lmsCourseGridSlot;
    private JComboBox<String> lmsYearBox;
    private JComboBox<String> lmsClassBox;
    private JComboBox<String> lmsTypeFilter; // holds current type: All / Theory / Practical / Tutorial
    private JTextField lmsSearchField;
    private int lmsCalendarYear;
    private int lmsCalendarMonth; // 0-based

    private JPanel buildLmsCard() {
        lmsLayout = new CardLayout();
        lmsPanel = new JPanel(lmsLayout);
        lmsPanel.setBackground(LIGHT_GRAY);

        lmsPanel.add(buildLmsHub(), "Hub");
        lmsPanel.add(buildLmsMyCoursesPage(), "MyCourses");
        JPanel initialCal = buildLmsCalendarPage();
        initialCal.setName("LmsCalendar");
        lmsPanel.add(initialCal, "LmsCalendar");

        lmsLayout.show(lmsPanel, "Hub");
        return lmsPanel;
    }

    /** Rebuilds the LMS calendar card after year/month change. */
    private void rebuildLmsCalendarCard() {
        // Remove existing calendar card if present (search by name via components)
        for (Component c : lmsPanel.getComponents()) {
            // CardLayout keeps all cards; replace by removing all and re-adding is heavy —
            // instead remove the one that is the calendar by checking client property.
            if (c instanceof JPanel && "LmsCalendar".equals(c.getName())) {
                lmsPanel.remove(c);
                break;
            }
        }
        JPanel cal = buildLmsCalendarPage();
        cal.setName("LmsCalendar");
        lmsPanel.add(cal, "LmsCalendar");
        lmsLayout.show(lmsPanel, "LmsCalendar");
        lmsPanel.revalidate();
        lmsPanel.repaint();
    }

    private JPanel buildLmsHub() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("LMS");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(NAVY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 3, 18, 18));
        grid.setBackground(LIGHT_GRAY);

        grid.add(buildLmsTile("My Courses", "\uD83D\uDCDA", () -> {
            refreshLmsCourseGrid();
            lmsLayout.show(lmsPanel, "MyCourses");
        }));
        grid.add(buildLmsTile("Calendar", "\uD83D\uDCC5", () -> {
            lmsCalendarYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            lmsCalendarMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH);
            rebuildLmsCalendarCard();
        }));
        grid.add(buildLmsTile("Timetable", "\uD83D\uDD50", () -> showContentCard("Time Table")));
        grid.add(buildLmsTile("Attendance", "\u2713", () -> showContentCard("Attendance")));
        grid.add(new JPanel() {{ setOpaque(false); }});
        grid.add(new JPanel() {{ setOpaque(false); }});

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(LIGHT_GRAY);
        wrap.add(grid, BorderLayout.NORTH);
        panel.add(wrap, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildLmsTile(String label, String iconGlyph, Runnable onClick) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1, true),
                BorderFactory.createEmptyBorder(22, 12, 16, 12)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(170, 130));

        JLabel icon = new JLabel(iconGlyph, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xE8, 0xF8, 0xF3));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(TEAL);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 22));
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                int tw = fm.stringWidth(t);
                int th = fm.getAscent();
                g2.drawString(t, (getWidth() - tw) / 2, (getHeight() + th) / 2 - 4);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(52, 52);
            }

            @Override
            public Dimension getMaximumSize() {
                return new Dimension(52, 52);
            }
        };
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Pill-style label
        JLabel pill = new JLabel(label, SwingConstants.CENTER);
        pill.setFont(new Font("SansSerif", Font.PLAIN, 13));
        pill.setForeground(NAVY);
        pill.setOpaque(true);
        pill.setBackground(new Color(0xE8, 0xF8, 0xF3));
        pill.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        pill.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(icon);
        card.add(Box.createVerticalStrut(14));
        card.add(pill);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(0xF7, 0xF9, 0xFC));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });
        return card;
    }

    private JButton buildBackToLmsButton() {
        JButton backBtn = new JButton("\u2190 Back to LMS");
        backBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        backBtn.setBackground(new Color(0xE8, 0xE8, 0xE8));
        backBtn.setForeground(NAVY);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> lmsLayout.show(lmsPanel, "Hub"));
        return backBtn;
    }

    // ---- LMS: My Courses ----
    private JPanel buildLmsMyCoursesPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(LIGHT_GRAY);
        top.add(buildBackToLmsButton(), BorderLayout.WEST);
        JLabel crumb = new JLabel("  LMS  \u203A  My Course List");
        crumb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        crumb.setForeground(new Color(0x5B, 0x6A, 0xAE));
        top.add(crumb, BorderLayout.CENTER);
        panel.add(top, BorderLayout.NORTH);

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xDD, 0xDD, 0xDD)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        lmsYearBox = new JComboBox<>(new String[]{"2024-2025", "2025-2026", "2026-2027"});
        lmsYearBox.setSelectedItem("2025-2026");
        lmsYearBox.setFont(new Font("SansSerif", Font.PLAIN, 13));

        String sem = fetchCurrentSemester();
        String classLabel = course + " - " + department
                + (sem != null ? " - Sem " + sem : "");
        lmsClassBox = new JComboBox<>(new String[]{classLabel});
        lmsClassBox.setFont(new Font("SansSerif", Font.PLAIN, 13));

        // Type tabs as combo for simplicity + buttons
        JPanel typeTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        typeTabs.setBackground(Color.WHITE);
        String[] types = {"All", "Theory", "Practical", "Tutorial"};
        lmsTypeFilter = new JComboBox<>(types); // hidden state holder
        lmsTypeFilter.setSelectedItem("All");
        java.util.List<JButton> typeBtns = new java.util.ArrayList<>();
        for (String t : types) {
            JButton b = new JButton(t);
            b.setFont(new Font("SansSerif", Font.PLAIN, 12));
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            styleLmsTypeBtn(b, t.equals("All"));
            b.addActionListener(e -> {
                lmsTypeFilter.setSelectedItem(t);
                for (JButton other : typeBtns) {
                    styleLmsTypeBtn(other, other.getText().equals(t));
                }
                refreshLmsCourseGrid();
            });
            typeBtns.add(b);
            typeTabs.add(b);
        }

        lmsSearchField = new JTextField(16);
        lmsSearchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lmsSearchField.putClientProperty("JTextField.placeholderText", "Type to begin search");

        filterBar.add(lmsYearBox);
        filterBar.add(lmsClassBox);
        filterBar.add(typeTabs);
        filterBar.add(Box.createHorizontalStrut(8));
        filterBar.add(lmsSearchField);

        lmsYearBox.addActionListener(e -> refreshLmsCourseGrid());
        lmsSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshLmsCourseGrid(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshLmsCourseGrid(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshLmsCourseGrid(); }
        });

        // Info banner
        JPanel banner = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        banner.setBackground(new Color(0xD4, 0xF0, 0xF0));
        banner.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JLabel bannerText = new JLabel(
                "Click on the course cards to view details. Courses are loaded from your timetable / subjects.");
        bannerText.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bannerText.setForeground(NAVY);
        banner.add(bannerText);

        lmsCourseGridSlot = new JPanel(new GridLayout(0, 4, 14, 14));
        lmsCourseGridSlot.setBackground(LIGHT_GRAY);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(LIGHT_GRAY);
        center.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        center.add(filterBar, BorderLayout.NORTH);

        JPanel mid = new JPanel(new BorderLayout(0, 10));
        mid.setBackground(LIGHT_GRAY);
        mid.add(banner, BorderLayout.NORTH);

        JScrollPane gridScroll = new JScrollPane(lmsCourseGridSlot);
        gridScroll.setBorder(BorderFactory.createEmptyBorder());
        gridScroll.getViewport().setBackground(LIGHT_GRAY);
        gridScroll.getVerticalScrollBar().setUnitIncrement(16);
        mid.add(gridScroll, BorderLayout.CENTER);
        center.add(mid, BorderLayout.CENTER);

        panel.add(center, BorderLayout.CENTER);

        refreshLmsCourseGrid();
        return panel;
    }

    private void styleLmsTypeBtn(JButton b, boolean active) {
        if (active) {
            b.setBackground(new Color(0x2A, 0xB4, 0xC8));
            b.setForeground(Color.WHITE);
            b.setOpaque(true);
            b.setBorderPainted(false);
        } else {
            b.setBackground(new Color(0xE8, 0xF4, 0xF8));
            b.setForeground(NAVY);
            b.setOpaque(true);
            b.setBorderPainted(false);
        }
    }

    private void refreshLmsCourseGrid() {
        if (lmsCourseGridSlot == null) return;
        lmsCourseGridSlot.removeAll();

        String typeFilter = lmsTypeFilter != null ? (String) lmsTypeFilter.getSelectedItem() : "All";
        String search = lmsSearchField != null ? lmsSearchField.getText().trim().toLowerCase() : "";
        String semester = fetchCurrentSemester();

        java.util.List<String[]> courses = fetchLmsCourses(semester);
        int shown = 0;
        for (String[] c : courses) {
            String code = c[0];
            String name = c[1];
            String faculty = c[2];
            String type = c[3];

            if (typeFilter != null && !"All".equals(typeFilter) && !type.equalsIgnoreCase(typeFilter)) {
                continue;
            }
            if (!search.isEmpty()) {
                String hay = (name + " " + code + " " + faculty).toLowerCase();
                if (!hay.contains(search)) continue;
            }

            lmsCourseGridSlot.add(buildLmsCourseCard(name, faculty, type, code));
            shown++;
        }
        if (shown == 0) {
            JLabel empty = new JLabel("No courses found for the selected filters.", SwingConstants.CENTER);
            empty.setFont(new Font("SansSerif", Font.PLAIN, 14));
            empty.setForeground(new Color(0x7F, 0x8C, 0x8D));
            lmsCourseGridSlot.setLayout(new BorderLayout());
            lmsCourseGridSlot.add(empty, BorderLayout.CENTER);
        } else {
            lmsCourseGridSlot.setLayout(new GridLayout(0, 4, 14, 14));
        }
        lmsCourseGridSlot.revalidate();
        lmsCourseGridSlot.repaint();
    }

    /**
     * Distinct subjects for this student's course/dept/semester from timetable (with faculty).
     * Type inferred from subject_id suffix: T=Theory, B/P=Practical, otherwise Theory.
     */
    private java.util.List<String[]> fetchLmsCourses(String semester) {
        java.util.List<String[]> result = new java.util.ArrayList<>();
        java.util.LinkedHashMap<String, String[]> byCode = new java.util.LinkedHashMap<>();

        String sql = "SELECT DISTINCT s.subject_id, s.subject_name, tt.faculty_name "
                + "FROM timetable tt JOIN subjects s ON tt.subject_id = s.subject_id "
                + "WHERE tt.course = ? AND tt.department = ? "
                + (semester != null ? "AND tt.semester = ? " : "")
                + "ORDER BY s.subject_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course);
            stmt.setString(2, department);
            if (semester != null) stmt.setString(3, semester);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("subject_id");
                    String name = rs.getString("subject_name");
                    String faculty = rs.getString("faculty_name");
                    if (faculty == null || faculty.isEmpty()) faculty = "Faculty TBA";
                    String type = inferCourseType(code);
                    byCode.putIfAbsent(code, new String[]{code, name, faculty, type});
                }
            }
        } catch (Exception ex) {
            // Fallback: subjects linked via student_marks
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT DISTINCT s.subject_id, s.subject_name FROM student_marks sm "
                                 + "JOIN subjects s ON sm.subject_id = s.subject_id "
                                 + "WHERE sm.roll_no = ?"
                                 + (semester != null ? " AND sm.semester_recorded = ?" : ""))) {
                stmt.setString(1, rollNo);
                if (semester != null) stmt.setString(2, semester);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String code = rs.getString("subject_id");
                        String name = rs.getString("subject_name");
                        byCode.putIfAbsent(code, new String[]{code, name, "Faculty TBA", inferCourseType(code)});
                    }
                }
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
        result.addAll(byCode.values());
        return result;
    }

    private String inferCourseType(String code) {
        if (code == null) return "Theory";
        String u = code.toUpperCase();
        if (u.endsWith("P") || u.endsWith("B") || u.contains("LAB")) return "Practical";
        if (u.endsWith("TU") || u.contains("TUT")) return "Tutorial";
        return "Theory";
    }

    private JPanel buildLmsCourseCard(String name, String faculty, String type, String code) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(0x5B, 0x6A, 0xE0)),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0xE5, 0xE7, 0xEB), 1, true),
                        BorderFactory.createEmptyBorder(14, 14, 12, 14))));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel nameLabel = new JLabel("<html><body style='width:140px'>" + name + "</body></html>");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        nameLabel.setForeground(NAVY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel facultyLabel = new JLabel(faculty);
        facultyLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        facultyLabel.setForeground(new Color(0x6B, 0x72, 0x80));
        facultyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel typeLabel = new JLabel(type);
        typeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        typeLabel.setForeground(new Color(0x5B, 0x6A, 0xE0));
        JLabel codeLabel = new JLabel(code);
        codeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        codeLabel.setForeground(new Color(0x9C, 0xA3, 0xAF));
        footer.add(typeLabel, BorderLayout.WEST);
        footer.add(codeLabel, BorderLayout.EAST);

        card.add(nameLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(sep);
        card.add(Box.createVerticalStrut(10));
        card.add(Box.createVerticalGlue());
        card.add(facultyLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(footer);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(0xF8, 0xFA, 0xFF));
                footer.setBackground(new Color(0xF8, 0xFA, 0xFF));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
                footer.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(card,
                        "Course: " + name + "\nCode: " + code + "\nFaculty: " + faculty
                                + "\nType: " + type + "\n\nTopic list / session plan / content "
                                + "can be added when LMS content tables are available.",
                        name, JOptionPane.INFORMATION_MESSAGE);
            }
        });

        return card;
    }

    // ---- LMS: Calendar (month grid with holidays marked) ----
    private JPanel buildLmsCalendarPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(LIGHT_GRAY);
        top.add(buildBackToLmsButton(), BorderLayout.WEST);
        JLabel crumb = new JLabel("  LMS  \u203A  Calendar");
        crumb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        crumb.setForeground(new Color(0x5B, 0x6A, 0xAE));
        top.add(crumb, BorderLayout.CENTER);
        panel.add(top, BorderLayout.NORTH);

        if (lmsCalendarYear == 0) {
            java.util.Calendar now = java.util.Calendar.getInstance();
            lmsCalendarYear = now.get(java.util.Calendar.YEAR);
            lmsCalendarMonth = now.get(java.util.Calendar.MONTH);
        }

        JPanel body = new JPanel(new BorderLayout(10, 0));
        body.setBackground(LIGHT_GRAY);
        body.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        // Left month list
        JPanel monthList = new JPanel();
        monthList.setLayout(new BoxLayout(monthList, BoxLayout.Y_AXIS));
        monthList.setBackground(new Color(0x2A, 0x9F, 0xD6));
        monthList.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        monthList.setPreferredSize(new Dimension(130, 0));

        JPanel yearNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        yearNav.setOpaque(false);
        JButton prevYear = new JButton("<");
        JButton nextYear = new JButton(">");
        JLabel yearLbl = new JLabel(String.valueOf(lmsCalendarYear));
        yearLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        yearLbl.setForeground(Color.WHITE);
        for (JButton b : new JButton[]{prevYear, nextYear}) {
            b.setFont(new Font("SansSerif", Font.BOLD, 12));
            b.setForeground(Color.WHITE);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        yearNav.add(prevYear);
        yearNav.add(yearLbl);
        yearNav.add(nextYear);
        monthList.add(yearNav);
        monthList.add(Box.createVerticalStrut(8));

        String[] monthNames = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        JPanel[] monthRows = new JPanel[12];
        for (int m = 0; m < 12; m++) {
            final int monthIndex = m;
            JPanel row = new JPanel(new BorderLayout());
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            row.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 8));
            boolean selected = (m == lmsCalendarMonth);
            row.setBackground(selected ? new Color(0x7B, 0x5E, 0xA7) : new Color(0x2A, 0x9F, 0xD6));
            JLabel ml = new JLabel(monthNames[m]);
            ml.setFont(new Font("SansSerif", Font.PLAIN, 13));
            ml.setForeground(Color.WHITE);
            row.add(ml, BorderLayout.WEST);
            row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            row.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    lmsCalendarMonth = monthIndex;
                    rebuildLmsCalendarCard();
                }
            });
            monthRows[m] = row;
            monthList.add(row);
        }

        prevYear.addActionListener(e -> {
            lmsCalendarYear--;
            rebuildLmsCalendarCard();
        });
        nextYear.addActionListener(e -> {
            lmsCalendarYear++;
            rebuildLmsCalendarCard();
        });

        // Center calendar grid
        JPanel calCard = new JPanel(new BorderLayout());
        calCard.setBackground(Color.WHITE);
        calCard.setBorder(BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1));

        JLabel monthTitle = new JLabel(monthNames[lmsCalendarMonth].toUpperCase() + " " + lmsCalendarYear,
                SwingConstants.CENTER);
        monthTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        monthTitle.setForeground(new Color(0x7B, 0x5E, 0xA7));
        monthTitle.setBorder(BorderFactory.createEmptyBorder(14, 0, 8, 0));
        calCard.add(monthTitle, BorderLayout.NORTH);

        JPanel daysGrid = new JPanel(new GridLayout(0, 7, 0, 0));
        daysGrid.setBackground(Color.WHITE);
        String[] dow = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String d : dow) {
            JLabel h = new JLabel(d, SwingConstants.CENTER);
            h.setFont(new Font("SansSerif", Font.BOLD, 12));
            h.setForeground(new Color(0x6B, 0x72, 0x80));
            h.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            daysGrid.add(h);
        }

        java.util.Set<String> holidayKeys = fetchHolidayDateKeys();
        java.util.Map<String, String> holidayNames = new java.util.HashMap<>();
        for (String[] h : fetchHolidays()) {
            // best-effort parse "d MMM yyyy"
            try {
                java.util.Date parsed = new java.text.SimpleDateFormat("d MMM yyyy").parse(h[0]);
                holidayNames.put(new java.text.SimpleDateFormat("yyyy-MM-dd").format(parsed), h[1]);
            } catch (Exception ignored) {
            }
        }

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.YEAR, lmsCalendarYear);
        cal.set(java.util.Calendar.MONTH, lmsCalendarMonth);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        // Convert Sunday=1..Saturday=7 to Monday-first index 0..6
        int firstDow = cal.get(java.util.Calendar.DAY_OF_WEEK);
        int offset = (firstDow == java.util.Calendar.SUNDAY) ? 6 : firstDow - java.util.Calendar.MONDAY;
        int daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);

        for (int i = 0; i < offset; i++) {
            daysGrid.add(new JLabel(""));
        }

        final String[] selectedHolidayName = {null};
        final JLabel sideDateLabel = new JLabel();
        final JLabel sideHolidayLabel = new JLabel();

        for (int day = 1; day <= daysInMonth; day++) {
            String key = String.format("%04d-%02d-%02d", lmsCalendarYear, lmsCalendarMonth + 1, day);
            boolean isHoliday = holidayKeys.contains(key);
            JPanel cell = new JPanel(new BorderLayout());
            cell.setBackground(Color.WHITE);
            cell.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
            JLabel dayLbl = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            dayLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            dayLbl.setForeground(NAVY);
            cell.add(dayLbl, BorderLayout.CENTER);
            if (isHoliday) {
                JLabel dot = new JLabel("\u25CF", SwingConstants.CENTER);
                dot.setFont(new Font("SansSerif", Font.PLAIN, 8));
                dot.setForeground(new Color(0xF5, 0xA6, 0x23));
                cell.add(dot, BorderLayout.SOUTH);
                cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                String hName = holidayNames.getOrDefault(key, "Holiday / Non Instructional");
                final int dayFinal = day;
                cell.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        sideDateLabel.setText(monthNames[lmsCalendarMonth] + " " + dayFinal + ", " + lmsCalendarYear);
                        sideHolidayLabel.setText("\u25CF  " + hName);
                    }
                });
            }
            daysGrid.add(cell);
        }
        calCard.add(daysGrid, BorderLayout.CENTER);

        // Right side panel
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(new Color(0x2A, 0x9F, 0xD6));
        side.setPreferredSize(new Dimension(200, 0));
        side.setBorder(BorderFactory.createEmptyBorder(24, 16, 16, 16));

        java.util.Calendar today = java.util.Calendar.getInstance();
        sideDateLabel.setText(new java.text.SimpleDateFormat("MMMM d, yyyy").format(today.getTime()));
        sideDateLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        sideDateLabel.setForeground(Color.WHITE);
        sideDateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sideHolidayLabel.setText("\u25CF  Non Instructional / Holiday");
        sideHolidayLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sideHolidayLabel.setForeground(Color.WHITE);
        sideHolidayLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        side.add(sideDateLabel);
        side.add(Box.createVerticalStrut(16));
        side.add(sideHolidayLabel);
        side.add(Box.createVerticalStrut(12));
        JLabel legend = new JLabel("<html><font color='white'>Orange dots mark holidays<br>updated by admin.</font></html>");
        legend.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(legend);

        body.add(monthList, BorderLayout.WEST);
        body.add(calCard, BorderLayout.CENTER);
        body.add(side, BorderLayout.EAST);
        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    // ---------------- My Report Card (Grade Card List + printable Grade Card) ----------------
    private JPanel reportCardResultSlot;

    private static final String[][] GRADE_POINTS = {
            {"O", "10"}, {"A+", "9"}, {"A", "8"}, {"B+", "7"},
            {"B", "6"}, {"C", "5"}, {"P", "4"}, {"F", "0"}
    };

    private JPanel buildReportCardCard() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // ---- filter card (matches screenshot: Grade Card List) ----
        JPanel filterCard = new JPanel();
        filterCard.setLayout(new BoxLayout(filterCard, BoxLayout.Y_AXIS));
        filterCard.setBackground(Color.WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));

        JLabel cardTitle = new JLabel("Grade Card List");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        cardTitle.setForeground(NAVY);
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterCard.add(cardTitle);
        filterCard.add(Box.createVerticalStrut(14));

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        filterRow.setBackground(Color.WHITE);
        filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] semesters = {"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"};
        JComboBox<String> semesterBox = new JComboBox<>(semesters);
        String currentSem = fetchCurrentSemester();
        if (currentSem != null) {
            semesterBox.setSelectedItem(currentSem);
        }

        JComboBox<String> sessionBox = new JComboBox<>(new String[]{
                "Consolidated", "Regular", "Supplementary", "Backlog"
        });
        sessionBox.setSelectedItem("Consolidated");

        for (JComboBox<String> box : new JComboBox[]{semesterBox, sessionBox}) {
            box.setFont(new Font("SansSerif", Font.PLAIN, 13));
            box.setPreferredSize(new Dimension(180, 30));
        }

        JButton submitBtn = new JButton("Submit");
        submitBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        submitBtn.setBackground(Color.WHITE);
        submitBtn.setForeground(NAVY);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCC, 0xCC, 0xCC), 1, true),
                BorderFactory.createEmptyBorder(6, 22, 6, 22)));
        submitBtn.setOpaque(true);
        submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        filterRow.add(labeledField("Select Semester", semesterBox));
        filterRow.add(labeledField("Select Session", sessionBox));
        filterRow.add(Box.createHorizontalStrut(8));

        JPanel submitWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 18));
        submitWrap.setBackground(Color.WHITE);
        submitWrap.add(submitBtn);
        filterRow.add(submitWrap);

        filterCard.add(filterRow);
        panel.add(filterCard, BorderLayout.NORTH);

        // ---- result slot ----
        reportCardResultSlot = new JPanel(new BorderLayout());
        reportCardResultSlot.setBackground(LIGHT_GRAY);
        reportCardResultSlot.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));

        JScrollPane outerScroll = new JScrollPane(reportCardResultSlot);
        outerScroll.setBorder(BorderFactory.createEmptyBorder());
        outerScroll.getViewport().setBackground(LIGHT_GRAY);
        panel.add(outerScroll, BorderLayout.CENTER);

        submitBtn.addActionListener(e -> loadGradeCardList(
                (String) semesterBox.getSelectedItem(),
                (String) sessionBox.getSelectedItem()));

        return panel;
    }

    /** Builds the Class / Numeric Semester / Year table + Print Now button. */
    private void loadGradeCardList(String semesterLabel, String session) {
        reportCardResultSlot.removeAll();

        int numericSem = semesterLabelToNumber(semesterLabel);
        String academicYear = inferAcademicYear(numericSem);

        String className = course + " - " + department + " "
                + academicYear.replace("-20", "-") + " (Sem " + toRoman(numericSem) + ")";
        // e.g. BCA-IOP (DA) II 2023-24 (Sem III) style — keep compact if course is long
        if (className.length() > 55) {
            className = course + " " + academicYear + " (Sem " + toRoman(numericSem) + ")";
        }

        String[] columns = {"Class", "Numeric Semester", "Year"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addRow(new Object[]{className, String.valueOf(numericSem), academicYear});

        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0xF0, 0xF2, 0xF5));
        table.getTableHeader().setForeground(NAVY);
        table.setGridColor(new Color(0xEE, 0xEE, 0xEE));
        table.setSelectionBackground(new Color(0xE8, 0xF8, 0xF3));
        table.setBackground(Color.WHITE);

        // Prefer wider Class column
        table.getColumnModel().getColumn(0).setPreferredWidth(340);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        tableCard.add(table.getTableHeader(), BorderLayout.NORTH);
        tableCard.add(table, BorderLayout.CENTER);

        JButton printBtn = new JButton("Print Now");
        printBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        printBtn.setBackground(Color.WHITE);
        printBtn.setForeground(NAVY);
        printBtn.setFocusPainted(false);
        printBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCC, 0xCC, 0xCC), 1, true),
                BorderFactory.createEmptyBorder(8, 28, 8, 28)));
        printBtn.setOpaque(true);
        printBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        printBtn.addActionListener(e -> showPrintableGradeCard(semesterLabel, numericSem, academicYear, session));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 16));
        btnRow.setBackground(Color.WHITE);
        btnRow.add(printBtn);

        JPanel listCard = new JPanel(new BorderLayout());
        listCard.setBackground(Color.WHITE);
        listCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1),
                BorderFactory.createEmptyBorder(0, 0, 8, 0)));
        listCard.add(tableCard, BorderLayout.CENTER);
        listCard.add(btnRow, BorderLayout.SOUTH);

        reportCardResultSlot.add(listCard, BorderLayout.NORTH);
        reportCardResultSlot.revalidate();
        reportCardResultSlot.repaint();
    }

    /** Opens a modal dialog with the formal Grade Card layout (screenshot style). */
    private void showPrintableGradeCard(String semesterLabel, int numericSem,
                                        String academicYear, String session) {
        JDialog dialog = new JDialog(this, "Grade Card", true);
        dialog.setSize(720, 640);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel gradeCard = buildFormalGradeCardPanel(semesterLabel, numericSem, academicYear, session);
        JScrollPane scroll = new JScrollPane(gradeCard);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        dialog.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottom.setBackground(Color.WHITE);
        JButton printBtn = new JButton("Print");
        printBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        printBtn.setBackground(TEAL);
        printBtn.setForeground(Color.WHITE);
        printBtn.setFocusPainted(false);
        printBtn.setBorderPainted(false);
        printBtn.setOpaque(true);
        printBtn.addActionListener(e -> {
            try {
                java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
                job.setPrintable((g, pf, pageIndex) -> {
                    if (pageIndex > 0) {
                        return java.awt.print.Printable.NO_SUCH_PAGE;
                    }
                    Graphics2D g2 = (Graphics2D) g;
                    g2.translate(pf.getImageableX(), pf.getImageableY());
                    double scaleX = pf.getImageableWidth() / Math.max(1, gradeCard.getWidth());
                    double scaleY = pf.getImageableHeight() / Math.max(1, gradeCard.getHeight());
                    double scale = Math.min(scaleX, scaleY);
                    g2.scale(scale, scale);
                    gradeCard.printAll(g2);
                    return java.awt.print.Printable.PAGE_EXISTS;
                });
                if (job.printDialog()) {
                    job.print();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Unable to print. You can take a screenshot of this Grade Card instead.",
                        "Print", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        closeBtn.addActionListener(e -> dialog.dispose());
        bottom.add(printBtn);
        bottom.add(closeBtn);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel buildFormalGradeCardPanel(String semesterLabel, int numericSem,
                                             String academicYear, String session) {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(Color.WHITE);
        page.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Title
        JLabel title = new JLabel("Grade Card", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(Color.BLACK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        page.add(title);
        page.add(Box.createVerticalStrut(14));

        // Student header table
        String[] fields = fetchFullStudentRecord();
        String motherName = fields[1] != null ? fields[1] : "N/A";
        String applicationNumber = fields[12] != null ? fields[12] : "N/A";

        page.add(buildGradeHeaderTable(new String[][]{
                {"Student Name:", studentName, "", ""},
                {"Mother's Name:", motherName, "", ""},
                {"Enrolment No:", rollNo, "Admission No:", applicationNumber},
                {"School Name:", "School of Computing Science & Engineering", "", ""},
                {"Program Name:", course + (department != null ? " — " + department : ""),
                        "Term / Semester:", semesterLabel + " (" + session + ")"},
        }));
        page.add(Box.createVerticalStrut(16));

        // Course results (no credits)
        java.util.List<Object[]> rows = loadGradeRows(semesterLabel);
        double sumGradePoints = 0;
        int subjectCount = 0;

        String[] courseCols = {"Course Code", "Course Name", "Marks / Total", "Percent", "Grade"};
        DefaultTableModel courseModel = new DefaultTableModel(courseCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        for (Object[] r : rows) {
            String code = (String) r[0];
            String name = (String) r[1];
            int obtained = (Integer) r[2];
            int total = (Integer) r[3];
            String grade = (String) r[4];
            double pct = total > 0 ? (obtained * 100.0) / total : 0;

            sumGradePoints += gradeToPoint(grade);
            subjectCount++;

            courseModel.addRow(new Object[]{
                    code, name,
                    obtained + " / " + total,
                    String.format("%.1f%%", pct),
                    grade
            });
        }

        JTable courseTable = new JTable(courseModel);
        styleGradeTable(courseTable);
        courseTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        courseTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        courseTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        courseTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        courseTable.getColumnModel().getColumn(4).setPreferredWidth(60);

        JPanel courseWrap = new JPanel(new BorderLayout());
        courseWrap.setBackground(Color.WHITE);
        courseWrap.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        courseWrap.add(courseTable.getTableHeader(), BorderLayout.NORTH);
        courseWrap.add(courseTable, BorderLayout.CENTER);
        courseWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        courseWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, courseTable.getPreferredSize().height
                + courseTable.getTableHeader().getPreferredSize().height + 4));
        page.add(courseWrap);
        page.add(Box.createVerticalStrut(16));

        // SGPA = average of grade points this semester; CGPA = average across all semesters up to this one
        double sgpa = subjectCount > 0 ? sumGradePoints / subjectCount : 0;
        double[] cum = computeCumulativeUpTo(numericSem);
        double cgpa = cum[1] > 0 ? cum[0] / cum[1] : sgpa; // cum[0]=sum GP, cum[1]=subject count

        JPanel summaryPanel = buildGradeSummaryBar(sgpa, cgpa, subjectCount);
        summaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(summaryPanel);

        page.add(Box.createVerticalStrut(18));
        JLabel dateLabel = new JLabel("DATE: " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()));
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(dateLabel);

        return page;
    }

    private JPanel buildGradeHeaderTable(String[][] rows) {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(Color.WHITE);
        wrap.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 0, 0);

        for (int r = 0; r < rows.length; r++) {
            String[] row = rows[r];
            boolean fourCol = row[2] != null && !row[2].isEmpty();
            for (int c = 0; c < 4; c++) {
                gbc.gridx = c;
                gbc.gridy = r;
                gbc.gridwidth = (!fourCol && c == 1) ? 3 : 1;
                if (!fourCol && c > 1) continue;

                String text = row[c] != null ? row[c] : "";
                JLabel cell = new JLabel("  " + text + "  ");
                cell.setFont(new Font("SansSerif",
                        (c % 2 == 0) ? Font.BOLD : Font.PLAIN, 12));
                cell.setBorder(BorderFactory.createMatteBorder(
                        r == 0 ? 0 : 1, c == 0 ? 0 : 1, 0, 0, Color.GRAY));
                cell.setOpaque(true);
                cell.setBackground(Color.WHITE);
                if (c % 2 == 0) {
                    cell.setPreferredSize(new Dimension(130, 26));
                }
                wrap.add(cell, gbc);
                if (!fourCol && c == 1) break;
            }
        }
        return wrap;
    }

    private JPanel buildGradeSummaryBar(double sgpa, double cgpa, int subjectsThisSem) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        DefaultTableModel m = new DefaultTableModel(new String[]{
                "Subjects",
                "Semester Grade Point Average (SGPA)",
                "Cumulative Grade Point Average (CGPA)"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        m.addRow(new Object[]{
                String.valueOf(subjectsThisSem),
                String.format("%.2f", sgpa),
                String.format("%.2f", cgpa)
        });

        JTable t = new JTable(m);
        t.setFont(new Font("SansSerif", Font.PLAIN, 12));
        t.setRowHeight(28);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        t.getTableHeader().setBackground(new Color(0xF5, 0xF5, 0xF5));
        t.getTableHeader().setReorderingAllowed(false);
        t.setGridColor(Color.GRAY);
        t.setEnabled(false);
        t.setBackground(Color.WHITE);

        bar.add(t.getTableHeader(), BorderLayout.NORTH);
        bar.add(t, BorderLayout.CENTER);
        return bar;
    }

    private void styleGradeTable(JTable table) {
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(0xF5, 0xF5, 0xF5));
        table.getTableHeader().setForeground(Color.BLACK);
        table.setGridColor(Color.GRAY);
        table.setEnabled(false);
        table.setBackground(Color.WHITE);
        table.setShowGrid(true);
    }

    /**
     * Loads subjects + marks for the semester; maps percent → letter grade via GRADE_SCALE.
     * Returns list of {code, name, obtained(Integer), total(Integer), grade(String)}.
     */
    private java.util.List<Object[]> loadGradeRows(String semester) {
        java.util.List<Object[]> result = new java.util.ArrayList<>();
        String sql = "SELECT s.subject_id, s.subject_name, sm.marks_obtained, sm.total_marks "
                + "FROM student_marks sm JOIN subjects s ON sm.subject_id = s.subject_id "
                + "WHERE sm.roll_no = ? AND sm.semester_recorded = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            stmt.setString(2, semester);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("subject_id");
                    String name = rs.getString("subject_name");
                    int obtained = rs.getInt("marks_obtained");
                    int total = rs.getInt("total_marks");
                    double pct = total > 0 ? (obtained * 100.0) / total : 0;
                    result.add(new Object[]{code, name, obtained, total, percentToLetterGrade(pct)});
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Cumulative grade points and subject count for all semesters numerically <= numericSem.
     * Returns {sumGradePoints, subjectCount}.
     */
    private double[] computeCumulativeUpTo(int numericSem) {
        double sumPoints = 0;
        int count = 0;
        String[] labels = {"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"};
        for (int i = 1; i <= numericSem && i <= labels.length; i++) {
            for (Object[] row : loadGradeRows(labels[i - 1])) {
                String g = (String) row[4];
                sumPoints += gradeToPoint(g);
                count++;
            }
        }
        return new double[]{sumPoints, count};
    }

    private String percentToLetterGrade(double percent) {
        for (String[] band : GRADE_SCALE) {
            double lo = Double.parseDouble(band[0]);
            double hi = Double.parseDouble(band[1]);
            if (percent >= lo && percent <= hi) {
                return band[2];
            }
        }
        return "F";
    }

    private double gradeToPoint(String letter) {
        if (letter == null) return 0;
        for (String[] gp : GRADE_POINTS) {
            if (gp[0].equalsIgnoreCase(letter.trim())) {
                return Double.parseDouble(gp[1]);
            }
        }
        return 0;
    }

    private int semesterLabelToNumber(String label) {
        if (label == null) return 1;
        String s = label.trim().toLowerCase();
        if (s.startsWith("1")) return 1;
        if (s.startsWith("2")) return 2;
        if (s.startsWith("3")) return 3;
        if (s.startsWith("4")) return 4;
        if (s.startsWith("5")) return 5;
        if (s.startsWith("6")) return 6;
        if (s.startsWith("7")) return 7;
        if (s.startsWith("8")) return 8;
        try {
            return Integer.parseInt(s.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 1;
        }
    }

    private String toRoman(int n) {
        String[] romans = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII"};
        return (n >= 1 && n <= 8) ? romans[n] : String.valueOf(n);
    }

    /** Rough academic year from semester number (odd = new year start). Adjust if your calendar differs. */
    private String inferAcademicYear(int numericSem) {
        // Prefer current year range used elsewhere in the app
        return "2024-2025";
    }

    // =====================================================================
    // Notifications (student ↔ admin alerts) — additive; does not alter other modules
    // =====================================================================
    private DefaultTableModel studentNotifTableModel;
    private JLabel studentNotifUnreadLabel;

    private void ensureNotificationsTableExists() {
        String ddl = "CREATE TABLE IF NOT EXISTS notifications ("
                + "id INT PRIMARY KEY AUTO_INCREMENT, "
                + "sender_role VARCHAR(20) NOT NULL, "
                + "sender_id VARCHAR(64) NOT NULL, "
                + "recipient_role VARCHAR(20) NOT NULL, "
                + "recipient_id VARCHAR(64), "
                + "title VARCHAR(200) NOT NULL, "
                + "message TEXT, "
                + "is_read TINYINT DEFAULT 0, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(ddl)) {
            stmt.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private JPanel buildNotificationsCard() {
        // Student is receive-only: only Admin can send notifications
        ensureNotificationsTableExists();

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(LIGHT_GRAY);
        JLabel title = new JLabel("Notifications");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(NAVY);
        header.add(title, BorderLayout.WEST);

        studentNotifUnreadLabel = new JLabel("Unread: 0");
        studentNotifUnreadLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        studentNotifUnreadLabel.setForeground(TEAL);
        header.add(studentNotifUnreadLabel, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "From", "Title", "Message", "When", "Status"};
        studentNotifTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(studentNotifTableModel);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(NAVY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(0xEE, 0xEE, 0xEE));
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(280);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xDD), 1));

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionRow.setBackground(LIGHT_GRAY);
        JButton refreshBtn = new JButton("Refresh");
        JButton markReadBtn = new JButton("Mark Selected Read");
        JButton markAllBtn = new JButton("Mark All Read");
        for (JButton b : new JButton[]{refreshBtn, markReadBtn, markAllBtn}) {
            b.setFont(new Font("SansSerif", Font.PLAIN, 12));
            b.setFocusPainted(false);
        }
        markReadBtn.setBackground(TEAL);
        markReadBtn.setForeground(Color.WHITE);
        markReadBtn.setOpaque(true);
        markReadBtn.setBorderPainted(false);
        markAllBtn.setBackground(NAVY);
        markAllBtn.setForeground(Color.WHITE);
        markAllBtn.setOpaque(true);
        markAllBtn.setBorderPainted(false);
        actionRow.add(refreshBtn);
        actionRow.add(markReadBtn);
        actionRow.add(markAllBtn);

        JLabel hint = new JLabel("Alerts are sent by Admin only. Double-click a row to open.");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        hint.setForeground(new Color(0x7F, 0x8C, 0x8D));
        hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel listSection = new JPanel(new BorderLayout(0, 8));
        listSection.setBackground(LIGHT_GRAY);
        listSection.add(hint, BorderLayout.NORTH);
        listSection.add(tableScroll, BorderLayout.CENTER);
        listSection.add(actionRow, BorderLayout.SOUTH);
        panel.add(listSection, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> loadStudentNotifications());
        markReadBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(panel, "Select a notification first.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) studentNotifTableModel.getValueAt(row, 0);
            markNotificationRead(id);
            loadStudentNotifications();
        });
        markAllBtn.addActionListener(e -> {
            markAllStudentNotificationsRead();
            loadStudentNotifications();
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        String from = String.valueOf(studentNotifTableModel.getValueAt(row, 1));
                        String t = String.valueOf(studentNotifTableModel.getValueAt(row, 2));
                        String m = String.valueOf(studentNotifTableModel.getValueAt(row, 3));
                        int id = (int) studentNotifTableModel.getValueAt(row, 0);
                        markNotificationRead(id);
                        JOptionPane.showMessageDialog(panel,
                                "From: " + from + "\n\n" + m,
                                t, JOptionPane.INFORMATION_MESSAGE);
                        loadStudentNotifications();
                    }
                }
            }
        });

        loadStudentNotifications();
        return panel;
    }

    private void loadStudentNotifications() {
        if (studentNotifTableModel == null) return;
        studentNotifTableModel.setRowCount(0);
        ensureNotificationsTableExists();
        int unread = 0;
        // Admin-sent alerts only (student cannot send)
        String sql = "SELECT id, sender_role, sender_id, title, message, is_read, created_at "
                + "FROM notifications WHERE sender_role = 'admin' AND ("
                + "  (recipient_role = 'student' AND (recipient_id = ? OR recipient_id IS NULL OR recipient_id = '')) "
                + "  OR recipient_role = 'all'"
                + ") ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            try (ResultSet rs = stmt.executeQuery()) {
                java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd MMM yyyy HH:mm");
                while (rs.next()) {
                    boolean read = rs.getInt("is_read") == 1;
                    if (!read) unread++;
                    String from = rs.getString("sender_role");
                    if ("admin".equalsIgnoreCase(from)) from = "Admin";
                    else from = "You / " + rs.getString("sender_id");
                    java.sql.Timestamp ts = rs.getTimestamp("created_at");
                    studentNotifTableModel.addRow(new Object[]{
                            rs.getInt("id"),
                            from,
                            rs.getString("title"),
                            rs.getString("message"),
                            ts != null ? fmt.format(ts) : "",
                            read ? "Read" : "Unread"
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (studentNotifUnreadLabel != null) {
            studentNotifUnreadLabel.setText("Unread: " + unread);
            studentNotifUnreadLabel.setForeground(unread > 0 ? new Color(0xE0, 0x6C, 0x5B) : TEAL);
        }
    }

    private boolean sendNotification(String senderRole, String senderId,
                                     String recipientRole, String recipientId,
                                     String title, String message) {
        ensureNotificationsTableExists();
        String sql = "INSERT INTO notifications (sender_role, sender_id, recipient_role, recipient_id, title, message) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, senderRole);
            stmt.setString(2, senderId);
            stmt.setString(3, recipientRole);
            if (recipientId == null || recipientId.isEmpty()) {
                stmt.setNull(4, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(4, recipientId);
            }
            stmt.setString(5, title);
            stmt.setString(6, message);
            stmt.executeUpdate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to send: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void markNotificationRead(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE notifications SET is_read = 1 WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void markAllStudentNotificationsRead() {
        String sql = "UPDATE notifications SET is_read = 1 WHERE "
                + "(recipient_role = 'student' AND (recipient_id = ? OR recipient_id IS NULL OR recipient_id = '')) "
                + "OR recipient_role = 'all'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rollNo);
            stmt.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private JPanel buildPlaceholderCard(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_GRAY);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(NAVY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JLabel placeholderText = new JLabel(title + " module coming soon.", SwingConstants.CENTER);
        placeholderText.setFont(new Font("SansSerif", Font.PLAIN, 14));
        placeholderText.setForeground(NAVY);
        panel.add(placeholderText, BorderLayout.CENTER);

        return panel;
    }

    private void showContentCard(String name) {
        contentLayout.show(contentPanel, name);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new StudentDashboardGUI("Anubhav Test", "26CSE9999001", "B.Tech", "Computer Science Engineering")
                        .setVisible(true));
    }
}