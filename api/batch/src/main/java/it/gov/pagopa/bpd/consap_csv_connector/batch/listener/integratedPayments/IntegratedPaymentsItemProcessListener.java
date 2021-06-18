package it.gov.pagopa.bpd.consap_csv_connector.batch.listener.integratedPayments;

import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundIntegratedPayments;
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
public class IntegratedPaymentsItemProcessListener implements ItemProcessListener<InboundIntegratedPayments, InboundIntegratedPayments> {

    private String errorPaymentInfosLogsPath;
    private String executionDate;
    private Boolean enableOnErrorLogging;
    private Boolean enableOnErrorFileLogging;
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Override
    public void beforeProcess(InboundIntegratedPayments item) {}

    @Override
    public void afterProcess(InboundIntegratedPayments item, InboundIntegratedPayments result) {}

    public void onProcessError(InboundIntegratedPayments item, Exception throwable) {


        if (enableOnErrorLogging) {
            log.error("Error during payment info record processing - {}, payment info: " +
                            "{}, filename: {}",
                    throwable.getMessage(),
                    item.getTicketId(),
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

    private String buildCsv(InboundIntegratedPayments inboundIntegratedPayments) {
        return (inboundIntegratedPayments.getFiscalCode() != null ? inboundIntegratedPayments.getFiscalCode() : "").concat(";")
                .concat(inboundIntegratedPayments.getAwardPeriodId() != null ? inboundIntegratedPayments.getAwardPeriodId().toString() : "").concat(";")
                .concat(inboundIntegratedPayments.getTicketId() != null ? inboundIntegratedPayments.getTicketId().toString() : "").concat(";")
                .concat(inboundIntegratedPayments.getRelatedPaymentId() != null ? inboundIntegratedPayments.getRelatedPaymentId().toString() : "").concat(";")
                .concat(inboundIntegratedPayments.getAmount() != null ? inboundIntegratedPayments.getAmount().toString() : "").concat(";")
                .concat(inboundIntegratedPayments.getCashbackAmount() != null ? inboundIntegratedPayments.getCashbackAmount().toString() : "").concat(";")
                .concat(inboundIntegratedPayments.getCashbackAmount() != null ? inboundIntegratedPayments.getCashbackAmount().toString() : "").concat("\n");
    }


}
