package it.gov.pagopa.bpd.consap_csv_connector.batch.listener;

import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentIntegration;
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
public class PaymentIntegrationItemWriterListener implements ItemWriteListener<InboundPaymentIntegration> {

    private String errorPaymentInfosLogsPath;
    private String executionDate;
    private Boolean enableOnErrorLogging;
    private Boolean enableOnErrorFileLogging;
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Override
    public void beforeWrite(List<? extends InboundPaymentIntegration> transactions) {}

    public void afterWrite(List<? extends InboundPaymentIntegration> transactions) {}

    public void onWriteError(Exception throwable, List<? extends InboundPaymentIntegration> inboundTransactions) {

        inboundTransactions.forEach(inboundPaymentInfo -> {

            if (enableOnErrorLogging) {
                log.error("Error during payment info record writing - {}, payment info: " +
                                " filename: {}",
                        throwable.getMessage(),
                        inboundPaymentInfo.getFilename());
            }

            if (enableOnErrorFileLogging) {
                try {
                    String filename = inboundPaymentInfo.getFilename().replaceAll("\\\\", "/");
                    String[] fileArr = filename.split("/");
                    File file = new File(
                            resolver.getResource(errorPaymentInfosLogsPath).getFile().getAbsolutePath()
                                    .concat("/".concat(executionDate))
                                    + "_WriteErrorRecords_"+fileArr[fileArr.length-1]
                                    .replaceAll(".csv","")
                                    .replaceAll(".pgp","")+".csv");
                    FileUtils.writeStringToFile(
                            file, buildCsv(inboundPaymentInfo), Charset.defaultCharset(), true);

                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }

        });

    }

    public void onWriteError(Exception throwable, InboundPaymentIntegration inboundPaymentInfo) {

        if (enableOnErrorLogging) {
            log.error("Error during payment info record writing - {}, payment info: " +
                            "filename: {}",
                    throwable.getMessage(),
                    inboundPaymentInfo.getFilename());
        }

        if (enableOnErrorFileLogging) {
            try {
                String filename = inboundPaymentInfo.getFilename().replaceAll("\\\\", "/");
                String[] fileArr = filename.split("/");
                File file = new File(
                        resolver.getResource(errorPaymentInfosLogsPath).getFile().getAbsolutePath()
                                .concat("/".concat(executionDate))
                                + "_WriteErrorRecords_"+fileArr[fileArr.length-1]
                                .replaceAll(".csv","")
                                .replaceAll(".pgp","")+".csv");
                FileUtils.writeStringToFile(
                        file, buildCsv(inboundPaymentInfo), Charset.defaultCharset(), true);

            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

    }

    private String buildCsv(InboundPaymentIntegration inboundPaymentInfo) {
        return (inboundPaymentInfo.getIdConsap() != null ? inboundPaymentInfo.getIdConsap() : "").concat(";")
                .concat(inboundPaymentInfo.getIdPagoPa() != null ? inboundPaymentInfo.getIdPagoPa() : "").concat(";")
                .concat(inboundPaymentInfo.getIdReclamo() != null ? inboundPaymentInfo.getIdReclamo() : "").concat(";")
                .concat(inboundPaymentInfo.getCro() != null ? inboundPaymentInfo.getCro() : "").concat(";")
                .concat(inboundPaymentInfo.getExecutionDate() != null ? inboundPaymentInfo.getExecutionDate() : "").concat("\n");
    }


}
