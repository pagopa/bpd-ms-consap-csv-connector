package it.gov.pagopa.bpd.consap_csv_connector.batch.step;

import eu.sia.meda.core.interceptors.BaseContextHolder;
import eu.sia.meda.core.model.ApplicationContext;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentInfo;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentIntegration;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.PaymentIntegrationItemWriterListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.PaymentIntegrationMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentIntegration;
import it.gov.pagopa.bpd.consap_csv_connector.service.CsvPaymentIntegrationPublisherService;
import it.gov.pagopa.bpd.consap_csv_connector.service.WriterTrackerService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * Implementation of {@link ItemWriter}, to be used for read/processed PaymentInfo files
 */

@RequiredArgsConstructor
@Slf4j
@Data
@Component
public class PaymentIntegrationWriter implements ItemWriter<InboundPaymentIntegration> {

    private static final String BATCH_CONSAP_CSV_CONNECTOR_NAME = "bpd-ms-consap-csv-connector";
    private static final String INTEGRATION_HEADER = "x-ms-integration";

    private final WriterTrackerService writerTrackerService;
    private final CsvPaymentIntegrationPublisherService csvPaymentIntegrationPublisherService;
    private PaymentIntegrationItemWriterListener paymentIntegrationItemWriterListener;
    private Executor executor;
    private final PaymentIntegrationMapper mapper;
    private Boolean applyHashing;
    private Boolean enableCheckpointFrequency;
    private Integer checkpointFrequency;

    /**
     * Implementation of the {@link ItemWriter} write method, used for {@link PaymentInfo} as the processed class
     *
     * @param inboundPaymentIntegrations list of {@link PaymentInfo} from the process phase of a reader to be sent on an outbound Kafka channel
     * @throws Exception
     */
    @Override
    public void write(List<? extends InboundPaymentIntegration> inboundPaymentIntegrations) throws Exception {

        CountDownLatch countDownLatch = new CountDownLatch(inboundPaymentIntegrations.size());

        ApplicationContext applicationContext = BaseContextHolder.getApplicationContext();
        applicationContext.setUserId(BATCH_CONSAP_CSV_CONNECTOR_NAME);

        String fileName = !inboundPaymentIntegrations.isEmpty() ?
                inboundPaymentIntegrations.get(0).getFilename().substring(inboundPaymentIntegrations.get(0)
                        .getFilename().lastIndexOf('/') + 1) :
                null;

        inboundPaymentIntegrations.forEach(inboundPaymentIntegration -> executor.execute(() -> {
            try {
                applicationContext.setRequestId(String.format("%s:%d",
                        fileName,
                        inboundPaymentIntegration.getLineNumber()));
                BaseContextHolder.forceSetApplicationContext(applicationContext);
                PaymentIntegration paymentIntegration = mapper.map(inboundPaymentIntegration, applyHashing);
                RecordHeaders recordHeaders = new RecordHeaders();
                recordHeaders.add(INTEGRATION_HEADER, "true".getBytes());
                csvPaymentIntegrationPublisherService.publishPaymentIntegrationEvent(
                        paymentIntegration, recordHeaders);
            } catch (Exception e) {
                paymentIntegrationItemWriterListener.onWriteError(e, inboundPaymentIntegration);
            }
            countDownLatch.countDown();
        }));

        if (!inboundPaymentIntegrations.isEmpty()) {
            writerTrackerService.addCountDownLatch(
                    countDownLatch, enableCheckpointFrequency,
                    inboundPaymentIntegrations.get(0).getFilename(), checkpointFrequency);
        }

    }

}
