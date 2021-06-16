package it.gov.pagopa.bpd.award_winner.integration.event.model;

import lombok.*;

import java.math.BigDecimal;

/**
 * Model for transaction to be sent in the outbound channel
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntegration {

    String idConsap;

    String idComplaint;

    String idPagoPa;

    String fiscalCode;

    String iban;

    String name;

    String surname;

    BigDecimal amount;

    BigDecimal jackpotAmount;

    BigDecimal cashbackAmount;

    String resultReason;

    String periodStartDate;

    String periodEndDate;

    String result;

    String cro;

    String executionDate;

}