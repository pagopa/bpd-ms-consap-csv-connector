package it.gov.pagopa.bpd.consap_csv_connector.batch.step;

import eu.sia.meda.BaseTest;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.validation.ConstraintViolationException;

/**
 * Class for unit testing of the InboundPaymentInfoItemProcessor class
 */
public class InboundPaymentInfoItemProcessorTest extends BaseTest {

    private InboundPaymentInfoItemProcessor inboundPaymentInfoItemProcessor;

    @Before
    public void initTest() {
        this.inboundPaymentInfoItemProcessor = new InboundPaymentInfoItemProcessor();
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void processValidInboundPaymentInfo() {

        try {
            InboundPaymentInfo inboundPaymentInfo = getInboundTransaction();
            InboundPaymentInfo paymentInfo = inboundPaymentInfoItemProcessor.
                    process(inboundPaymentInfo);
            Assert.assertNotNull(paymentInfo);
            Assert.assertEquals(paymentInfo.getUniqueID(), inboundPaymentInfo.getUniqueID());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void processInvalidInboundPaymentInfo() {

        InboundPaymentInfo inboundPaymentInfo = getInboundTransaction();
        inboundPaymentInfo.setUniqueID(null);
        inboundPaymentInfo.setCro("");

        exceptionRule.expect(ConstraintViolationException.class);
        inboundPaymentInfoItemProcessor.process(inboundPaymentInfo);

    }

    public InboundPaymentInfo getInboundTransaction() {
        return InboundPaymentInfo.builder()
                .uniqueID("000000001")
                .result("KO")
                .resultReason("resultReason")
                .cro("17270006101")
                .executionDate("27/07/2021")
                .build();
    }

}