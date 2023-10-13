package org.folio.spring.client;

import static java.util.Collections.singleton;
import static java.util.Objects.nonNull;
import static org.folio.spring.integration.XOkapiHeaders.TOKEN;

import feign.Client;
import feign.Request;
import feign.Response;
import feign.okhttp.OkHttpClient;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import lombok.extern.log4j.Log4j2;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.service.SystemUserProperties;
import org.folio.spring.service.SystemUserService;

@Log4j2
public class EnrichUrlAndHeadersClient implements Client {
  private final OkHttpClient delegate;
  private final FolioExecutionContext folioExecutionContext;
  private final SystemUserProperties systemUserProperties;
  private final SystemUserService systemUserService;

  public EnrichUrlAndHeadersClient(FolioExecutionContext folioExecutionContext, okhttp3.OkHttpClient okHttpClient,
    SystemUserProperties systemUserProperties, SystemUserService systemUserService) {
    this.folioExecutionContext = folioExecutionContext;
    this.delegate = new OkHttpClient(okHttpClient);
    this.systemUserProperties = systemUserProperties;
    this.systemUserService = systemUserService;
  }

  @Override
  public Response execute(Request request, Request.Options options) throws IOException {
    String url;

    var okapiUrl = folioExecutionContext.getOkapiUrl();
    if (okapiUrl != null) {
      if (!okapiUrl.endsWith("/")) {
        okapiUrl += "/";
      }
      url = request.url().replace("http://", okapiUrl);
    } else {
      url = request.url();
    }

    Map<String, Collection<String>> allHeaders = new HashMap<>(request.headers());
    allHeaders.putAll(folioExecutionContext.getOkapiHeaders());

    if (isSystemUserPresent(systemUserProperties)) {
      var systemUser = systemUserService.getAuthedSystemUser(folioExecutionContext.getTenantId());
      if (nonNull(systemUser) && Objects.equals(systemUser.userId(), folioExecutionContext.getUserId().toString())) {
        allHeaders.put(TOKEN, singleton(systemUser.token().accessToken()));
      }
    }

    var requestWithUrl = Request.create(request.httpMethod(), url, allHeaders, request.body(), request.charset(),
      request.requestTemplate());

    log.debug("FolioExecutionContext: {};\nPrepared the Feign Client Request: {} with headers {};\nCurrent thread: {}",
      folioExecutionContext, requestWithUrl, allHeaders, Thread.currentThread().getName());
    return delegate.execute(requestWithUrl, options);
  }

  private boolean isSystemUserPresent(SystemUserProperties systemUserProperties) {
    return nonNull(systemUserProperties.username()) && nonNull(systemUserProperties.password());
  }
}
