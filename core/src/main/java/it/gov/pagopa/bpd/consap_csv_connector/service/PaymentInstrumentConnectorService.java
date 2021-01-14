package it.gov.pagopa.bpd.consap_csv_connector.service;

import it.gov.pagopa.bpd.consap_csv_connector.connector.payment_instrument.model.PaymentInstrumentData;

public interface PaymentInstrumentConnectorService {

    void disablePaymentInstrument(PaymentInstrumentData paymentInstrumentRequest);

}
