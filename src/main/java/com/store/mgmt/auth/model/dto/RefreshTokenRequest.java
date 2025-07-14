package com.store.mgmt.auth.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "RefreshTokenRequest", description = "Data Transfer Object for a Refresh Token")
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh Token cannot be empty")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}