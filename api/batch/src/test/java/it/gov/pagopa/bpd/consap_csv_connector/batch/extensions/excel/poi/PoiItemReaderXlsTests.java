package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.poi;

import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.AbstractExcelItemReader;
import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.AbstractExcelItemReaderTests;

public class PoiItemReaderXlsTests extends AbstractExcelItemReaderTests {

    @Override
    protected AbstractExcelItemReader<String[]> createExcelItemReader() {
        return new PoiItemReader<>();
    }
}
