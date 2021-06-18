package it.gov.pagopa.bpd.consap_csv_connector.batch.config;

import it.gov.pagopa.bpd.consap_csv_connector.batch.CsvIntegratedPaymentsBatch;
import it.gov.pagopa.bpd.consap_csv_connector.batch.CsvPaymentInfoReaderBatch;
import it.gov.pagopa.bpd.consap_csv_connector.connector.config.CsvConsapConnectorBatchJpaConfig;
import it.gov.pagopa.bpd.consap_csv_connector.connector.config.csvIntegratedPaymentsBatchJpaConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@ComponentScan(basePackages = {"it.gov.pagopa"}, excludeFilters = {
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value= CsvIntegratedPaymentsBatch.class),
        @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value= csvIntegratedPaymentsBatchJpaConfig.class)
})
public class CsvIntegratedPaymentsTestConfig {
}
