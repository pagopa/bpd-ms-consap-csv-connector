package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel;

import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.mapping.PassThroughRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public abstract class AbstractExcelItemReaderTests {

    protected AbstractExcelItemReader<String[]> itemReader;

    @BeforeEach
    public void setup() throws Exception {
        this.itemReader = createExcelItemReader();
        this.itemReader.setLinesToSkip(1); // First line is column names
        this.itemReader.setResource(new ClassPathResource("payments.xls"));
        this.itemReader.setRowMapper(new PassThroughRowMapper());
        this.itemReader.setSkippedRowsCallback((rs) -> log.info("Skipping: " + Arrays.toString(rs.getCurrentRow())));
        configureItemReader(this.itemReader);
        this.itemReader.afterPropertiesSet();
        ExecutionContext executionContext = new ExecutionContext();
        this.itemReader.setSheetNumber(1);
        this.itemReader.open(executionContext);
    }

    protected void configureItemReader(AbstractExcelItemReader<String[]> itemReader) {
    }

    @AfterEach
    public void after() {
        this.itemReader.close();
    }

    @Test
    public void readExcelFile() throws Exception {
        assertThat(this.itemReader.getNumberOfSheets()).isEqualTo(1);
        String[] row;
        do {
            row = this.itemReader.read();
            if (log.isTraceEnabled()) {
                log.trace("Read: " + Arrays.toString(row));
            }
            if (row != null) {
                assertThat(row).hasSize(7);
            }
        }
        while (row != null);
        Integer readCount = (Integer) ReflectionTestUtils.getField(this.itemReader, "currentItemCount");
        assertThat(readCount).isEqualTo(1);
    }

    @Test
    public void testRequiredProperties() {
        assertThatThrownBy(() -> {
            final AbstractExcelItemReader<String[]> reader = createExcelItemReader();
            reader.afterPropertiesSet();
        }).isInstanceOf(IllegalArgumentException.class);
    }

    protected abstract AbstractExcelItemReader<String[]> createExcelItemReader();
}
