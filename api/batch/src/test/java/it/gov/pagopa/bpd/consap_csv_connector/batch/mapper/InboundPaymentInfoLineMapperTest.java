package it.gov.pagopa.bpd.consap_csv_connector.batch.mapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentInfo;
import lombok.SneakyThrows;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

public class InboundPaymentInfoLineMapperTest {

    public InboundPaymentInfoLineMapperTest(){
        MockitoAnnotations.initMocks(this);
    }

    @BeforeClass
    public static void configTest() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
        ((Logger) LoggerFactory.getLogger("eu.sia")).setLevel(Level.DEBUG);
    }

    private InboundPaymentInfoLineMapper<InboundPaymentInfo> lineAwareMapper;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        lineAwareMapper = new InboundPaymentInfoLineMapper<>();
        lineAwareMapper.setFilename("test.csv");
        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setDelimiter(";");
        delimitedLineTokenizer.setNames(
                "uniqueID", "result", "resultReason", "cro", "executionDate");
        lineAwareMapper.setTokenizer(delimitedLineTokenizer);
        lineAwareMapper.setFieldSetMapper(new InboundPaymentInfoFieldSetMapper("MM/dd/yyyy HH:mm:ss"));
    }

    @Test
    public void testMapper() {

        try {
            InboundPaymentInfo inboundPaymentInfo = lineAwareMapper.mapLine(
                    "000000001;KO;resultReason;17270006101;27/07/2021",
                    1);
            Assert.assertEquals(getInboundTransaction(), inboundPaymentInfo);
            Assert.assertEquals((Integer) 1, inboundPaymentInfo.getLineNumber());
            Assert.assertEquals("test.csv", inboundPaymentInfo.getFilename());
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
                ";KO;resultReason;17270006101;27/07/2021",
                1);

    }

    public InboundPaymentInfo getInboundTransaction() {
        return InboundPaymentInfo.builder()
                .uniqueID("000000001")
                .result("KO")
                .resultReason("resultReason")
                .cro("17270006101")
                .executionDate("27/07/2021")
                .filename("test.csv")
                .lineNumber(1)
                .build();
    }

}