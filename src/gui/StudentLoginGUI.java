import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.prefs.Preferences;

public class StudentLoginGUI extends JFrame {

    private static final Color BLUE = new Color(0x1A, 0xBC, 0x9C);        // TEAL
    private static final Color DARK_BLUE = new Color(0x16, 0xA0, 0x85);   // TEAL_DARK
    private static final Color TEXT = new Color(0x2C, 0x3E, 0x50);        // TEXT_DARK
    private static final Color MUTED = new Color(0x7F, 0x8C, 0x8D);       // TEXT_MUTED
    private static final Color PAGE_BACKGROUND = new Color(0xF4, 0xF7, 0xFA); // LIGHT_BG
    private static final Color WHITE = Color.WHITE;                       // CARD_BG

    private static final Preferences PREFS =
            Preferences.userNodeForPackage(StudentLoginGUI.class);

    private static final String PREF_REMEMBERED_ROLLNO =
            "remembered_roll_no";

    private static final String PREF_REMEMBERED_PASSWORD =
            "remembered_password";

    private JTextField rollNoField;
    private JPasswordField passwordField;
    private JCheckBox rememberMeCheckBox;
    private JButton togglePasswordBtn;

    private char hiddenEchoChar;
    private boolean passwordVisible = false;

    public StudentLoginGUI() {
        setTitle("University Student Panel");
        setSize(840, 475);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(PAGE_BACKGROUND);

        root.add(createIllustrationPanel());
        root.add(createLoginPanel());

        add(root);
        loadRememberedCredentials();
    }

    private JPanel createIllustrationPanel() {
        IllustrationPanel panel = new IllustrationPanel();
        panel.setBackground(new Color(235, 247, 253));
        return panel;
    }

