package com.rvsfishworld.model;

public class CompanyProfile {
    private long companyId;
    private String companyName;
    private String companyAddress;
    private String tin;
    private String sssNo;
    private String phone;
    private String fax;
    private String email;
    private String executiveName;
    private boolean readOnly;
    private boolean autoAc;
    private String updatedBy;

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    public String getSssNo() {
        return sssNo;
    }

    public void setSssNo(String sssNo) {
        this.sssNo = sssNo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getExecutiveName() {
        return executiveName;
    }

    public void setExecutiveName(String executiveName) {
        this.executiveName = executiveName;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isAutoAc() {
        return autoAc;
    }

    public void setAutoAc(boolean autoAc) {
        this.autoAc = autoAc;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
