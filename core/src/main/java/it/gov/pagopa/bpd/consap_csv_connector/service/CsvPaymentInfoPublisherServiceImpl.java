package it.gov.pagopa.bpd.consap_csv_connector.service;

import eu.sia.meda.event.transformer.SimpleEventResponseTransformer;
import it.gov.pagopa.bpd.award_winner.integration.event.CsvPaymentInfoPublisherConnector;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentInfo;
import it.gov.pagopa.bpd.consap_csv_connector.service.trasformer.HeaderAwareRequestTransformer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Implementation of {@link CsvPaymentInfoPublisherService}
 */
@Service
@Slf4j
class CsvPaymentInfoPublisherServiceImpl implements CsvPaymentInfoPublisherService {

    private final CsvPaymentInfoPublisherConnector csvPaymentInfoPublisherConnector;
    private final HeaderAwareRequestTransformer<PaymentInfo> simpleEventRequestTransformer;
    private final SimpleEventResponseTransformer simpleEventResponseTransformer;

    @Autowired
    public CsvPaymentInfoPublisherServiceImpl(CsvPaymentInfoPublisherConnector csvPaymentInfoPublisherConnector,
                                              HeaderAwareRequestTransformer<PaymentInfo> simpleEventRequestTransformer,
                                              SimpleEventResponseTransformer simpleEventResponseTransformer) {
        this.csvPaymentInfoPublisherConnector = csvPaymentInfoPublisherConnector;
        this.simpleEventRequestTransformer = simpleEventRequestTransformer;
        this.simpleEventResponseTransformer = simpleEventResponseTransformer;
    }

    /**
     * Method that has the logic for publishing a {@link PaymentInfo} to an outbound channel,
     * calling on the appropriate connector
     * @param paymentInfo
     *              {@link PaymentInfo} instance to be published
     */
    @SneakyThrows
    @Override
    public void publishPaymentInfoEvent(PaymentInfo paymentInfo) {
        RecordHeaders recordHeaders = new RecordHeaders();
        recordHeaders.add("PAYMENT_INFO_VALIDATION_DATETIME", "PAYMENT_INFO_VALIDATION_DATETIME".getBytes(StandardCharsets.UTF_8));

        if (!csvPaymentInfoPublisherConnector.doCall(
                paymentInfo, simpleEventRequestTransformer, simpleEventResponseTransformer, recordHeaders)) {
            throw new Exception("Error on event publishing");
        };
    }

}
