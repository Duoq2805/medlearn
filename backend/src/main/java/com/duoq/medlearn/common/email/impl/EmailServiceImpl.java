package com.duoq.medlearn.common.email.impl;

import com.duoq.medlearn.common.email.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.url}")
    private String appUrl;

    @Async
    public void sendPasswordResetEmail(String to, String username, String token) {
        String resetUrl = appUrl + "/api/auth/reset-password?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Medvora");
            helper.setTo(to);
            helper.setSubject("Reset Your Medvora Password");

            String htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Reset Password</title>
                    </head>
                    <body style="margin:0;padding:0;background-color:#111827;font-family:Arial,sans-serif;color:#F9FAFB;">
                    <table width="100%%" cellspacing="0" cellpadding="0" style="background-color:#111827;padding:40px 16px;">
                        <tr><td align="center">
                            <table width="100%%" style="max-width:600px;background:#1F2937;border-radius:24px;overflow:hidden;border:1px solid rgba(143,191,159,0.15);box-shadow:0 12px 40px rgba(0,0,0,0.45);" cellspacing="0" cellpadding="0">
                                <tr><td style="background:linear-gradient(135deg,#8FBF9F 0%%,#5C8374 100%%);padding:42px;text-align:center;">
                                    <h1 style="margin:0;color:#111827;font-size:34px;font-weight:800;letter-spacing:1px;">Medvora</h1>
                                    <p style="margin-top:12px;color:#1F2937;font-size:15px;font-weight:500;">Medical Learning Support Platform</p>
                                </td></tr>
                                <tr><td style="padding:48px;">
                                    <h2 style="margin-top:0;color:#F9FAFB;font-size:28px;font-weight:700;">Reset Your Password</h2>
                                    <p style="color:#D1D5DB;font-size:16px;line-height:1.8;">Hello <strong style="color:#8FBF9F;">%s</strong>,</p>
                                    <p style="color:#D1D5DB;font-size:16px;line-height:1.8;">We received a request to reset your Medvora account password. Click the button below to set a new password.</p>
                                    <table width="100%%" cellspacing="0" cellpadding="0" style="margin:42px 0;">
                                        <tr><td align="center">
                                            <table cellspacing="0" cellpadding="0">
                                                <tr><td align="center" bgcolor="#8FBF9F" style="border-radius:14px;box-shadow:0 6px 18px rgba(143,191,159,0.35);">
                                                    <a href="%s" style="display:inline-block;padding:16px 34px;color:#111827;font-size:16px;font-weight:700;text-decoration:none;">Reset Password</a>
                                                </td></tr>
                                            </table>
                                        </td></tr>
                                    </table>
                                    <div style="background:#111827;border:1px solid rgba(143,191,159,0.15);border-radius:14px;padding:18px;margin-top:24px;">
                                        <p style="margin:0 0 12px 0;color:#9CA3AF;font-size:14px;line-height:1.7;">This link will expire in <strong style="color:#8FBF9F;">1 hour</strong>.</p>
                                        <p style="margin:0;color:#9CA3AF;font-size:13px;line-height:1.7;word-break:break-all;">If the button does not work, copy and paste this link into your browser:</p>
                                        <a href="%s" style="color:#8FBF9F;font-size:13px;text-decoration:none;word-break:break-all;">%s</a>
                                    </div>
                                    <p style="margin-top:28px;color:#6B7280;font-size:13px;line-height:1.7;">If you did not request a password reset, you can safely ignore this email. Your password will not be changed.</p>
                                </td></tr>
                                <tr><td style="padding:28px;background:#111827;text-align:center;border-top:1px solid rgba(143,191,159,0.08);">
                                    <p style="margin:0;color:#8FBF9F;font-size:14px;font-weight:600;">Medvora</p>
                                    <p style="margin-top:8px;color:#6B7280;font-size:12px;">Medical Learning Support System</p>
                                    <p style="margin-top:16px;color:#4B5563;font-size:11px;">© 2026 Medvora. All rights reserved.</p>
                                </td></tr>
                            </table>
                        </td></tr>
                    </table>
                    </body>
                    </html>
                    """.formatted(username, resetUrl, resetUrl, resetUrl);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send password reset email to: {}", to, e);
        }
    }

    @Async
    public void sendVerificationEmail(String to, String username, String token) {

        String verificationUrl =
                appUrl + "/api/auth/verify?token=" + token;

        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true,
                            "UTF-8"
                    );

            helper.setFrom(fromEmail, "Medvora");
            helper.setTo(to);
            helper.setSubject("Verify Your Medvora Account");

            String htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport"
                              content="width=device-width, initial-scale=1.0">

                        <title>Verify Email</title>
                    </head>

                    <body style="
                        margin:0;
                        padding:0;
                        background-color:#111827;
                        font-family:Arial,sans-serif;
                        color:#F9FAFB;
                    ">

                    <table width="100%%"
                           cellspacing="0"
                           cellpadding="0"
                           style="
                                background-color:#111827;
                                padding:40px 16px;
                           ">

                        <tr>
                            <td align="center">

                                <table width="100%%"
                                       style="
                                            max-width:600px;
                                            background:#1F2937;
                                            border-radius:24px;
                                            overflow:hidden;
                                            border:1px solid rgba(143,191,159,0.15);
                                            box-shadow:
                                            0 12px 40px rgba(0,0,0,0.45);
                                       "
                                       cellspacing="0"
                                       cellpadding="0">

                                    <!-- HEADER -->
                                    <tr>
                                        <td style="
                                            background:
                                            linear-gradient(
                                                135deg,
                                                #8FBF9F 0%%,
                                                #5C8374 100%%
                                            );

                                            padding:42px;
                                            text-align:center;
                                        ">

                                            <h1 style="
                                                margin:0;
                                                color:#111827;
                                                font-size:34px;
                                                font-weight:800;
                                                letter-spacing:1px;
                                            ">
                                                Medvora
                                            </h1>

                                            <p style="
                                                margin-top:12px;
                                                color:#1F2937;
                                                font-size:15px;
                                                font-weight:500;
                                            ">
                                                Medical Learning Support Platform
                                            </p>

                                        </td>
                                    </tr>

                                    <!-- CONTENT -->
                                    <tr>
                                        <td style="padding:48px;">

                                            <h2 style="
                                                margin-top:0;
                                                color:#F9FAFB;
                                                font-size:28px;
                                                font-weight:700;
                                            ">
                                                Verify Your Email
                                            </h2>

                                            <p style="
                                                color:#D1D5DB;
                                                font-size:16px;
                                                line-height:1.8;
                                            ">
                                                Hello
                                                <strong style="color:#8FBF9F;">
                                                    %s
                                                </strong>,
                                            </p>

                                            <p style="
                                                color:#D1D5DB;
                                                font-size:16px;
                                                line-height:1.8;
                                            ">
                                                Welcome to Medvora.
                                                Please verify your email address
                                                to activate your account and
                                                start exploring structured
                                                medical learning content.
                                            </p>

                                            <!-- BUTTON -->
                                            <table width="100%%"
                                                   cellspacing="0"
                                                   cellpadding="0"
                                                   style="margin:42px 0;">

                                                <tr>
                                                    <td align="center">

                                                        <table cellspacing="0"
                                                               cellpadding="0">

                                                            <tr>
                                                                <td align="center"
                                                                    bgcolor="#8FBF9F"
                                                                    style="
                                                                        border-radius:14px;
                                                                        box-shadow:
                                                                        0 6px 18px
                                                                        rgba(
                                                                            143,
                                                                            191,
                                                                            159,
                                                                            0.35
                                                                        );
                                                                    ">

                                                                    <a href="%s"
                                                                       style="
                                                                            display:inline-block;
                                                                            padding:16px 34px;
                                                                            color:#111827;
                                                                            font-size:16px;
                                                                            font-weight:700;
                                                                            text-decoration:none;
                                                                       ">
                                                                        Verify Email
                                                                    </a>

                                                                </td>
                                                            </tr>

                                                        </table>

                                                    </td>
                                                </tr>

                                            </table>

                                            <!-- FALLBACK LINK -->
                                            <div style="
                                                background:#111827;
                                                border:1px solid
                                                rgba(143,191,159,0.15);

                                                border-radius:14px;
                                                padding:18px;
                                                margin-top:24px;
                                            ">

                                                <p style="
                                                    margin:0 0 12px 0;
                                                    color:#9CA3AF;
                                                    font-size:14px;
                                                    line-height:1.7;
                                                ">
                                                    This verification link will
                                                    expire in
                                                    <strong
                                                        style="color:#8FBF9F;">
                                                        24 hours
                                                    </strong>.
                                                </p>

                                                <p style="
                                                    margin:0;
                                                    color:#9CA3AF;
                                                    font-size:13px;
                                                    line-height:1.7;
                                                    word-break:break-all;
                                                ">
                                                    If the button does not work,
                                                    copy and paste this link
                                                    into your browser:
                                                </p>

                                                <a href="%s"
                                                   style="
                                                        color:#8FBF9F;
                                                        font-size:13px;
                                                        text-decoration:none;
                                                        word-break:break-all;
                                                   ">
                                                    %s
                                                </a>

                                            </div>

                                            <p style="
                                                margin-top:28px;
                                                color:#6B7280;
                                                font-size:13px;
                                                line-height:1.7;
                                            ">
                                                If you did not create an account,
                                                you can safely ignore this email.
                                            </p>

                                        </td>
                                    </tr>

                                    <!-- FOOTER -->
                                    <tr>
                                        <td style="
                                            padding:28px;
                                            background:#111827;
                                            text-align:center;
                                            border-top:1px solid
                                            rgba(143,191,159,0.08);
                                        ">

                                            <p style="
                                                margin:0;
                                                color:#8FBF9F;
                                                font-size:14px;
                                                font-weight:600;
                                            ">
                                                Medvora
                                            </p>

                                            <p style="
                                                margin-top:8px;
                                                color:#6B7280;
                                                font-size:12px;
                                            ">
                                                Medical Learning Support System
                                            </p>

                                            <p style="
                                                margin-top:16px;
                                                color:#4B5563;
                                                font-size:11px;
                                            ">
                                                © 2026 Medvora.
                                                All rights reserved.
                                            </p>

                                        </td>
                                    </tr>

                                </table>

                            </td>
                        </tr>

                    </table>

                    </body>
                    </html>
                    """.formatted(
                    username,
                    verificationUrl,
                    verificationUrl,
                    verificationUrl
            );

            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info(
                    "Verification email sent successfully to: {}",
                    to
            );

        } catch (
                MessagingException |
                UnsupportedEncodingException e
        ) {

            log.error(
                    "Failed to send verification email to: {}",
                    to,
                    e
            );
        }
    }
}
