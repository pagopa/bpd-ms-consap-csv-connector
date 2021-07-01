package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.support.rowset;

import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.Sheet;

import java.util.Iterator;
import java.util.Properties;

public class DefaultRowSet implements RowSet {

    private final Iterator<String[]> sheetData;

    private final RowSetMetaData metaData;

    private int currentRowIndex = -1;

    private String[] currentRow;

    DefaultRowSet(Sheet sheet, RowSetMetaData metaData) {
        this.sheetData = sheet.iterator();
        this.metaData = metaData;
    }

    @Override
    public RowSetMetaData getMetaData() {
        return this.metaData;
    }

    @Override
    public boolean next() {
        this.currentRow = null;
        this.currentRowIndex++;
        if (this.sheetData.hasNext()) {
            this.currentRow = this.sheetData.next();
            return true;
        }
        return false;
    }

    @Override
    public int getCurrentRowIndex() {
        return this.currentRowIndex;
    }

    @Override
    public String[] getCurrentRow() {
        return this.currentRow;
    }

    @Override
    public Properties getProperties() {
        final String[] names = this.metaData.getColumnNames();
        if (names == null) {
            throw new IllegalStateException("Cannot create properties without meta data");
        }

        Properties props = new Properties();
        for (int i = 0; i < this.currentRow.length; i++) {
            String value = this.currentRow[i];
            if (value != null) {
                props.setProperty(names[i], value);
            }
        }
        return props;
    }
}
