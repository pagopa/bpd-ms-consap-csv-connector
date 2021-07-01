package it.gov.pagopa.bpd.award_winner.integration.event.config;

import it.gov.pagopa.bpd.award_winner.integration.event.CsvIntegratedPaymentsPublisherConnector;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration class for {@link CsvIntegratedPaymentsPublisherConnector}
 */
@Configuration
@PropertySource("classpath:config/csvIntegratedPaymentsPublisher.properties")
public class CsvIntegratedPaymentsPublisherConfig {
}
