package io.kadai.example.boot.spi;

import io.kadai.common.api.KadaiEngine;
import io.kadai.spi.task.api.AfterRequestReviewProvider;
import io.kadai.task.api.models.Task;
import org.springframework.web.reactive.function.client.WebClient;

public class AfterRequestReviewProviderImpl implements AfterRequestReviewProvider {

  private KadaiEngine kadaiEngine;
  private WebClient webClient;

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    this.webClient = WebClient.builder().baseUrl("http://localhost:8081").build();
    this.kadaiEngine = kadaiEngine;
  }

  @Override
  public Task afterRequestReview(Task task) {
    //webClient
    //    .post()
    //    .uri("/api/v1/tasks/" + task.getId() + "/3")
    //    .retrieve()
    //    .toBodilessEntity()
    //    .block();
    kadaiEngine.clearSqlSessionCache();
    return task;
  }
}
