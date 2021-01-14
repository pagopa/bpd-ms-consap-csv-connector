package it.gov.pagopa.bpd.consap_csv_connector.integration.event.model;

import lombok.*;

import java.math.BigDecimal;

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