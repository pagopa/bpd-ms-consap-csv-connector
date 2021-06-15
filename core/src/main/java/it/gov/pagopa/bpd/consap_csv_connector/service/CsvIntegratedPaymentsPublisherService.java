package it.gov.pagopa.bpd.consap_csv_connector.service;

import it.gov.pagopa.bpd.award_winner.integration.event.model.IntegratedPayments;

/**
 * Interface for the event publisher service, responsible for handling the transactions
 * to be passed through the connector for the outbound channel
 * @see CsvIntegratedPaymentsPublisherServiceImpl
 */
public interface CsvIntegratedPaymentsPublisherService {

    /**
     * Method that has the logic for publishing a {@link IntegratedPayments} to an outbound channel,
     * calling on the appropriate connector
     * @param integratedPayments
     *            {@link IntegratedPayments} instance to be published
     */
    void publishIntegratedPaymentsEvent(IntegratedPayments integratedPayments);
}
