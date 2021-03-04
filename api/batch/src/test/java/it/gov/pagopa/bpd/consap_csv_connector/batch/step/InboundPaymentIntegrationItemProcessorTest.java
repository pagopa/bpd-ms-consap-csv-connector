package it.gov.pagopa.bpd.consap_csv_connector.batch.step;

import eu.sia.meda.BaseTest;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentIntegration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.validation.ConstraintViolationException;

/**
 * Class for unit testing of the InboundPaymentInfoItemProcessor class
 */
public class InboundPaymentIntegrationItemProcessorTest extends BaseTest {

    private InboundPaymentIntegrationProcessor inboundPaymentIntegrationItemProcessor;

    @Before
    public void initTest() {
        this.inboundPaymentIntegrationItemProcessor = new InboundPaymentIntegrationProcessor();
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void processValidInboundPaymentInfo() {

        try {
            InboundPaymentIntegration inboundPaymentIntegration = getInboundPaymentIntegration();
            InboundPaymentIntegration paymentIntegration = inboundPaymentIntegrationItemProcessor.
                    process(inboundPaymentIntegration);
            Assert.assertNotNull(paymentIntegration);
            Assert.assertEquals(inboundPaymentIntegration.getIdConsap(),
                    inboundPaymentIntegration.getIdConsap());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void processInvalidInboundPaymentInfo() {

        InboundPaymentIntegration inboundPaymentIntegration = getInboundPaymentIntegration();
        inboundPaymentIntegration.setIdConsap(null);
        inboundPaymentIntegration.setCro("");

        exceptionRule.expect(ConstraintViolationException.class);
        inboundPaymentIntegrationItemProcessor.process(inboundPaymentIntegration);

    }

    public InboundPaymentIntegration getInboundPaymentIntegration() {
        return InboundPaymentIntegration.builder()
                .idConsap("000000001")
                .idComplaint("000000001")
                .idPagoPa("000000001")
                .awardPeriodId("1")
                .periodStartDate("01/01/2021")
                .periodEndDate("01/06/2021")
                .iban("iban")
                .technicalCountProperty("technicalProperty")
                .fiscalCode("fiscalCode")
                .name("name")
                .surname("surname")
                .cashbackAmount("100")
                .result("ORDINE ESEGUITO")
                .resultReason("result reason")
                .cro("17270006101")
                .executionDate("27/07/2021")
                .filename("test.csv")
                .lineNumber(1)
                .build();
    }

}