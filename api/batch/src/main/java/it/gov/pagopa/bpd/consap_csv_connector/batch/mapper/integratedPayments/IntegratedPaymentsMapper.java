package it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.integratedPayments;

import it.gov.pagopa.bpd.award_winner.integration.event.model.IntegratedPayments;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundIntegratedPayments;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * Class to be used to map a {@link IntegratedPayments} from an {@link InboundIntegratedPayments}
 */

@Service
public class IntegratedPaymentsMapper {

    /**
     *
     * @param inboundIntegratedPayments
     *              instance of an  {@link InboundIntegratedPayments}, to be mapped into a {@link IntegratedPayments}
     * @return  {@link IntegratedPayments} instance from the input inboundPaymentInfo, normalized and with an hashed PAN
     */
    public IntegratedPayments map(InboundIntegratedPayments inboundIntegratedPayments, Boolean applyHashing) {

        IntegratedPayments IntegratedPayments = null;

        if (inboundIntegratedPayments != null) {
            IntegratedPayments = IntegratedPayments.builder().build();
            BeanUtils.copyProperties(inboundIntegratedPayments, IntegratedPayments);

        }

        return IntegratedPayments;

    }
}
