package it.gov.pagopa.bpd.consap_csv_connector.connector.config;

import it.gov.pagopa.bpd.common.connector.jpa.config.BaseJpaConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:config/jpaConnectionConfig.properties")
public class csvIntegratedPaymentsBatchJpaConfig extends BaseJpaConfig {
}
