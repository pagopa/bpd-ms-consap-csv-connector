package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.support.rowset;

import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.Sheet;

public class RowNumberColumnNameExtractor implements ColumnNameExtractor {

    private int headerRowNumber;

    @Override
    public String[] getColumnNames(final Sheet sheet) {
        return sheet.getRow(this.headerRowNumber);
    }

    public void setHeaderRowNumber(int headerRowNumber) {
        this.headerRowNumber = headerRowNumber;
    }
}
