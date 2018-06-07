package com.sharpsec.fmw.auth.siam;

public class SiamLogoutResponse {
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    String user;
    String status;

    public SiamLogoutResponse(String user, String status) {
        this.user = user;
        this.status = status;
    }

}
