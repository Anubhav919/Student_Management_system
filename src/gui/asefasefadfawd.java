// import javax.swing.*;
// import java.awt.*;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.util.prefs.Preferences;

// public class StudentLoginGUI extends JFrame {

//     // --- shared color palette (only 3 colors used across the whole app) ---
//     private static final Color NAVY = new Color(0x2C, 0x3E, 0x50);
//     private static final Color TEAL = new Color(0x1A, 0xBC, 0x9C);
//     private static final Color LIGHT_GRAY = new Color(0xEC, 0xF0, 0xF1);

//     private static final Preferences PREFS = Preferences.userNodeForPackage(StudentLoginGUI.class);
//     private static final String PREF_REMEMBERED_ROLLNO = "remembered_roll_no";
//     private static final String PREF_REMEMBERED_PASSWORD = "remembered_password";

//     private JTextField rollNoField;
//     private JPasswordField passwordField;
//     private JCheckBox rememberMeCheckBox;
//     private JButton togglePasswordBtn;
//     private char hiddenEchoChar;
//     private boolean passwordVisible = false;

//     public StudentLoginGUI() {
//         setTitle("Student Login");
//         setSize(420, 460);
//         setLocationRelativeTo(null);
//         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//         setResizable(false);

//         JPanel mainPanel = new JPanel(new BorderLayout());
//         mainPanel.setBackground(LIGHT_GRAY);

//         // ---- header ----
//         JPanel header = new JPanel();
//         header.setBackground(NAVY);
//         header.setPreferredSize(new Dimension(420, 70));
//         JLabel titleLabel = new JLabel("Student Login");
//         titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
//         titleLabel.setForeground(LIGHT_GRAY);
//         header.add(titleLabel);
//         mainPanel.add(header, BorderLayout.NORTH);

//         // ---- form ----
//         JPanel form = new JPanel();
//         form.setBackground(LIGHT_GRAY);
//         form.setLayout(new GridBagLayout());
//         GridBagConstraints gbc = new GridBagConstraints();
//         gbc.insets = new Insets(10, 20, 10, 20);
//         gbc.fill = GridBagConstraints.HORIZONTAL;

//         Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
//         Font fieldFont = new Font("SansSerif", Font.PLAIN, 14);

//         JLabel rollLabel = new JLabel("Roll Number");
//         rollLabel.setFont(labelFont);
//         rollLabel.setForeground(NAVY);
//         gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
//         form.add(rollLabel, gbc);

//         rollNoField = new JTextField();
//         rollNoField.setFont(fieldFont);
//         rollNoField.setBackground(Color.WHITE);
//         rollNoField.setForeground(NAVY);
//         gbc.gridy = 1;
//         form.add(rollNoField, gbc);

//         JLabel passLabel = new JLabel("Password");
//         passLabel.setFont(labelFont);
//         passLabel.setForeground(NAVY);
//         gbc.gridy = 2;
//         form.add(passLabel, gbc);

//         // ---- password field + show/hide eye toggle, side by side ----
//         passwordField = new JPasswordField();
//         passwordField.setFont(fieldFont);
//         passwordField.setBackground(Color.WHITE);
//         passwordField.setForeground(NAVY);
//         hiddenEchoChar = passwordField.getEchoChar(); // remember the default masking char

//         togglePasswordBtn = new JButton("Show");
//         togglePasswordBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
//         togglePasswordBtn.setFocusable(false);
//         togglePasswordBtn.setMargin(new Insets(2, 6, 2, 6));
//         togglePasswordBtn.addActionListener(e -> togglePasswordVisibility());

//         JPanel passwordRow = new JPanel(new BorderLayout(6, 0));
//         passwordRow.setBackground(LIGHT_GRAY);
//         passwordRow.add(passwordField, BorderLayout.CENTER);
//         passwordRow.add(togglePasswordBtn, BorderLayout.EAST);

//         gbc.gridy = 3;
//         form.add(passwordRow, gbc);

//         // ---- remember me + forgot password row ----
//         JPanel optionsRow = new JPanel(new BorderLayout());
//         optionsRow.setBackground(LIGHT_GRAY);

//         rememberMeCheckBox = new JCheckBox("Remember Me");
//         rememberMeCheckBox.setBackground(LIGHT_GRAY);
//         rememberMeCheckBox.setForeground(NAVY);
//         rememberMeCheckBox.setFont(new Font("SansSerif", Font.PLAIN, 12));
//         optionsRow.add(rememberMeCheckBox, BorderLayout.WEST);

//         JButton forgotPasswordLink = new JButton("Forgot Password?");
//         forgotPasswordLink.setBorderPainted(false);
//         forgotPasswordLink.setContentAreaFilled(false);
//         forgotPasswordLink.setForeground(NAVY);
//         forgotPasswordLink.setFont(new Font("SansSerif", Font.PLAIN, 12));
//         forgotPasswordLink.setFocusPainted(false);
//         optionsRow.add(forgotPasswordLink, BorderLayout.EAST);

//         gbc.gridy = 4;
//         form.add(optionsRow, gbc);

