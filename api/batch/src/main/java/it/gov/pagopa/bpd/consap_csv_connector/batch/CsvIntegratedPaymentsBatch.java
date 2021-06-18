package it.gov.pagopa.bpd.consap_csv_connector.batch;

import io.swagger.models.auth.In;
import it.gov.pagopa.bpd.award_winner.integration.event.model.IntegratedPayments;
import it.gov.pagopa.bpd.consap_csv_connector.batch.encryption.exception.PGPDecryptException;
import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.RowMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.integratedPayments.IntegratedCustomMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.Sheet;
//import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.mapping.PassThroughRowMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.poi.PgpPoiItemReader;
import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.poi.PoiItemReader;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.PaymentInfoItemProcessListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.PaymentInfoItemReaderListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.PaymentInfoItemWriterListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.PaymentInfoReaderStepListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.integratedPayments.IntegratedPaymentsItemProcessListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.integratedPayments.IntegratedPaymentsItemReaderListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.integratedPayments.IntegratedPaymentsItemWriterListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.integratedPayments.IntegratedPaymentsReaderStepListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.InboundPaymentInfoFieldSetMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.InboundPaymentInfoLineMapper;
//import it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.integratedPayments.InboundIntegratedPaymentsFieldSetMapper;
//import it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.integratedPayments.InboundIntegratedPaymentsLineMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.integratedPayments.IntegratedCustomMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundIntegratedPayments;
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
 * to be processed in instances of IntegratedPayment class, to be sent in an outbound Kafka channel
 */

