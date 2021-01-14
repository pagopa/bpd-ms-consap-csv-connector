package it.gov.pagopa.bpd.consap_csv_connector.batch.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Model for the processed lines in the batch
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"uniqueID"}, callSuper = false)
public class InboundPaymentInfo {

    @NotNull
    @NotBlank
    String uniqueID;

    @NotNull
    @NotBlank
    String result;

    String resultReason;

    String cro;

    String executionDate;

    Integer lineNumber;
    String filename;

}