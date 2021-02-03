package it.gov.pagopa.bpd.award_winner.integration.event.model;

import lombok.*;

/**
 * Model for transaction to be sent in the outbound channel
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"uniqueID"}, callSuper = false)
public class PaymentInfo {

    String uniqueID;

    String result;

    String resultReason;

    String cro;

    String executionDate;

}