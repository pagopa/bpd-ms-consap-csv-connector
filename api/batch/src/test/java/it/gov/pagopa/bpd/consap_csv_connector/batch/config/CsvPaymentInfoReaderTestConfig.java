package it.gov.pagopa.bpd.consap_csv_connector.batch.config;

import it.gov.pagopa.bpd.consap_csv_connector.connector.config.CsvConsapConnectorBatchJpaConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Test configuration class for api/event
 */

@ComponentScan(basePackages = {"it.gov.pagopa"}, excludeFilters = {
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value= CsvConsapConnectorBatchJpaConfig.class)
})
public class CsvPaymentInfoReaderTestConfig {}
