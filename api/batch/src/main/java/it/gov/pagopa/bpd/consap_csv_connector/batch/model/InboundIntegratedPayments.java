package it.gov.pagopa.bpd.consap_csv_connector.batch.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Model for the processed lines in the batch
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@EqualsAndHashCode(of = {"uniqueID"}, callSuper = false)
public class InboundIntegratedPayments {

    @NotNull
    @NotBlank
    String fiscalCode;

    @NotNull
    @NotBlank
    String iban;

    @NotNull
    @NotBlank
    Long awardPeriodId;

    @NotNull
    @NotBlank
    String ticketId;

    String relatedPaymentId;

    @NotNull
    BigDecimal amount;

    @NotNull
    BigDecimal cashbackAmount;

    @NotNull
    BigDecimal jackpotAmount;


    Integer lineNumber;
    String filename;


}
