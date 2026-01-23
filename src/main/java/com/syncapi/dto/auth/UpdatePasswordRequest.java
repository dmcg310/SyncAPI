package com.syncapi.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating user password.
 */
public class UpdatePasswordRequest {
    @NotBlank(message = "Original password is required")
    private String originalPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters long")
    private String newPassword;

    /**
     * Default constructor.
     */
    public UpdatePasswordRequest() {
    }

    /**
     * Parameterized constructor.
     *
     * @param originalPassword the user's original password
     * @param newPassword      the user's new password
     */
    public UpdatePasswordRequest(String originalPassword, String newPassword) {
        this.originalPassword = originalPassword;
        this.newPassword = newPassword;
    }

    public String getOriginalPassword() {
        return originalPassword;
    }

    public void setOriginalPassword(String originalPassword) {
        this.originalPassword = originalPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
