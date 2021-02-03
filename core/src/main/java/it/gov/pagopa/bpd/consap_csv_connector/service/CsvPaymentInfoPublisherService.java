package it.gov.pagopa.bpd.consap_csv_connector.service;

import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentInfo;

/**
 * Interface for the event publisher service, responsible for handling the transactions
 * to be passed through the connector for the outbound channel
 * @see CsvPaymentInfoPublisherServiceImpl
 */
public interface CsvPaymentInfoPublisherService {

    /**
     * Method that has the logic for publishing a {@link PaymentInfo} to an outbound channel,
     * calling on the appropriate connector
     * @param paymentInfo
     *            {@link PaymentInfo} instance to be published
     */
    void publishPaymentInfoEvent(PaymentInfo paymentInfo);

}
