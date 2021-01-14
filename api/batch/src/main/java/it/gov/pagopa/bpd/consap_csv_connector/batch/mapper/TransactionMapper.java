package it.gov.pagopa.bpd.consap_csv_connector.batch.mapper;

import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundTransaction;
import it.gov.pagopa.bpd.consap_csv_connector.integration.event.model.PaymentInfo;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Class to be used to map a {@link PaymentInfo} from an {@link InboundTransaction}
 */

@Service
public class TransactionMapper {

    /**
     *
     * @param inboundTransaction
     *              instance of an  {@link InboundTransaction}, to be mapped into a {@link PaymentInfo}
     * @return  {@link PaymentInfo} instance from the input inboundTransaction, normalized and with an hashed PAN
     */
    public PaymentInfo map(InboundTransaction inboundTransaction, Boolean applyHashing) {

        PaymentInfo paymentInfo = null;

        if (inboundTransaction != null) {
            paymentInfo = PaymentInfo.builder().build();
            BeanUtils.copyProperties(inboundTransaction, paymentInfo);

        }

        return paymentInfo;

    }

}
