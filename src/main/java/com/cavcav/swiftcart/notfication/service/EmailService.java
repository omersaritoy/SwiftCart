package com.cavcav.swiftcart.notfication.service;

import com.cavcav.swiftcart.order.model.Order;
import com.cavcav.swiftcart.order.model.OrderItem;
import com.cavcav.swiftcart.order.model.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.cavcav.swiftcart.order.model.OrderStatus.*;

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
    @Async
    public void sendOrderCancellationEmail(String toEmail, Order order) {
        try {
            String orderTrackingUrl = baseUrl + "/api/v1/orders/" + order.getId();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, "SwiftCart");
            helper.setTo(toEmail);
            helper.setSubject("SwiftCart — Siparişiniz İptal Edildi ❌");
            helper.setText(buildHtmlOrderCancellation(toEmail, order, orderTrackingUrl), true);

            mailSender.send(message);
            log.info("Order cancellation email sent → {} (orderId={})", toEmail, order.getId());

        } catch (MessagingException e) {
            log.error("Failed to send order cancellation email → {}", toEmail, e);
        } catch (Exception e) {
            log.error("Unexpected error sending order cancellation email → {}", toEmail, e);
        }
    }
    @Async
    public void sendOrderStatusChangedEmail(String toEmail, Order order, OrderStatus newStatus) {
        try {
            String orderTrackingUrl = baseUrl + "/api/v1/orders/" + order.getId();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, "SwiftCart");
            helper.setTo(toEmail);
            helper.setSubject("SwiftCart — Sipariş Durumunuz Güncellendi: " + describeStatus(newStatus));
            helper.setText(buildHtmlOrderStatusChanged(toEmail, order, newStatus, orderTrackingUrl), true);

            mailSender.send(message);
            log.info("Order status change email sent → {} (orderId={}, newStatus={})", toEmail, order.getId(), newStatus);

        } catch (MessagingException e) {
            log.error("Failed to send order status change email → {}", toEmail, e);
        } catch (Exception e) {
            log.error("Unexpected error sending order status change email → {}", toEmail, e);
        }
    }
    @Async
    public void sendPaymentSuccessEmail(String email, Order order) {
        try {
            String orderTrackingUrl=baseUrl+ "/api/v1/orders/"+order.getId();
            MimeMessage message=mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, "SwiftCart");
            helper.setTo(email);
            helper.setSubject("SwiftCart — Ödemeniz Alındı 💳");
            helper.setText(buildHtmlPaymentSuccess(email, order, orderTrackingUrl), true);
            mailSender.send(message);
            log.info("Payment success email sent → {} (orderId={})", email, order.getId());

        } catch (MessagingException e) {
            log.error("Failed to send payment success email → {}", email, e);
        } catch (Exception e) {
            log.error("Unexpected error sending payment success email → {}", email, e);
        }
    }
    @Async
    public void sendPaymentFailedEmail(String email, Order order) {
        try {
            String orderTrackingUrl = baseUrl + "/api/v1/orders/" + order.getId();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, "SwiftCart");
            helper.setTo(email);
            helper.setSubject("SwiftCart — Ödemeniz Alınamadı ⚠️");
            helper.setText(buildHtmlPaymentFailed(email, order, orderTrackingUrl), true);

            mailSender.send(message);
            log.info("Payment failed email sent → {} (orderId={})", email, order.getId());

        } catch (MessagingException e) {
            log.error("Failed to send payment failed email → {}", email, e);
        } catch (Exception e) {
            log.error("Unexpected error sending payment failed email → {}", email, e);
        }
    }
    @Async
    public void sendRefundEmail(String email, Order order) {
        try {
            String orderTrackingUrl = baseUrl + "/api/v1/orders/" + order.getId();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress, "SwiftCart");
            helper.setTo(email);
            helper.setSubject("SwiftCart — İadeniz İşleme Alındı 💸");
            helper.setText(buildHtmlRefund(email, order, orderTrackingUrl), true);

            mailSender.send(message);
            log.info("Refund email sent → {} (orderId={})", email, order.getId());

        } catch (MessagingException e) {
            log.error("Failed to send refund email → {}", email, e);
        } catch (Exception e) {
            log.error("Unexpected error sending refund email → {}", email, e);
        }
    }
    private String describeStatus(OrderStatus status) {
        return switch (status) {
            case PAID -> "Ödeme Alındı 💳";
            case PROCESSING -> "Hazırlanıyor 📦";
            case SHIPPED -> "Kargoya Verildi 🚚";
            case DELIVERED -> "Teslim Edildi ✅";
            default -> status.toString();
        };
    }
    private String buildHtmlPaymentSuccess(String userName, Order order, String orderTrackingUrl) {
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
                      <td style="background:linear-gradient(135deg,#38a169,#2f855a);padding:40px 48px;text-align:center;">
                        <h1 style="margin:0;color:#fff;font-size:28px;font-weight:700;">🛒 SwiftCart</h1>
                        <p style="margin:8px 0 0;color:rgba(255,255,255,.85);font-size:14px;">Ödemeniz başarıyla alındı</p>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:48px 48px 24px;">
                        <h2 style="margin:0 0 16px;color:#1a1a2e;font-size:22px;">Ödeme Onaylandı 💳</h2>
                        <p style="margin:0 0 24px;color:#4a5568;font-size:15px;line-height:1.6;">
                          Merhaba <strong style="color:#38a169;">%s</strong>!<br/>
                          Sipariş numarası <strong>#%s</strong> için ödemeniz başarıyla alındı, siparişiniz hazırlanmaya başlanacak.
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
                             style="display:inline-block;background:linear-gradient(135deg,#38a169,#2f855a);
                                    color:#fff;text-decoration:none;font-size:16px;font-weight:600;
                                    padding:16px 48px;border-radius:50px;
                                    box-shadow:0 4px 15px rgba(56,161,105,.4);">
                            📦 Siparişimi Takip Et
                          </a>
                        </td></tr></table>
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
            """.formatted(userName, order.getId(), itemsHtml, order.getTotalPrice(), orderTrackingUrl);
    }

    private String buildHtmlPaymentFailed(String userName, Order order, String orderTrackingUrl) {
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
                      <td style="background:linear-gradient(135deg,#e53e3e,#c53030);padding:40px 48px;text-align:center;">
                        <h1 style="margin:0;color:#fff;font-size:28px;font-weight:700;">🛒 SwiftCart</h1>
                        <p style="margin:8px 0 0;color:rgba(255,255,255,.85);font-size:14px;">Ödeme işlemi başarısız oldu</p>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:48px 48px 24px;">
                        <h2 style="margin:0 0 16px;color:#1a1a2e;font-size:22px;">Ödeme Alınamadı ⚠️</h2>
                        <p style="margin:0 0 16px;color:#4a5568;font-size:15px;line-height:1.6;">
                          Merhaba <strong style="color:#e53e3e;">%s</strong>!<br/>
                          Sipariş numarası <strong>#%s</strong> için ödeme işlemi gerçekleştirilemedi.
                        </p>
                        <p style="margin:0 0 32px;color:#4a5568;font-size:15px;line-height:1.6;">
                          Lütfen ödeme bilgilerinizi kontrol ederek tekrar deneyin. Sorun devam ederse destek ekibimizle iletişime geçebilirsiniz.
                        </p>
                        <table cellpadding="0" cellspacing="0" width="100%%"><tr><td align="center">
                          <a href="%s"
                             style="display:inline-block;background:linear-gradient(135deg,#e53e3e,#c53030);
                                    color:#fff;text-decoration:none;font-size:16px;font-weight:600;
                                    padding:16px 48px;border-radius:50px;
                                    box-shadow:0 4px 15px rgba(229,62,62,.4);">
                            🔄 Ödemeyi Tekrar Dene
                          </a>
                        </td></tr></table>
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
            """.formatted(userName, order.getId(), orderTrackingUrl);
    }

    private String buildHtmlRefund(String userName, Order order, String orderTrackingUrl) {
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
                      <td style="background:linear-gradient(135deg,#3182ce,#2b6cb0);padding:40px 48px;text-align:center;">
                        <h1 style="margin:0;color:#fff;font-size:28px;font-weight:700;">🛒 SwiftCart</h1>
                        <p style="margin:8px 0 0;color:rgba(255,255,255,.85);font-size:14px;">İadeniz işleme alındı</p>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:48px 48px 24px;">
                        <h2 style="margin:0 0 16px;color:#1a1a2e;font-size:22px;">İade Onaylandı 💸</h2>
                        <p style="margin:0 0 24px;color:#4a5568;font-size:15px;line-height:1.6;">
                          Merhaba <strong style="color:#3182ce;">%s</strong>!<br/>
                          Sipariş numarası <strong>#%s</strong> için iade tutarınız işleme alınmıştır.
                          Tutar, ödeme yönteminize bağlı olarak birkaç iş günü içinde hesabınıza yansıyacaktır.
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
                              İade Edilen Tutar: %s TL
                            </td>
                          </tr>
                        </table>
                        <table cellpadding="0" cellspacing="0" width="100%%" style="margin-top:24px;"><tr><td align="center">
                          <a href="%s"
                             style="display:inline-block;background:linear-gradient(135deg,#3182ce,#2b6cb0);
                                    color:#fff;text-decoration:none;font-size:16px;font-weight:600;
                                    padding:16px 48px;border-radius:50px;
                                    box-shadow:0 4px 15px rgba(49,130,206,.4);">
                            📦 Sipariş Detayını Görüntüle
                          </a>
                        </td></tr></table>
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
            """.formatted(userName, order.getId(), itemsHtml, order.getTotalPrice(), orderTrackingUrl);
    }
    private String buildHtmlOrderStatusChanged(String userName, Order order, OrderStatus newStatus, String orderTrackingUrl) {
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
                        <p style="margin:8px 0 0;color:rgba(255,255,255,.85);font-size:14px;">Sipariş durumunuz güncellendi</p>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:48px 48px 24px;">
                        <h2 style="margin:0 0 16px;color:#1a1a2e;font-size:22px;">%s</h2>
                        <p style="margin:0 0 24px;color:#4a5568;font-size:15px;line-height:1.6;">
                          Merhaba <strong style="color:#667eea;">%s</strong>!<br/>
                          Sipariş numarası <strong>#%s</strong> olan siparişinizin durumu güncellendi.
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
                describeStatus(newStatus),
                userName,
                order.getId(),
                itemsHtml,
                orderTrackingUrl
        );
    }
    private String buildHtmlOrderCancellation(String userName, Order order, String orderTrackingUrl) {
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
                      <td style="background:linear-gradient(135deg,#e53e3e,#c53030);padding:40px 48px;text-align:center;">
                        <h1 style="margin:0;color:#fff;font-size:28px;font-weight:700;">🛒 SwiftCart</h1>
                        <p style="margin:8px 0 0;color:rgba(255,255,255,.85);font-size:14px;">Siparişiniz iptal edildi</p>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:48px 48px 24px;">
                        <h2 style="margin:0 0 16px;color:#1a1a2e;font-size:22px;">Siparişiniz İptal Edildi ❌</h2>
                        <p style="margin:0 0 24px;color:#4a5568;font-size:15px;line-height:1.6;">
                          Merhaba <strong style="color:#e53e3e;">%s</strong>!<br/>
                          Sipariş numarası <strong>#%s</strong> olan siparişiniz iptal edilmiştir.
                          Ödemeniz alınmışsa iade süreci başlatılacaktır.
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
                             style="display:inline-block;background:linear-gradient(135deg,#e53e3e,#c53030);
                                    color:#fff;text-decoration:none;font-size:16px;font-weight:600;
                                    padding:16px 48px;border-radius:50px;
                                    box-shadow:0 4px 15px rgba(229,62,62,.4);">
                            📦 Sipariş Detayını Görüntüle
                          </a>
                        </td></tr></table>
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
                orderTrackingUrl
        );
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