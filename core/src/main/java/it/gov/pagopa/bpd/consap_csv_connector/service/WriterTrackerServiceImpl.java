package it.gov.pagopa.bpd.consap_csv_connector.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Service
@Data
@RequiredArgsConstructor
@Slf4j
public class WriterTrackerServiceImpl implements WriterTrackerService {

    private final List<CountDownLatch> countDownLatches;
    private final HashMap<String,List<CountDownLatch>> fileLatchesMap;

    @SneakyThrows
    @Override
    public synchronized void addCountDownLatch(
            CountDownLatch countDownLatch, Boolean enableCheckpointFrequency,
            String filename, Integer checkpointFrequency) {
        countDownLatches.add(countDownLatch);

        List<CountDownLatch> latchesFilename = fileLatchesMap.get(filename);

        if (latchesFilename == null) {
            latchesFilename = new ArrayList<>();
        }

        latchesFilename.add(countDownLatch);
        fileLatchesMap.put(filename,latchesFilename);

        if (enableCheckpointFrequency && countDownLatches.size() % checkpointFrequency == 0) {
            countDownLatch.await();
        }
    }

    @Override
    public synchronized List<CountDownLatch> getCountDownLatches() {
        return countDownLatches;
    }

    @Override
    public List<CountDownLatch> getFileCountDownLatches(String filename) {
        return fileLatchesMap.get(filename);
    }

    @Override
    public void clearAll() {
        countDownLatches.clear();
    }
}
