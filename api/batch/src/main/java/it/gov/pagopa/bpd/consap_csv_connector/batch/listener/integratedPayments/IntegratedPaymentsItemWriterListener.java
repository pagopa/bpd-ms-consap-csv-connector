package it.gov.pagopa.bpd.consap_csv_connector.batch.listener.integratedPayments;

import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundIntegratedPayments;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Implementation of {@link ItemWriteListener}, to be used to log and/or store records
 * that have produced an error while reading a record writing phase
 */

@Slf4j
@Data
public class IntegratedPaymentsItemWriterListener implements ItemWriteListener<InboundIntegratedPayments> {

    private String errorPaymentInfosLogsPath;
    private String executionDate;
    private Boolean enableOnErrorLogging;
    private Boolean enableOnErrorFileLogging;
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Override
    public void beforeWrite(List<? extends InboundIntegratedPayments> transactions) {}

    public void afterWrite(List<? extends InboundIntegratedPayments> transactions) {}

    public void onWriteError(Exception throwable, List<? extends InboundIntegratedPayments> inboundTransactions) {

        inboundTransactions.forEach(inboundIntegratedPayments -> {

            if (enableOnErrorLogging) {
                log.error("Error during payment info record writing - {}, payment info: " +
                                "{}, filename: {}",
                        throwable.getMessage(),
                        inboundIntegratedPayments.getTicketId(),
                        inboundIntegratedPayments.getFilename());
            }

            if (enableOnErrorFileLogging) {
                try {
                    String filename = inboundIntegratedPayments.getFilename().replaceAll("\\\\", "/");
                    String[] fileArr = filename.split("/");
                    File file = new File(
                            resolver.getResource(errorPaymentInfosLogsPath).getFile().getAbsolutePath()
                                    .concat("/".concat(executionDate))
                                    + "_WriteErrorRecords_"+fileArr[fileArr.length-1]
                                    .replaceAll(".xls","")
                                    .replaceAll(".pgp","")+".csv");
                    FileUtils.writeStringToFile(
                            file, buildCsv(inboundIntegratedPayments), Charset.defaultCharset(), true);

                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }

        });

    }

    public void onWriteError(Exception throwable, InboundIntegratedPayments inboundIntegratedPayments) {

        if (enableOnErrorLogging) {
            log.error("Error during payment info record writing - {}, payment info: " +
                            "{}, filename: {}",
                    throwable.getMessage(),
                    inboundIntegratedPayments.getTicketId(),
                    inboundIntegratedPayments.getFilename());
        }

        if (enableOnErrorFileLogging) {
            try {
                String filename = inboundIntegratedPayments.getFilename().replaceAll("\\\\", "/");
                String[] fileArr = filename.split("/");
                File file = new File(
                        resolver.getResource(errorPaymentInfosLogsPath).getFile().getAbsolutePath()
                                .concat("/".concat(executionDate))
                                + "_WriteErrorRecords_"+fileArr[fileArr.length-1]
                                .replaceAll(".xls","")
                                .replaceAll(".pgp","")+".csv");
                FileUtils.writeStringToFile(
                        file, buildCsv(inboundIntegratedPayments), Charset.defaultCharset(), true);

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
