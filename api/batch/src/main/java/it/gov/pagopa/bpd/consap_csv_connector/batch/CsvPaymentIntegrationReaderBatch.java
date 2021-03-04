package it.gov.pagopa.bpd.consap_csv_connector.batch;

import it.gov.pagopa.bpd.consap_csv_connector.batch.encryption.exception.PGPDecryptException;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.PaymentIntegrationItemProcessListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.PaymentIntegrationItemReaderListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.PaymentIntegrationItemWriterListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.PaymentIntegrationReaderStepListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.InboundPaymentIntegrationFieldSetMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.InboundPaymentIntegrationLineMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentIntegration;
import it.gov.pagopa.bpd.consap_csv_connector.batch.step.*;
import it.gov.pagopa.bpd.consap_csv_connector.service.WriterTrackerService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configuration of a scheduled batch job to read and decrypt .pgp files with csv content,
 * to be processed in instances of PaymentIntegration class, to be sent in an outbound Kafka channel
 */

@Data
@Configuration
@PropertySource("classpath:config/csvPaymentIntegrationReaderBatch.properties")
@EnableBatchProcessing
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class CsvPaymentIntegrationReaderBatch {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final BeanFactory beanFactory;
    private AtomicInteger batchRunCounter = new AtomicInteger(0);

    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.job.name}")
    private String jobName;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.isolationForCreate}")
    private String isolationForCreate;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.classpath}")
    private String directoryPath;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.successArchivePath}")
    private String successArchivePath;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.errorArchivePath}")
    private String errorArchivePath;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.secretKeyPath}")
    private String secretKeyPath;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.passphrase}")
    private String passphrase;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.applyHashing}")
    private Boolean applyHashing;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.applyDecrypt}")
    private Boolean applyDecrypt;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.partitionerSize}")
    private Integer partitionerSize;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.chunkSize}")
    private Integer chunkSize;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.partitionerMaxPoolSize}")
    private Integer partitionerMaxPoolSize;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.partitionerCorePoolSize}")
    private Integer partitionerCorePoolSize;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.readerMaxPoolSize}")
    private Integer readerMaxPoolSize;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.readerCorePoolSize}")
    private Integer readerCorePoolSize;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.skipLimit}")
    private Integer skipLimit;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.linesToSkip}")
    private Integer linesToSkip;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.timestampPattern}")
    private String timestampPattern;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.tablePrefix}")
    private String tablePrefix;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.errorLogsPath}")
    private String errorLogsPath;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.enableOnReadErrorFileLogging}")
    private Boolean enableOnReadErrorFileLogging;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.enableOnReadErrorLogging}")
    private Boolean enableOnReadErrorLogging;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.enableOnProcessErrorFileLogging}")
    private Boolean enableOnProcessErrorFileLogging;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.enableOnProcessErrorLogging}")
    private Boolean enableOnProcessErrorLogging;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.enableOnWriteErrorFileLogging}")
    private Boolean enableOnWriteErrorFileLogging;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.enableOnWriteErrorLogging}")
    private Boolean enableOnWriteErrorLogging;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.executorPoolSize}")
    private Integer executorPoolSize;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.checkpointFrequency}")
    private Integer checkpointFrequency;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.enableCheckpointFrequency}")
    private Boolean enableCheckpointFrequency;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.applyEncrypt}")
    private Boolean applyEncrypt;
    @Value("${batchConfiguration.CsvPaymentIntegrationReaderBatch.publicKeyPath}")
    private String publicKey;

    private DataSource dataSource;
    private WriterTrackerService writerTrackerService;
    private ExecutorService executorService;

    public void createWriterTrackerService() {
        this.writerTrackerService = writerTrackerService();
    }

    public void clearWriterTrackerService() {
        writerTrackerService.clearAll();
    }

    public WriterTrackerService writerTrackerService() {
        return beanFactory.getBean(WriterTrackerService.class);
    }

    /**
     * PaymentIntegrationItemProcessListener
     * PaymentIntegrationItemReaderListener
     * PaymentIntegrationItemWriterListener
     * PaymentIntegrationReaderStepListener method used to launch the configured batch job for processing payment integration
     * from a defined directory.
     * The scheduler is based on a cron execution, based on the provided configuration
     * @throws  Exception
     */
    @Scheduled(cron = "${batchConfiguration.CsvPaymentIntegrationReaderBatch.cron}")
    public void launchPaymentIntegrationJob() throws Exception {

        Date startDate = new Date();
        log.info("CsvPaymentIntegrationReader scheduled job started at {}", startDate);

        if (writerTrackerService == null) {
            createWriterTrackerService();
        }

        paymentIntegrationJobLauncher().run(
                paymentIntegrationJob(), new JobParametersBuilder()
                        .addDate("startDateTime", startDate)
                        .toJobParameters());

        clearWriterTrackerService();

        Date endDate = new Date();

        log.info("CsvPaymentIntegrationReader scheduled job ended at {}" , endDate);
        log.info("Completed in: {} (ms)", + (endDate.getTime() - startDate.getTime()));

    }

    /**
     *
     * @return configured instance of TransactionManager
     */
    @Bean
    public PlatformTransactionManager paymentIntegrationTransactionManager() {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }

    /**
     *
     * @return configured instance of JobRepository
     * @throws Exception
     */
    @Bean
    public JobRepository paymentIntegrationJobRepository() throws Exception {
            JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
            jobRepositoryFactoryBean.setTransactionManager(paymentIntegrationTransactionManager());
            jobRepositoryFactoryBean.setTablePrefix(tablePrefix);
            jobRepositoryFactoryBean.setDataSource(dataSource);
            jobRepositoryFactoryBean.setIsolationLevelForCreate(isolationForCreate);
            jobRepositoryFactoryBean.afterPropertiesSet();
            return jobRepositoryFactoryBean.getObject();
    }

    /**
     *
     * @return configured instance of JobLauncher
     * @throws Exception
     */
    @Bean
    public JobLauncher paymentIntegrationJobLauncher() throws Exception {
        SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
        simpleJobLauncher.setJobRepository(paymentIntegrationJobRepository());
        return simpleJobLauncher;
    }

    /**
     *
     * @return instance of the LineTokenizer to be used in the itemReader configured for the job
     */
    @Bean
    public LineTokenizer paymentIntegrationLineTokenizer() {
        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setDelimiter(";");
        delimitedLineTokenizer.setNames(
                "idConsap", "idReclamo", "idPagoPA", "fiscalCode", "iban", "name", "surname", "cashbackAmount",
                "causale", "periodStart", "periodEnd", "awardPeriodId", "esito", "cro", "executionDate",
                "technicalCountProperty");
        return delimitedLineTokenizer;
    }

    /**
     *
     * @return instance of the FieldSetMapper to be used in the itemReader configured for the job
     */
    @Bean
    public FieldSetMapper<InboundPaymentIntegration> paymentIntegrationFieldSetMapper() {
        return new InboundPaymentIntegrationFieldSetMapper(timestampPattern);
    }

    /**
     *
     * @return instance of the LineMapper to be used in the itemReader configured for the job
     */
    public LineMapper<InboundPaymentIntegration> paymentIntegrationLineMapper(String file) {
        InboundPaymentIntegrationLineMapper lineMapper = new InboundPaymentIntegrationLineMapper();
        lineMapper.setTokenizer(paymentIntegrationLineTokenizer());
        lineMapper.setFilename(file);
        lineMapper.setFieldSetMapper(paymentIntegrationFieldSetMapper());
        return lineMapper;
    }

    /**
     *
     * @param file
     *          Late-Binding parameter to be used as the resource for the reader instance
     * @return instance of the itemReader to be used in the first step of the configured job
     */
    @SneakyThrows
    @Bean
    @StepScope
    public FlatFileItemReader<InboundPaymentIntegration> paymentIntegrationItemReader(
            @Value("#{stepExecutionContext['fileName']}") String file) {
        PGPFlatFileItemReader flatFileItemReader = new PGPFlatFileItemReader(secretKeyPath, passphrase, applyDecrypt);
        flatFileItemReader.setResource(new UrlResource(file));
        flatFileItemReader.setLineMapper(paymentIntegrationLineMapper(file));
        flatFileItemReader.setLinesToSkip(linesToSkip);
        return flatFileItemReader;
    }

    /**
     *
     * @return instance of the itemProcessor to be used in the first step of the configured job
     */
    @Bean
    @StepScope
    public ItemProcessor<InboundPaymentIntegration,
            InboundPaymentIntegration> paymentIntegrationItemProcessor() {
        return beanFactory.getBean(InboundPaymentIntegrationProcessor.class);
    }

    /**
     *
     * @return instance of the itemWriter to be used in the first step of the configured job
     */
    @Bean
    @StepScope
    public ItemWriter<InboundPaymentIntegration> paymentIntegrationItemWriter(
            PaymentIntegrationItemWriterListener writerListener) {
        PaymentIntegrationWriter paymentIntegrationWriter = beanFactory.getBean(
                PaymentIntegrationWriter.class, writerTrackerService);
        paymentIntegrationWriter.setPaymentIntegrationItemWriterListener(writerListener);
        paymentIntegrationWriter.setApplyHashing(applyHashing);
        paymentIntegrationWriter.setExecutor(paymentIntegrationWriterExecutor());
        paymentIntegrationWriter.setCheckpointFrequency(checkpointFrequency);
        paymentIntegrationWriter.setEnableCheckpointFrequency(enableCheckpointFrequency);
        return paymentIntegrationWriter;
    }

    /**
     *
     * @return step instance based on the tasklet to be used for file archival at the end of the reading process
     */
    @Bean
    public Step paymentIntegrationTerminationTask() {
        if (writerTrackerService == null) {
            createWriterTrackerService();
        }
        TerminationTasklet terminationTasklet = new TerminationTasklet(writerTrackerService);
        return stepBuilderFactory.get("csv-payment-integration-success-termination-step")
                .tasklet(terminationTasklet).build();
    }


    /**
     *
     * @return step instance based on the tasklet to be used for file archival at the end of the reading process
     */
    @Bean
    public Step paymentIntegrationArchivalTask() {
        ArchivalTasklet archivalTasklet = new ArchivalTasklet();
        archivalTasklet.setSuccessPath(successArchivePath);
        archivalTasklet.setErrorPath(errorArchivePath);
        archivalTasklet.setApplyEncrypt(false);
        archivalTasklet.setErrorDir(errorLogsPath);
        archivalTasklet.setPublicKeyDir(publicKey);
        archivalTasklet.setApplyArchive(false);
        return stepBuilderFactory.get("csv-payment-integration-success-archive-step")
                .tasklet(archivalTasklet).build();
    }

    /**
     *
     * @return instance of the job to process and archive .pgp files containing PaymentIntegration data in csv format
     */
    public FlowJobBuilder paymentIntegrationJobBuilder() throws Exception {
        return jobBuilderFactory.get(jobName)
                .repository(paymentIntegrationJobRepository())
                .start(paymentIntegrationMasterStep()).on("*").to(paymentIntegrationArchivalTask())
                .build();
    }

    /**
     *
     * @return instance of a partitioner to be used for processing multiple files from a single directory
     * @throws Exception
     */
    @Bean
    @JobScope
    public Partitioner paymentIntegrationPartitioner() throws IOException {
        MultiResourcePartitioner partitioner = new MultiResourcePartitioner();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        partitioner.setResources(resolver.getResources(directoryPath));
        partitioner.partition(partitionerSize);
        return partitioner;
    }

    /**
     *
     * @return master step to be used as the formal main step in the reading phase of the job,
     * partitioned for scalability on multiple file reading
     * @throws Exception
     */
    @Bean
    public Step paymentIntegrationMasterStep() throws IOException {
        return stepBuilderFactory.get("csv-payment-integration-connector-master-step")
                .partitioner(paymentIntegrationWorkerStep(writerTrackerService))
                .partitioner("partition", paymentIntegrationPartitioner())
                .taskExecutor(paymentIntegrationPartitionerTaskExecutor()).build();
    }

    /**
     *
     * @return worker step, defined as a standard reader/processor/writer process,
     * using chunk processing for scalability
     * @throws Exception
     */
    @Bean
    public TaskletStep paymentIntegrationWorkerStep(WriterTrackerService writerTrackerService) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String executionDate = OffsetDateTime.now().format(fmt);

        return stepBuilderFactory.get("csv-payment-integration-connector-master-inner-step")
                .<InboundPaymentIntegration, InboundPaymentIntegration>chunk(chunkSize)
                .reader(paymentIntegrationItemReader(null))
                .processor(paymentIntegrationItemProcessor())
                .writer(paymentIntegrationItemWriter(paymentIntegrationItemWriteListener(executionDate)))
                .faultTolerant()
                .skipLimit(skipLimit)
                .noSkip(PGPDecryptException.class)
                .noSkip(FileNotFoundException.class)
                .skip(Exception.class)
                .listener(paymentIntegrationItemReaderListener(executionDate))
                .listener(paymentIntegrationItemWriteListener(executionDate))
                .listener(paymentIntegrationItemProcessListener(executionDate))
                .listener(paymentIntegrationStepListener(writerTrackerService))
                .taskExecutor(readerTaskExecutor())
                .build();
    }

    @Bean
    public PaymentIntegrationItemReaderListener paymentIntegrationItemReaderListener(String executionDate) {
        PaymentIntegrationItemReaderListener paymentIntegrationItemReaderListener = new PaymentIntegrationItemReaderListener();
        paymentIntegrationItemReaderListener.setExecutionDate(executionDate);
        paymentIntegrationItemReaderListener.setErrorPaymentIntegrationLogsPath(errorLogsPath);
        paymentIntegrationItemReaderListener.setEnableOnErrorFileLogging(enableOnReadErrorFileLogging);
        paymentIntegrationItemReaderListener.setEnableOnErrorLogging(enableOnReadErrorLogging);
        return paymentIntegrationItemReaderListener;
    }

    @Bean
    public PaymentIntegrationItemWriterListener paymentIntegrationItemWriteListener(String executionDate) {
        PaymentIntegrationItemWriterListener paymentIntegrationItemWriterListener =
                new PaymentIntegrationItemWriterListener();
        paymentIntegrationItemWriterListener.setExecutionDate(executionDate);
        paymentIntegrationItemWriterListener.setErrorPaymentIntegrationLogsPath(errorLogsPath);
        paymentIntegrationItemWriterListener.setEnableOnErrorFileLogging(enableOnWriteErrorFileLogging);
        paymentIntegrationItemWriterListener.setEnableOnErrorLogging(enableOnWriteErrorLogging);
        return paymentIntegrationItemWriterListener;
    }

    @Bean
    public PaymentIntegrationItemProcessListener paymentIntegrationItemProcessListener(String executionDate) {
        PaymentIntegrationItemProcessListener paymentIntegrationItemProcessListener =
                new PaymentIntegrationItemProcessListener();
        paymentIntegrationItemProcessListener.setExecutionDate(executionDate);
        paymentIntegrationItemProcessListener.setErrorPaymentIntegrationLogsPath(errorLogsPath);
        paymentIntegrationItemProcessListener.setEnableOnErrorFileLogging(enableOnProcessErrorFileLogging);
        paymentIntegrationItemProcessListener.setEnableOnErrorLogging(enableOnProcessErrorLogging);
        return paymentIntegrationItemProcessListener;
    }

    @Bean
    public PaymentIntegrationReaderStepListener paymentIntegrationStepListener(
            WriterTrackerService writerTrackerService) {
        PaymentIntegrationReaderStepListener paymentIntegrationReaderStepListener =
                new PaymentIntegrationReaderStepListener();
        paymentIntegrationReaderStepListener.setErrorPath(errorArchivePath);
        paymentIntegrationReaderStepListener.setSuccessPath(successArchivePath);
        paymentIntegrationReaderStepListener.setWriterTrackerService(writerTrackerService);
        paymentIntegrationReaderStepListener.setApplyEncrypt(applyEncrypt);
        paymentIntegrationReaderStepListener.setErrorDir(errorLogsPath);
        paymentIntegrationReaderStepListener.setPublicKeyDir(publicKey);
        return paymentIntegrationReaderStepListener;
    }

    /**
     *
     * @return bean configured for usage in the partitioner instance of the job
     */
    @Bean
    public TaskExecutor paymentIntegrationPartitionerTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(partitionerMaxPoolSize);
        taskExecutor.setCorePoolSize(partitionerCorePoolSize);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

    /**
     *
     * @return bean configured for usage for chunk reading of a single file
     */
    @Bean
    public Executor paymentIntegrationWriterExecutor() {
        if (this.executorService == null) {
            executorService =  Executors.newFixedThreadPool(executorPoolSize);
        }
        return executorService;
    }

    /**
     *
     * @return bean configured for usage for chunk reading of a single file
     */
    public TaskExecutor readerTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(readerMaxPoolSize);
        taskExecutor.setCorePoolSize(readerCorePoolSize);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }


    /**
     *
     * @return instance of a job for payment integration processing
     */
    @SneakyThrows
    @Bean
    public Job paymentIntegrationJob() {
        return paymentIntegrationJobBuilder().build();
    }

    /**
     *
     * @return bean for a ThreadPoolTaskScheduler
     */
    @Bean
    public TaskScheduler poolScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

}
