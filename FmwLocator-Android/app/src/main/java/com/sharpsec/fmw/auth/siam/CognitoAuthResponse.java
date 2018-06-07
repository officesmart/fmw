package com.sharpsec.fmw.auth.siam;

public class CognitoAuthResponse {

    public CognitoAuthResponse(String token, String access) {
        this.token = token;
        this.access = access;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    String token;
    String access;

}
