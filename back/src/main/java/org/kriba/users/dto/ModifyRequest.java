package org.kriba.users.dto;

public record ModifyRequest(LoginRequest loginRequest, String newUsername, String newEmail, String newPassword) {

}