package it.gov.pagopa.bpd.consap_csv_connector.batch.step;

import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentIntegration;
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
public class InboundPaymentIntegrationProcessor
        implements ItemProcessor<InboundPaymentIntegration, InboundPaymentIntegration> {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    /**
     * Validates the input {@link InboundPaymentIntegrationProcessor}, and maps it to an instance of PaymentInfo
     * @param inboundPaymentIntegration
     *              instance of {@link InboundPaymentIntegration} from the read phase of the step
     * @return instance of  {@link InboundPaymentIntegration}, mapped from a normalized instance of {@link InboundPaymentIntegration}
     * @throws ConstraintViolationException
     */
    @Override
    public InboundPaymentIntegration process(
            InboundPaymentIntegration inboundPaymentIntegration) {

        Set<ConstraintViolation<InboundPaymentIntegration>> constraintViolations =
                validator.validate(inboundPaymentIntegration);
        if (constraintViolations.size() > 0) {
            throw new ConstraintViolationException(constraintViolations);
        }

        return inboundPaymentIntegration;

    }

}
