package io.kadai.example.boot.spi;

import io.kadai.classification.api.exceptions.ClassificationNotFoundException;
import io.kadai.common.api.KadaiEngine;
import io.kadai.common.api.exceptions.ConcurrencyException;
import io.kadai.spi.task.api.ReviewRequiredProvider;
import io.kadai.task.api.TaskCustomField;
import io.kadai.task.api.exceptions.AttachmentPersistenceException;
import io.kadai.task.api.exceptions.InvalidTaskStateException;
import io.kadai.task.api.exceptions.ObjectReferencePersistenceException;
import io.kadai.task.api.exceptions.TaskNotFoundException;
import io.kadai.task.api.models.Task;
import io.kadai.workbasket.api.exceptions.NotAuthorizedOnWorkbasketException;
import org.springframework.web.reactive.function.client.WebClient;

public class ReviewRequiredProviderImpl implements ReviewRequiredProvider {

  private KadaiEngine kadaiEngine;
  private WebClient webClient;

  @Override
  public void initialize(KadaiEngine kadaiEngine) {
    this.webClient = WebClient.builder().baseUrl("http://localhost:8081").build();
    this.kadaiEngine = kadaiEngine;
    ReviewRequiredProvider.super.initialize(kadaiEngine);
  }

  @Override
  public boolean reviewRequired(Task task) {
    //task.setCustomField(
    //    TaskCustomField.CUSTOM_3, "3rd UPDATE from " + this.getClass().getSimpleName());
    //try {
    //  task = kadaiEngine.getTaskService().updateTask(task);
    //} catch (TaskNotFoundException e) {
    //  throw new RuntimeException(e);
    //} catch (ConcurrencyException e) {
    //  throw new RuntimeException(e);
    //} catch (ClassificationNotFoundException e) {
    //  throw new RuntimeException(e);
    //} catch (AttachmentPersistenceException e) {
    //  throw new RuntimeException(e);
    //} catch (ObjectReferencePersistenceException e) {
    //  throw new RuntimeException(e);
    //} catch (NotAuthorizedOnWorkbasketException e) {
    //  throw new RuntimeException(e);
    //} catch (InvalidTaskStateException e) {
    //  throw new RuntimeException(e);
    //}
    //task.setCustomField(
    //    TaskCustomField.CUSTOM_4, "4th UPDATE from " + this.getClass().getSimpleName());
    //try {
    //  task = kadaiEngine.getTaskService().updateTask(task);
    //} catch (TaskNotFoundException e) {
    //  throw new RuntimeException(e);
    //} catch (ConcurrencyException e) {
    //  throw new RuntimeException(e);
    //} catch (ClassificationNotFoundException e) {
    //  throw new RuntimeException(e);
    //} catch (AttachmentPersistenceException e) {
    //  throw new RuntimeException(e);
    //} catch (ObjectReferencePersistenceException e) {
    //  throw new RuntimeException(e);
    //} catch (NotAuthorizedOnWorkbasketException e) {
    //  throw new RuntimeException(e);
    //} catch (InvalidTaskStateException e) {
    //  throw new RuntimeException(e);
    //}
    webClient
        .post()
        .uri("/api/v1/tasks/" + task.getId() + "/1")
        .retrieve()
        .toBodilessEntity()
        .block();
    //webClient
    //    .post()
    //    .uri("/api/v1/tasks/" + task.getId() + "/2")
    //    .retrieve()
    //    .toBodilessEntity()
    //    .block();
    return false;
  }
}
