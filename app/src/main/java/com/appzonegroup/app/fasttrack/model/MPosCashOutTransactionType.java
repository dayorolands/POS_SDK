package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 12/14/2017.
 */

public class MPosCashOutTransactionType {

    private String AgentsPhoneNumber;
    private String AgentsPIN;
    private String TransactionAmount;
    private String BeneficiaryAccountNumber;
    private String BeneficiaryBank;
    private String CardCipher;
    private String InstitutionCode;
    private String SenderBank;
    private String SenderPhoneNumber;
    private TransactionStatus Status;
    private String TerminalID;
    private String TransactionID;
    private int ID;
    private boolean IgnoreExistingTransaction;

    public String getAgentsPhoneNumber() {
        return AgentsPhoneNumber;
    }

    public void setAgentsPhoneNumber(String agentsPhoneNumber) {
        AgentsPhoneNumber = agentsPhoneNumber;
    }

    public String getAgentsPIN() {
        return AgentsPIN;
    }

    public void setAgentsPIN(String agentsPIN) {
        AgentsPIN = agentsPIN;
    }

    public String getTransactionAmount() {
        return TransactionAmount;
    }

    public void setTransactionAmount(String transactionAmount) {
        TransactionAmount = transactionAmount;
    }

    public String getBeneficiaryAccountNumber() {
        return BeneficiaryAccountNumber;
    }

    public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) {
        BeneficiaryAccountNumber = beneficiaryAccountNumber;
    }

    public String getBeneficiaryBank() {
        return BeneficiaryBank;
    }

    public void setBeneficiaryBank(String beneficiaryBank) {
        BeneficiaryBank = beneficiaryBank;
    }

    public String getCardCipher() {
        return CardCipher;
    }

    public void setCardCipher(String cardCipher) {
        CardCipher = cardCipher;
    }

    public String getInstitutionCode() {
        return InstitutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        InstitutionCode = institutionCode;
    }

    public String getSenderBank() {
        return SenderBank;
    }

    public void setSenderBank(String senderBank) {
        SenderBank = senderBank;
    }

    public String getSenderPhoneNumber() {
        return SenderPhoneNumber;
    }

    public void setSenderPhoneNumber(String senderPhoneNumber) {
        SenderPhoneNumber = senderPhoneNumber;
    }

    public String getTerminalID() {
        return TerminalID;
    }

    public void setTerminalID(String terminalID) {
        TerminalID = terminalID;
    }

    public String getTransactionID() {
        return TransactionID;
    }

    public void setTransactionID(String transactionID) {
        TransactionID = transactionID;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public TransactionStatus getStatus() {
        return Status;
    }

    public void setStatus(TransactionStatus status) {
        Status = status;
    }

    public boolean isIgnoreExistingTransaction() {
        return IgnoreExistingTransaction;
    }

    public void setIgnoreExistingTransaction(boolean ignoreExistingTransaction) {
        IgnoreExistingTransaction = ignoreExistingTransaction;
    }
}
