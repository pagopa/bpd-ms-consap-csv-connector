package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.support.rowset;

import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class DefaultRowSetMetaDataTest {

    private static final String[] COLUMNS = { "col1", "col2", "col3" };

    private DefaultRowSetMetaData rowSetMetaData;

    private Sheet sheet;

    private ColumnNameExtractor columnNameExtractor;

    @BeforeEach
    public void setup() {
        this.sheet = Mockito.mock(Sheet.class);
        this.columnNameExtractor = Mockito.mock(ColumnNameExtractor.class);
        this.rowSetMetaData = new DefaultRowSetMetaData(this.sheet, this.columnNameExtractor);
    }

    @Test
    public void shouldReturnColumnsFromColumnNameExtractor() {

        given(this.columnNameExtractor.getColumnNames(this.sheet)).willReturn(COLUMNS);

        String[] names = this.rowSetMetaData.getColumnNames();

        assertThat(names).isEqualTo(new String[] { "col1", "col2", "col3" });

        verify(this.columnNameExtractor, times(1)).getColumnNames(this.sheet);
        verifyNoMoreInteractions(this.sheet, this.columnNameExtractor);
    }

    @Test
    public void shouldGetAndReturnNameOfTheSheet() {

        given(this.sheet.getName()).willReturn("testing123");

        String name = this.rowSetMetaData.getSheetName();

        assertThat(name).isEqualTo("testing123");

        verify(this.sheet, times(1)).getName();
        verifyNoMoreInteractions(this.sheet);
    }

}
