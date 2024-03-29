package it.gov.pagopa.bpd.consap_csv_connector.batch.listener.integratedPayments;

import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundIntegratedPayments;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Implementation of {@link ItemReadListener}, to be used to log and/or store records
 * that have produced an error while reading a record
 */

@Slf4j
@Data
public class IntegratedPaymentsItemReaderListener implements ItemReadListener<InboundIntegratedPayments> {

    private String errorPaymentInfosLogsPath;
    private String executionDate;
    private Boolean enableOnErrorLogging;
    private Boolean enableOnErrorFileLogging;
    private Long loggingFrequency;
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Override
    public void beforeRead() {}

    public void afterRead(InboundIntegratedPayments item) {}

    public void onReadError(Exception throwable) {

        if (enableOnErrorLogging) {
            log.error("Error while reading a payment info record - {}", throwable.getMessage());
        }

        if (enableOnErrorFileLogging && throwable instanceof FlatFileParseException) {
            FlatFileParseException flatFileParseException = (FlatFileParseException) throwable;
            String filename =  flatFileParseException.getMessage().split("\\[",3)[2]
                    .replaceAll("]","").replaceAll("\\\\", "/");
            String[] fileArr = filename.split("/");

            try {
                File file = new File(
                        resolver.getResource(errorPaymentInfosLogsPath).getFile().getAbsolutePath()
                                .concat("/".concat(executionDate))
                                + "_ValidationErrorRecords_"+fileArr[fileArr.length-1]
                                .replaceAll(".csv","")
                                .replaceAll(".pgp","")+".csv");
                String[] lineArray = flatFileParseException.getInput().split("_",2);
                FileUtils.writeStringToFile(
                        file, (lineArray.length > 1 ? lineArray[1] : lineArray[0]).concat("\n"),
                        Charset.defaultCharset(), true);
            } catch (IOException e) {
                log.error(e.getMessage(),e);
            }

        }

    }

}
