package com.rvsfishworld;

import com.rvsfishworld.model.AppSession;
import com.rvsfishworld.model.CompanyProfile;
import com.rvsfishworld.model.UserAccount;

public final class AppRuntime {
    private static AppSession session;

    private AppRuntime() {
    }

    public static AppSession getSession() {
        return session;
    }

    public static void setSession(AppSession value) {
        session = value;
    }

    public static String username() {
        UserAccount user = session == null ? null : session.getUserAccount();
        return user == null ? "" : user.getUsername();
    }

    public static String displayName() {
        UserAccount user = session == null ? null : session.getUserAccount();
        return user == null ? "" : user.getDisplayName();
    }

    public static CompanyProfile companyProfile() {
        return session == null ? null : session.getCompanyProfile();
    }
}
