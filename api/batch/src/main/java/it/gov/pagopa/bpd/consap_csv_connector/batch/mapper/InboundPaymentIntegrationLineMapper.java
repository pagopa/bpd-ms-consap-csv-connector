package it.gov.pagopa.bpd.consap_csv_connector.batch.mapper;

import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentInfo;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentIntegration;
import lombok.Data;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

@Data
public class InboundPaymentIntegrationLineMapper<T> implements LineMapper<InboundPaymentIntegration>, InitializingBean {

    private LineTokenizer tokenizer;

    private FieldSetMapper<InboundPaymentIntegration> fieldSetMapper;

    private String filename;

    public InboundPaymentIntegration mapLine(String line, int lineNumber) throws Exception {
        try{
            InboundPaymentIntegration inboundPaymentIntegration = fieldSetMapper.mapFieldSet(tokenizer.tokenize(line));
            inboundPaymentIntegration.setLineNumber(lineNumber);
            inboundPaymentIntegration.setFilename(filename);
            return inboundPaymentIntegration;
        }
        catch(Exception ex){
            throw new FlatFileParseException("Parsing error at line: " + lineNumber, ex, line, lineNumber);
        }
    }

    public void afterPropertiesSet() {
        Assert.notNull(tokenizer, "The LineTokenizer must be set");
        Assert.notNull(fieldSetMapper, "The FieldSetMapper must be set");
    }

}