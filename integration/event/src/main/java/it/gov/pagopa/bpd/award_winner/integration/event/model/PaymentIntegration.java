package it.gov.pagopa.bpd.award_winner.integration.event.model;

import lombok.*;

/**
 * Model for transaction to be sent in the outbound channel
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntegration {

    String idConsap;

    String idReclamo;

    String idPagoPa;

    String fiscalCode;

    String iban;

    String name;

    String surname;

    String cashbackAmount;

    String causale;

    String periodStartDate;

    String periodEndDate;

    String awardPeriodId;

    String esito;

    String cro;

    String executionDate;

    String technicalCountProperty;

}