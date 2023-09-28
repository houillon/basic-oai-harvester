package fr.persee.oai.harvest;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Element;

@Slf4j
public class FileWriter {

  private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

  public static void write(Path root, Element metadata, String identifier, String prefix) {
    String dirname = identifier.replaceAll("[^\\w-.,;]", "_");
    Path dir = root.resolve(dirname);
    Path file = dir.resolve(prefix + ".xml");

    log.atDebug().log("writing record to file: {}", file);

    try {
      Files.createDirectories(dir);
    } catch (IOException e) {
      log.atError().setCause(e).log("error creating directory {}", dir);
      return;
    }

    try (OutputStream out = Files.newOutputStream(file)) {
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      transformer.transform(new DOMSource(metadata), new StreamResult(out));
    } catch (IOException | TransformerException e) {
      log.atError().setCause(e).log("error writing record {} to file: {}", identifier, file);
    }
  }
}
