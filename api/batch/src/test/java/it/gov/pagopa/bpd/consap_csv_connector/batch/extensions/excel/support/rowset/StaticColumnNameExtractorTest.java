package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.support.rowset;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StaticColumnNameExtractorTest {

    private static final String[] COLUMNS = { "col1", "col2", "col3", "foo", "bar" };

    @Test
    public void shouldReturnSameHeadersAsPassedIn() {

        StaticColumnNameExtractor columnNameExtractor = new StaticColumnNameExtractor(COLUMNS);
        String[] names = columnNameExtractor.getColumnNames(null);
        assertThat(names)
                .isEqualTo(new String[] { "col1", "col2", "col3", "foo", "bar" })
                .isNotSameAs(COLUMNS);
    }

    @Test
    public void shouldReturnACopyOfTheHeaders() {

        StaticColumnNameExtractor columnNameExtractor = new StaticColumnNameExtractor(COLUMNS);
        String[] names = columnNameExtractor.getColumnNames(null);

        assertThat(names)
                .isEqualTo(new String[] { "col1", "col2", "col3", "foo", "bar" })
                .isNotSameAs(COLUMNS);
    }

}
