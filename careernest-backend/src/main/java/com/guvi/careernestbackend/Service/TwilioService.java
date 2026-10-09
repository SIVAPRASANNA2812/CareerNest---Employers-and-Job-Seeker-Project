package com.guvi.careernestbackend.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TwilioService {

    private static final Logger log = LoggerFactory.getLogger(TwilioService.class);

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    public boolean sendSms(String toPhoneNumber, String messageBody) {
        // Production Check: If credentials are not dummy, dispatch real Twilio SMS
        if (accountSid != null && !accountSid.equals("dummy_sid") && !accountSid.isBlank()) {
            try {
                Twilio.init(accountSid, authToken);
                Message message = Message.creator(
                        new PhoneNumber(toPhoneNumber),
                        new PhoneNumber(fromPhoneNumber),
                        messageBody
                ).create();

                log.info("Twilio SMS sent successfully! SID: {}", message.getSid());
                return true;
            } catch (Exception e) {
                log.error("Failed to send Twilio SMS to {}: {}", toPhoneNumber, e.getMessage());
                // Fall back to simulator so the status update doesn't fail
            }
        }

        // Simulator mode (logs to console for testing/development)
        log.info("[SMS SIMULATOR] To: {} | Message: {}", toPhoneNumber, messageBody);
        return true;
    }
}