package it.gov.pagopa.bpd.consap_csv_connector.batch.mapper;

import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of {@link FieldSetMapper}, to be used for a reader
 * related to files containing {@link InboundPaymentInfo} data
 */

@RequiredArgsConstructor
public class InboundPaymentInfoFieldSetMapper implements FieldSetMapper<InboundPaymentInfo> {

    private final String timestampParser;

    /**
     *
     * @param fieldSet
     *          instance of FieldSet containing fields related to an {@link InboundPaymentInfo}
     * @return instance of  {@link InboundPaymentInfo}, mapped from a FieldSet
     * @throws BindException
     */
    @Override
    public InboundPaymentInfo mapFieldSet(@Nullable FieldSet fieldSet) throws BindException {

        if (fieldSet == null) {
            return null;
        }

//        DateTimeFormatter dtf = timestampParser != null && !timestampParser.isEmpty() ?
//                DateTimeFormatter.ofPattern(timestampParser).withZone(ZoneId.systemDefault()): null;

        InboundPaymentInfo inboundPaymentInfo =
                InboundPaymentInfo.builder()
                        .uniqueID(fieldSet.readString("uniqueID"))
                        .result(fieldSet.readString("result"))
                        .resultReason(fieldSet.readString("resultReason"))
                        .cro(fieldSet.readString("cro"))
                        .executionDate(fieldSet.readString("executionDate"))
                        .build();

//        OffsetDateTime dateTime = dtf != null ?
//                ZonedDateTime.parse(fieldSet.readString("timestamp"), dtf).toOffsetDateTime() :
//                OffsetDateTime.parse(fieldSet.readString("timestamp"));
//
//        if (dateTime != null) {
//            inboundPaymentInfo.setTrxDate(fieldSet.readString("timestamp"));
//        }

        return inboundPaymentInfo;

    }

}
