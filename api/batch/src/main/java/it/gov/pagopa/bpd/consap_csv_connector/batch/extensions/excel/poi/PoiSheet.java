package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.poi;

import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.lang.Nullable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class PoiSheet implements Sheet {

    private final DataFormatter dataFormatter = new DataFormatter();

    private final org.apache.poi.ss.usermodel.Sheet delegate;

    private final int numberOfRows;

    private final String name;

    private FormulaEvaluator evaluator;

    /**
     * Constructor which takes the delegate sheet.
     * @param delegate the apache POI sheet
     */
    PoiSheet(final org.apache.poi.ss.usermodel.Sheet delegate) {
        super();
        this.delegate = delegate;
        this.numberOfRows = this.delegate.getLastRowNum() + 1;
        this.name = this.delegate.getSheetName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfRows() {
        return this.numberOfRows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public String[] getRow(final int rowNumber) {
        final Row row = this.delegate.getRow(rowNumber);
        return map(row);
    }

    @Nullable
    private String[] map(Row row) {
        if (row == null) {
            return null;
        }
        final List<String> cells = new LinkedList<>();
        final int numberOfColumns = row.getLastCellNum();

        for (int i = 0; i < numberOfColumns; i++) {
            Cell cell = row.getCell(i);
            CellType cellType = cell.getCellType();
            if (cellType == CellType.FORMULA) {
                cells.add(this.dataFormatter.formatCellValue(cell, getFormulaEvaluator()));
            }
            else {
                cells.add(this.dataFormatter.formatCellValue(cell));
            }
        }
        return cells.toArray(new String[0]);
    }

    /**
     * Lazy getter for the {@code FormulaEvaluator}. Takes some time to create an
     * instance, so if not necessary don't create it.
     * @return the {@code FormulaEvaluator}
     */
    private FormulaEvaluator getFormulaEvaluator() {
        if (this.evaluator == null) {
            this.evaluator = this.delegate.getWorkbook().getCreationHelper().createFormulaEvaluator();
        }
        return this.evaluator;
    }

    @Override
    public Iterator<String[]> iterator() {
        return new Iterator<String[]>() {
            private final Iterator<Row> delegateIter = PoiSheet.this.delegate.iterator();

            @Override
            public boolean hasNext() {
                return this.delegateIter.hasNext();
            }

            @Override
            public String[] next() {
                return map(this.delegateIter.next());
            }
        };
    }
}
