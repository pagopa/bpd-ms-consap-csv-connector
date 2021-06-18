package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.support.rowset;

import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.Sheet;

public class DefaultRowSetFactory implements RowSetFactory{

    private ColumnNameExtractor columnNameExtractor = new RowNumberColumnNameExtractor();

    @Override
    public RowSet create(Sheet sheet) {
        DefaultRowSetMetaData metaData = new DefaultRowSetMetaData(sheet, this.columnNameExtractor);
        return new DefaultRowSet(sheet, metaData);
    }

    public void setColumnNameExtractor(ColumnNameExtractor columnNameExtractor) {
        this.columnNameExtractor = columnNameExtractor;
    }
}
