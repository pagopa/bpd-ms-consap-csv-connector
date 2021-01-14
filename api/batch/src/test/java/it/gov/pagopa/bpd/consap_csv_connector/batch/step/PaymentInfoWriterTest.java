package it.gov.pagopa.bpd.consap_csv_connector.batch.step;

import eu.sia.meda.BaseTest;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.PaymentInfoItemWriterListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.PaymentInfoMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentInfo;
import it.gov.pagopa.bpd.consap_csv_connector.integration.event.model.PaymentInfo;
import it.gov.pagopa.bpd.consap_csv_connector.service.CsvPaymentInfoPublisherService;
import it.gov.pagopa.bpd.consap_csv_connector.service.WriterTrackerService;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.Executors;

/**
 * Class for unit testing of the PaymentInfoWriter class
 */
public class PaymentInfoWriterTest extends BaseTest {

    @Mock
    private CsvPaymentInfoPublisherService csvPaymentInfoPublisherServiceMock;

    @Mock
    private WriterTrackerService writerTrackerServiceMock;

    @Mock
    private PaymentInfoItemWriterListener paymentInfoItemWriterListenerMock;

    private PaymentInfoWriter paymentInfoWriter;

    @Spy
    private PaymentInfoMapper mapperSpy;


    @Before
    public void initTest() {
        Mockito.reset(csvPaymentInfoPublisherServiceMock, mapperSpy);
        paymentInfoWriter = new PaymentInfoWriter(
                writerTrackerServiceMock, csvPaymentInfoPublisherServiceMock, mapperSpy);
        paymentInfoWriter.setPaymentInfoItemWriterListener(paymentInfoItemWriterListenerMock);
        paymentInfoWriter.setExecutor(Executors.newSingleThreadExecutor());
        paymentInfoWriter.setApplyHashing(true);
        paymentInfoWriter.setCheckpointFrequency(3);
        paymentInfoWriter.setEnableCheckpointFrequency(true);
        BDDMockito.doNothing().when(csvPaymentInfoPublisherServiceMock)
                .publishPaymentInfoEvent(Mockito.any(PaymentInfo.class));
        BDDMockito.doNothing().when(writerTrackerServiceMock)
                .addCountDownLatch(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @SneakyThrows
    @Test
    public void testWriterNullList() {
        exceptionRule.expect(NullPointerException.class);
        paymentInfoWriter.write(null);
        BDDMockito.verifyZeroInteractions(csvPaymentInfoPublisherServiceMock);
    }

    @Test
    public void testWriterEmptyList() {
        try {
            paymentInfoWriter.write(Collections.emptyList());
            BDDMockito.verifyZeroInteractions(csvPaymentInfoPublisherServiceMock);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testWriterMonoList() {
        try {
            paymentInfoWriter.write(Collections.singletonList(getInboundTransaction()));
            Thread.sleep(1000);
            BDDMockito.verify(csvPaymentInfoPublisherServiceMock, Mockito.times(1))
                    .publishPaymentInfoEvent(Mockito.eq(getPaymentInfo()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testWriterMultiList() {
        try {
            paymentInfoWriter.write(Collections.nCopies(5,getInboundTransaction()));
            Thread.sleep(1000);
            BDDMockito.verify(csvPaymentInfoPublisherServiceMock, Mockito.times(5))
                    .publishPaymentInfoEvent(Mockito.eq(getPaymentInfo()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }


    public InboundPaymentInfo getInboundTransaction() {
        return InboundPaymentInfo.builder()
                .uniqueID("000000001")
                .result("KO")
                .resultReason("resultReason")
                .cro("17270006101")
                .executionDate("27/07/2021")
                .filename("filename")
                .lineNumber(1)
                .build();
    }

    protected PaymentInfo getPaymentInfo() {
        return PaymentInfo.builder()
                .uniqueID("000000001")
                .result("KO")
                .resultReason("resultReason")
                .cro("17270006101")
                .executionDate("27/07/2021")
                .build();
    }

}