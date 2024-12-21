package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class ShipShapeNotificationEmp extends JPanel{


    private static final String EMAIL_USERNAME = "cravikantha@gmai.com";
    private static final String EMAIL_PASSWORD = "dhfn gywz qool lcjr";


    String url = "jdbc:mysql://localhost:3306/shipshape";
    String user = "root";
    String password = "";


    private JTextField EmpIdField;
    private JButton sendButton;

    public ShipShapeNotificationEmp() {
        super();


        EmpIdField = new JTextField(30);
        sendButton = new JButton("Send Notification");


        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(10, 10, 10, 10);


        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(new JLabel("Employee ID:"), constraints);

        constraints.gridx = 1;
        panel.add(EmpIdField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        panel.add(sendButton, constraints);


        add(panel);


        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String EmpId = EmpIdField.getText().trim();


                if (EmpId.isEmpty()) {
                    JOptionPane.showMessageDialog(ShipShapeNotificationEmp.this,
                            "Please fill in the Order ID field.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ShipShapeNotificationEmp.EmpInfo EmpInfo = getEmpInfo(EmpId);
                            if (EmpInfo != null) {
                                sendNotification(EmpInfo.EmpMail, EmpId, EmpInfo.EmpName);
                            } else {
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ShipShapeNotificationEmp.this,
                                        "Order information not found.", "Error", JOptionPane.ERROR_MESSAGE));
                            }
                        }
                    }).start();
                }
            }
        });
    }


    class EmpInfo {
        String EmpMail;
        String EmpName;

        public EmpInfo(String EmpMail, String EmpName) {
            this.EmpMail = EmpMail;
            this.EmpName = EmpName;
        }
    }


    private ShipShapeNotificationEmp.EmpInfo getEmpInfo(String EmpId) {
        ShipShapeNotificationEmp.EmpInfo EmpInfo = null;

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String query = "SELECT email_address, full_name FROM employees WHERE employee_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, EmpId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String EmpMail = rs.getString("email_address");
                        String EmpName = rs.getString("full_name");
                        EmpInfo = new ShipShapeNotificationEmp.EmpInfo(EmpMail, EmpName);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return EmpInfo;
    }


    private void sendNotification(String EmpMail, String EmpId, String EmpName) {
        String subject = "Your Ship is Ready for Collection";
        String body = "Dear " + EmpName + ",\n\n"
                + "We are pleased to inform you that you have allocated to a new job "
                + "you can get more details by contact our team  \n\n";



        sendEmail(EmpMail, subject, body);
    }


    private void sendEmail(String recipientEmail, String subject, String content) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587"); // SMTP port

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(content);

            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ShipShapeNotificationEmp.this,
                    "Email sent successfully to " + recipientEmail, "Success", JOptionPane.INFORMATION_MESSAGE));

        } catch (MessagingException e) {
            e.printStackTrace();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ShipShapeNotificationEmp.this,
                    "Failed to send email. Check console for details.", "Error", JOptionPane.ERROR_MESSAGE));
        }
    }
}
