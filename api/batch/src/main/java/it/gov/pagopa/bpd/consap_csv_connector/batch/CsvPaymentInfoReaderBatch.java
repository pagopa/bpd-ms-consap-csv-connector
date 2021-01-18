package it.gov.pagopa.bpd.consap_csv_connector.batch;

import it.gov.pagopa.bpd.consap_csv_connector.batch.encryption.exception.PGPDecryptException;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.PaymentInfoItemProcessListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.PaymentInfoItemReaderListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.PaymentInfoItemWriterListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.PaymentInfoReaderStepListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.InboundPaymentInfoFieldSetMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.InboundPaymentInfoLineMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentInfo;
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
 * to be processed in instances of PaymentInfo class, to be sent in an outbound Kafka channel
 */

@Data
@Configuration
@PropertySource("classpath:config/csvPaymentInfoReaderBatch.properties")
@EnableBatchProcessing
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class CsvPaymentInfoReaderBatch {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final BeanFactory beanFactory;
    private AtomicInteger batchRunCounter = new AtomicInteger(0);

    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.job.name}")
    private String jobName;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.isolationForCreate}")
    private String isolationForCreate;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.classpath}")
    private String directoryPath;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.successArchivePath}")
    private String successArchivePath;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.errorArchivePath}")
    private String errorArchivePath;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.secretKeyPath}")
    private String secretKeyPath;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.passphrase}")
    private String passphrase;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.applyHashing}")
    private Boolean applyHashing;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.applyDecrypt}")
    private Boolean applyDecrypt;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.partitionerSize}")
    private Integer partitionerSize;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.chunkSize}")
    private Integer chunkSize;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.partitionerMaxPoolSize}")
    private Integer partitionerMaxPoolSize;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.partitionerCorePoolSize}")
    private Integer partitionerCorePoolSize;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.readerMaxPoolSize}")
    private Integer readerMaxPoolSize;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.readerCorePoolSize}")
    private Integer readerCorePoolSize;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.skipLimit}")
    private Integer skipLimit;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.linesToSkip}")
    private Integer linesToSkip;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.timestampPattern}")
    private String timestampPattern;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.tablePrefix}")
    private String tablePrefix;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.errorLogsPath}")
    private String errorLogsPath;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.enableOnReadErrorFileLogging}")
    private Boolean enableOnReadErrorFileLogging;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.enableOnReadErrorLogging}")
    private Boolean enableOnReadErrorLogging;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.enableOnProcessErrorFileLogging}")
    private Boolean enableOnProcessErrorFileLogging;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.enableOnProcessErrorLogging}")
    private Boolean enableOnProcessErrorLogging;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.enableOnWriteErrorFileLogging}")
    private Boolean enableOnWriteErrorFileLogging;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.enableOnWriteErrorLogging}")
    private Boolean enableOnWriteErrorLogging;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.executorPoolSize}")
    private Integer executorPoolSize;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.checkpointFrequency}")
    private Integer checkpointFrequency;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.enableCheckpointFrequency}")
    private Boolean enableCheckpointFrequency;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.applyEncrypt}")
    private Boolean applyEncrypt;
    @Value("${batchConfiguration.CsvPaymentInfoReaderBatch.publicKeyPath}")
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
     * PaymentInfoItemProcessListener
     * PaymentInfoItemReaderListener
     * PaymentInfoItemWriterListener
     * PaymentInfoReaderStepListener method used to launch the configured batch job for processing payment info
     * from a defined directory.
     * The scheduler is based on a cron execution, based on the provided configuration
     * @throws  Exception
     */
    @Scheduled(cron = "${batchConfiguration.CsvPaymentInfoReaderBatch.cron}")
    public void launchJob() throws Exception {

        Date startDate = new Date();
        log.info("CsvPaymentInfoReader scheduled job started at {}", startDate);

        if (writerTrackerService == null) {
            createWriterTrackerService();
        }

        paymentInfoJobLauncher().run(
                job(), new JobParametersBuilder()
                        .addDate("startDateTime", startDate)
                        .toJobParameters());

        clearWriterTrackerService();

        Date endDate = new Date();

        log.info("CsvPaymentInfoReader scheduled job ended at {}" , endDate);
        log.info("Completed in: {} (ms)", + (endDate.getTime() - startDate.getTime()));

    }

    /**
     *
     * @return configured instance of TransactionManager
     */
    @Bean
    public PlatformTransactionManager getTransactionManager() {
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
    public JobRepository getJobRepository() throws Exception {
            JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
            jobRepositoryFactoryBean.setTransactionManager( getTransactionManager());
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
    public JobLauncher paymentInfoJobLauncher() throws Exception {
        SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
        simpleJobLauncher.setJobRepository(getJobRepository());
        return simpleJobLauncher;
    }

    /**
     *
     * @return instance of the LineTokenizer to be used in the itemReader configured for the job
     */
    @Bean
    public LineTokenizer paymentInfoLineTokenizer() {
        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setDelimiter(";");
        delimitedLineTokenizer.setNames(
                "uniqueID", "result", "resultReason", "cro", "executionDate");
        return delimitedLineTokenizer;
    }

    /**
     *
     * @return instance of the FieldSetMapper to be used in the itemReader configured for the job
     */
    @Bean
    public FieldSetMapper<InboundPaymentInfo> paymentInfoFieldSetMapper() {
        return new InboundPaymentInfoFieldSetMapper(timestampPattern);
    }

    /**
     *
     * @return instance of the LineMapper to be used in the itemReader configured for the job
     */
    public LineMapper<InboundPaymentInfo> paymentInfoLineMapper(String file) {
        InboundPaymentInfoLineMapper lineMapper = new InboundPaymentInfoLineMapper();
        lineMapper.setTokenizer(paymentInfoLineTokenizer());
        lineMapper.setFilename(file);
        lineMapper.setFieldSetMapper(paymentInfoFieldSetMapper());
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
    public FlatFileItemReader<InboundPaymentInfo> paymentInfoItemReader(
            @Value("#{stepExecutionContext['fileName']}") String file) {
        PGPFlatFileItemReader flatFileItemReader = new PGPFlatFileItemReader(secretKeyPath, passphrase, applyDecrypt);
        flatFileItemReader.setResource(new UrlResource(file));
        flatFileItemReader.setLineMapper(paymentInfoLineMapper(file));
        flatFileItemReader.setLinesToSkip(linesToSkip);
        return flatFileItemReader;
    }

    /**
     *
     * @return instance of the itemProcessor to be used in the first step of the configured job
     */
    @Bean
    @StepScope
    public ItemProcessor<InboundPaymentInfo, InboundPaymentInfo> paymentInfoItemProcessor() {
        return beanFactory.getBean(InboundPaymentInfoItemProcessor.class);
    }

    /**
     *
     * @return instance of the itemWriter to be used in the first step of the configured job
     */
    @Bean
    @StepScope
    public ItemWriter<InboundPaymentInfo> getItemWriter(PaymentInfoItemWriterListener writerListener) {
        PaymentInfoWriter paymentInfoWriter = beanFactory.getBean(PaymentInfoWriter.class, writerTrackerService);
        paymentInfoWriter.setPaymentInfoItemWriterListener(writerListener);
        paymentInfoWriter.setApplyHashing(applyHashing);
        paymentInfoWriter.setExecutor(writerExecutor());
        paymentInfoWriter.setCheckpointFrequency(checkpointFrequency);
        paymentInfoWriter.setEnableCheckpointFrequency(enableCheckpointFrequency);
        return paymentInfoWriter;
    }

    /**
     *
     * @return step instance based on the tasklet to be used for file archival at the end of the reading process
     */
    @Bean
    public Step terminationTask() {
        if (writerTrackerService == null) {
            createWriterTrackerService();
        }
        TerminationTasklet terminationTasklet = new TerminationTasklet(writerTrackerService);
        return stepBuilderFactory.get("csv-success-termination-step").tasklet(terminationTasklet).build();
    }


    /**
     *
     * @return step instance based on the tasklet to be used for file archival at the end of the reading process
     */
    @Bean
    public Step archivalTask() {
        ArchivalTasklet archivalTasklet = new ArchivalTasklet();
        archivalTasklet.setSuccessPath(successArchivePath);
        archivalTasklet.setErrorPath(errorArchivePath);
        archivalTasklet.setApplyEncrypt(false);
        archivalTasklet.setErrorDir(errorLogsPath);
        archivalTasklet.setPublicKeyDir(publicKey);
        archivalTasklet.setApplyArchive(false);
        return stepBuilderFactory.get("csv-success-archive-step").tasklet(archivalTasklet).build();
    }

    /**
     *
     * @return instance of the job to process and archive .pgp files containing PaymentInfo data in csv format
     */
    public FlowJobBuilder paymentInfoJobBuilder() throws Exception {
        return jobBuilderFactory.get(jobName)
                .repository(getJobRepository())
                .start(masterStep()).on("*").to(archivalTask())
                .build();
    }

    /**
     *
     * @return instance of a partitioner to be used for processing multiple files from a single directory
     * @throws Exception
     */
    @Bean
    @JobScope
    public Partitioner partitioner() throws IOException {
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
    public Step masterStep() throws IOException {
        return stepBuilderFactory.get("csv-payment-info-connector-master-step")
                .partitioner(workerStep(writerTrackerService))
                .partitioner("partition", partitioner())
                .taskExecutor(partitionerTaskExecutor()).build();
    }

    /**
     *
     * @return worker step, defined as a standard reader/processor/writer process,
     * using chunk processing for scalability
     * @throws Exception
     */
    @Bean
    public TaskletStep workerStep(WriterTrackerService writerTrackerService) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String executionDate = OffsetDateTime.now().format(fmt);

        return stepBuilderFactory.get("csv-payment-info-connector-master-inner-step").<InboundPaymentInfo, InboundPaymentInfo>chunk(chunkSize)
                .reader(paymentInfoItemReader(null))
                .processor(paymentInfoItemProcessor())
                .writer(getItemWriter(paymentInfoItemWriteListener(executionDate)))
                .faultTolerant()
                .skipLimit(skipLimit)
                .noSkip(PGPDecryptException.class)
                .noSkip(FileNotFoundException.class)
                .skip(Exception.class)
                .listener(paymentInfoItemReaderListener(executionDate))
                .listener(paymentInfoItemWriteListener(executionDate))
                .listener(paymentInfoItemProcessListener(executionDate))
                .listener(paymentInfoStepListener(writerTrackerService))
                .taskExecutor(readerTaskExecutor())
                .build();
    }

    @Bean
    public PaymentInfoItemReaderListener paymentInfoItemReaderListener(String executionDate) {
        PaymentInfoItemReaderListener paymentInfoItemReaderListener = new PaymentInfoItemReaderListener();
        paymentInfoItemReaderListener.setExecutionDate(executionDate);
        paymentInfoItemReaderListener.setErrorPaymentInfosLogsPath(errorLogsPath);
        paymentInfoItemReaderListener.setEnableOnErrorFileLogging(enableOnReadErrorFileLogging);
        paymentInfoItemReaderListener.setEnableOnErrorLogging(enableOnReadErrorLogging);
        return paymentInfoItemReaderListener;
    }

    @Bean
    public PaymentInfoItemWriterListener paymentInfoItemWriteListener(String executionDate) {
        PaymentInfoItemWriterListener paymentInfoItemWriterListener = new PaymentInfoItemWriterListener();
        paymentInfoItemWriterListener.setExecutionDate(executionDate);
        paymentInfoItemWriterListener.setErrorPaymentInfosLogsPath(errorLogsPath);
        paymentInfoItemWriterListener.setEnableOnErrorFileLogging(enableOnWriteErrorFileLogging);
        paymentInfoItemWriterListener.setEnableOnErrorLogging(enableOnWriteErrorLogging);
        return paymentInfoItemWriterListener;
    }

    @Bean
    public PaymentInfoItemProcessListener paymentInfoItemProcessListener(String executionDate) {
        PaymentInfoItemProcessListener paymentInfoItemProcessListener = new PaymentInfoItemProcessListener();
        paymentInfoItemProcessListener.setExecutionDate(executionDate);
        paymentInfoItemProcessListener.setErrorPaymentInfosLogsPath(errorLogsPath);
        paymentInfoItemProcessListener.setEnableOnErrorFileLogging(enableOnProcessErrorFileLogging);
        paymentInfoItemProcessListener.setEnableOnErrorLogging(enableOnProcessErrorLogging);
        return paymentInfoItemProcessListener;
    }

    @Bean
    public PaymentInfoReaderStepListener paymentInfoStepListener(WriterTrackerService writerTrackerService) {
        PaymentInfoReaderStepListener paymentInfoReaderStepListener = new PaymentInfoReaderStepListener();
        paymentInfoReaderStepListener.setErrorPath(errorArchivePath);
        paymentInfoReaderStepListener.setSuccessPath(successArchivePath);
        paymentInfoReaderStepListener.setWriterTrackerService(writerTrackerService);
        paymentInfoReaderStepListener.setApplyEncrypt(applyEncrypt);
        paymentInfoReaderStepListener.setErrorDir(errorLogsPath);
        paymentInfoReaderStepListener.setPublicKeyDir(publicKey);
        return paymentInfoReaderStepListener;
    }

    /**
     *
     * @return bean configured for usage in the partitioner instance of the job
     */
    @Bean
    public TaskExecutor partitionerTaskExecutor() {
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
    public Executor writerExecutor() {
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
     * @return instance of a job for payment info processing
     */
    @SneakyThrows
    @Bean
    public Job job() {
        return paymentInfoJobBuilder().build();
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
