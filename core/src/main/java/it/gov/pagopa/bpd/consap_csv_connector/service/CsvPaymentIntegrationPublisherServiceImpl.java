package it.gov.pagopa.bpd.consap_csv_connector.service;

import eu.sia.meda.event.transformer.SimpleEventRequestTransformer;
import eu.sia.meda.event.transformer.SimpleEventResponseTransformer;
import it.gov.pagopa.bpd.award_winner.integration.event.CsvPaymentInfoPublisherConnector;
import it.gov.pagopa.bpd.award_winner.integration.event.CsvPaymentIntegrationPublisherConnector;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentInfo;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentIntegration;
import it.gov.pagopa.bpd.consap_csv_connector.service.transformer.HeaderAwareRequestTransformer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link CsvPaymentInfoPublisherService}
 */
@Service
@Slf4j
class CsvPaymentIntegrationPublisherServiceImpl implements CsvPaymentIntegrationPublisherService {

    private final CsvPaymentIntegrationPublisherConnector csvPaymentIntegrationPublisherConnector;
    private final HeaderAwareRequestTransformer<PaymentIntegration> simpleEventRequestTransformer;
    private final SimpleEventResponseTransformer simpleEventResponseTransformer;

    @Autowired
    public CsvPaymentIntegrationPublisherServiceImpl(
            CsvPaymentIntegrationPublisherConnector csvPaymentIntegrationPublisherConnector,
            HeaderAwareRequestTransformer<PaymentIntegration> simpleEventRequestTransformer,
            SimpleEventResponseTransformer simpleEventResponseTransformer) {
        this.csvPaymentIntegrationPublisherConnector = csvPaymentIntegrationPublisherConnector;
        this.simpleEventRequestTransformer = simpleEventRequestTransformer;
        this.simpleEventResponseTransformer = simpleEventResponseTransformer;
    }

    /**
     * Method that has the logic for publishing a {@link PaymentInfo} to an outbound channel,
     * calling on the appropriate connector
     * @param paymentIntegration
     *              {@link PaymentIntegration} instance to be published
     */
    @SneakyThrows
    @Override
    public void publishPaymentIntegrationEvent(PaymentIntegration paymentIntegration, RecordHeaders recordHeaders) {
        if (!csvPaymentIntegrationPublisherConnector.doCall(
                paymentIntegration, simpleEventRequestTransformer, simpleEventResponseTransformer, recordHeaders)) {
            throw new Exception("Error on event publishing");
        };
    }

}
