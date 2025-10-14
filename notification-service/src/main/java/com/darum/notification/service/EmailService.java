package com.darum.notification.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final SendGrid sendGrid;

    // You can use either @Value or the bean injection
    @Value("${sendgrid.from-email}")
    private String fromEmail;

    @Value("${sendgrid.from-name}")
    private String fromName;


    public Mono<Boolean> sendEmail(String to, String subject, String plainTextBody) {
        return Mono.fromCallable(() -> {
                    try {
                        Email from = new Email(fromEmail, fromName);
                        Email toEmail = new Email(to);
                        Content content = new Content("text/plain", plainTextBody);
                        Mail mail = new Mail(from, subject, toEmail, content);

                        Request request = new Request();
                        request.setMethod(Method.POST);
                        request.setEndpoint("mail/send");
                        request.setBody(mail.build());

                        log.info("📧 Attempting to send email to: {} | Subject: {}", to, subject);

                        Response response = sendGrid.api(request);

                        boolean success = response.getStatusCode() >= 200 && response.getStatusCode() < 300;

                        if (success) {
                            log.info("✅ Email sent successfully to: {} | Status: {}", to, response.getStatusCode());
                        } else {
                            log.error("❌ Failed to send email to: {} | Status: {} | Body: {}",
                                    to, response.getStatusCode(), response.getBody());
                        }

                        return success;

                    } catch (IOException e) {
                        log.error("❌ IOException sending email to {}: {}", to, e.getMessage());
                        return false;
                    } catch (Exception e) {
                        log.error("❌ Unexpected error sending email to {}: {}", to, e.getMessage());
                        return false;
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }


}
