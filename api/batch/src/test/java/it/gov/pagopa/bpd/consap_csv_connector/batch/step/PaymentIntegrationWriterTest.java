package it.gov.pagopa.bpd.consap_csv_connector.batch.step;

import eu.sia.meda.BaseTest;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentIntegration;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.PaymentIntegrationItemWriterListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.PaymentIntegrationMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentIntegration;
import it.gov.pagopa.bpd.consap_csv_connector.service.CsvPaymentIntegrationPublisherService;
import it.gov.pagopa.bpd.consap_csv_connector.service.WriterTrackerService;
import lombok.SneakyThrows;
import org.apache.kafka.common.header.internals.RecordHeaders;
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
public class PaymentIntegrationWriterTest extends BaseTest {

    @Mock
    private CsvPaymentIntegrationPublisherService csvPaymentIntegrationPublisherServiceMock;

    @Mock
    private WriterTrackerService writerTrackerServiceMock;

    @Mock
    private PaymentIntegrationItemWriterListener paymentIntegrationItemWriterListenerMock;

    private PaymentIntegrationWriter paymentIntegrationWriter;

    @Spy
    private PaymentIntegrationMapper mapperSpy;


    @Before
    public void initTest() {
        Mockito.reset(csvPaymentIntegrationPublisherServiceMock, mapperSpy);
        paymentIntegrationWriter = new PaymentIntegrationWriter(
                writerTrackerServiceMock, csvPaymentIntegrationPublisherServiceMock, mapperSpy);
        paymentIntegrationWriter.setPaymentIntegrationItemWriterListener(paymentIntegrationItemWriterListenerMock);
        paymentIntegrationWriter.setExecutor(Executors.newSingleThreadExecutor());
        paymentIntegrationWriter.setApplyHashing(true);
        paymentIntegrationWriter.setCheckpointFrequency(3);
        paymentIntegrationWriter.setEnableCheckpointFrequency(true);
        BDDMockito.doNothing().when(csvPaymentIntegrationPublisherServiceMock)
                .publishPaymentIntegrationEvent(Mockito.any(PaymentIntegration.class),Mockito.any());
        BDDMockito.doNothing().when(writerTrackerServiceMock)
                .addCountDownLatch(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @SneakyThrows
    @Test
    public void testWriterNullList() {
        exceptionRule.expect(NullPointerException.class);
        paymentIntegrationWriter.write(null);
        BDDMockito.verifyZeroInteractions(csvPaymentIntegrationPublisherServiceMock);
    }

    @Test
    public void testWriterEmptyList() {
        try {
            paymentIntegrationWriter.write(Collections.emptyList());
            BDDMockito.verifyZeroInteractions(csvPaymentIntegrationPublisherServiceMock);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testWriterMonoList() {
        try {
            paymentIntegrationWriter.write(Collections.singletonList(getInboundPaymentIntegration()));
            Thread.sleep(1000);
            BDDMockito.verify(csvPaymentIntegrationPublisherServiceMock, Mockito.times(1))
                    .publishPaymentIntegrationEvent(Mockito.eq(getPaymentIntegration()), Mockito.any());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testWriterMultiList() {
        try {
            paymentIntegrationWriter.write(Collections.nCopies(5,getInboundPaymentIntegration()));
            Thread.sleep(1000);
            BDDMockito.verify(csvPaymentIntegrationPublisherServiceMock, Mockito.times(5))
                    .publishPaymentIntegrationEvent(Mockito.eq(getPaymentIntegration()), Mockito.any());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }



    public InboundPaymentIntegration getInboundPaymentIntegration() {
        return InboundPaymentIntegration.builder()
                .idConsap("000000001")
                .idComplaint("000000001")
                .idPagoPa("000000001")
                .periodStartDate("01/01/2021")
                .periodEndDate("01/06/2021")
                .iban("iban")
                .jackpotAmount("100")
                .amount("100")
                .cashbackAmount("100")
                .fiscalCode("fiscalCode")
                .name("name")
                .surname("surname")
                .result("ORDINE ESEGUITO")
                .resultReason("result reason")
                .cro("17270006101")
                .executionDate("27/07/2021")
                .filename("test.csv")
                .lineNumber(1)
                .build();
    }

    protected PaymentIntegration getPaymentIntegration() {
        return PaymentIntegration.builder()
                .idConsap("000000001")
                .idComplaint("000000001")
                .idPagoPa("000000001")
                .periodStartDate("01/01/2021")
                .periodEndDate("01/06/2021")
                .iban("iban")
                .fiscalCode("fiscalCode")
                .name("name")
                .surname("surname")
                .amount(BigDecimal.valueOf(100L,2))
                .cashbackAmount(BigDecimal.valueOf(100L,2))
                .jackpotAmount(BigDecimal.valueOf(100L,2))
                .result("ORDINE ESEGUITO")
                .resultReason("result reason")
                .cro("17270006101")
                .executionDate("27/07/2021")
                .build();
    }

}