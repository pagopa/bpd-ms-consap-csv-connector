package it.gov.pagopa.bpd.consap_csv_connector.batch.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Model for the processed lines in the batch
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundPaymentIntegration {

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

    Integer lineNumber;
    String filename;

}