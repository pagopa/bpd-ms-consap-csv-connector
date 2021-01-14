package it.gov.pagopa.bpd.consap_csv_connector.batch.mapper;

import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentInfo;
import it.gov.pagopa.bpd.consap_csv_connector.integration.event.model.PaymentInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * Class to be used to map a {@link PaymentInfo} from an {@link InboundPaymentInfo}
 */

@Service
public class PaymentInfoMapper {

    /**
     *
     * @param inboundPaymentInfo
     *              instance of an  {@link InboundPaymentInfo}, to be mapped into a {@link PaymentInfo}
     * @return  {@link PaymentInfo} instance from the input inboundPaymentInfo, normalized and with an hashed PAN
     */
    public PaymentInfo map(InboundPaymentInfo inboundPaymentInfo, Boolean applyHashing) {

        PaymentInfo paymentInfo = null;

        if (inboundPaymentInfo != null) {
            paymentInfo = PaymentInfo.builder().build();
            BeanUtils.copyProperties(inboundPaymentInfo, paymentInfo);

        }

        return paymentInfo;

    }

}