    private JPanel createLoginPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(PAGE_BACKGROUND);
        wrapper.setBorder(new EmptyBorder(8, 8, 8, 18));

        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(20, 88, 20, 88));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel universityLabel = new JLabel("Smart Student Management", SwingConstants.CENTER);
        universityLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        universityLabel.setForeground(Color.BLACK);

        JLabel panelLabel = new JLabel("STUDENT PANEL", SwingConstants.CENTER);
        panelLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        panelLabel.setForeground(BLUE);

        JLabel enrollmentLabel = new JLabel("Enrollment number / Admin ID");
        enrollmentLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        enrollmentLabel.setForeground(MUTED);

        rollNoField = createTextField();
        rollNoField.setToolTipText("Enter student enrollment number or admin username");

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        passwordLabel.setForeground(MUTED);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setForeground(TEXT);
        passwordField.setBackground(WHITE);
        passwordField.setBorder(new UnderlineBorder(new Color(150, 150, 150)));
        hiddenEchoChar = passwordField.getEchoChar();

        togglePasswordBtn = new JButton("●");
        togglePasswordBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        togglePasswordBtn.setForeground(new Color(125, 125, 125));
        togglePasswordBtn.setBackground(WHITE);
        togglePasswordBtn.setBorderPainted(false);
        togglePasswordBtn.setContentAreaFilled(false);
        togglePasswordBtn.setFocusPainted(false);
        togglePasswordBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        togglePasswordBtn.setToolTipText("Show or hide password");
        togglePasswordBtn.addActionListener(e -> togglePasswordVisibility());

        JPanel passwordRow = new JPanel(new BorderLayout());
        passwordRow.setBackground(WHITE);
        passwordRow.add(passwordField, BorderLayout.CENTER);
        passwordRow.add(togglePasswordBtn, BorderLayout.EAST);

        rememberMeCheckBox = new JCheckBox("Remember me");
        rememberMeCheckBox.setFont(new Font("SansSerif", Font.PLAIN, 11));
        rememberMeCheckBox.setForeground(TEXT);
        rememberMeCheckBox.setBackground(WHITE);
        rememberMeCheckBox.setFocusPainted(false);

        JButton forgotPasswordLink = new JButton("Forgot Password?");
        forgotPasswordLink.setFont(new Font("SansSerif", Font.PLAIN, 11));
        forgotPasswordLink.setForeground(TEXT);
        forgotPasswordLink.setBorderPainted(false);
        forgotPasswordLink.setContentAreaFilled(false);
        forgotPasswordLink.setFocusPainted(false);
        forgotPasswordLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPasswordLink.addActionListener(
                e -> new ForgotPasswordGUI().setVisible(true)
        );

        JPanel optionsRow = new JPanel(new BorderLayout());
        optionsRow.setBackground(WHITE);
        optionsRow.add(rememberMeCheckBox, BorderLayout.WEST);
        optionsRow.add(forgotPasswordLink, BorderLayout.EAST);

        JButton loginButton = createLoginButton("Sign in");
        loginButton.addActionListener(e -> attemptLogin());

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 7, 0);
        card.add(createUniversityLogo(), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(universityLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 27, 0);
        card.add(panelLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(enrollmentLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 17, 0);
        card.add(rollNoField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(passwordLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        card.add(passwordRow, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 21, 0);
        card.add(optionsRow, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(loginButton, gbc);

        wrapper.add(card);
        passwordField.addActionListener(e -> attemptLogin());

        return wrapper;
    }

    private JLabel createUniversityLogo() {
        JLabel logo = new JLabel("🎓", SwingConstants.CENTER);
        logo.setFont(new Font("SansSerif", Font.PLAIN, 45));
        logo.setPreferredSize(new Dimension(100, 58));
        return logo;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBackground(WHITE);
        field.setBorder(new UnderlineBorder(new Color(150, 150, 150)));
        return field;
    }

    private JButton createLoginButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(WHITE);
        button.setBackground(BLUE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(190, 40));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(DARK_BLUE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BLUE);
            }
        });

        return button;
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            passwordField.setEchoChar((char) 0);
            togglePasswordBtn.setText("○");
        } else {
            passwordField.setEchoChar(hiddenEchoChar);
            togglePasswordBtn.setText("●");
        }
    }

    private void loadRememberedCredentials() {
        String rememberedRollNo =
                PREFS.get(PREF_REMEMBERED_ROLLNO, null);

        String rememberedPassword =
                PREFS.get(PREF_REMEMBERED_PASSWORD, null);

        if (rememberedRollNo != null) {
            rollNoField.setText(rememberedRollNo);
            rememberMeCheckBox.setSelected(true);
        }

        if (rememberedPassword != null) {
            passwordField.setText(rememberedPassword);
        }
    }

    private void attemptLogin() {
        String enteredId = rollNoField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (enteredId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter both Enrollment Number/Admin ID and Password.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        boolean loggedIn =
                tryStudentLogin(enteredId, password)
                        || tryAdminLogin(enteredId, password);

        if (loggedIn) {
            saveRememberedCredentials(enteredId, password);
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Invalid Enrollment Number/Admin ID or Password.",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE
        );

        passwordField.setText("");
    }

    private void saveRememberedCredentials(
            String enteredId,
            String password
    ) {
        if (rememberMeCheckBox.isSelected()) {
            PREFS.put(PREF_REMEMBERED_ROLLNO, enteredId);
            PREFS.put(PREF_REMEMBERED_PASSWORD, password);
        } else {
            PREFS.remove(PREF_REMEMBERED_ROLLNO);
            PREFS.remove(PREF_REMEMBERED_PASSWORD);
        }
    }

    private boolean tryStudentLogin(
            String rollNo,
            String password
    ) {
        String sql =
                "SELECT name, course, department " +
                "FROM students " +
                "WHERE roll_no = ? AND password = ?";

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, rollNo);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String studentName = rs.getString("name");
                    String course = rs.getString("course");
                    String department = rs.getString("department");

                    dispose();

                    new StudentDashboardGUI(
                            studentName,
                            rollNo,
                            course,
                            department
                    ).setVisible(true);

                    return true;
                }
            }

        } catch (Exception ex) {
            showDatabaseError(ex);
        }

        return false;
    }

    private boolean tryAdminLogin(
            String username,
            String password
    ) {
        String sql =
                "SELECT username " +
                "FROM admin " +
                "WHERE username = ? AND password = ?";

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    dispose();
                    new AdminDashboardGUI(username).setVisible(true);
                    return true;
                }
            }

        } catch (Exception ex) {
            showDatabaseError(ex);
        }

        return false;
    }

    private void showDatabaseError(Exception ex) {
        JOptionPane.showMessageDialog(
                this,
                "Database error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );

        ex.printStackTrace();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new StudentLoginGUI().setVisible(true)
        );
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;

        RoundedPanel(int radius, Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(backgroundColor);
            g2.fillRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius,
                    radius
            );

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class UnderlineBorder
            extends javax.swing.border.AbstractBorder {

        private final Color color;

        UnderlineBorder(Color color) {
            this.color = color;
        }

        @Override
        public void paintBorder(
                Component component,
                Graphics graphics,
                int x,
                int y,
                int width,
                int height
        ) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setColor(color);
            g2.drawLine(
                    x + 1,
                    y + height - 1,
                    x + width - 2,
                    y + height - 1
            );
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component component) {
            return new Insets(5, 2, 7, 2);
        }
    }

    private static class IllustrationPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int width = getWidth();
            int height = getHeight();

            // Soft background circles
            g2.setColor(new Color(218, 239, 250));
            g2.fillOval(-80, -100, 310, 250);
            g2.fillOval(85, 110, 320, 260);
            g2.fillOval(-100, 275, 315, 230);

            // Monitor
            int monitorX = 125;
            int monitorY = 200;
            int monitorW = 165;
            int monitorH = 100;

            g2.setColor(new Color(45, 45, 65));
            g2.fillRoundRect(monitorX, monitorY, monitorW, monitorH, 10, 10);

            g2.setColor(new Color(185, 189, 194));
            g2.drawRoundRect(monitorX, monitorY, monitorW, monitorH, 10, 10);

            g2.setColor(new Color(195, 201, 205));
            g2.fillRect(monitorX + 58, monitorY + monitorH, 50, 12);
            g2.fillRect(monitorX + 38, monitorY + monitorH + 12, 90, 7);

            // Graduation cap
            Polygon cap = new Polygon();
            cap.addPoint(35, 240);
            cap.addPoint(88, 155);
            cap.addPoint(160, 145);
            cap.addPoint(175, 203);
            cap.addPoint(88, 255);

            g2.setColor(Color.BLACK);
            g2.fillPolygon(cap);

            Polygon top = new Polygon();
            top.addPoint(52, 156);
            top.addPoint(140, 145);
            top.addPoint(174, 167);
            top.addPoint(84, 180);

            g2.setColor(new Color(25, 25, 25));
            g2.fillPolygon(top);

            // Diploma
            g2.setColor(new Color(250, 250, 250));
            g2.rotate(-0.25, 178, 133);
            g2.fillRoundRect(166, 80, 22, 105, 5, 5);
            g2.rotate(0.25, 178, 133);

            // Red diploma ribbon
            g2.setColor(new Color(239, 75, 82));
            g2.fillRect(184, 139, 28, 8);
            g2.fillRect(198, 133, 8, 22);

            // Small people
            g2.setColor(new Color(38, 169, 222));
            g2.fillOval(310, 278, 20, 20);
            g2.fillRect(310, 296, 20, 38);

            g2.setColor(new Color(38, 169, 222));
            g2.fillOval(290, 184, 15, 15);
            g2.fillRect(287, 198, 17, 30);

            g2.setColor(new Color(37, 44, 57));
            g2.fillOval(286, 224, 14, 14);
            g2.fillRect(286, 236, 12, 37);

            g2.setColor(new Color(42, 45, 55));
            g2.fillOval(315, 331, 12, 12);
            g2.fillRect(313, 325, 10, 30);

            // Plug and cable
            g2.setColor(new Color(120, 125, 130));
            g2.drawLine(292, 300, 335, 300);
            g2.drawLine(335, 300, 335, 270);
            g2.drawRoundRect(332, 260, 30, 22, 5, 5);

            g2.setColor(new Color(40, 160, 215));
            g2.fillOval(238, 333, 28, 15);

            g2.setColor(Color.WHITE);
            g2.fillOval(248, 337, 5, 3);

            g2.dispose();
        }
    }
}