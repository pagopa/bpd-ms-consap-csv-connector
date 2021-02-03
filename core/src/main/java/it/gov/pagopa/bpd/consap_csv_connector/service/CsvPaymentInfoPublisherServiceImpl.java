package it.gov.pagopa.bpd.consap_csv_connector.service;

import eu.sia.meda.event.transformer.SimpleEventRequestTransformer;
import eu.sia.meda.event.transformer.SimpleEventResponseTransformer;
import it.gov.pagopa.bpd.award_winner.integration.event.CsvPaymentInfoPublisherConnector;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link CsvPaymentInfoPublisherService}
 */
@Service
@Slf4j
class CsvPaymentInfoPublisherServiceImpl implements CsvPaymentInfoPublisherService {

    private final CsvPaymentInfoPublisherConnector csvPaymentInfoPublisherConnector;
    private final SimpleEventRequestTransformer<PaymentInfo> simpleEventRequestTransformer;
    private final SimpleEventResponseTransformer simpleEventResponseTransformer;

    @Autowired
    public CsvPaymentInfoPublisherServiceImpl(CsvPaymentInfoPublisherConnector csvPaymentInfoPublisherConnector,
                                              SimpleEventRequestTransformer<PaymentInfo> simpleEventRequestTransformer,
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
        if (!csvPaymentInfoPublisherConnector.doCall(
                paymentInfo, simpleEventRequestTransformer, simpleEventResponseTransformer)) {
            throw new Exception("Error on event publishing");
        };
    }

}
