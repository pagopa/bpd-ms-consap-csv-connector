package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel;

import java.util.Iterator;
import java.util.List;

public class MockSheet implements Sheet {

    private final List<String[]> rows;

    private final String name;

    public MockSheet(String name, List<String[]> rows) {
        this.name = name;
        this.rows = rows;
    }

    @Override
    public int getNumberOfRows() {
        return this.rows.size();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String[] getRow(int rowNumber) {
        if (rowNumber < getNumberOfRows()) {
            return this.rows.get(rowNumber);
        }
        else {
            return null;
        }
    }

    @Override
    public Iterator<String[]> iterator() {
        return this.rows.iterator();
    }

}
