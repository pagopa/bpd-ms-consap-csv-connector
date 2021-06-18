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
public class InboundIntegratedPayments {

    @NotNull
    @NotBlank
    String fiscalCode;

    @NotNull
    Long awardPeriodId;

    Long ticketId;

    Long relatedPaymentId;

    @NotNull
    BigDecimal amount;

    @NotNull
    BigDecimal cashbackAmount;


    BigDecimal jackpotAmount;


    Integer lineNumber;
    String filename;


}
