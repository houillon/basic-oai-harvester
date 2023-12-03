package fr.persee.oai.harvest.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@CommandLine.Command(
    name = "version",
    description = "Print version information",
    versionProvider = VersionProvider.class)
@RequiredArgsConstructor
@Slf4j
public class VersionCommand implements Runnable {

  @SuppressWarnings("NullAway.Init")
  @CommandLine.Spec
  CommandLine.Model.CommandSpec spec;

  @Override
  public void run() {
    spec.commandLine().printVersionHelp(System.out);
  }
}
