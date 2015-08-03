package no.difi.vefa.validator.source;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import no.difi.asic.AsicReader;
import no.difi.asic.AsicReaderFactory;
import no.difi.asic.SignatureMethod;
import no.difi.vefa.validator.api.SourceInstance;
import no.difi.xsd.asic.model._1.Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

class AbstractSourceInstance implements SourceInstance {

    private static Logger logger = LoggerFactory.getLogger(AbstractSourceInstance.class);

    protected static AsicReaderFactory asicReaderFactory = AsicReaderFactory.newFactory(SignatureMethod.CAdES);

    protected FileSystem fileSystem;

    public AbstractSourceInstance() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    protected void unpackContainer(AsicReader asicReader, String targetName) throws IOException {
        // Prepare copying from asice-file to in-memory filesystem
        Path targetDirectory = fileSystem.getPath(targetName);

        // Copy content
        String filename;
        while ((filename = asicReader.getNextFile()) != null) {
            Path outputPath = targetDirectory.resolve(filename);
            Files.createDirectories(outputPath.getParent());
            logger.debug(outputPath.toString());

            asicReader.writeFile(outputPath);
        }

        // Close asice-file
        asicReader.close();

        // Listing signatures
        for (Certificate certificate : asicReader.getAsicManifest().getCertificates())
            logger.info(String.format("Signature: %s", certificate.getSubject()));

        // TODO Validate certificate?
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }
}