package it.gov.pagopa.bpd.consap_csv_connector.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.sia.meda.core.properties.PropertiesManager;
import eu.sia.meda.event.configuration.ArchEventConfigurationService;
import eu.sia.meda.event.transformer.SimpleEventRequestTransformer;
import eu.sia.meda.event.transformer.SimpleEventResponseTransformer;
import it.gov.pagopa.bpd.consap_csv_connector.batch.encryption.PGPDecryptUtil;
import it.gov.pagopa.bpd.consap_csv_connector.integration.event.CsvPaymentInfoPublisherConnector;
import it.gov.pagopa.bpd.consap_csv_connector.service.CsvPaymentInfoPublisherService;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.util.Date;

import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

/**
 * Class for testing the CsvPaymentInfoReaderBatch class
 */
@RunWith(SpringRunner.class)
@SpringBatchTest
@EmbeddedKafka(
        partitions = 1,
        count = 1,
        controlledShutdown = true
)
@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
@Sql({
        "classpath:org/springframework/batch/core/schema-drop-hsqldb.sql",
        "classpath:org/springframework/batch/core/schema-hsqldb.sql"})
@EnableAutoConfiguration
@ContextConfiguration(classes = {
        JacksonAutoConfiguration.class,
        AuthenticationConfiguration.class,
        KafkaAutoConfiguration.class,
        ArchEventConfigurationService.class,
        PropertiesManager.class,
        KafkaAutoConfiguration.class,
        SimpleEventRequestTransformer.class,
        SimpleEventResponseTransformer.class,
        FeignAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        CsvPaymentInfoReaderBatch.class
})
@TestPropertySource(
        locations = {
                "classpath:config/testCsvPaymentInfoPublisher.properties",
        },
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "batchConfiguration.CsvPaymentInfoReaderBatch.applyHashing=true",
                "batchConfiguration.CsvPaymentInfoReaderBatch.applyDecrypt=true",
                "batchConfiguration.CsvPaymentInfoReaderBatch.secretKeyPath=classpath:/test-encrypt/secretKey.asc",
                "batchConfiguration.CsvPaymentInfoReaderBatch.passphrase=test",
                "batchConfiguration.CsvPaymentInfoReaderBatch.skipLimit=3",
                "batchConfiguration.CsvPaymentInfoReaderBatch.partitionerMaxPoolSize=1",
                "batchConfiguration.CsvPaymentInfoReaderBatch.partitionerCorePoolSize=1",
                "batchConfiguration.CsvPaymentInfoReaderBatch.readerMaxPoolSize=1",
                "batchConfiguration.CsvPaymentInfoReaderBatch.readerCorePoolSize=1",
                "batchConfiguration.CsvPaymentInfoReaderBatch.classpath=classpath:/test-encrypt/**/*.pgp",
                "batchConfiguration.CsvPaymentInfoReaderBatch.successArchivePath=classpath:/test-encrypt/**/success",
                "batchConfiguration.CsvPaymentInfoReaderBatch.errorArchivePath=classpath:/test-encrypt/**/error",
                "batchConfiguration.CsvPaymentInfoReaderBatch.timestampPattern=MM/dd/yyyy HH:mm:ss",
                "batchConfiguration.CsvPaymentInfoReaderBatch.linesToSkip=0",
                "connectors.eventConfigurations.items.CsvPaymentInfoPublisherConnector.bootstrapServers=${spring.embedded.kafka.brokers}"
        })
public class CsvPaymentInfoReaderBatchTest {

    @Autowired
    ArchEventConfigurationService archEventConfigurationService;
    @Autowired
    private EmbeddedKafkaBroker kafkaBroker;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private KafkaTemplate<String, String> template;
    @Value("${spring.embedded.kafka.brokers}")
    private String bootstrapServers;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @SpyBean
    private CsvPaymentInfoPublisherConnector csvPaymentInfoPublisherConnectorSpy;

    @SpyBean
    private CsvPaymentInfoPublisherService csvPaymentInfoPublisherServiceSpy;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder(
            new File(getClass().getResource("/test-encrypt").getFile()));


