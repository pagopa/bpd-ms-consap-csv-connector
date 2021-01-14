package it.gov.pagopa.bpd.consap_csv_connector.batch.mapper;

import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentInfo;
import lombok.Data;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

@Data
public class InboundPaymentInfoLineMapper<T> implements LineMapper<InboundPaymentInfo>, InitializingBean {

    private LineTokenizer tokenizer;

    private FieldSetMapper<InboundPaymentInfo> fieldSetMapper;

    private String filename;

    public InboundPaymentInfo mapLine(String line, int lineNumber) throws Exception {
        try{
            InboundPaymentInfo inboundPaymentInfo = fieldSetMapper.mapFieldSet(tokenizer.tokenize(line));
            inboundPaymentInfo.setLineNumber(lineNumber);
            inboundPaymentInfo.setFilename(filename);
            return inboundPaymentInfo;
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