package com.cavcav.swiftcart.notfication.service;

import com.cavcav.swiftcart.order.model.Order;
import com.cavcav.swiftcart.order.model.OrderItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.base.url}")
    private String baseUrl;


    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            String verificationUrl = baseUrl + "/api/v1/auth/verify?token=" + token;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, "SwiftCart");
            helper.setTo(toEmail);
            helper.setSubject("SwiftCart — E-posta Adresinizi Doğrulayın");
            helper.setText(buildHtmlSignup(toEmail, verificationUrl), true);

            mailSender.send(message);
            log.info("Verification email sent → {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send verification email → {}", toEmail, e);
        } catch (Exception e) {
            log.error("Unexpected error sending email → {}", toEmail, e);
        }
    }
    @Async
    public void sendOrderConfirmationEmail(String toEmail, Order order) {
        try {
            String orderTrackingUrl = baseUrl + "/api/v1/orders/" + order.getId();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, "SwiftCart");
            helper.setTo(toEmail);
            helper.setSubject("SwiftCart — Siparişiniz Alındı ✅");
            helper.setText(buildHtmlOrderConfirmation(toEmail, order, orderTrackingUrl), true);

            mailSender.send(message);
            log.info("Order confirmation email sent → {} (orderId={})", toEmail, order.getId());

        } catch (MessagingException e) {
            log.error("Failed to send order confirmation email → {}", toEmail, e);
        } catch (Exception e) {
            log.error("Unexpected error sending order confirmation email → {}", toEmail, e);
        }
    }
    private String buildHtmlSignup(String email, String verificationUrl) {
        return """
                <!DOCTYPE html>
                <html lang="tr">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width,initial-scale=1.0"/>
                </head>
                <body style="margin:0;padding:0;background:#f4f6f9;font-family:'Segoe UI',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6f9;padding:40px 0;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0"
                             style="background:#fff;border-radius:16px;overflow:hidden;
                                    box-shadow:0 4px 24px rgba(0,0,0,.08);max-width:600px;width:100%%;">
                        <tr>
                          <td style="background:linear-gradient(135deg,#667eea,#764ba2);padding:40px 48px;text-align:center;">
                            <h1 style="margin:0;color:#fff;font-size:28px;font-weight:700;">🛒 SwiftCart</h1>
                            <p style="margin:8px 0 0;color:rgba(255,255,255,.85);font-size:14px;">Hızlı. Güvenli. Kolay alışveriş.</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:48px 48px 32px;">
                            <h2 style="margin:0 0 16px;color:#1a1a2e;font-size:22px;">Hesabınızı Doğrulayın 👋</h2>
                            <p style="margin:0 0 16px;color:#4a5568;font-size:15px;line-height:1.6;">
                              Merhaba <strong style="color:#667eea;">%s</strong>!<br/>
                              SwiftCart'a hoş geldiniz. Hesabınızı aktifleştirmek için butona tıklayın.
                            </p>
                            <p style="margin:0 0 32px;color:#4a5568;font-size:15px;line-height:1.6;">
                              Bu link <strong>24 saat</strong> geçerlidir.
                            </p>
                            <table cellpadding="0" cellspacing="0" width="100%%"><tr><td align="center">
                              <a href="%s"
                                 style="display:inline-block;background:linear-gradient(135deg,#667eea,#764ba2);
                                        color:#fff;text-decoration:none;font-size:16px;font-weight:600;
                                        padding:16px 48px;border-radius:50px;
                                        box-shadow:0 4px 15px rgba(102,126,234,.4);">
                                ✅ E-postamı Doğrula
                              </a>
                            </td></tr></table>
                            <table cellpadding="0" cellspacing="0" width="100%%" style="margin:32px 0;">
                              <tr><td style="border-top:1px solid #e2e8f0;"></td></tr>
                            </table>
                            <p style="margin:0 0 8px;color:#718096;font-size:13px;">Butona tıklayamıyor musunuz?</p>
                            <p style="margin:0;word-break:break-all;">
                              <a href="%s" style="color:#667eea;font-size:12px;text-decoration:none;">%s</a>
                            </p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:0 48px 32px;">
                            <table width="100%%" cellpadding="0" cellspacing="0"
                                   style="background:#fff8e1;border-left:4px solid #f6c90e;border-radius:8px;">
                              <tr><td style="padding:16px;">
                                <p style="margin:0;color:#7d6608;font-size:13px;line-height:1.5;">
                                  ⚠️ Bu e-postayı siz talep etmediyseniz herhangi bir işlem yapmanıza gerek yok.
                                </p>
                              </td></tr>
                            </table>
                          </td>
                        </tr>
                        <tr>
                          <td style="background:#f7fafc;padding:24px 48px;text-align:center;border-top:1px solid #e2e8f0;">
                            <p style="margin:0 0 4px;color:#a0aec0;font-size:12px;">© 2025 SwiftCart. Tüm hakları saklıdır.</p>
                            <p style="margin:0;color:#a0aec0;font-size:12px;">Bu otomatik bir e-postadır, lütfen yanıtlamayın.</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(email, verificationUrl, verificationUrl, verificationUrl);
    }

    private String buildHtmlOrderConfirmation(String userName, Order order, String orderTrackingUrl) {
        String itemsHtml = buildOrderItemsHtml(order);

        return """
                <!DOCTYPE html>
                <html lang="tr">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width,initial-scale=1.0"/>
                </head>
                <body style="margin:0;padding:0;background:#f4f6f9;font-family:'Segoe UI',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6f9;padding:40px 0;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0"
                             style="background:#fff;border-radius:16px;overflow:hidden;
                                    box-shadow:0 4px 24px rgba(0,0,0,.08);max-width:600px;width:100%%;">
                        <tr>
                          <td style="background:linear-gradient(135deg,#667eea,#764ba2);padding:40px 48px;text-align:center;">
                            <h1 style="margin:0;color:#fff;font-size:28px;font-weight:700;">🛒 SwiftCart</h1>
                            <p style="margin:8px 0 0;color:rgba(255,255,255,.85);font-size:14px;">Siparişiniz onaylandı!</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:48px 48px 24px;">
                            <h2 style="margin:0 0 16px;color:#1a1a2e;font-size:22px;">Siparişiniz Alındı 🎉</h2>
                            <p style="margin:0 0 24px;color:#4a5568;font-size:15px;line-height:1.6;">
                              Merhaba <strong style="color:#667eea;">%s</strong>!<br/>
                              Siparişiniz başarıyla oluşturuldu. Sipariş numaranız: <strong>#%s</strong>
                            </p>
                            <table width="100%%" cellpadding="0" cellspacing="0"
                                   style="border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;">
                              <tr style="background:#f7fafc;">
                                <td style="padding:12px 16px;color:#718096;font-size:12px;font-weight:600;">ÜRÜN</td>
                                <td style="padding:12px 16px;color:#718096;font-size:12px;font-weight:600;text-align:center;">ADET</td>
                                <td style="padding:12px 16px;color:#718096;font-size:12px;font-weight:600;text-align:right;">TUTAR</td>
                              </tr>
                              %s
                            </table>
                            <table width="100%%" cellpadding="0" cellspacing="0" style="margin-top:16px;">
                              <tr>
                                <td style="padding:12px 16px;color:#1a1a2e;font-size:16px;font-weight:700;text-align:right;">
                                  Toplam: %s TL
                                </td>
                              </tr>
                            </table>
                            <table cellpadding="0" cellspacing="0" width="100%%" style="margin-top:24px;"><tr><td align="center">
                              <a href="%s"
                                 style="display:inline-block;background:linear-gradient(135deg,#667eea,#764ba2);
                                        color:#fff;text-decoration:none;font-size:16px;font-weight:600;
                                        padding:16px 48px;border-radius:50px;
                                        box-shadow:0 4px 15px rgba(102,126,234,.4);">
                                📦 Siparişimi Takip Et
                              </a>
                            </td></tr></table>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:0 48px 32px;">
                            <h3 style="margin:0 0 8px;color:#1a1a2e;font-size:15px;">Teslimat Adresi</h3>
                            <p style="margin:0;color:#4a5568;font-size:13px;line-height:1.6;">
                              %s<br/>
                              %s, %s %s<br/>
                              Tel: %s
                            </p>
                          </td>
                        </tr>
                        <tr>
                          <td style="background:#f7fafc;padding:24px 48px;text-align:center;border-top:1px solid #e2e8f0;">
                            <p style="margin:0 0 4px;color:#a0aec0;font-size:12px;">© 2025 SwiftCart. Tüm hakları saklıdır.</p>
                            <p style="margin:0;color:#a0aec0;font-size:12px;">Bu otomatik bir e-postadır, lütfen yanıtlamayın.</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                userName,
                order.getId(),
                itemsHtml,
                order.getTotalPrice(),
                orderTrackingUrl,
                order.getShippingAddress(),
                order.getShippingCity(),
                order.getShippingCountry(),
                order.getShippingZipCode(),
                order.getShippingPhone()
        );
    }

    private String buildOrderItemsHtml(Order order) {
        StringBuilder rowsBuilder = new StringBuilder();

        for (OrderItem item : order.getItems()) {
            rowsBuilder.append("""
                    <tr style="border-top:1px solid #e2e8f0;">
                      <td style="padding:12px 16px;color:#1a1a2e;font-size:14px;">%s</td>
                      <td style="padding:12px 16px;color:#4a5568;font-size:14px;text-align:center;">%d</td>
                      <td style="padding:12px 16px;color:#1a1a2e;font-size:14px;text-align:right;">%s TL</td>
                    </tr>
                    """.formatted(item.getProductName(), item.getQuantity(), item.getTotalPrice()));
        }

        return rowsBuilder.toString();
    }
}