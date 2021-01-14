package it.gov.pagopa.bpd.consap_csv_connector.batch.listener;

import it.gov.pagopa.bpd.consap_csv_connector.batch.encryption.PGPDecryptUtil;
import it.gov.pagopa.bpd.consap_csv_connector.service.WriterTrackerService;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Implementation of {@link StepExecutionListener}, to be used to log and define the exit status of a step
 */

@Slf4j
@Data
public class PaymentInfoReaderStepListener implements StepExecutionListener {

    private WriterTrackerService writerTrackerService;
    private String successPath;
    private String errorPath;
    private String errorDir;
    private String publicKeyDir;
    private Boolean applyEncrypt;
    private String executionDate;
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Starting processing for file: {}", stepExecution.getExecutionContext().get("fileName"));
    }

    @SneakyThrows
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        ExitStatus exitStatus = stepExecution.getExitStatus();

        if (!exitStatus.getExitCode().equals(ExitStatus.FAILED.getExitCode()) &&
                stepExecution.getSkipCount() > 0) {
            exitStatus = new ExitStatus("COMPLETED WITH SKIPS");
        }

        log.info("Processing for file: {} ended with status: {}",
                stepExecution.getExecutionContext().get("fileName"), exitStatus.getExitCode());

        return exitStatus;

    }

}
