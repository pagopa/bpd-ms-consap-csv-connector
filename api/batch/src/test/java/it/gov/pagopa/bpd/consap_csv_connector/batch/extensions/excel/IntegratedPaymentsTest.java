package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel;


import java.math.BigDecimal;

public class IntegratedPaymentsTest {

    String fiscalCode;

    String iban;

    Long awardPeriodId;

    String ticketId;

    String relatedPaymentId;

    BigDecimal amount;

    BigDecimal cashbackAmount;

    BigDecimal jackpotAmount;


    public String getFiscalCode() {
        return fiscalCode;
    }

    public void setFiscalCode(String fiscalCode) {
        this.fiscalCode = fiscalCode;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public Long getAwardPeriodId() {
        return awardPeriodId;
    }

    public void setAwardPeriodId(Long awardPeriodId) {
        this.awardPeriodId = awardPeriodId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getRelatedPaymentId() {
        return relatedPaymentId;
    }

    public void setRelatedPaymentId(String relatedPaymentId) {
        this.relatedPaymentId = relatedPaymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getCashbackAmount() {
        return cashbackAmount;
    }

    public void setCashbackAmount(BigDecimal cashbackAmount) {
        this.cashbackAmount = cashbackAmount;
    }

    public BigDecimal getJackpotAmount() {
        return jackpotAmount;
    }

    public void setJackpotAmount(BigDecimal jackpotAmount) {
        this.jackpotAmount = jackpotAmount;
    }





}
