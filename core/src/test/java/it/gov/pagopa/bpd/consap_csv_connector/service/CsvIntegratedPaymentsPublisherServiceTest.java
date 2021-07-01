package it.gov.pagopa.bpd.consap_csv_connector.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sia.meda.BaseSpringTest;
import eu.sia.meda.event.transformer.SimpleEventRequestTransformer;
import eu.sia.meda.event.transformer.SimpleEventResponseTransformer;
import it.gov.pagopa.bpd.award_winner.integration.event.CsvIntegratedPaymentsPublisherConnector;
import it.gov.pagopa.bpd.award_winner.integration.event.model.IntegratedPayments;
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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@ContextConfiguration(classes = CsvIntegratedPaymentsPublisherServiceImpl.class)
public class CsvIntegratedPaymentsPublisherServiceTest extends BaseSpringTest {

    @SpyBean
    ObjectMapper objectMapper;

    @MockBean
    private CsvIntegratedPaymentsPublisherConnector csvIntegratedPaymentsPublisherConnectorMock;

    @SpyBean
    private HeaderAwareRequestTransformer<IntegratedPayments> headerAwareRequestTransformerSpy;

    @SpyBean
    private SimpleEventRequestTransformer<IntegratedPayments> simpleEventRequestTransformerSpy;

    @SpyBean
    private SimpleEventResponseTransformer simpleEventResponseTransformerSpy;

    @Autowired
    CsvIntegratedPaymentsPublisherService csvIntegratedPaymentsPublisherService;

    private IntegratedPayments integratedPayments;

    @Before
    public void setUp() throws Exception {
        Mockito.reset(
                csvIntegratedPaymentsPublisherConnectorMock,
                headerAwareRequestTransformerSpy,
                simpleEventRequestTransformerSpy,
                simpleEventResponseTransformerSpy);
        integratedPayments = getRequestObject();
    }

    @Test
    public void publishPaymentInfoEvent() {
        RecordHeaders recordHeaders = new RecordHeaders();
        recordHeaders.add("INTEGRATED_PAYMENT_VALIDATION_DATETIME", "INTEGRATED_PAYMENT_VALIDATION_DATETIME".getBytes(StandardCharsets.UTF_8));

        BDDMockito.doReturn(true)
                .when(csvIntegratedPaymentsPublisherConnectorMock)
                .doCall(Mockito.eq(integratedPayments),
                        Mockito.eq(headerAwareRequestTransformerSpy),
                        Mockito.eq(simpleEventResponseTransformerSpy),
                        Mockito.any());

        try {
            csvIntegratedPaymentsPublisherService.publishIntegratedPaymentsEvent(integratedPayments);
            BDDMockito.verify(csvIntegratedPaymentsPublisherConnectorMock,Mockito.atLeastOnce())
                    .doCall(Mockito.eq(integratedPayments),
                            Mockito.eq(headerAwareRequestTransformerSpy),
                            Mockito.eq(simpleEventResponseTransformerSpy),
                            Mockito.any());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    protected IntegratedPayments getRequestObject() {
        return IntegratedPayments.builder()
                .fiscalCode("fiscalCode")
                .awardPeriodId(1L)
                .ticketId(2L)
                .relatedPaymentId(10L)
                .amount(new BigDecimal(12))
                .cashbackAmount(new BigDecimal(52))
                .jackpotAmount(new BigDecimal(100))
                .build();
    }
}
