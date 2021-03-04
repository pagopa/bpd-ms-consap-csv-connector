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
@EqualsAndHashCode(of = {"idConsap"}, callSuper = false)
public class InboundPaymentIntegration {

    @NotBlank
    @NotNull
    String idConsap;

    @NotBlank
    @NotNull
    String idComplaint;

    String idPagoPa;

    @NotBlank
    @NotNull
    String fiscalCode;

    @NotBlank
    @NotNull
    String iban;

    @NotBlank
    @NotNull
    String name;

    @NotBlank
    @NotNull
    String surname;

    @NotBlank
    @NotNull
    String cashbackAmount;

    @NotBlank
    @NotNull
    String resultReason;

    @NotBlank
    @NotBlank
    String periodStartDate;

    @NotNull
    @NotBlank
    String periodEndDate;

    @NotNull
    @NotBlank
    String awardPeriodId;

    @NotNull
    @NotBlank
    String result;

    @NotNull
    @NotBlank
    String cro;

    @NotNull
    @NotBlank
    String executionDate;

    @NotNull
    @NotBlank
    String technicalCountProperty;

    Integer lineNumber;
    String filename;

}