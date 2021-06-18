package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.streaming;

import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.AbstractExcelItemReader;
import it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.Sheet;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.Styles;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StreamingXlsxItemReader<T> extends AbstractExcelItemReader<T> {

    private final List<StreamingSheet> sheets = new ArrayList<>();

    private OPCPackage pkg;

    private InputStream inputStream;

    @Override
    protected Sheet getSheet(int sheet) {
        return this.sheets.get(sheet);
    }

    @Override
    protected int getNumberOfSheets() {
        return this.sheets.size();
    }

    @Override
    protected void openExcelFile(Resource resource, String password) throws Exception {
        try {
            File file = resource.getFile();
            this.pkg = OPCPackage.open(file, PackageAccess.READ);
        }
        catch (FileNotFoundException ex) {
            this.inputStream = resource.getInputStream();
            this.pkg = OPCPackage.open(this.inputStream);
        }
        XSSFReader reader = new XSSFReader(this.pkg);
        initSheets(reader, this.pkg);
    }

    private void initSheets(XSSFReader reader, OPCPackage pkg) throws IOException, InvalidFormatException {
        XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) reader.getSheetsData();
        SharedStrings sharedStrings;
        try {
            sharedStrings = new ReadOnlySharedStringsTable(pkg);
        }
        catch (SAXException ex) {
            throw new IllegalStateException("Cannot read shared-strings-table.", ex);
        }
        Styles styles = reader.getStylesTable();
        while (iter.hasNext()) {
            InputStream is = iter.next();
            String name = iter.getSheetName();
            this.sheets.add(new StreamingSheet(name, is, sharedStrings, styles));
        }

        if (log.isTraceEnabled()) {
            log.trace("Prepared " + this.sheets.size() + " sheets.");
        }
    }

    @Override
    protected void doClose() throws Exception {
        this.pkg.revert();

        for (StreamingSheet sheet : this.sheets) {
            sheet.close();
        }
        this.sheets.clear();

        if (this.inputStream != null) {
            this.inputStream.close();
            this.inputStream = null;
        }
        super.doClose();
    }

}
