package it.gov.pagopa.bpd.consap_csv_connector.batch.listener.integratedPayments;

import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.PaymentInfoItemWriterListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentInfo;
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

public class IntegratedPaymentsItemWriterListenerTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder(
            new File(getClass().getResource("/test-encrypt-intPayments").getFile()));

    @SneakyThrows
    @Test
    public void onWriteError_OK() {

        File folder = tempFolder.newFolder("testWriter");
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String executionDate = OffsetDateTime.now().format(fmt);

        PaymentInfoItemWriterListener paymentInfoItemWriterListener = new PaymentInfoItemWriterListener();
        paymentInfoItemWriterListener.setExecutionDate(executionDate);
        paymentInfoItemWriterListener.setEnableOnErrorFileLogging(true);
        paymentInfoItemWriterListener.setEnableOnErrorLogging(true);
        paymentInfoItemWriterListener.setResolver(new PathMatchingResourcePatternResolver());
        paymentInfoItemWriterListener.setErrorPaymentInfosLogsPath("file:/"+folder.getAbsolutePath());
        paymentInfoItemWriterListener.onWriteError(new Exception(), Collections.singletonList(
                InboundPaymentInfo.builder().filename("test").lineNumber(1).build()));

        Assert.assertEquals(1,
                FileUtils.listFiles(
                        resolver.getResources("classpath:/test-encrypt-intPayments/**/testWriter")[0].getFile(),
                        new String[]{"csv"},false).size());

    }

    @After
    public void tearDown() {
        tempFolder.delete();
    }

}
