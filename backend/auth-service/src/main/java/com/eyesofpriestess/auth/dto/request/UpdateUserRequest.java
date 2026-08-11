package com.eyesofpriestess.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** UpdateUserRequest — Payload for PUT /seal/self (Update Profile). */
public class UpdateUserRequest {

    @Size(min = 2, max = 100, message = "Full name must be 2-100 characters")
    public String fullName;

    @Email(message = "Invalid email format")
    public String email;

    public String profilePhotoUrl;
}