@Data
@Configuration
@PropertySource("classpath:config/csvIntegratedPaymentsBatch.properties")
@EnableBatchProcessing
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class CsvIntegratedPaymentsBatch {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final BeanFactory beanFactory;
    private AtomicInteger batchRunCounter = new AtomicInteger(0);

    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.job.name}")
    private String jobName;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.isolationForCreate}")
    private String isolationForCreate;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.classpath}")
    private String directoryPath;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.successArchivePath}")
    private String successArchivePath;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.errorArchivePath}")
    private String errorArchivePath;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.secretKeyPath}")
    private String secretKeyPath;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.passphrase}")
    private String passphrase;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.applyHashing}")
    private Boolean applyHashing;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.applyDecrypt}")
    private Boolean applyDecrypt;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.partitionerSize}")
    private Integer partitionerSize;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.chunkSize}")
    private Integer chunkSize;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.partitionerMaxPoolSize}")
    private Integer partitionerMaxPoolSize;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.partitionerCorePoolSize}")
    private Integer partitionerCorePoolSize;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.readerMaxPoolSize}")
    private Integer readerMaxPoolSize;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.readerCorePoolSize}")
    private Integer readerCorePoolSize;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.skipLimit}")
    private Integer skipLimit;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.linesToSkip}")
    private Integer linesToSkip;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.timestampPattern}")
    private String timestampPattern;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.tablePrefix}")
    private String tablePrefix;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.errorLogsPath}")
    private String errorLogsPath;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.enableOnReadErrorFileLogging}")
    private Boolean enableOnReadErrorFileLogging;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.enableOnReadErrorLogging}")
    private Boolean enableOnReadErrorLogging;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.enableOnProcessErrorFileLogging}")
    private Boolean enableOnProcessErrorFileLogging;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.enableOnProcessErrorLogging}")
    private Boolean enableOnProcessErrorLogging;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.enableOnWriteErrorFileLogging}")
    private Boolean enableOnWriteErrorFileLogging;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.enableOnWriteErrorLogging}")
    private Boolean enableOnWriteErrorLogging;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.executorPoolSize}")
    private Integer executorPoolSize;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.checkpointFrequency}")
    private Integer checkpointFrequency;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.enableCheckpointFrequency}")
    private Boolean enableCheckpointFrequency;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.applyEncrypt}")
    private Boolean applyEncrypt;
    @Value("${batchConfiguration.CsvIntegratedPaymentsBatch.publicKeyPath}")
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
    @Scheduled(cron = "${batchConfiguration.CsvIntegratedPaymentsBatch.cron}")
    public void launchJob() throws Exception {


        Date startDate = new Date();
        log.info("CsvIntegratedPaymentsReader scheduled job started at {}", startDate);

        if (writerTrackerService == null) {
            createWriterTrackerService();
        }

        integratedPaymentJobLauncher().run(
                integratedPaymentJob(), new JobParametersBuilder()
                        .addDate("startDateTime", startDate)
                        .toJobParameters());

        clearWriterTrackerService();

        Date endDate = new Date();

        log.info("CsvIntegratedPaymentsReader scheduled job ended at {}" , endDate);
        log.info("Completed in: {} (ms)", + (endDate.getTime() - startDate.getTime()));

    }

    /**
     *
     * @return configured instance of TransactionManager
     */
    @Bean
    public PlatformTransactionManager getIntegratedPaymentTransactionManager() {
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
    public JobRepository getIntegratedPaymentJobRepository() throws Exception {
        JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
        jobRepositoryFactoryBean.setTransactionManager( getIntegratedPaymentTransactionManager());
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
    public JobLauncher integratedPaymentJobLauncher() throws Exception {
        SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
        simpleJobLauncher.setJobRepository(getIntegratedPaymentJobRepository());
        return simpleJobLauncher;
    }


    @SneakyThrows
    @Bean
    @StepScope
    public PgpPoiItemReader excelReader(
            @Value("#{stepExecutionContext['fileName']}") String file) {
        PgpPoiItemReader flatFileItemReader = new PgpPoiItemReader(secretKeyPath, passphrase, applyDecrypt);
        flatFileItemReader.setLinesToSkip(linesToSkip);
        flatFileItemReader.setResource(new UrlResource(file));
        flatFileItemReader.setRowMapper(rowMapper());
        return flatFileItemReader;
    }

    @Bean
    public RowMapper<InboundIntegratedPayments> rowMapper() {
        return new IntegratedCustomMapper();
    }



    /**
     *
     * @return instance of the itemProcessor to be used in the first step of the configured job
     */
    @Bean
    @StepScope
    public ItemProcessor<InboundIntegratedPayments, InboundIntegratedPayments> integratedPaymentItemProcessor() {
        return beanFactory.getBean(InboundIntegratedPaymentsItemProcessor.class);
    }

    /**
     *
     * @return instance of the itemWriter to be used in the first step of the configured job
     */
    @Bean
    @StepScope
    public ItemWriter<InboundIntegratedPayments> getIntegratedPaymentItemWriter(IntegratedPaymentsItemWriterListener writerListener) {
        IntegratedPaymentsWriter integratedPaymentsWriter = beanFactory.getBean(IntegratedPaymentsWriter.class, writerTrackerService);
        integratedPaymentsWriter.setIntegratedPaymentsItemWriterListener(writerListener);
        integratedPaymentsWriter.setApplyHashing(applyHashing);
        integratedPaymentsWriter.setExecutor(integratedPaymentWriterExecutor());
        integratedPaymentsWriter.setCheckpointFrequency(checkpointFrequency);
        integratedPaymentsWriter.setEnableCheckpointFrequency(enableCheckpointFrequency);
        return integratedPaymentsWriter;
    }

    /**
     *
     * @return step instance based on the tasklet to be used for file archival at the end of the reading process
     */
    @Bean
    public Step integratedPaymentTerminationTask() {
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
    public Step integratedPaymentArchivalTask() {
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
    public FlowJobBuilder integratedPaymentJobBuilder() throws Exception {
        return jobBuilderFactory.get(jobName)
                .repository(getIntegratedPaymentJobRepository())
                .start(integratedPaymentMasterStep()).on("*").to(integratedPaymentArchivalTask())
                .build();
    }

    /**
     *
     * @return instance of a partitioner to be used for processing multiple files from a single directory
     * @throws Exception
     */
    @Bean
    @JobScope
    public Partitioner integratedPaymentPartitioner() throws IOException {
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
    public Step integratedPaymentMasterStep() throws IOException {
        return stepBuilderFactory.get("csv-integrated-payments-connector-master-step")
                .partitioner(integratedPaymentWorkerStep(writerTrackerService))
                .partitioner("partition", integratedPaymentPartitioner())
                .taskExecutor(integratedPaymentPartitionerTaskExecutor()).build();
    }

    /**
     *
     * @return worker step, defined as a standard reader/processor/writer process,
     * using chunk processing for scalability
     * @throws Exception
     */
    @Bean
    public TaskletStep integratedPaymentWorkerStep(WriterTrackerService writerTrackerService) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String executionDate = OffsetDateTime.now().format(fmt);

        return stepBuilderFactory.get("csv-integrated-payments-connector-master-inner-step").<InboundIntegratedPayments, InboundIntegratedPayments>chunk(chunkSize)
                .reader(excelReader(null))
                .processor(integratedPaymentItemProcessor())
                .writer(getIntegratedPaymentItemWriter(integratedPaymentWriteListener(executionDate)))
                .faultTolerant()
                .skipLimit(skipLimit)
                .noSkip(PGPDecryptException.class)
                .noSkip(FileNotFoundException.class)
                .skip(Exception.class)
                .listener(integratedPaymentItemReaderListener(executionDate))
                .listener(integratedPaymentWriteListener(executionDate))
                .listener(integratedPaymentItemProcessListener(executionDate))
                .listener(integratedPaymentsStepListener(writerTrackerService))
                .build();
    }

    @Bean
    public IntegratedPaymentsItemReaderListener integratedPaymentItemReaderListener(String executionDate) {
        IntegratedPaymentsItemReaderListener integratedPaymentsItemReaderListener = new IntegratedPaymentsItemReaderListener();
        integratedPaymentsItemReaderListener.setExecutionDate(executionDate);
        integratedPaymentsItemReaderListener.setErrorPaymentInfosLogsPath(errorLogsPath);
        integratedPaymentsItemReaderListener.setEnableOnErrorFileLogging(enableOnReadErrorFileLogging);
        integratedPaymentsItemReaderListener.setEnableOnErrorLogging(enableOnReadErrorLogging);
        return integratedPaymentsItemReaderListener;
    }

    @Bean
    public IntegratedPaymentsItemWriterListener integratedPaymentWriteListener(String executionDate) {
        IntegratedPaymentsItemWriterListener integratedPaymentsItemWriterListener = new IntegratedPaymentsItemWriterListener();
        integratedPaymentsItemWriterListener.setExecutionDate(executionDate);
        integratedPaymentsItemWriterListener.setErrorPaymentInfosLogsPath(errorLogsPath);
        integratedPaymentsItemWriterListener.setEnableOnErrorFileLogging(enableOnWriteErrorFileLogging);
        integratedPaymentsItemWriterListener.setEnableOnErrorLogging(enableOnWriteErrorLogging);
        return integratedPaymentsItemWriterListener;
    }

    @Bean
    public IntegratedPaymentsItemProcessListener integratedPaymentItemProcessListener(String executionDate) {
        IntegratedPaymentsItemProcessListener integratedPaymentsItemProcessListener = new IntegratedPaymentsItemProcessListener();
        integratedPaymentsItemProcessListener.setExecutionDate(executionDate);
        integratedPaymentsItemProcessListener.setErrorPaymentInfosLogsPath(errorLogsPath);
        integratedPaymentsItemProcessListener.setEnableOnErrorFileLogging(enableOnProcessErrorFileLogging);
        integratedPaymentsItemProcessListener.setEnableOnErrorLogging(enableOnProcessErrorLogging);
        return integratedPaymentsItemProcessListener;
    }

    @Bean
    public IntegratedPaymentsReaderStepListener integratedPaymentsStepListener(WriterTrackerService writerTrackerService) {
        IntegratedPaymentsReaderStepListener integratedPaymentsReaderStepListener = new IntegratedPaymentsReaderStepListener();
        integratedPaymentsReaderStepListener.setErrorPath(errorArchivePath);
        integratedPaymentsReaderStepListener.setSuccessPath(successArchivePath);
        integratedPaymentsReaderStepListener.setWriterTrackerService(writerTrackerService);
        integratedPaymentsReaderStepListener.setApplyEncrypt(applyEncrypt);
        integratedPaymentsReaderStepListener.setErrorDir(errorLogsPath);
        integratedPaymentsReaderStepListener.setPublicKeyDir(publicKey);
        return integratedPaymentsReaderStepListener;
    }

    /**
     *
     * @return bean configured for usage in the partitioner instance of the job
     */
    @Bean
    public TaskExecutor integratedPaymentPartitionerTaskExecutor() {
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
    public Executor integratedPaymentWriterExecutor() {
        if (this.executorService == null) {
            executorService =  Executors.newFixedThreadPool(executorPoolSize);
        }
        return executorService;
    }

    /**
     *
     * @return bean configured for usage for chunk reading of a single file
     */
    public TaskExecutor integratedPaymentReaderTaskExecutor() {
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
    public Job integratedPaymentJob() {
        return integratedPaymentJobBuilder().build();
    }

    /**
     *
     * @return bean for a ThreadPoolTaskScheduler
     */
    @Bean
    public TaskScheduler integratedPaymentPoolScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }



}
