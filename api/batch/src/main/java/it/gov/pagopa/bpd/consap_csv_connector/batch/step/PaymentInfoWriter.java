package it.gov.pagopa.bpd.consap_csv_connector.batch.step;

import eu.sia.meda.core.interceptors.BaseContextHolder;
import eu.sia.meda.core.model.ApplicationContext;
import it.gov.pagopa.bpd.award_winner.integration.event.model.PaymentInfo;
import it.gov.pagopa.bpd.consap_csv_connector.batch.listener.PaymentInfoItemWriterListener;
import it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.PaymentInfoMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundPaymentInfo;
import it.gov.pagopa.bpd.consap_csv_connector.service.CsvPaymentInfoPublisherService;
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
 * Implementation of {@link ItemWriter}, to be used for read/processed PaymentInfo files
 */

@RequiredArgsConstructor
@Slf4j
@Data
@Component
public class PaymentInfoWriter implements ItemWriter<InboundPaymentInfo> {

    private static final String BATCH_CONSAP_CSV_CONNECTOR_NAME = "bpd-ms-consap-csv-connector";

    private final WriterTrackerService writerTrackerService;
    private final CsvPaymentInfoPublisherService csvPaymentInfoPublisherService;
    private PaymentInfoItemWriterListener paymentInfoItemWriterListener;
    private Executor executor;
    private final PaymentInfoMapper mapper;
    private Boolean applyHashing;
    private Boolean enableCheckpointFrequency;
    private Integer checkpointFrequency;

    /**
     * Implementation of the {@link ItemWriter} write method, used for {@link PaymentInfo} as the processed class
     *
     * @param inboundPaymentInfos list of {@link PaymentInfo} from the process phase of a reader to be sent on an outbound Kafka channel
     * @throws Exception
     */
    @Override
    public void write(List<? extends InboundPaymentInfo> inboundPaymentInfos) throws Exception {

        CountDownLatch countDownLatch = new CountDownLatch(inboundPaymentInfos.size());

        ApplicationContext applicationContext = BaseContextHolder.getApplicationContext();
        applicationContext.setUserId(BATCH_CONSAP_CSV_CONNECTOR_NAME);

        String fileName = !inboundPaymentInfos.isEmpty() ?
                inboundPaymentInfos.get(0).getFilename().substring(inboundPaymentInfos.get(0)
                        .getFilename().lastIndexOf('/') + 1) :
                null;

        inboundPaymentInfos.forEach(inboundPaymentInfo -> executor.execute(() -> {
            try {
                applicationContext.setRequestId(String.format("%s:%d",
                        fileName,
                        inboundPaymentInfo.getLineNumber()));
                BaseContextHolder.forceSetApplicationContext(applicationContext);
                PaymentInfo paymentInfo = mapper.map(inboundPaymentInfo, applyHashing);
                csvPaymentInfoPublisherService.publishPaymentInfoEvent(paymentInfo);
            } catch (Exception e) {
                paymentInfoItemWriterListener.onWriteError(e, inboundPaymentInfo);
            }
            countDownLatch.countDown();
        }));

        if (!inboundPaymentInfos.isEmpty()) {
            writerTrackerService.addCountDownLatch(
                    countDownLatch, enableCheckpointFrequency,
                    inboundPaymentInfos.get(0).getFilename(), checkpointFrequency);
        }

    }

}
