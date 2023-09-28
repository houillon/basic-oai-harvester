package fr.persee.oai.harvest.command;

import fr.persee.oai.harvest.HarvesterApp;
import java.util.Objects;
import picocli.CommandLine;

public class HarvesterVersionProvider implements CommandLine.IVersionProvider {
  @Override
  public String[] getVersion() {
    String implementationVersion =
        Objects.requireNonNullElse(
            HarvesterApp.class.getPackage().getImplementationVersion(), "<development>");

    return new String[] {implementationVersion};
  }
}
