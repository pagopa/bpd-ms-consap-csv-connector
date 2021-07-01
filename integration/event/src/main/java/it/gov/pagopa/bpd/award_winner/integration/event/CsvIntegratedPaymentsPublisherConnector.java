package it.gov.pagopa.bpd.award_winner.integration.event;

import eu.sia.meda.event.BaseEventConnector;
import eu.sia.meda.event.transformer.IEventRequestTransformer;
import eu.sia.meda.event.transformer.IEventResponseTransformer;
import it.gov.pagopa.bpd.award_winner.integration.event.model.IntegratedPayments;
import org.springframework.stereotype.Service;

/**
 * Class extending the MEDA {@link BaseEventConnector}, is responsible for calling
 * a Kafka outbound channel with messagescontaining a json mapped on the PaymentInfo class
 */

@Service
public class CsvIntegratedPaymentsPublisherConnector extends BaseEventConnector<IntegratedPayments, Boolean, IntegratedPayments, Void> {

    /**
     *
     * @param integratedPayments
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
            IntegratedPayments integratedPayments, IEventRequestTransformer<IntegratedPayments,
            IntegratedPayments> requestTransformer,
            IEventResponseTransformer<Void, Boolean> responseTransformer,
            Object... args) {
        return this.call(integratedPayments, requestTransformer, responseTransformer, args);
    }
}
