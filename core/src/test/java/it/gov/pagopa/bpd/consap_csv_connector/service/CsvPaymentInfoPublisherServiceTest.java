package it.gov.pagopa.bpd.consap_csv_connector.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sia.meda.BaseSpringTest;
import eu.sia.meda.event.transformer.SimpleEventRequestTransformer;
import eu.sia.meda.event.transformer.SimpleEventResponseTransformer;
import it.gov.pagopa.bpd.award_winner.integration.event.CsvPaymentInfoPublisherConnector;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentInfo;
import it.gov.pagopa.bpd.consap_csv_connector.service.transformer.HeaderAwareRequestTransformer;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

import java.nio.charset.StandardCharsets;

/**
 * Class for unit testing of {@link CsvPaymentInfoPublisherService}
 */
@ContextConfiguration(classes = CsvPaymentInfoPublisherServiceImpl.class)
public class CsvPaymentInfoPublisherServiceTest extends BaseSpringTest {

    @SpyBean
    ObjectMapper objectMapper;

    @MockBean
    private CsvPaymentInfoPublisherConnector csvPaymentInfoPublisherConnectorMock;

    @SpyBean
    private HeaderAwareRequestTransformer<PaymentInfo> headerAwareRequestTransformerSpy;

    @SpyBean
    private SimpleEventRequestTransformer<PaymentInfo> simpleEventRequestTransformerSpy;

    @SpyBean
    private SimpleEventResponseTransformer simpleEventResponseTransformerSpy;

    @Autowired
    CsvPaymentInfoPublisherService csvPaymentInfoPublisherService;

    private PaymentInfo paymentInfo;

    @Before
    public void setUp() throws Exception {
        Mockito.reset(
                csvPaymentInfoPublisherConnectorMock,
                headerAwareRequestTransformerSpy,
                simpleEventRequestTransformerSpy,
                simpleEventResponseTransformerSpy);
        paymentInfo = getRequestObject();
    }

    @Test
    public void publishPaymentInfoEvent() {
        RecordHeaders recordHeaders = new RecordHeaders();
        recordHeaders.add("PAYMENT_INFO_VALIDATION_DATETIME", "PAYMENT_INFO_VALIDATION_DATETIME".getBytes(StandardCharsets.UTF_8));

        BDDMockito.doReturn(true)
                .when(csvPaymentInfoPublisherConnectorMock)
                .doCall(Mockito.eq(paymentInfo),
                        Mockito.eq(headerAwareRequestTransformerSpy),
                        Mockito.eq(simpleEventResponseTransformerSpy),
                        Mockito.any());

        try {
            csvPaymentInfoPublisherService.publishPaymentInfoEvent(paymentInfo);
            BDDMockito.verify(csvPaymentInfoPublisherConnectorMock,Mockito.atLeastOnce())
                    .doCall(Mockito.eq(paymentInfo),
                            Mockito.eq(headerAwareRequestTransformerSpy),
                            Mockito.eq(simpleEventResponseTransformerSpy),
                            Mockito.any());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    protected PaymentInfo getRequestObject() {
        return PaymentInfo.builder()
                .uniqueID("000000001")
                .result("ORDINE ESEGUITO")
                .resultReason("result reason")
                .cro("17270006101")
                .executionDate("27/07/2021")
                .build();
    }

}
