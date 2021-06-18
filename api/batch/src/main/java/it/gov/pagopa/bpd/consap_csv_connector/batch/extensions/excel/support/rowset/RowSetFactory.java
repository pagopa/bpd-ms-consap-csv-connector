package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.support.rowset;

import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.Sheet;

public interface RowSetFactory {

    /**
     * Create a rowset instance.
     * @param sheet an Excel sheet.
     * @return a {@code RowSet} instance.
     */
    RowSet create(Sheet sheet);
}
