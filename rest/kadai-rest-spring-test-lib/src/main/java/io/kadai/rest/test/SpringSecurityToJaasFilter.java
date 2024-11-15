/*
 * Copyright [2024] [envite consulting GmbH]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *
 */

package io.kadai.rest.test;

import io.kadai.common.api.exceptions.SystemException;
import io.kadai.common.api.security.GroupPrincipal;
import io.kadai.common.api.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.security.AccessController;
import java.util.Optional;
import javax.security.auth.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.GenericFilterBean;

// Duplicate of io.kadai.common.rest.SpringSecurityToJaasFilter
/** Simple Filter to map all Spring Security Roles to JAAS-Principals. */
public class SpringSecurityToJaasFilter extends GenericFilterBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringSecurityToJaasFilter.class);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    Optional<Authentication> authentication = getCurrentAuthentication();
    if (authentication.isPresent()) {
      LOGGER
          .atDebug()
          .setMessage("Authentication found in Spring security context: {}")
          .addArgument(() -> authentication)
          .log();
      obtainSubject()
          .ifPresent(
              subject -> {
                initializeUserPrincipalFromAuthentication(authentication.get(), subject);
                initializeGroupPrincipalsFromAuthentication(authentication.get(), subject);
              });
    } else {
      LOGGER.debug(
          "No authentication found in Spring security context. Continuing unauthenticatic.");
    }

    chain.doFilter(request, response);
  }

  /**
   * Obtains the <code>Subject</code> to run as or <code>null</code> if no <code>Subject</code> is
   * available.
   *
   * <p>The default implementation attempts to obtain the <code>Subject</code> from the <code>
   * SecurityContext</code>'s <code>Authentication</code>. If it is of type <code>
   * JaasAuthenticationToken</code> and is authenticated, the <code>Subject</code> is returned from
   * it.
   *
   * @return the Subject to run.
   */
  @SuppressWarnings("removal")
  protected Optional<Subject> obtainSubject() {
    Optional<Authentication> authentication = getCurrentAuthentication();
    if (logger.isDebugEnabled()) {
      // This is not SLF4J!
      logger.debug("Attempting to obtainSubject using authentication : " + authentication);
    }
    if (authentication.isEmpty() || !authentication.get().isAuthenticated()) {
      return Optional.empty();
    }
    // TODO replace with Subject.current() when migrating to newer Version than 17
    return Optional.of(Subject.getSubject(AccessController.getContext()));
  }

  Optional<Authentication> getCurrentAuthentication() {
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
  }

  private void initializeUserPrincipalFromAuthentication(
      Authentication authentication, Subject subject) {
    if (subject.getPrincipals().isEmpty()) {
      LOGGER
          .atDebug()
          .setMessage("Setting the principal of the subject with {}.")
          .addArgument(authentication::getPrincipal)
          .log();
      subject
          .getPrincipals()
          .add(new UserPrincipal(((UserDetails) authentication.getPrincipal()).getUsername()));
    } else {
      LOGGER
          .atDebug()
          .setMessage("Principal of the subject is already set to {}.")
          .addArgument(subject::getPrincipals)
          .log();
      throw new SystemException("Finding an existing principal is unexpected. Please investigate.");
    }
  }

  private void initializeGroupPrincipalsFromAuthentication(
      Authentication authentication, Subject subject) {

    LOGGER
        .atDebug()
        .setMessage("Adding roles {} to subject.")
        .addArgument(authentication::getAuthorities)
        .log();

    authentication
        .getAuthorities()
        .forEach(
            grantedAuthority ->
                subject.getPrincipals().add(new GroupPrincipal(grantedAuthority.getAuthority())));

    LOGGER
        .atDebug()
        .setMessage("{}")
        .addArgument(() -> subject.getPublicCredentials(GroupPrincipal.class))
        .log();
  }
}
