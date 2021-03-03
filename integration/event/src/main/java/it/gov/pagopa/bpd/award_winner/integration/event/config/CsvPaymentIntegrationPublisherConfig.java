package it.gov.pagopa.bpd.award_winner.integration.event.config;

import it.gov.pagopa.bpd.award_winner.integration.event.CsvPaymentInfoPublisherConnector;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration class for {@link CsvPaymentInfoPublisherConnector}
 */
@Configuration
@PropertySource("classpath:config/csvPaymentIntegrationPublisher.properties")
public class CsvPaymentIntegrationPublisherConfig { }
