package it.gov.pagopa.bpd.award_winner.integration.event;

import eu.sia.meda.event.BaseEventConnectorTest;
import eu.sia.meda.util.TestUtils;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentInfo;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Class for unit testing of the CsvPaymentInfoPublisherConnector class
 */
@Import({CsvPaymentIntegrationPublisherConnector.class})
@TestPropertySource(
        locations = "classpath:config/testCsvPaymentIntegrationPublisher.properties",
        properties = {

                "connectors.eventConfigurations.items.CsvPaymentIntegrationPublisherConnector.bootstrapServers=${spring.embedded.kafka.brokers}"
        })
public class CsvPaymentIntegrationPublisherConnectorTest extends
        BaseEventConnectorTest<PaymentIntegration, Boolean, PaymentIntegration, Void, CsvPaymentIntegrationPublisherConnector> {

    @Value("${connectors.eventConfigurations.items.CsvPaymentIntegrationPublisherConnector.topic}")
    private String topic;

    @Autowired
    private CsvPaymentIntegrationPublisherConnector csvPaymentIntegrationPublisherConnector;

    @Override
    protected CsvPaymentIntegrationPublisherConnector getEventConnector() {
        return csvPaymentIntegrationPublisherConnector;
    }

    @Override
    protected PaymentIntegration getRequestObject() {
        return TestUtils.mockInstance(new PaymentIntegration());
    }

    @Override
    protected String getTopic() {
        return topic;
    }

}