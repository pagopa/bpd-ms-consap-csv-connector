package it.gov.pagopa.bpd.consap_csv_connector.service;

import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentInfo;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentIntegration;
import org.apache.kafka.common.header.internals.RecordHeaders;

/**
 * Interface for the event publisher service, responsible for handling the transactions
 * to be passed through the connector for the outbound channel
 * @see CsvPaymentInfoPublisherServiceImpl
 */
public interface CsvPaymentIntegrationPublisherService {

    /**
     * Method that has the logic for publishing a {@link PaymentIntegration} to an outbound channel,
     * calling on the appropriate connector
     * @param paymentInfo
     *            {@link PaymentIntegration} instance to be published
     */
    void publishPaymentIntegrationEvent(PaymentIntegration paymentInfo, RecordHeaders recordHeaders);

}
