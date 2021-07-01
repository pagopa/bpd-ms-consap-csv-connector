package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.support.rowset;

public interface RowSetMetaData {

    /**
     * Retrieves the names of the columns for the current sheet.
     * @return the column names.
     */
    String[] getColumnNames();

    /**
     * Retrieves the name of the sheet the RowSet is based on.
     * @return the name of the sheet
     */
    String getSheetName();
}
