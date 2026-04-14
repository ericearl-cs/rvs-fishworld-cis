package com.rvsfishworld.model;

import java.time.LocalDateTime;

public class AppSession {
    private final UserAccount userAccount;
    private final CompanyProfile companyProfile;
    private final LocalDateTime loginTime;

    public AppSession(UserAccount userAccount, CompanyProfile companyProfile, LocalDateTime loginTime) {
        this.userAccount = userAccount;
        this.companyProfile = companyProfile;
        this.loginTime = loginTime;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public CompanyProfile getCompanyProfile() {
        return companyProfile;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }
}
