package it.gov.pagopa.bpd.award_winner.integration.event;

import eu.sia.meda.event.BaseEventConnectorTest;
import eu.sia.meda.util.TestUtils;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Class for unit testing of the CsvPaymentInfoPublisherConnector class
 */
@Import({CsvPaymentInfoPublisherConnector.class})
@TestPropertySource(
        locations = "classpath:config/testCsvTransactionPublisher.properties",
        properties = {

                "connectors.eventConfigurations.items.CsvPaymentInfoPublisherConnector.bootstrapServers=${spring.embedded.kafka.brokers}"
        })
public class CsvPaymentInfoPublisherConnectorTest extends
        BaseEventConnectorTest<PaymentInfo, Boolean, PaymentInfo, Void, CsvPaymentInfoPublisherConnector> {

    @Value("${connectors.eventConfigurations.items.CsvPaymentInfoPublisherConnector.topic}")
    private String topic;

    @Autowired
    private CsvPaymentInfoPublisherConnector csvPaymentInfoPublisherConnector;

    @Override
    protected CsvPaymentInfoPublisherConnector getEventConnector() {
        return csvPaymentInfoPublisherConnector;
    }

    @Override
    protected PaymentInfo getRequestObject() {
        return TestUtils.mockInstance(new PaymentInfo());
    }

    @Override
    protected String getTopic() {
        return topic;
    }

}