    @SneakyThrows
    @Before
    public void setUp() {
        Mockito.reset(
                csvPaymentInfoPublisherConnectorSpy,
                csvPaymentInfoPublisherServiceSpy);
        ObjectName kafkaServerMbeanName = new ObjectName("kafka.server:type=app-info,id=0");
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        if (mBeanServer.isRegistered(kafkaServerMbeanName)) {
            mBeanServer.unregisterMBean(kafkaServerMbeanName);
        }
        tempFolder.newFolder("success");
        tempFolder.newFolder("error");
    }

    private JobParameters defaultJobParameters() {
        return new JobParametersBuilder()
                .addDate("startDateTime",  new Date())
                .toJobParameters();
    }

    @Test
    public void testJob_KO_WrongKey() {
        try {

            File testTrxPgp = tempFolder.newFile("wrong-test-trx.pgp");
            FileOutputStream textTrxPgpFOS = new FileOutputStream(testTrxPgp);

            PGPDecryptUtil.encryptFile(textTrxPgpFOS,
                    this.getClass().getResource("/test-encrypt").getFile() + "/test-trx.csv",
                    PGPDecryptUtil.readPublicKey(
                            this.getClass().getResourceAsStream("/test-encrypt/otherPublicKey.asc")),
                    false,false);

            textTrxPgpFOS.close();

            JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
            Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());

            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Assert.assertEquals(0,
                    FileUtils.listFiles(
                            resolver.getResources("classpath:/test-encrypt/**/success")[0].getFile(),
                            new String[]{"pgp"},false).size());
            Assert.assertEquals(1,
                    FileUtils.listFiles(
                            resolver.getResources("classpath:/test-encrypt/**/error")[0].getFile(),
                            new String[]{"pgp"},false).size());

            Mockito.verifyZeroInteractions(csvPaymentInfoPublisherServiceSpy);
            Mockito.verifyZeroInteractions(csvPaymentInfoPublisherConnectorSpy);

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testJob_Ok_FileWithSkipsOnLimit() {
        try {

            File testTrxPgp = tempFolder.newFile("test-trx.pgp");

            FileOutputStream textTrxPgpFOS = new FileOutputStream(testTrxPgp);

            PGPDecryptUtil.encryptFile(textTrxPgpFOS,
                    this.getClass().getResource("/test-encrypt").getFile() + "/test-trx.csv",
                    PGPDecryptUtil.readPublicKey(
                            this.getClass().getResourceAsStream("/test-encrypt/publicKey.asc")),
                    false,false);

            textTrxPgpFOS.close();

            JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
            Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());

            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Assert.assertEquals(1,
                    FileUtils.listFiles(
                            resolver.getResources("classpath:/test-encrypt/**/success")[0].getFile(),
                            new String[]{"pgp"},false).size());
            Assert.assertEquals(0,
                    FileUtils.listFiles(
                            resolver.getResources("classpath:/test-encrypt/**/error")[0].getFile(),
                            new String[]{"pgp"},false).size());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testJob_KO_FileOverSkipLimit() {
        try {

            File testTrxPgp = tempFolder.newFile("test-err-trx.pgp");

            FileOutputStream textTrxPgpFOS = new FileOutputStream(testTrxPgp);

            PGPDecryptUtil.encryptFile(textTrxPgpFOS,
                    this.getClass().getResource("/test-encrypt").getFile() + "/test-err-trx.csv",
                    PGPDecryptUtil.readPublicKey(
                            this.getClass().getResourceAsStream("/test-encrypt/publicKey.asc")),
                    false,false);

            textTrxPgpFOS.close();

            JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
            Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());

            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Assert.assertEquals(0,
                    FileUtils.listFiles(
                            resolver.getResources("classpath:/test-encrypt/**/success")[0].getFile(),
                            new String[]{"pgp"},false).size());
            Assert.assertEquals(1,
                    FileUtils.listFiles(
                            resolver.getResources("classpath:/test-encrypt/**/error")[0].getFile(),
                            new String[]{"pgp"},false).size());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
        tempFolder.delete();
    }

}