package com.guvi.careernestbackend.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TwilioServiceTest {

    private TwilioService twilioService;

    @BeforeEach
    void setUp() {
        twilioService = new TwilioService();
    }

    @Test
    @DisplayName("Send SMS - Falls back cleanly to Simulator Mode when account SID is dummy_sid")
    void testSendSms_DummySid_FallsBackToSimulator() {
        ReflectionTestUtils.setField(twilioService, "accountSid", "dummy_sid");
        ReflectionTestUtils.setField(twilioService, "authToken", "dummy_token");
        ReflectionTestUtils.setField(twilioService, "fromPhoneNumber", "+1234567890");

        boolean result = twilioService.sendSms("+919876543210", "Status update: ACCEPTED");

        assertTrue(result, "Simulator mode should return true without throwing exceptions");
    }

    @Test
    @DisplayName("Send SMS - Falls back cleanly to Simulator Mode when account SID is null or blank")
    void testSendSms_BlankSid_FallsBackToSimulator() {
        ReflectionTestUtils.setField(twilioService, "accountSid", "");
        ReflectionTestUtils.setField(twilioService, "authToken", "");
        ReflectionTestUtils.setField(twilioService, "fromPhoneNumber", "");

        boolean result = twilioService.sendSms("+919876543210", "Status update: UNDER_REVIEW");

        assertTrue(result, "Blank SID should fall back to simulator and return true");
    }

    @Test
    @DisplayName("Send SMS - Handles Twilio API failure gracefully without breaking business flow")
    void testSendSms_InvalidCredentials_FallsBackGracefully() {
        ReflectionTestUtils.setField(twilioService, "accountSid", "ACinvalidFakeSid123456789012345678");
        ReflectionTestUtils.setField(twilioService, "authToken", "invalidToken");
        ReflectionTestUtils.setField(twilioService, "fromPhoneNumber", "+15555555555");

        boolean result = twilioService.sendSms("+919876543210", "Status update: REJECTED");

        assertTrue(result, "Any Twilio network/auth exception should be caught gracefully");
    }
}