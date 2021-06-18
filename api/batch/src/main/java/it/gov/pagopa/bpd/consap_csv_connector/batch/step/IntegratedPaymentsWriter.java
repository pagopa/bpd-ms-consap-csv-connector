package it.gov.pagopa.bpd.consap_csv_connector.batch.step;

import eu.sia.meda.core.interceptors.BaseContextHolder;
import eu.sia.meda.core.model.ApplicationContext;
import it.gov.pagopa.bpd.award_winner.integration.event.model.IntegratedPayments;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.integratedPayments.IntegratedPaymentsItemWriterListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.integratedPayments.IntegratedPaymentsMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundIntegratedPayments;
import it.gov.pagopa.bpd.consap_csv_connector.service.CsvIntegratedPaymentsPublisherService;
import it.gov.pagopa.bpd.consap_csv_connector.service.WriterTrackerService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * Implementation of {@link ItemWriter}, to be used for read/processed IntegratedPayments files
 */

@RequiredArgsConstructor
@Slf4j
@Data
@Component
public class IntegratedPaymentsWriter implements ItemWriter<InboundIntegratedPayments> {

    private static final String BATCH_CONSAP_CSV_CONNECTOR_NAME = "bpd-ms-consap-csv-connector";

    private final WriterTrackerService writerTrackerService;
    private final CsvIntegratedPaymentsPublisherService csvIntegratedPaymentsPublisherService;
    private IntegratedPaymentsItemWriterListener integratedPaymentsItemWriterListener;
    private Executor executor;
    private final IntegratedPaymentsMapper mapper;
    private Boolean applyHashing;
    private Boolean enableCheckpointFrequency;
    private Integer checkpointFrequency;

    /**
     * Implementation of the {@link ItemWriter} write method, used for {@link IntegratedPayments} as the processed class
     *
     * @param inboundIntegratedPayment list of {@link IntegratedPayments} from the process phase of a reader to be sent on an outbound Kafka channel
     * @throws Exception
     */
    @Override
    public void write(List<? extends InboundIntegratedPayments> inboundIntegratedPayment) throws Exception {

        CountDownLatch countDownLatch = new CountDownLatch(inboundIntegratedPayment.size());

        ApplicationContext applicationContext = BaseContextHolder.getApplicationContext();
        applicationContext.setUserId(BATCH_CONSAP_CSV_CONNECTOR_NAME);

//        String fileName = !inboundIntegratedPayment.isEmpty() ?
//                inboundIntegratedPayment.get(0).getFilename().substring(inboundIntegratedPayment.get(0)
//                        .getFilename().lastIndexOf('/') + 1) :
//                null;

        inboundIntegratedPayment.forEach(inboundIntegratedPayments -> executor.execute(() -> {
            try {
                applicationContext.setRequestId(String.format("%s:%d",
                        "fileName",
                        inboundIntegratedPayments.getLineNumber()));
                BaseContextHolder.forceSetApplicationContext(applicationContext);
                IntegratedPayments integratedPayments = mapper.map(inboundIntegratedPayments, applyHashing);
                csvIntegratedPaymentsPublisherService.publishIntegratedPaymentsEvent(integratedPayments);
            } catch (Exception e) {
                integratedPaymentsItemWriterListener.onWriteError(e, inboundIntegratedPayments);
            }
            countDownLatch.countDown();
        }));

        if (!inboundIntegratedPayment.isEmpty()) {
            writerTrackerService.addCountDownLatch(
                    countDownLatch, enableCheckpointFrequency,
                    inboundIntegratedPayment.get(0).getFilename(), checkpointFrequency);
        }

    }
}
