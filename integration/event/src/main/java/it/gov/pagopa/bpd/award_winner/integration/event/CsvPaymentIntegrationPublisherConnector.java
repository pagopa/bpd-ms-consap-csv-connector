package it.gov.pagopa.bpd.award_winner.integration.event;

import eu.sia.meda.event.BaseEventConnector;
import eu.sia.meda.event.transformer.IEventRequestTransformer;
import eu.sia.meda.event.transformer.IEventResponseTransformer;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentInfo;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentIntegration;
import org.springframework.stereotype.Service;

/**
 * Class extending the MEDA {@link BaseEventConnector}, is responsible for calling
 * a Kafka outbound channel with messagescontaining a json mapped on the PaymentInfo class
 */

@Service
public class CsvPaymentIntegrationPublisherConnector
        extends BaseEventConnector<PaymentIntegration, Boolean, PaymentIntegration, Void> {

    /**
     *
     * @param paymentInfo
                PaymentInfo instance to be used as message content
     * @param requestTransformer
                Trannsformer for the request data
     * @param responseTransformer
                Transformer for the call response
     * @param args
                Additional args to be used in the call
     * @return Exit status for the call
     */
    public Boolean doCall(
            PaymentIntegration paymentInfo, IEventRequestTransformer<PaymentIntegration,
            PaymentIntegration> requestTransformer,
            IEventResponseTransformer<Void, Boolean> responseTransformer,
            Object... args) {
        return this.call(paymentInfo, requestTransformer, responseTransformer, args);
    }

}
