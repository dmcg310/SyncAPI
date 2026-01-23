package com.syncapi.dto.workspace;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for adding a member to a workspace.
 */
public class AddMemberRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    /**
     * Default constructor.
     */
    public AddMemberRequest() {
    }

    /**
     * Parameterized constructor.
     *
     * @param email the email of the member to add
     */
    public AddMemberRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
