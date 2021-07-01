package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel;

import org.springframework.lang.Nullable;

public interface Sheet extends Iterable<String[]>, AutoCloseable{

    /**
     * Get the number of rows in this sheet.
     * @return the number of rows.
     */
    int getNumberOfRows();

    /**
     * Get the name of the sheet.
     * @return the name of the sheet.
     */
    String getName();

    /**
     * Get the row as a {@code String[]}. Returns {@code null} if the row doesn't exist.
     * @param rowNumber the row number to read.
     * @return a {@code String[]} or {@code null}
     */
    @Nullable
    String[] getRow(int rowNumber);

    @Override
    default void close() throws Exception {
    }

}
