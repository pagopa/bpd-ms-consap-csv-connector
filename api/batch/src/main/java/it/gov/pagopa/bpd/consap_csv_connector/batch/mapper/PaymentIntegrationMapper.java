package it.gov.pagopa.bpd.consap_csv_connector.batch.mapper;

import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentInfo;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentIntegration;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentInfo;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentIntegration;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * Class to be used to map a {@link PaymentInfo} from an {@link InboundPaymentInfo}
 */

@Service
public class PaymentIntegrationMapper {

    /**
     *
     * @param inboundPaymentIntegration
     *              instance of an  {@link InboundPaymentInfo}, to be mapped into a {@link PaymentInfo}
     * @return  {@link PaymentInfo} instance from the input inboundPaymentInfo, normalized and with an hashed PAN
     */
    public PaymentIntegration map(InboundPaymentIntegration inboundPaymentIntegration, Boolean applyHashing) {

        PaymentIntegration paymentIntegration = null;

        if (inboundPaymentIntegration != null) {
            paymentIntegration = PaymentIntegration.builder().build();
            BeanUtils.copyProperties(inboundPaymentIntegration, paymentIntegration);

        }

        return paymentIntegration;

    }

}
