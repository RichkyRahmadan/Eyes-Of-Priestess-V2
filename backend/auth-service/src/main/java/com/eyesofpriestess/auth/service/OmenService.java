package com.eyesofpriestess.auth.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.UUID;

/**
 * OmenService — OTP (One-Time Password) simulation service.
 *
 * In development/sandbox mode: OTP is always "123456" and is printed to logs.
 * In production: would integrate with SMS gateway (Twilio, etc.).
 *
 * OTP flow:
 * 1. Client calls /seal/omen/request → OTP generated, stored in Crystal, omenToken returned
 * 2. Client calls /seal/omen/verify with omenToken + code → verified, verificationToken returned
 * 3. Client uses verificationToken in /seal/forge to prove phone ownership
 */
@ApplicationScoped
public class OmenService {

    private static final Logger LOG = Logger.getLogger(OmenService.class);
    private static final String DEV_OTP = "123456"; // Fixed OTP for development

    @Inject
    CrystalService crystalService;

    /**
     * Requests an Omen (OTP) for the given phone.
     * @param phone phone number to send OTP to
     * @param purpose FORGE | RESET_PASSWORD | CHANGE_PHONE
     * @return omenToken that the client uses to verify the OTP
     */
    public Uni<String> requestOmen(String phone, String purpose) {
        String omenToken = UUID.randomUUID().toString();
        String otp = generateOtp();

        LOG.infof("[OMEN SIMULATED] Phone: %s | Purpose: %s | OTP: %s | Token: %s",
                phone, purpose, otp, omenToken);

        // In production: send SMS via SMS gateway here
        // await smsGateway.send(phone, "Your EyesOfPriestess code: " + otp);

        return crystalService.storeOmen(omenToken, otp, phone)
                .replaceWith(omenToken);
    }

    /**
     * Verifies an Omen token+code pair.
     * @return verificationToken if valid, null if expired or incorrect
     */
    public Uni<String> verifyOmen(String omenToken, String omenCode) {
        return crystalService.verifyOmen(omenToken, omenCode)
                .flatMap(phone -> {
                    if (phone == null) return Uni.createFrom().nullItem();

                    // OTP is valid — issue verification token
                    String verificationToken = UUID.randomUUID().toString();
                    return crystalService.storeVerificationToken(verificationToken, phone)
                            .replaceWith(verificationToken);
                });
    }

    /**
     * Validates a verification token and returns the associated phone.
     */
    public Uni<String> getVerifiedPhone(String verificationToken) {
        return crystalService.getVerifiedPhone(verificationToken);
    }

    /**
     * Invalidates a verification token after use (one-time use).
     */
    public Uni<Void> consumeVerificationToken(String verificationToken) {
        return crystalService.invalidateVerificationToken(verificationToken);
    }

    private String generateOtp() {
        // In production: return String.format("%06d", new Random().nextInt(1000000));
        return DEV_OTP;
    }
}
