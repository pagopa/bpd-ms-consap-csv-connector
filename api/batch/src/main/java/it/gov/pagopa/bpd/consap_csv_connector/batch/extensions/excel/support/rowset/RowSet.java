package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.support.rowset;

import java.util.Properties;

public interface RowSet {

    /**
     * Retrieves the meta data (name of the sheet, number of columns, names) of this row
     * set.
     * @return a corresponding {@code RowSetMetaData} instance.
     */
    RowSetMetaData getMetaData();

    /**
     * Move to the next row in the document.
     * @return {@code true} if the row is valid, {@code false} if there are no more rows
     */
    boolean next();

    /**
     * Returns the current row number.
     * @return the current row number
     */
    int getCurrentRowIndex();

    /**
     * Return the current row as a {@code String[]}.
     * @return the row as a {@code String[]}
     */
    String[] getCurrentRow();

    /**
     * Construct name-value pairs from the column names and string values. {@code null}
     * values are omitted.
     * @return some properties representing the row set.
     * @throws IllegalStateException if the column name meta data is not available.
     */
    Properties getProperties();
}
