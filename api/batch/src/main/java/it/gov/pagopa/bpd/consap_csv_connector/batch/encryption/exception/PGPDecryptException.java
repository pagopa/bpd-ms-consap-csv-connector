package it.gov.pagopa.bpd.consap_csv_connector.batch.encryption.exception;

/**
 * Custom {@link Throwable} used to define errors in the decrypt phase of the reader
 */
public class PGPDecryptException extends Exception {

    public PGPDecryptException() {
        super();
    }

    public PGPDecryptException(String message, Throwable cause) {
        super(message, cause);
    }

}
