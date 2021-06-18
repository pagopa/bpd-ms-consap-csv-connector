package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.poi;

import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.AbstractExcelItemReader;
import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class PoiItemReader<T> extends AbstractExcelItemReader<T> {

    private Workbook workbook;

    private InputStream inputStream;

    @Override
    public Sheet getSheet(final int sheet) {
        return new PoiSheet(this.workbook.getSheetAt(sheet));
    }

    @Override
    protected int getNumberOfSheets() {
        return this.workbook.getNumberOfSheets();
    }

    @Override
    protected void doClose() throws Exception {
        super.doClose();
        if (this.inputStream != null) {
            this.inputStream.close();
            this.inputStream = null;
        }

        if (this.workbook != null) {
            this.workbook.close();
            this.workbook = null;
        }
    }

    /**
     * Open the underlying file using the {@code WorkbookFactory}. Prefer {@code File}
     * based access over an {@code InputStream}. Using a file will use fewer resources
     * compared to an input stream. The latter will need to cache the whole sheet
     * in-memory.
     * @param resource the {@code Resource} pointing to the Excel file.
     * @param password the password for opening the file
     * @throws Exception is thrown for any errors.
     */
    @Override
    protected void openExcelFile(final Resource resource, String password) throws Exception {

        try {
            File file = resource.getFile();

            this.workbook = WorkbookFactory.create(file, password, false);
        }
        catch (FileNotFoundException ex) {
            this.inputStream = resource.getInputStream();
            this.workbook = WorkbookFactory.create(this.inputStream, password);
        }
        this.workbook.setMissingCellPolicy(Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
    }
}
