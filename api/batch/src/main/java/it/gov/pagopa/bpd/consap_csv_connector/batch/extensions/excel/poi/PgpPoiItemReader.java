package it.gov.pagopa.bpd.consap_csv_connector.batch.extensions.excel.poi;

import it.gov.pagopa.bpd.consap_csv_connector.batch.encryption.PGPDecryptUtil;
import it.gov.pagopa.bpd.consap_csv_connector.batch.encryption.exception.PGPDecryptException;
import it.gov.pagopa.bpd.consap_csv_connector.batch.model.InboundIntegratedPayments;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.openpgp.PGPException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;

import java.io.*;
import java.nio.file.Files;
import java.security.NoSuchProviderException;

@RequiredArgsConstructor
@Slf4j
public class PgpPoiItemReader extends PoiItemReader<InboundIntegratedPayments> {

    private final String secretFilePath;
    private final String passphrase;
    private final Boolean applyDecrypt;

    private Resource resource;

    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
        super.setResource(resource);
    }

    /**
     * Override of {@link PoiItemReader#doOpen},introduces a
     * decrypt pass before calling on the parent implementation
     *
     * @throws Exception
     */
    @Override
    protected void doOpen() throws Exception {
        Assert.notNull(this.resource, "Input resource must be set");
        if (applyDecrypt) {
            File fileToProcess = resource.getFile();
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource secretKeyResource = resolver.getResource(secretFilePath);
            try {
                try (FileInputStream fileToProcessIS = new FileInputStream(fileToProcess);
                     FileInputStream secretFilePathIS = new FileInputStream(secretKeyResource.getFile())) {
                    byte[] decryptFileData = PGPDecryptUtil.decryptFile(
                            fileToProcessIS,
                            secretFilePathIS,
                            passphrase.toCharArray()
                    );
//                    super.setFilename(this.resource.getDescription());
                    File file = Files.createTempFile("tempFile",".xls").toFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    IOUtils.copy(new ByteArrayInputStream(decryptFileData), fileOutputStream);

                    fileOutputStream.close();

                    super.setResource(new InputStreamResource(new FileInputStream(file)));

                }
            } catch (IllegalArgumentException | IOException | PGPException | NoSuchProviderException e ) {
                log.error(e.getMessage(),e);
                throw new PGPDecryptException(e.getMessage(),e);
            }
        }
        super.doOpen();

    }

}
