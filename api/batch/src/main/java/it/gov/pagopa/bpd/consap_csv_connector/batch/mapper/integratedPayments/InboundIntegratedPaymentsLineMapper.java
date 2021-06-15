package it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.integratedPayments;

import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundIntegratedPayments;
import lombok.Data;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

@Data
public class InboundIntegratedPaymentsLineMapper implements LineMapper<InboundIntegratedPayments>, InitializingBean  {

    private LineTokenizer tokenizer;

    private FieldSetMapper<InboundIntegratedPayments> fieldSetMapper;

    private String filename;

    public InboundIntegratedPayments mapLine(String line, int lineNumber) throws Exception {
        try{
            InboundIntegratedPayments inboundIntegratedPayments = fieldSetMapper.mapFieldSet(tokenizer.tokenize(line));
            inboundIntegratedPayments.setLineNumber(lineNumber);
            inboundIntegratedPayments.setFilename(filename);
            return inboundIntegratedPayments;
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
