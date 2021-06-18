package it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.integratedPayments;

import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundIntegratedPayments;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of {@link FieldSetMapper}, to be used for a reader
 * related to files containing {@link InboundPaymentInfo} data
 */

@RequiredArgsConstructor
public class InboundIntegratedPaymentsFieldSetMapper implements FieldSetMapper<InboundIntegratedPayments> {

    private final String timestampParser;

    /**
     * @param fieldSet instance of FieldSet containing fields related to an {@link InboundIntegratedPayments}
     * @return instance of  {@link InboundIntegratedPayments}, mapped from a FieldSet
     * @throws BindException
     */
    @Override
    public InboundIntegratedPayments mapFieldSet(@Nullable FieldSet fieldSet) throws BindException {

        if (fieldSet == null) {
            return null;
        }

//        String executionDateString = fieldSet.readString("executionDate");

        DateTimeFormatter dtf = timestampParser != null && !timestampParser.isEmpty() ?
                DateTimeFormatter.ofPattern(timestampParser).withZone(ZoneId.systemDefault()) : null;

        InboundIntegratedPayments inboundIntegratedPayments =
                InboundIntegratedPayments.builder()
                        .fiscalCode(fieldSet.readString("fiscalCode"))
                        .awardPeriodId(fieldSet.readLong("awardPeriodId"))
                        .ticketId(fieldSet.readLong("ticketId"))
                        .relatedPaymentId(fieldSet.readLong("relatedPaymentId"))
                        .amount(fieldSet.readBigDecimal("amount"))
                        .cashbackAmount(fieldSet.readBigDecimal("cashbackAmount"))
                        .jackpotAmount(fieldSet.readBigDecimal("jackpotAmount"))
                        .build();

//        if (executionDateString != null
//                && !executionDateString.isEmpty()) {
//
//            LocalDate dateTime = dtf != null ?
//                    LocalDate.parse(executionDateString, dtf) :
//                    LocalDate.parse(executionDateString);
//
//            if (dateTime != null) {
//                inboundIntegratedPayments.setExecutionDate(executionDateString);
//            }
//        }

        return inboundIntegratedPayments;

    }
}
