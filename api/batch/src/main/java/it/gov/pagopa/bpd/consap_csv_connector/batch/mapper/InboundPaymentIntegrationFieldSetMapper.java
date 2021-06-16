package it.gov.pagopa.bpd.consap_csv_connector.batch.mapper;

import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentInfo;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentIntegration;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of {@link FieldSetMapper}, to be used for a reader
 * related to files containing {@link InboundPaymentInfo} data
 */

@RequiredArgsConstructor
public class InboundPaymentIntegrationFieldSetMapper implements FieldSetMapper<InboundPaymentIntegration> {

    private final String timestampParser;

    /**
     * @param fieldSet instance of FieldSet containing fields related to an {@link InboundPaymentInfo}
     * @return instance of  {@link InboundPaymentInfo}, mapped from a FieldSet
     * @throws BindException
     */
    @Override
    public InboundPaymentIntegration mapFieldSet(@Nullable FieldSet fieldSet) throws BindException {

        if (fieldSet == null) {
            return null;
        }

        String executionDateString = fieldSet.readString("executionDate");
        String periodStartDateString = fieldSet.readString("periodStart");
        String periodEndDateString = fieldSet.readString("periodEnd");

        DateTimeFormatter dtf = timestampParser != null && !timestampParser.isEmpty() ?
                DateTimeFormatter.ofPattern(timestampParser).withZone(ZoneId.systemDefault()) : null;

        InboundPaymentIntegration inboundPaymentIntegration =
                InboundPaymentIntegration.builder()
                        .idConsap(fieldSet.readString("idConsap"))
                        .idComplaint(fieldSet.readString("idComplaint"))
                        .idPagoPa(fieldSet.readString("idPagoPa"))
                        .fiscalCode(fieldSet.readString("fiscalCode"))
                        .iban(fieldSet.readString("iban"))
                        .name(fieldSet.readString("name"))
                        .surname(fieldSet.readString("surname"))
                        .amount(fieldSet.readString("amount"))
                        .jackpotAmount(fieldSet.readString("jackpotAmount"))
                        .cashbackAmount(fieldSet.readString("cashbackAmount"))
                        .resultReason(fieldSet.readString("resultReason"))
                        .result(fieldSet.readString("result"))
                        .cro(fieldSet.readString("cro"))
                        .build();

        if (!executionDateString.isEmpty()) {

            LocalDate dateTime = dtf != null ?
                    LocalDate.parse(executionDateString, dtf) :
                    LocalDate.parse(executionDateString);

            if (dateTime != null) {
                inboundPaymentIntegration.setExecutionDate(executionDateString);
            }
        }

        if (!periodStartDateString.isEmpty()) {

            LocalDate dateTime = dtf != null ?
                    LocalDate.parse(periodStartDateString, dtf) :
                    LocalDate.parse(periodStartDateString);

            if (dateTime != null) {
                inboundPaymentIntegration.setPeriodStartDate(periodStartDateString);
            }
        }

        if (!periodEndDateString.isEmpty()) {

            LocalDate dateTime = dtf != null ?
                    LocalDate.parse(periodEndDateString, dtf) :
                    LocalDate.parse(periodEndDateString);

            if (dateTime != null) {
                inboundPaymentIntegration.setPeriodEndDate(periodEndDateString);
            }
        }

        return inboundPaymentIntegration;

    }

}
