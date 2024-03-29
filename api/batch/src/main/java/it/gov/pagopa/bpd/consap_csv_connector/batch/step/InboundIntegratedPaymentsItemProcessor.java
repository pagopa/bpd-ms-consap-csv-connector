package it.gov.pagopa.bpd.consap_csv_connector.batch.step;

import it.gov.pagopa.bpd.award_winner.integration.event.model.IntegratedPayments;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundIntegratedPayments;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import javax.validation.*;
import java.util.Set;

/**
 * Implementation of the ItemProcessor interface, used to process instances of InboundPaymentInfo,
 * to be mapped into a normalized version defined as instances of PaymentInfo
 */

@RequiredArgsConstructor
@Slf4j
@Data
@Component
public class InboundIntegratedPaymentsItemProcessor implements ItemProcessor<InboundIntegratedPayments, InboundIntegratedPayments> {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    /**
     * Validates the input {@link InboundIntegratedPayments}, and maps it to an instance of PaymentInfo
     * @param inboundIntegratedPayments
     *              instance of {@link InboundIntegratedPayments} from the read phase of the step
     * @return instance of  {@link IntegratedPayments}, mapped from a normalized instance of {@link InboundIntegratedPayments}
     * @throws ConstraintViolationException
     */
    @Override
    public InboundIntegratedPayments process(InboundIntegratedPayments inboundIntegratedPayments) {

        Set<ConstraintViolation<InboundIntegratedPayments>> constraintViolations = validator.validate(inboundIntegratedPayments);
        if (constraintViolations.size() > 0) {
            throw new ConstraintViolationException(constraintViolations);
        }

        return inboundIntegratedPayments;

    }
}