//         JButton loginButton = new JButton("Login");
//         loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
//         loginButton.setBackground(TEAL);
//         loginButton.setForeground(Color.WHITE);
//         loginButton.setFocusPainted(false);
//         loginButton.setBorderPainted(false);
//         loginButton.setOpaque(true);
//         gbc.gridy = 5;
//         gbc.insets = new Insets(20, 20, 12, 20);
//         form.add(loginButton, gbc);

//         mainPanel.add(form, BorderLayout.CENTER);
//         add(mainPanel);

//         loginButton.addActionListener(e -> attemptLogin());
//         passwordField.addActionListener(e -> attemptLogin()); // allow Enter key to submit
//         forgotPasswordLink.addActionListener(e -> new ForgotPasswordGUI().setVisible(true));

//         loadRememberedCredentials();
//     }

//     private void togglePasswordVisibility() {
//         passwordVisible = !passwordVisible;
//         if (passwordVisible) {
//             passwordField.setEchoChar((char) 0); // 0 = show the actual characters
//             togglePasswordBtn.setText("Hide");
//         } else {
//             passwordField.setEchoChar(hiddenEchoChar);
//             togglePasswordBtn.setText("Show");
//         }
//     }

//     private void loadRememberedCredentials() {
//         String rememberedRollNo = PREFS.get(PREF_REMEMBERED_ROLLNO, null);
//         String rememberedPassword = PREFS.get(PREF_REMEMBERED_PASSWORD, null);
//         if (rememberedRollNo != null) {
//             rollNoField.setText(rememberedRollNo);
//             rememberMeCheckBox.setSelected(true);
//             if (rememberedPassword != null) {
//                 passwordField.setText(rememberedPassword);
//             }
//         }
//     }

//     /**
//      * Single shared login: the same Roll Number + Password fields are checked
//      * against the students table first, then against the admin table.
//      * No separate admin login screen - whichever record matches decides which
//      * dashboard opens.
//      */
//     private void attemptLogin() {
//         String enteredId = rollNoField.getText().trim();
//         String password = new String(passwordField.getPassword()).trim();

//         if (enteredId.isEmpty() || password.isEmpty()) {
//             JOptionPane.showMessageDialog(this, "Please enter both Roll Number and Password.",
//                     "Missing Information", JOptionPane.WARNING_MESSAGE);
//             return;
//         }

//         boolean loggedIn = tryStudentLogin(enteredId, password) || tryAdminLogin(enteredId, password);

//         if (loggedIn) {
//             if (rememberMeCheckBox.isSelected()) {
//                 PREFS.put(PREF_REMEMBERED_ROLLNO, enteredId);
//                 PREFS.put(PREF_REMEMBERED_PASSWORD, password);
//             } else {
//                 PREFS.remove(PREF_REMEMBERED_ROLLNO);
//                 PREFS.remove(PREF_REMEMBERED_PASSWORD);
//             }
//             return;
//         }

//         JOptionPane.showMessageDialog(this, "Invalid Roll Number/ID or Password.",
//                 "Login Failed", JOptionPane.ERROR_MESSAGE);
//         passwordField.setText("");
//     }

//     /** Returns true and opens the student dashboard if enteredId/password matches a student. */
//     private boolean tryStudentLogin(String rollNo, String password) {
//         String sql = "SELECT name, course, department FROM students WHERE roll_no = ? AND password = ?";

//         try (Connection conn = DatabaseConnection.getConnection();
//              PreparedStatement stmt = conn.prepareStatement(sql)) {

//             stmt.setString(1, rollNo);
//             stmt.setString(2, password);

//             try (ResultSet rs = stmt.executeQuery()) {
//                 if (rs.next()) {
//                     String studentName = rs.getString("name");
//                     String course = rs.getString("course");
//                     String department = rs.getString("department");

//                     dispose();
//                     new StudentDashboardGUI(studentName, rollNo, course, department).setVisible(true);
//                     return true;
//                 }
//             }

//         } catch (Exception ex) {
//             JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
//                     "Error", JOptionPane.ERROR_MESSAGE);
//             ex.printStackTrace();
//         }
//         return false;
//     }

//     /** Returns true and opens the admin dashboard if enteredId/password matches an admin. */
//     private boolean tryAdminLogin(String username, String password) {
//         String sql = "SELECT username FROM admin WHERE username = ? AND password = ?";

//         try (Connection conn = DatabaseConnection.getConnection();
//              PreparedStatement stmt = conn.prepareStatement(sql)) {

//             stmt.setString(1, username);
//             stmt.setString(2, password);

//             try (ResultSet rs = stmt.executeQuery()) {
//                 if (rs.next()) {
//                     dispose();
//                     new AdminDashboardGUI(username).setVisible(true);
//                     return true;
//                 }
//             }

//         } catch (Exception ex) {
//             JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
//                     "Error", JOptionPane.ERROR_MESSAGE);
//             ex.printStackTrace();
//         }
//         return false;
//     }

//     public static void main(String[] args) {
//         SwingUtilities.invokeLater(() -> new StudentLoginGUI().setVisible(true));
//     }
// }