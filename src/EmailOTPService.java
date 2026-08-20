import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailOTPService {

    // ---- Fill these in with YOUR Gmail account ----
    // 1. Turn on 2-Step Verification on this Gmail account (myaccount.google.com/security)
    // 2. Go to myaccount.google.com/apppasswords and generate an App Password
    // 3. Paste that 16-character app password below (NOT your normal Gmail password)
    private static final String SENDER_EMAIL = "your-email@gmail.com";
    private static final String SENDER_APP_PASSWORD = "xxxx xxxx xxxx xxxx";

    /**
     * Sends a 6-digit OTP to the given email address.
     * Returns true if the email was sent successfully.
     */
    public static boolean sendOtp(String toEmail, String otp) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("SmartStudent - Password Reset OTP");
            message.setText("Your OTP for resetting your SmartStudent password is: " + otp +
                    "\n\nThis OTP is valid for 5 minutes. If you did not request this, ignore this email.");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Generates a random 6-digit OTP as a String, e.g. "042817" */
    public static String generateOtp() {
        int otp = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(otp);
    }
}
