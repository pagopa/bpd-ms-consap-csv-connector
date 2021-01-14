package it.gov.pagopa.bpd.consap_csv_connector.connector.payment_instrument;

import it.gov.pagopa.bpd.consap_csv_connector.connector.payment_instrument.model.PaymentInstrumentData;

public interface PaymentInstrumentRestClient {

    void delete(PaymentInstrumentData paymentInstrumentData);

}
