package it.gov.pagopa.bpd.consap_csv_connector.batch.config;

import it.gov.pagopa.bpd.consap_csv_connector.batch.CsvIntegratedPaymentsBatch;
import it.gov.pagopa.bpd.consap_csv_connector.batch.CsvPaymentIntegrationReaderBatch;
import it.gov.pagopa.bpd.consap_csv_connector.connector.config.CsvConsapConnectorBatchJpaConfig;
import it.gov.pagopa.bpd.consap_csv_connector.connector.config.csvIntegratedPaymentsBatchJpaConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Test configuration class for api/event
 */

@ComponentScan(basePackages = {"it.gov.pagopa"}, excludeFilters = {
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value= CsvConsapConnectorBatchJpaConfig.class),
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value= CsvPaymentIntegrationReaderBatch.class),
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value= CsvIntegratedPaymentsBatch.class),
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value= csvIntegratedPaymentsBatchJpaConfig.class)
})
public class CsvPaymentInfoReaderTestConfig {}
