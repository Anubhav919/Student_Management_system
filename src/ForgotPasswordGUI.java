import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;

public class ForgotPasswordGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color BLUE = new Color(0x1A, 0xBC, 0x9C);        // TEAL
    private static final Color DARK_BLUE = new Color(0x16, 0xA0, 0x85);   // TEAL_DARK
    private static final Color TEXT = new Color(0x2C, 0x3E, 0x50);        // TEXT_DARK
    private static final Color MUTED = new Color(0x7F, 0x8C, 0x8D);       // TEXT_MUTED
    private static final Color PAGE_BACKGROUND = new Color(0xF4, 0xF7, 0xFA); // LIGHT_BG
    private static final Color WHITE = Color.WHITE;                       // CARD_BG

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    // Step 1 fields
    private JTextField rollNoField;
    private JTextField emailField;

    // Step 2 fields
    private JTextField otpField;
    private String generatedOtp;
    private String verifiedRollNo;
    private long otpGeneratedAt;

    private static final long OTP_VALID_MILLIS = 5 * 60 * 1000;

    // Step 3 fields
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public ForgotPasswordGUI() {
        setTitle("University Password Recovery");
        setSize(840, 475);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        cardPanel.setBackground(PAGE_BACKGROUND);

        cardPanel.add(createStep1Panel(), "step1");
        cardPanel.add(createStep2Panel(), "step2");
        cardPanel.add(createStep3Panel(), "step3");

        add(cardPanel);
    }

    // ============================================================
    // COMMON ROOT PANEL
    // ============================================================

    private JPanel createRootPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PAGE_BACKGROUND);
        return root;
    }

    // ============================================================
    // STEP 1: ENROLLMENT NUMBER AND EMAIL
    // ============================================================

    private JPanel createStep1Panel() {
        JPanel root = createRootPanel();

        root.add(createIllustrationPanel(), BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(PAGE_BACKGROUND);
        rightPanel.setBorder(new EmptyBorder(8, 8, 8, 18));

        RoundedPanel card = createCardPanel();
        GridBagConstraints gbc = createGbc();

        card.add(createUniversityLogo(), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(createTitle("Smart Student Management"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(createSubtitle("PASSWORD RECOVERY"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 24, 0);
        card.add(createStepLabel("STEP 1 OF 3"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(createFieldLabel("Enrollment number"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);
        rollNoField = createTextField();
        card.add(rollNoField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(createFieldLabel("Registered email"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        emailField = createTextField();
        card.add(emailField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        JButton sendOtpButton = createPrimaryButton("Send OTP");
        card.add(sendOtpButton, gbc);

        sendOtpButton.addActionListener(e -> handleSendOtp());
        emailField.addActionListener(e -> handleSendOtp());

        rightPanel.add(card);
        root.add(rightPanel, BorderLayout.CENTER);

        return root;
    }

    private void handleSendOtp() {
        String rollNo = rollNoField.getText().trim();
        String email = emailField.getText().trim();

        if (rollNo.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter both Enrollment Number and Email.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String sql = "SELECT email FROM students WHERE roll_no = ?";

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, rollNo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "No student found with that Enrollment Number.",
                            "Not Found",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                String registeredEmail = rs.getString("email");

                if (registeredEmail == null
                        || !registeredEmail.equalsIgnoreCase(email)) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Email does not match our records.",
                            "Email Mismatch",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
            }

            generatedOtp = EmailOTPService.generateOtp();

            boolean sent = EmailOTPService.sendOtp(
                    email,
                    generatedOtp
            );

            if (sent) {
                verifiedRollNo = rollNo;
                otpGeneratedAt = System.currentTimeMillis();

                JOptionPane.showMessageDialog(
                        this,
                        "An OTP has been sent to " + email,
                        "OTP Sent",
                        JOptionPane.INFORMATION_MESSAGE
                );

                otpField.setText("");
                cardLayout.show(cardPanel, "step2");
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to send OTP email. Check EmailOTPService.",
                        "Email Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (Exception ex) {
            showDatabaseError(ex);
        }
    }

    // ============================================================
    // STEP 2: OTP VERIFICATION
    // ============================================================

    private JPanel createStep2Panel() {
        JPanel root = createRootPanel();

        root.add(createIllustrationPanel(), BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(PAGE_BACKGROUND);
        rightPanel.setBorder(new EmptyBorder(8, 8, 8, 18));

        RoundedPanel card = createCardPanel();
        GridBagConstraints gbc = createGbc();

        card.add(createUniversityLogo(), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(createTitle("UNIVERSITY NAME"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(createSubtitle("VERIFY YOUR EMAIL"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 24, 0);
        card.add(createStepLabel("STEP 2 OF 3"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 7, 0);
        card.add(
                createFieldLabel("Enter the 6-digit OTP"),
                gbc
        );

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 8, 0);

        otpField = createTextField();
        otpField.setHorizontalAlignment(SwingConstants.CENTER);
        otpField.setFont(new Font("SansSerif", Font.BOLD, 18));
        otpField.setToolTipText("Enter the OTP sent to your email");

        card.add(otpField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 19, 0);

        JLabel infoLabel = new JLabel(
                "<html><center>"
                        + "We sent a verification code to your<br>"
                        + "registered email address."
                        + "</center></html>",
                SwingConstants.CENTER
        );

        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        infoLabel.setForeground(MUTED);
        card.add(infoLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);

        JButton verifyButton = createPrimaryButton("Verify OTP");
        card.add(verifyButton, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);

        JButton resendButton = createLinkButton("Resend OTP");
        card.add(resendButton, gbc);

        verifyButton.addActionListener(e -> handleVerifyOtp());
        resendButton.addActionListener(e -> handleSendOtp());
        otpField.addActionListener(e -> handleVerifyOtp());

        rightPanel.add(card);
        root.add(rightPanel, BorderLayout.CENTER);

        return root;
    }

    private void handleVerifyOtp() {
        String enteredOtp = otpField.getText().trim();

        if (enteredOtp.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter the OTP.",
                    "Missing OTP",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (generatedOtp == null || otpGeneratedAt == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "No OTP is available. Please request a new OTP.",
                    "OTP Error",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (System.currentTimeMillis() - otpGeneratedAt
                > OTP_VALID_MILLIS) {

            JOptionPane.showMessageDialog(
                    this,
                    "OTP expired. Please request a new one.",
                    "OTP Expired",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (enteredOtp.equals(generatedOtp)) {
            cardLayout.show(cardPanel, "step3");
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Incorrect OTP. Please try again.",
                    "Invalid OTP",
                    JOptionPane.ERROR_MESSAGE
            );

            otpField.setText("");
        }
    }

    // ============================================================
    // STEP 3: PASSWORD UPDATE
    // ============================================================

    private JPanel createStep3Panel() {
        JPanel root = createRootPanel();

        root.add(createIllustrationPanel(), BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(PAGE_BACKGROUND);
        rightPanel.setBorder(new EmptyBorder(8, 8, 8, 18));

        RoundedPanel card = createCardPanel();
        GridBagConstraints gbc = createGbc();

        card.add(createUniversityLogo(), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(createTitle("UNIVERSITY NAME"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(createSubtitle("CREATE NEW PASSWORD"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 24, 0);
        card.add(createStepLabel("STEP 3 OF 3"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(createFieldLabel("New password"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);

        newPasswordField = createPasswordField();
        card.add(newPasswordField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(createFieldLabel("Confirm password"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);

        confirmPasswordField = createPasswordField();
        card.add(confirmPasswordField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);

        JButton updateButton = createPrimaryButton("Update Password");
        card.add(updateButton, gbc);

        updateButton.addActionListener(e -> handleUpdatePassword());
        confirmPasswordField.addActionListener(e -> handleUpdatePassword());

        rightPanel.add(card);
        root.add(rightPanel, BorderLayout.CENTER);

        return root;
    }

    private void handleUpdatePassword() {
        String newPassword =
                new String(newPasswordField.getPassword()).trim();

        String confirmPassword =
                new String(confirmPasswordField.getPassword()).trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please fill in both password fields.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Passwords do not match.",
                    "Mismatch",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String sql =
                "UPDATE students SET password = ? WHERE roll_no = ?";

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, newPassword);
            stmt.setString(2, verifiedRollNo);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Password updated successfully! You can now log in.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                dispose();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Could not update password. Please try again.",
                        "Update Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (Exception ex) {
            showDatabaseError(ex);
        }
    }

    // ============================================================
    // UI HELPERS
    // ============================================================

    private RoundedPanel createCardPanel() {
        RoundedPanel card = new RoundedPanel(18, WHITE);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(19, 88, 19, 88));
        return card;
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 7, 0);

        return gbc;
    }

    private JLabel createUniversityLogo() {
        JLabel logo = new JLabel("🎓", SwingConstants.CENTER);
        logo.setFont(new Font("SansSerif", Font.PLAIN, 42));
        logo.setPreferredSize(new Dimension(100, 55));
        return logo;
    }

    private JLabel createTitle(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 17));
        label.setForeground(Color.BLACK);
        return label;
    }

    private JLabel createSubtitle(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(BLUE);
        return label;
    }

    private JLabel createStepLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(MUTED);
        return label;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        label.setForeground(MUTED);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();

        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBackground(WHITE);
        field.setBorder(
                new UnderlineBorder(new Color(150, 150, 150))
        );

        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();

        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(TEXT);
        field.setBackground(WHITE);
        field.setBorder(
                new UnderlineBorder(new Color(150, 150, 150))
        );

        return field;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);

        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(WHITE);
        button.setBackground(BLUE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(190, 39));

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

    private JButton createLinkButton(String text) {
        JButton button = new JButton(text);

        button.setFont(new Font("SansSerif", Font.PLAIN, 11));
        button.setForeground(TEXT);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
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

    // ============================================================
    // MISSING METHOD FROM YOUR ERROR: ILLUSTRATION PANEL CREATOR
    // ============================================================

    private JPanel createIllustrationPanel() {
        IllustrationPanel panel = new IllustrationPanel();

        panel.setPreferredSize(new Dimension(420, 475));
        panel.setBackground(new Color(235, 247, 253));

        return panel;
    }

    // ============================================================
    // ROUNDED CARD PANEL
    // ============================================================

    private static class RoundedPanel extends JPanel {

        private static final long serialVersionUID = 1L;

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

    // ============================================================
    // UNDERLINE BORDER
    // ============================================================

    private static class UnderlineBorder extends AbstractBorder {

        private static final long serialVersionUID = 1L;

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

    // ============================================================
    // LEFT ILLUSTRATION PANEL
    // ============================================================

    private static class IllustrationPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            // Background circles
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
            g2.fillRoundRect(
                    monitorX,
                    monitorY,
                    monitorW,
                    monitorH,
                    10,
                    10
            );

            g2.setColor(new Color(185, 189, 194));
            g2.drawRoundRect(
                    monitorX,
                    monitorY,
                    monitorW,
                    monitorH,
                    10,
                    10
            );

            // Monitor stand
            g2.setColor(new Color(195, 201, 205));
            g2.fillRect(
                    monitorX + 58,
                    monitorY + monitorH,
                    50,
                    12
            );

            g2.fillRect(
                    monitorX + 38,
                    monitorY + monitorH + 12,
                    90,
                    7
            );

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

            // Red ribbon
            g2.setColor(new Color(239, 75, 82));
            g2.fillRect(184, 139, 28, 8);
            g2.fillRect(198, 133, 8, 22);

            // Person on right
            g2.setColor(new Color(38, 169, 222));
            g2.fillOval(310, 278, 20, 20);
            g2.fillRect(310, 296, 20, 38);

            g2.setColor(new Color(37, 44, 57));
            g2.fillOval(315, 331, 12, 12);
            g2.fillRect(313, 325, 10, 30);

            // Person near top of monitor
            g2.setColor(new Color(38, 169, 222));
            g2.fillOval(290, 184, 15, 15);
            g2.fillRect(287, 198, 17, 30);

            g2.setColor(new Color(42, 45, 55));
            g2.fillOval(286, 224, 14, 14);
            g2.fillRect(286, 236, 12, 37);

            // Cable and plug
            g2.setColor(new Color(120, 125, 130));
            g2.drawLine(292, 300, 335, 300);
            g2.drawLine(335, 300, 335, 270);
            g2.drawRoundRect(332, 260, 30, 22, 5, 5);

            // Mouse
            g2.setColor(new Color(40, 160, 215));
            g2.fillOval(238, 333, 28, 15);

            g2.setColor(Color.WHITE);
            g2.fillOval(248, 337, 5, 3);

            g2.dispose();
        }
    }

    // ============================================================
    // MAIN METHOD
    // ============================================================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new ForgotPasswordGUI().setVisible(true)
        );
    }
}