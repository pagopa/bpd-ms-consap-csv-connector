package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.support.rowset;

import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.Sheet;


public class DefaultRowSetMetaData implements RowSetMetaData {

    private final Sheet sheet;

    private final ColumnNameExtractor columnNameExtractor;

    private String[] columnNames;

    DefaultRowSetMetaData(Sheet sheet, ColumnNameExtractor columnNameExtractor) {
        this.sheet = sheet;
        this.columnNameExtractor = columnNameExtractor;
    }

    @Override
    public String[] getColumnNames() {
        if (this.columnNames == null) {
            this.columnNames = this.columnNameExtractor.getColumnNames(this.sheet);
        }
        return this.columnNames;
    }

    @Override
    public String getSheetName() {
        return this.sheet.getName();
    }
}
