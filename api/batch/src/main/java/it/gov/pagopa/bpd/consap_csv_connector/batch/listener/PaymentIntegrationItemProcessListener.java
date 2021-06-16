package it.gov.pagopa.bpd.consap_csv_connector.batch.listener;

import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentIntegration;
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
public class PaymentIntegrationItemProcessListener
        implements ItemProcessListener<InboundPaymentIntegration, InboundPaymentIntegration> {

    private String errorPaymentIntegrationLogsPath;
    private String executionDate;
    private Boolean enableOnErrorLogging;
    private Boolean enableOnErrorFileLogging;
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Override
    public void beforeProcess(InboundPaymentIntegration item) {}

    @Override
    public void afterProcess(InboundPaymentIntegration item, InboundPaymentIntegration result) {}

    public void onProcessError(InboundPaymentIntegration item, Exception throwable) {


        if (enableOnErrorLogging) {
            log.error("Error during payment info record processing - {}, " +
                            " filename: {}",
                    throwable.getMessage(),
                    item.getFilename());
        }

        if (enableOnErrorFileLogging) {
            try {
                String filename = item.getFilename().replaceAll("\\\\", "/");
                String[] fileArr = filename.split("/");
                File file = new File(
                        resolver.getResource(errorPaymentIntegrationLogsPath).getFile().getAbsolutePath()
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

    private String buildCsv(InboundPaymentIntegration inboundPaymentIntegration) {
        return (inboundPaymentIntegration.getIdConsap() != null ? inboundPaymentIntegration.getIdConsap() : "").concat(";")
                .concat(inboundPaymentIntegration.getIdComplaint() != null ? inboundPaymentIntegration.getIdComplaint() : "").concat(";")
                .concat(inboundPaymentIntegration.getIdPagoPa() != null ? inboundPaymentIntegration.getIdPagoPa() : "").concat(";")
                .concat(inboundPaymentIntegration.getFiscalCode() != null ? inboundPaymentIntegration.getFiscalCode() : "").concat(";")
                .concat(inboundPaymentIntegration.getIban() != null ? inboundPaymentIntegration.getIban() : "").concat(";")
                .concat(inboundPaymentIntegration.getName() != null ? inboundPaymentIntegration.getName() : "").concat(";")
                .concat(inboundPaymentIntegration.getSurname() != null ? inboundPaymentIntegration.getSurname() : "").concat(";")
                .concat(inboundPaymentIntegration.getJackpotAmount() != null ? inboundPaymentIntegration.getJackpotAmount() : "").concat(";")
                .concat(inboundPaymentIntegration.getAmount() != null ? inboundPaymentIntegration.getAmount() : "").concat(";")
                .concat(inboundPaymentIntegration.getCashbackAmount() != null ? inboundPaymentIntegration.getCashbackAmount() : "").concat(";")
                .concat(inboundPaymentIntegration.getResultReason() != null ? inboundPaymentIntegration.getResultReason() : "").concat(";")
                .concat(inboundPaymentIntegration.getPeriodStartDate() != null ? inboundPaymentIntegration.getPeriodStartDate() : "").concat(";")
                .concat(inboundPaymentIntegration.getPeriodEndDate() != null ? inboundPaymentIntegration.getPeriodEndDate() : "").concat(";")
                .concat(inboundPaymentIntegration.getResult() != null ? inboundPaymentIntegration.getResult() : "").concat(";")
                .concat(inboundPaymentIntegration.getCro() != null ? inboundPaymentIntegration.getCro() : "").concat(";")
                .concat(inboundPaymentIntegration.getExecutionDate() != null ? inboundPaymentIntegration.getExecutionDate() : "").concat("\n");
    }

}
