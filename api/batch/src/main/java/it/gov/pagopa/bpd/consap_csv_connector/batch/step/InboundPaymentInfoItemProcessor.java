package it.gov.pagopa.bpd.consap_csv_connector.batch.step;

import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentInfo;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentInfo;
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
public class InboundPaymentInfoItemProcessor implements ItemProcessor<InboundPaymentInfo, InboundPaymentInfo> {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    /**
     * Validates the input {@link InboundPaymentInfo}, and maps it to an instance of PaymentInfo
     * @param inboundPaymentInfo
     *              instance of {@link InboundPaymentInfo} from the read phase of the step
     * @return instance of  {@link PaymentInfo}, mapped from a normalized instance of {@link InboundPaymentInfo}
     * @throws ConstraintViolationException
     */
    @Override
    public InboundPaymentInfo process(InboundPaymentInfo inboundPaymentInfo) {

        Set<ConstraintViolation<InboundPaymentInfo>> constraintViolations = validator.validate(inboundPaymentInfo);
        if (constraintViolations.size() > 0) {
            throw new ConstraintViolationException(constraintViolations);
        }

        return inboundPaymentInfo;

    }

}
