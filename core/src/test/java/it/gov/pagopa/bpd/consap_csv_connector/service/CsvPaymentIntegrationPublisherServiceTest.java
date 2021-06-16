package it.gov.pagopa.bpd.consap_csv_connector.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sia.meda.BaseSpringTest;
import eu.sia.meda.event.transformer.SimpleEventRequestTransformer;
import eu.sia.meda.event.transformer.SimpleEventResponseTransformer;
import it.gov.pagopa.bpd.award_winner.integration.event.CsvPaymentInfoPublisherConnector;
import it.gov.pagopa.bpd.award_winner.integration.event.CsvPaymentIntegrationPublisherConnector;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentInfo;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentIntegration;
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

/**
 * Class for unit testing of {@link CsvPaymentIntegrationPublisherService}
 */
@ContextConfiguration(classes = CsvPaymentIntegrationPublisherServiceImpl.class)
public class CsvPaymentIntegrationPublisherServiceTest extends BaseSpringTest {

    @SpyBean
    ObjectMapper objectMapper;

    @MockBean
    private CsvPaymentIntegrationPublisherConnector csvPaymentIntegrationPublisherConnectorMock;

    @SpyBean
    private HeaderAwareRequestTransformer<PaymentIntegration> headerAwareRequestTransformerSpy;

    @SpyBean
    private SimpleEventResponseTransformer simpleEventResponseTransformerSpy;

    @Autowired
    CsvPaymentIntegrationPublisherService csvPaymentIntegrationPublisherService;

    private PaymentIntegration paymentIntegration;

    @Before
    public void setUp() throws Exception {
        Mockito.reset(
                csvPaymentIntegrationPublisherConnectorMock,
                headerAwareRequestTransformerSpy,
                simpleEventResponseTransformerSpy);
        paymentIntegration = getRequestObject();
    }

    @Test
    public void publishIntegration() {

        BDDMockito.doReturn(true)
                .when(csvPaymentIntegrationPublisherConnectorMock)
                .doCall(Mockito.eq(paymentIntegration),
                        Mockito.eq(headerAwareRequestTransformerSpy),
                        Mockito.eq(simpleEventResponseTransformerSpy),
                        Mockito.any());

        try {
            csvPaymentIntegrationPublisherService.publishPaymentIntegrationEvent(
                    paymentIntegration, new RecordHeaders());
            BDDMockito.verify(csvPaymentIntegrationPublisherConnectorMock,Mockito.atLeastOnce())
                    .doCall(Mockito.eq(paymentIntegration),
                            Mockito.eq(headerAwareRequestTransformerSpy),
                            Mockito.eq(simpleEventResponseTransformerSpy),
                            Mockito.any());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    protected PaymentIntegration getRequestObject() {
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
                .amount(BigDecimal.valueOf(1L,2))
                .cashbackAmount(BigDecimal.valueOf(1L,2))
                .jackpotAmount(BigDecimal.valueOf(1L,2))
                .result("ORDINE ESEGUITO")
                .resultReason("result reason")
                .cro("17270006101")
                .executionDate("27/07/2021")
                .build();
    }

}
