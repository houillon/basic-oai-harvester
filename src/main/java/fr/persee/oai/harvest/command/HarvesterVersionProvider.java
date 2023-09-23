package fr.persee.oai.harvest.command;

import fr.persee.oai.harvest.HarvesterApp;
import picocli.CommandLine;

public class HarvesterVersionProvider implements CommandLine.IVersionProvider {
  @Override
  public String[] getVersion() {
    String implementationVersion = HarvesterApp.class.getPackage().getImplementationVersion();

    return new String[] {implementationVersion};
  }
}
