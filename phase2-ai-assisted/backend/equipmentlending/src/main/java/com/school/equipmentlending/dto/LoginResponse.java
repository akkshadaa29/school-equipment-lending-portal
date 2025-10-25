package com.school.equipmentlending.dto;
import com.school.equipmentlending.dto.UserDTO;
public class LoginResponse {
    private String accessToken;
    private UserDTO user;

    public LoginResponse() {}

    public LoginResponse(String accessToken, UserDTO user) {
        this.accessToken = accessToken;
        this.user = user;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }
}