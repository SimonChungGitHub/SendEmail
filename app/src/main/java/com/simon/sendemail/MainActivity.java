package com.simon.sendemail;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MainActivity extends AppCompatActivity {

    private final Handler handler = new Handler();
    private TextView msg;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        msg = findViewById(R.id.textView);
        msg.setText("");
        Button send = findViewById(R.id.button);
        send.setText("Email send");
        send.setOnClickListener(v -> send());

        //開啟檔案存取權限(視需要)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
//        }

    }

    @SuppressLint("SetTextI18n")
    private void send() {
        new Thread(() -> {
            handler.post(() -> msg.setText(""));
            final String username = "primochung@yahoo.com.tw";
            final String password = "密碼不是信箱密碼,必須在yahoo信箱的安全性設定內建立給第三方應用使用的驗證碼";
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.mail.yahoo.com");
            props.put("mail.smtp.port", "587");
            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse("simonchungtest@gmail.com"));
                message.setSubject("Android develop test");

                @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                Date date = new Date(System.currentTimeMillis());
                String time = formatter.format(date);
                String str = "Dear Simon,\n 這是一封來自android develop email\n" + time + "\n";

                //要附加檔案請先在Download目錄放image.png檔案
                Path path = Paths.get(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), "image.png");
                if (Files.notExists(path)) {
                    message.setText(str);
                } else {
                    // Create the message part
                    BodyPart messageBodyPart = new MimeBodyPart();
                    // Now set the actual message
                    messageBodyPart.setText(str);
                    // Create a multipar message
                    Multipart multipart = new MimeMultipart();
                    // Set text message part
                    multipart.addBodyPart(messageBodyPart);
                    // Part two is attachment
                    messageBodyPart = new MimeBodyPart();
                    String filename = path.toFile().getAbsolutePath();
                    DataSource source = new FileDataSource(filename);
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(filename);
                    multipart.addBodyPart(messageBodyPart);
                    // Send the complete message parts
                    message.setContent(multipart);
                }

                Transport.send(message);
                handler.post(() -> msg.setText("郵件已傳送\n" + time));
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }).start();
    }

}