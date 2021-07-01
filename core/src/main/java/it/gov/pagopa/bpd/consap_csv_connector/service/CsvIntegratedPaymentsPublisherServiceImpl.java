package it.gov.pagopa.bpd.consap_csv_connector.service;

import eu.sia.meda.event.transformer.SimpleEventResponseTransformer;
import it.gov.pagopa.bpd.award_winner.integration.event.CsvIntegratedPaymentsPublisherConnector;
import it.gov.pagopa.bpd.award_winner.integration.event.model.IntegratedPayments;
import it.gov.pagopa.bpd.consap_csv_connector.service.trasformer.HeaderAwareRequestTransformer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Implementation of {@link CsvIntegratedPaymentsPublisherService}
 */
@Service
@Slf4j
class CsvIntegratedPaymentsPublisherServiceImpl implements CsvIntegratedPaymentsPublisherService {

    private final CsvIntegratedPaymentsPublisherConnector csvIntegratedPaymentsPublisherConnector;
    private final HeaderAwareRequestTransformer<IntegratedPayments> simpleEventRequestTransformer;
    private final SimpleEventResponseTransformer simpleEventResponseTransformer;

    @Autowired
    public CsvIntegratedPaymentsPublisherServiceImpl(CsvIntegratedPaymentsPublisherConnector csvIntegratedPaymentsPublisherConnector,
                                                     HeaderAwareRequestTransformer<IntegratedPayments> simpleEventRequestTransformer,
                                                     SimpleEventResponseTransformer simpleEventResponseTransformer) {
        this.csvIntegratedPaymentsPublisherConnector = csvIntegratedPaymentsPublisherConnector;
        this.simpleEventRequestTransformer = simpleEventRequestTransformer;
        this.simpleEventResponseTransformer = simpleEventResponseTransformer;
    }

    /**
     * Method that has the logic for publishing a {@link IntegratedPayments} to an outbound channel,
     * calling on the appropriate connector
     * @param integratedPayments
     *              {@link IntegratedPayments} instance to be published
     */
    @SneakyThrows
    @Override
    public void publishIntegratedPaymentsEvent(IntegratedPayments integratedPayments) {
        RecordHeaders recordHeaders = new RecordHeaders();
        recordHeaders.add("INTEGRATED_PAYMENT_VALIDATION_DATETIME", "INTEGRATED_PAYMENT_VALIDATION_DATETIME".getBytes(StandardCharsets.UTF_8));

        if (!csvIntegratedPaymentsPublisherConnector.doCall(
                integratedPayments, simpleEventRequestTransformer, simpleEventResponseTransformer, recordHeaders)) {
            throw new Exception("Error on event publishing");
        };
    }

}
