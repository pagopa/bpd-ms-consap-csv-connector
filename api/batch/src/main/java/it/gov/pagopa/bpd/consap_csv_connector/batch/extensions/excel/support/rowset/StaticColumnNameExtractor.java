package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.support.rowset;

import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.Sheet;

import java.util.Arrays;

public class StaticColumnNameExtractor implements ColumnNameExtractor {

    private final String[] columnNames;

    public StaticColumnNameExtractor(String[] columnNames) {
        this.columnNames = columnNames;
    }

    @Override
    public String[] getColumnNames(Sheet sheet) {
        return Arrays.copyOf(this.columnNames, this.columnNames.length);
    }
}
