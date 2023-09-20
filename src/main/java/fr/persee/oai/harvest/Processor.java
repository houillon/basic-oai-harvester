package fr.persee.oai.harvest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class Processor implements SmartLifecycle {

  private final ExecutorService executorService = Executors.newSingleThreadExecutor();
  private boolean started = false;

  @Override
  public void start() {
    started = true;
  }

  @Override
  public void stop() {
    executorService.shutdownNow();
  }

  @Override
  public boolean isRunning() {
    return started && !executorService.isShutdown();
  }
}
