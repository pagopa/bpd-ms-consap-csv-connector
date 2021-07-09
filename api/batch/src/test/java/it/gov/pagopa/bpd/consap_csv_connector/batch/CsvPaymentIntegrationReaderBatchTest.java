package it.gov.pagopa.bpd.consap_csv_connector.batch;

/**
 * Class for testing the CsvPaymentInfoReaderBatch class
 */
//@RunWith(SpringRunner.class)
//@SpringBatchTest
//@EmbeddedKafka(
//        partitions = 1,
//        count = 1,
//        controlledShutdown = true
//)
//@DataJpaTest
//@Transactional(propagation = NOT_SUPPORTED)
//@Sql({
//        "classpath:org/springframework/batch/core/schema-drop-hsqldb.sql",
//        "classpath:org/springframework/batch/core/schema-hsqldb.sql"})
//@EnableAutoConfiguration
//@ContextConfiguration(classes = {
//        CsvPaymentIntegrationReaderTestConfig.class,
//        JacksonAutoConfiguration.class,
//        AuthenticationConfiguration.class,
//        KafkaAutoConfiguration.class,
//        ArchEventConfigurationService.class,
//        PropertiesManager.class,
//        KafkaAutoConfiguration.class,
//        SimpleEventRequestTransformer.class,
//        SimpleEventResponseTransformer.class,
//        FeignAutoConfiguration.class,
//        HttpMessageConvertersAutoConfiguration.class,
//        CsvPaymentIntegrationReaderBatch.class
//})
//@TestPropertySource(
//        locations = {
//                "classpath:config/testCsvPaymentIntegrationPublisher.properties",
//        },
//        properties = {
//                "spring.main.allow-bean-definition-overriding=true",
//                "batchConfiguration.CsvPaymentIntegrationReaderBatch.applyHashing=true",
//                "batchConfiguration.CsvPaymentIntegrationReaderBatch.applyDecrypt=true",
//                "batchConfiguration.CsvPaymentIntegrationReaderBatch.secretKeyPath=classpath:/test-encrypt-integr/secretKey.asc",
//                "batchConfiguration.CsvPaymentIntegrationReaderBatch.passphrase=test",
//                "batchConfiguration.CsvPaymentIntegrationReaderBatch.skipLimit=3",
//                "batchConfiguration.CsvPaymentIntegrationReaderBatch.partitionerMaxPoolSize=1",
//                "batchConfiguration.CsvPaymentIntegrationReaderBatch.partitionerCorePoolSize=1",
//                "batchConfiguration.CsvPaymentIntegrationReaderBatch.readerMaxPoolSize=1",
//                "batchConfiguration.CsvPaymentIntegrationReaderBatch.readerCorePoolSize=1",
//                "batchConfiguration.CsvPaymentIntegrationReaderBatch.classpath=classpath:/test-encrypt-integr/**/*.pgp",
//                "batchConfiguration.CsvPaymentIntegrationReaderBatch.successArchivePath=classpath:/test-encrypt-integr/**/success",
//                "batchConfiguration.CsvPaymentIntegrationReaderBatch.errorArchivePath=classpath:/test-encrypt-integr/**/error",
//                "batchConfiguration.CsvPaymentIntegrationReaderBatch.timestampPattern=dd/MM/yyyy",
//                "batchConfiguration.CsvPaymentIntegrationReaderBatch.linesToSkip=0",
//                "connectors.eventConfigurations.items.CsvPaymentIntegrationPublisherConnector.bootstrapServers=${spring.embedded.kafka.brokers}"
//        })
public class CsvPaymentIntegrationReaderBatchTest {

//    @Autowired
//    ArchEventConfigurationService archEventConfigurationService;
//    @Autowired
//    private EmbeddedKafkaBroker kafkaBroker;
//    @Autowired
//    private ObjectMapper objectMapper;
//    @Autowired
//    private KafkaTemplate<String, String> template;
//    @Value("${spring.embedded.kafka.brokers}")
//    private String bootstrapServers;
//
//    @Autowired
//    private JobLauncherTestUtils jobLauncherTestUtils;
//
//    @Autowired
//    private JobRepositoryTestUtils jobRepositoryTestUtils;
//
//    @SpyBean
//    private CsvPaymentIntegrationPublisherConnector csvPaymentIntegrationPublisherConnectorSpy;
//
//    @SpyBean
//    private CsvPaymentIntegrationPublisherService csvPaymentIntegrationPublisherServiceSpy;
//
//    @Rule
//    public TemporaryFolder tempFolder = new TemporaryFolder(
//            new File(getClass().getResource("/test-encrypt-integr").getFile()));
//
//
//    @SneakyThrows
//    @Before
//    public void setUp() {
//        Mockito.reset(
//                csvPaymentIntegrationPublisherConnectorSpy,
//                csvPaymentIntegrationPublisherServiceSpy);
//        ObjectName kafkaServerMbeanName = new ObjectName("kafka.server:type=app-info,id=0");
//        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
//        if (mBeanServer.isRegistered(kafkaServerMbeanName)) {
//            mBeanServer.unregisterMBean(kafkaServerMbeanName);
//        }
//        tempFolder.newFolder("success");
//        tempFolder.newFolder("error");
//    }
//
//    private JobParameters defaultJobParameters() {
//        return new JobParametersBuilder()
//                .addDate("startDateTime",  new Date())
//                .toJobParameters();
//    }
//
//    @Test
//    public void testJob_KO_WrongKey() {
//        try {
//
//            File testTrxPgp = tempFolder.newFile("wrong-test-trx.pgp");
//            FileOutputStream textTrxPgpFOS = new FileOutputStream(testTrxPgp);
//
//            PGPDecryptUtil.encryptFile(textTrxPgpFOS,
//                    this.getClass().getResource("/test-encrypt-integr").getFile() + "/test-trx.csv",
//                    PGPDecryptUtil.readPublicKey(
//                            this.getClass().getResourceAsStream("/test-encrypt-integr/otherPublicKey.asc")),
//                    false,false);
//
//            textTrxPgpFOS.close();
//
//            JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
//            Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
//
//            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//
//            Collection<File> fileSuccList = FileUtils.listFiles(
//                    resolver.getResources("classpath:/test-encrypt-integr/**/success")[0].getFile(),
//                    new String[]{"pgp"},false);
//            Collection<File> fileErrList = FileUtils.listFiles(
//                    resolver.getResources("classpath:/test-encrypt-integr/**/error")[0].getFile(),
//                    new String[]{"pgp"},false);
//
//            Assert.assertEquals(0,
//                    FileUtils.listFiles(
//                            resolver.getResources("classpath:/test-encrypt-integr/**/success")[0].getFile(),
//                            new String[]{"pgp"},false).size());
//            Assert.assertEquals(1,
//                    FileUtils.listFiles(
//                            resolver.getResources("classpath:/test-encrypt-integr/**/error")[0].getFile(),
//                            new String[]{"pgp"},false).size());
//
//            Mockito.verifyZeroInteractions(csvPaymentIntegrationPublisherServiceSpy);
//            Mockito.verifyZeroInteractions(csvPaymentIntegrationPublisherConnectorSpy);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//    }
//
//    @Test
//    public void testJob_Ok_FileWithSkipsOnLimit() {
//        try {
//
//            File testTrxPgp = tempFolder.newFile("test-trx.pgp");
//
//            FileOutputStream textTrxPgpFOS = new FileOutputStream(testTrxPgp);
//
//            PGPDecryptUtil.encryptFile(textTrxPgpFOS,
//                    this.getClass().getResource("/test-encrypt-integr").getFile() + "/test-trx.csv",
//                    PGPDecryptUtil.readPublicKey(
//                            this.getClass().getResourceAsStream("/test-encrypt-integr/publicKey.asc")),
//                    false,false);
//
//            textTrxPgpFOS.close();
//
//            JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
//            Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
//
//            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//            Assert.assertEquals(1,
//                    FileUtils.listFiles(
//                            resolver.getResources("classpath:/test-encrypt-integr/**/success")[0].getFile(),
//                            new String[]{"pgp"},false).size());
//            Assert.assertEquals(0,
//                    FileUtils.listFiles(
//                            resolver.getResources("classpath:/test-encrypt-integr/**/error")[0].getFile(),
//                            new String[]{"pgp"},false).size());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//    }
//
//    @Test
//    public void testJob_KO_FileOverSkipLimit() {
//        try {
//
//            File testTrxPgp = tempFolder.newFile("test-err-trx.pgp");
//
//            FileOutputStream textTrxPgpFOS = new FileOutputStream(testTrxPgp);
//
//            PGPDecryptUtil.encryptFile(textTrxPgpFOS,
//                    this.getClass().getResource("/test-encrypt-integr").getFile() + "/test-err-trx.csv",
//                    PGPDecryptUtil.readPublicKey(
//                            this.getClass().getResourceAsStream("/test-encrypt-integr/publicKey.asc")),
//                    false,false);
//
//            textTrxPgpFOS.close();
//
//            JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
//            Assert.assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
//
//            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//            Collection<File> fileSuccList = FileUtils.listFiles(
//                    resolver.getResources("classpath:/test-encrypt-integr/**/success")[0].getFile(),
//                    new String[]{"pgp"},false);
//            Collection<File> fileErrList = FileUtils.listFiles(
//                    resolver.getResources("classpath:/test-encrypt-integr/**/error")[0].getFile(),
//                    new String[]{"pgp"},false);
//            Assert.assertEquals(0,
//                    FileUtils.listFiles(
//                            resolver.getResources("classpath:/test-encrypt-integr/**/success")[0].getFile(),
//                            new String[]{"pgp"},false).size());
//            Assert.assertEquals(1,
//                    FileUtils.listFiles(
//                            resolver.getResources("classpath:/test-encrypt-integr/**/error")[0].getFile(),
//                            new String[]{"pgp"},false).size());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Assert.fail();
//        }
//    }
//
//    @After
//    public void tearDown() {
//        tempFolder.delete();
//    }

}