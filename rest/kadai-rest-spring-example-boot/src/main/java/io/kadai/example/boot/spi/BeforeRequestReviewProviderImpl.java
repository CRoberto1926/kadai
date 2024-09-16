package io.kadai.example.boot.spi;

import io.kadai.classification.api.exceptions.ClassificationNotFoundException;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.spi.task.api.BeforeRequestReviewProvider;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.exceptions.AttachmentPersistenceException;
import io.kadai.task.api.exceptions.InvalidTaskStateException;
import io.kadai.task.api.exceptions.ObjectReferencePersistenceException;
import io.kadai.task.api.exceptions.TaskNotFoundException;
import io.kadai.task.api.models.Task;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import org.springframework.web.reactive.function.client.WebClient;

public class BeforeRequestReviewProviderImpl implements BeforeRequestReviewProvider {

  private KadaiEngine kadaiEngine;
  private WebClient webClient;

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    this.webClient = WebClient.builder().baseUrl("http://localhost:8081").build();
    this.kadaiEngine = kadaiEngine;
  }

  @Override
  public Task beforeRequestReview(Task task)
      throws InvalidTaskStateException,
          ObjectReferencePersistenceException,
          ConcurrencyException,
          TaskNotFoundException,
          NotAuthorizedOnWorkbasketException,
          ClassificationNotFoundException,
          AttachmentPersistenceException {

    task.setCustomField(
        TaskCustomField.CUSTOM_2, "2nd UPDATE from " + this.getClass().getSimpleName());

    //webClient
    //    .post()
    //    .uri("/api/v1/tasks/" + task.getId() + "/2")
    //    .retrieve()
    //    .toBodilessEntity()
    //    .block();
    //task = kadaiEngine.getTaskService().updateTask(task);

    //task = kadaiEngine.getTaskService().updateTask(task);

    kadaiEngine.clearSqlSessionCache();

    return task;
  }
}
