package com.eyesofpriestess.room.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AcceptRoomRequest {

    @NotBlank(message = "Invitation code is required")
    public String invitationCode;
}
