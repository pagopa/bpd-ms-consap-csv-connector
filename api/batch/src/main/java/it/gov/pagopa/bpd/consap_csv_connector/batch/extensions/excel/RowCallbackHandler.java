package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel;

import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.support.rowset.RowSet;

public interface RowCallbackHandler {

    /**
     * Implementations must implement this method to process each row of data in the
     * {@code RowSet}.
     * <p>This method should not call {@code next()} on the {@code RowSetSet}; it is only
     * supposed to extract values of the current row.
     * <p>Exactly what the implementation chooses to do is up to it: A trivial implementation
     * might simply count rows, while another implementation might build a special header
     * row.
     * @param rs the {@code RowSet} to process (preset at the current row)
     */
    void handleRow(RowSet rs);
}
