package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel;

import it.gov.pagopa.bpd.consap_csv_connector.batch.mapper.integratedPayments.IntegratedCustomMapper;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundIntegratedPayments;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanPropertyItemReaderTest {

    private MockExcelItemReader<InboundIntegratedPayments> reader;

    @BeforeEach
    public void setup() throws Exception {
        ExecutionContext executionContext = new ExecutionContext();

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] { "fiscalCode1", "1", "1234", "986795", "10", "50", "100"});
        rows.add(new String[] { "fiscalCode2", "1", "5678", "642625", "11", "51", "101" });
        MockSheet sheet = new MockSheet("payments", rows);

        this.reader = new MockExcelItemReader<>(sheet);

        IntegratedCustomMapper rowMapper = new IntegratedCustomMapper();

        this.reader.setRowMapper(rowMapper);

        this.reader.afterPropertiesSet();
        this.reader.open(executionContext);
    }

    @Test
    public void readandMapPlayers() throws Exception {
        InboundIntegratedPayments p1 = this.reader.read();
        InboundIntegratedPayments p2 = this.reader.read();
        assertThat(p1).isNotNull();
        assertThat(p2).isNotNull();

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(p1.getFiscalCode()).isEqualTo("fiscalCode1");
        softly.assertThat("1").isEqualTo(p1.getAwardPeriodId().toString());
        softly.assertThat("1234").isEqualTo(p1.getTicketId().toString());
        softly.assertThat("986795").isEqualTo(p1.getRelatedPaymentId().toString());
        softly.assertThat("10").isEqualTo(p1.getAmount().toString());
        softly.assertThat("50").isEqualTo(p1.getCashbackAmount().toString());
        softly.assertThat("100").isEqualTo(p1.getJackpotAmount().toString());


        softly.assertThat(p2.getFiscalCode()).isEqualTo("fiscalCode2");
        softly.assertThat("1").isEqualTo(p2.getAwardPeriodId().toString());
        softly.assertThat("5678").isEqualTo(p2.getTicketId().toString());
        softly.assertThat("642625").isEqualTo(p2.getRelatedPaymentId().toString());
        softly.assertThat("11").isEqualTo(p2.getAmount().toString());
        softly.assertThat("51").isEqualTo(p2.getCashbackAmount().toString());
        softly.assertThat("101").isEqualTo(p2.getJackpotAmount().toString());

        softly.assertAll();
    }
}
