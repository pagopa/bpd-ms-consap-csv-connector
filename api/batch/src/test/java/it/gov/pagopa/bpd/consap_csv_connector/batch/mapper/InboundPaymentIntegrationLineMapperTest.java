package it.gov.pagopa.bpd.consap_csv_connector.batch.mapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentIntegration;
import lombok.SneakyThrows;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

public class InboundPaymentIntegrationLineMapperTest {

    public InboundPaymentIntegrationLineMapperTest(){
        MockitoAnnotations.initMocks(this);
    }

    @BeforeClass
    public static void configTest() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        ((Logger) LoggerFactory.getLogger("eu.sia")).setLevel(Level.DEBUG);
    }

    private InboundPaymentIntegrationLineMapper<InboundPaymentIntegration> lineAwareMapper;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        lineAwareMapper = new InboundPaymentIntegrationLineMapper<>();
        lineAwareMapper.setFilename("test.csv");
        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setDelimiter(";");
        delimitedLineTokenizer.setNames(
                "idConsap", "idComplaint", "idPagoPa", "fiscalCode", "iban", "name", "surname", "cashbackAmount",
                "resultReason", "periodStart", "periodEnd", "awardPeriodId", "result", "cro", "executionDate",
                "technicalCountProperty");
        lineAwareMapper.setTokenizer(delimitedLineTokenizer);
        lineAwareMapper.setFieldSetMapper(new InboundPaymentIntegrationFieldSetMapper("dd/MM/yyyy"));
    }

    @Test
    public void testMapper() {

        try {
            InboundPaymentIntegration inboundPaymentIntegration = lineAwareMapper.mapLine(
                    "000000001;000000001;000000001;fiscalCode;iban;name;surname;100;result reason;" +
                            "01/07/2021;01/06/2021;1;ORDINE ESEGUITO;17270006101;27/07/2021;technicalProperty",
                    1);
            Assert.assertEquals(getInboundPaymentIntegration(), inboundPaymentIntegration);
            Assert.assertEquals((Integer) 1, inboundPaymentIntegration.getLineNumber());
            Assert.assertEquals("test.csv", inboundPaymentIntegration.getFilename());
        } catch (Exception exception) {
            exception.printStackTrace();
            Assert.fail();
        }

    }

    @SneakyThrows
    @Test
    public void testMapper_KO() {

        expectedException.expect(FlatFileParseException.class);
        lineAwareMapper.mapLine(
                "KO;resultReason;17270006101;27/07/2021",
                1);

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