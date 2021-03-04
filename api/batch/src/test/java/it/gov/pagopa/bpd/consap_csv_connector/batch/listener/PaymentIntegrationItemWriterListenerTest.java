package it.gov.pagopa.bpd.consap_csv_connector.batch.listener;

import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentIntegration;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

public class PaymentIntegrationItemWriterListenerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder(
            new File(getClass().getResource("/test-encrypt").getFile()));

    @SneakyThrows
    @Test
    public void onWriteError_OK() {

        File folder = tempFolder.newFolder("testWriter");
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String executionDate = OffsetDateTime.now().format(fmt);

        PaymentIntegrationItemWriterListener paymentIntegrationItemWriterListener = new PaymentIntegrationItemWriterListener();
        paymentIntegrationItemWriterListener.setExecutionDate(executionDate);
        paymentIntegrationItemWriterListener.setEnableOnErrorFileLogging(true);
        paymentIntegrationItemWriterListener.setEnableOnErrorLogging(true);
        paymentIntegrationItemWriterListener.setResolver(new PathMatchingResourcePatternResolver());
        paymentIntegrationItemWriterListener.setErrorPaymentIntegrationLogsPath("file:/"+folder.getAbsolutePath());
        paymentIntegrationItemWriterListener.onWriteError(new Exception(), Collections.singletonList(
                InboundPaymentIntegration.builder().filename("test").lineNumber(1).build()));

        Assert.assertEquals(1,
                FileUtils.listFiles(
                        resolver.getResources("classpath:/test-encrypt/**/testWriter")[0].getFile(),
                        new String[]{"csv"},false).size());

    }

    @After
    public void tearDown() {
        tempFolder.delete();
    }

}