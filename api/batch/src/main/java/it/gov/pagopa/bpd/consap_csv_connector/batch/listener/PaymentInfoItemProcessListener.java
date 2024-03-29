package it.gov.pagopa.bpd.consap_csv_connector.batch.listener;

import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Implementation of {@link ItemProcessListener}, to be used to log and/or store records
 * filtered or that have produced an error during a record processing phase
 */
@Slf4j
@Data
public class PaymentInfoItemProcessListener implements ItemProcessListener<InboundPaymentInfo, InboundPaymentInfo> {

    private String errorPaymentInfosLogsPath;
    private String executionDate;
    private Boolean enableOnErrorLogging;
    private Boolean enableOnErrorFileLogging;
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Override
    public void beforeProcess(InboundPaymentInfo item) {}

    @Override
    public void afterProcess(InboundPaymentInfo item, InboundPaymentInfo result) {}

    public void onProcessError(InboundPaymentInfo item, Exception throwable) {


        if (enableOnErrorLogging) {
            log.error("Error during payment info record processing - {}, payment info: " +
                            "{}, filename: {}",
                    throwable.getMessage(),
                    item.getUniqueID(),
                    item.getFilename());
        }

        if (enableOnErrorFileLogging) {
            try {
                String filename = item.getFilename().replaceAll("\\\\", "/");
                String[] fileArr = filename.split("/");
                File file = new File(
                        resolver.getResource(errorPaymentInfosLogsPath).getFile().getAbsolutePath()
                                .concat("/".concat(executionDate))
                                + "_ValidationErrorRecords_"+fileArr[fileArr.length-1]
                                .replaceAll(".csv","")
                                .replaceAll(".pgp","")+".csv");
                FileUtils.writeStringToFile(file, buildCsv(item), Charset.defaultCharset(), true);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

    }

    private String buildCsv(InboundPaymentInfo inboundPaymentInfo) {
        return (inboundPaymentInfo.getUniqueID() != null ? inboundPaymentInfo.getUniqueID() : "").concat(";")
                .concat(inboundPaymentInfo.getResult() != null ? inboundPaymentInfo.getResult() : "").concat(";")
                .concat(inboundPaymentInfo.getResultReason() != null ? inboundPaymentInfo.getResultReason() : "").concat(";")
                .concat(inboundPaymentInfo.getCro() != null ? inboundPaymentInfo.getCro() : "").concat(";")
                .concat(inboundPaymentInfo.getExecutionDate() != null ? inboundPaymentInfo.getExecutionDate() : "").concat("\n");
    }

}
