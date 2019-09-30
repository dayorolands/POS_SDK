package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 6/3/2016.
 */
public class Association {

    private String id ;
    private String group_id ;
    private String name ;
    private String group_email ;
    private String group_address ;
    private String physical_address ;
    private String type ;
    private String accreditation_status ;
    private String beneficiary_loan_eligibility ;
    private String loan_eligibility ;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup_email() {
        return group_email;
    }

    public void setGroup_email(String group_email) {
        this.group_email = group_email;
    }

    public String getGroup_address() {
        return group_address;
    }

    public void setGroup_address(String group_address) {
        this.group_address = group_address;
    }

    public String getPhysical_address() {
        return physical_address;
    }

    public void setPhysical_address(String physical_address) {
        this.physical_address = physical_address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccreditation_status() {
        return accreditation_status;
    }

    public void setAccreditation_status(String accreditation_status) {
        this.accreditation_status = accreditation_status;
    }

    public String getBeneficiary_loan_eligibility() {
        return beneficiary_loan_eligibility;
    }

    public void setBeneficiary_loan_eligibility(String beneficiary_loan_eligibility) {
        this.beneficiary_loan_eligibility = beneficiary_loan_eligibility;
    }

    public String getLoan_eligibility() {
        return loan_eligibility;
    }

    public void setLoan_eligibility(String loan_eligibility) {
        this.loan_eligibility = loan_eligibility;
    }
}
