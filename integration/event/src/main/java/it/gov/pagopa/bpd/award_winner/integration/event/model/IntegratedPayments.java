package it.gov.pagopa.bpd.award_winner.integration.event.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@EqualsAndHashCode(of = {"uniqueID"}, callSuper = false)
public class IntegratedPayments {


    String fiscalCode;

    String iban;

    Long awardPeriodId;

    String ticketId;

    String relatedPaymentId;

    BigDecimal amount;

    BigDecimal cashbackAmount;

    BigDecimal jackpotAmount;


}
