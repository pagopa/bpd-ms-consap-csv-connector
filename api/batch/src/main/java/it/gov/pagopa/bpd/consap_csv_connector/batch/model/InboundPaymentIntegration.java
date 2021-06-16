package it.gov.pagopa.bpd.consap_csv_connector.batch.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
    @Size(max = 9)
    String idConsap;

    @NotBlank
    @NotNull
    @Size(max = 28)
    String idComplaint;

    @Size(max = 9)
    String idPagoPa;

    @NotBlank
    @NotNull
    @Size(max = 16)
    String fiscalCode;

    @NotBlank
    @NotNull
    @Size(max = 27)
    String iban;

    @NotBlank
    @NotNull
    @Size(max = 50)
    String name;

    @NotBlank
    @NotNull
    @Size(max = 50)
    String surname;

    @NotBlank
    @NotNull
    @Size(max = 6)
    String amount;

    @NotBlank
    @NotNull
    @Size(max = 6)
    String cashbackAmount;

    @NotBlank
    @NotNull
    @Size(max = 6)
    String jackpotAmount;

    @NotBlank
    @NotNull
    @Size(max = 140)
    String resultReason;

    @NotBlank
    @NotBlank
    @Size(max = 10)
    String periodStartDate;

    @NotNull
    @NotBlank
    @Size(max = 10)
    String periodEndDate;

    @NotNull
    @NotBlank
    @Size(max = 25)
    String result;

    @NotNull
    @NotBlank
    @Size(max = 35)
    String cro;

    @NotNull
    @NotBlank
    @Size(max = 10)
    String executionDate;

    Integer lineNumber;
    String filename;

}