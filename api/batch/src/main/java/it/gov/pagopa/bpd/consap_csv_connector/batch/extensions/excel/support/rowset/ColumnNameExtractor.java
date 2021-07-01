package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.support.rowset;

import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.Sheet;

public interface ColumnNameExtractor {

    /**
     * Retrieves the names of the columns in the given {@code Sheet}.
     * @param sheet the sheet
     * @return the column names
     */
    String[] getColumnNames(Sheet sheet);
}
