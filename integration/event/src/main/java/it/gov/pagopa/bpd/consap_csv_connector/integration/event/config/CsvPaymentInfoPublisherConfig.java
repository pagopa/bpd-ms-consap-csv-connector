package it.gov.pagopa.bpd.consap_csv_connector.integration.event.config;

import it.gov.pagopa.bpd.consap_csv_connector.integration.event.CsvPaymentInfoPublisherConnector;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration class for {@link CsvPaymentInfoPublisherConnector}
 */
@Configuration
@PropertySource("classpath:config/csvPaymentInfoPublisher.properties")
public class CsvPaymentInfoPublisherConfig { }
