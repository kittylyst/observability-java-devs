package com.redhat.example;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.http.HttpRequest;
import java.util.List;

@RestController
public class MarvelController {
  private static final List<String> characters = List.of("Iron Man", "Scarlet Witch", "Black Panther", "Black Widow");

  private static final HttpServletRequestExtractor EXTRACTOR = new HttpServletRequestExtractor();

  @Autowired private HttpServletRequest httpServletRequest;

  @GetMapping("/getCharacter")
  public String getCharacter() {
    // Extract the propagated context from the request.
    var extractedContext = extractContext();

    try (var scope = extractedContext.makeCurrent()) {
      // Start a span in the scope of the extracted context.
      var span = serverSpan("/getCharacter", HttpMethod.GET.name());

      try {
        Thread.sleep((int)(100 * Math.random()));
        return characters.get((int) (characters.size() * Math.random()));
      } catch (InterruptedException e) {
        return "<Error>";
      } finally {
        span.end();
      }
    }
  }

  /**
   * Extract the propagated context from the {@link #httpServletRequest}.
   *
   * @return the extracted context
   */
  private Context extractContext() {
    return GlobalOpenTelemetry.getPropagators()
        .getTextMapPropagator()
        .extract(Context.current(), httpServletRequest, EXTRACTOR);
  }

  /**
   * Create a {@link SpanKind#SERVER} span, setting the parent context if available from the {@link
   * #httpServletRequest}.
   *
   * @param path the HTTP path
   * @param method the HTTP method
   * @return the span
   */
  private Span serverSpan(String path, String method) {
    return GlobalOpenTelemetry.getTracer(MarvelController.class.getName())
        .spanBuilder(path)
        .setSpanKind(SpanKind.SERVER)
        .setAttribute(SemanticAttributes.HTTP_METHOD, method)
        .setAttribute(SemanticAttributes.HTTP_SCHEME, "http")
        .setAttribute(SemanticAttributes.HTTP_HOST, "localhost:8080")
        .setAttribute(SemanticAttributes.HTTP_TARGET, path)
        .startSpan();
  }

  /**
   * Inject the {@code span}'s context into the {@code requestBuilder}.
   *
   * @param span the span
   * @param requestBuilder the request builder
   */
  private static void injectContext(Span span, HttpRequest.Builder requestBuilder) {
    var context = Context.current().with(span);
    GlobalOpenTelemetry.getPropagators()
        .getTextMapPropagator()
        .inject(context, requestBuilder, HttpRequest.Builder::header);
  }

  /**
   * A simple {@link TextMapGetter} implementation that extracts context from {@link
   * HttpServletRequest} headers.
   */
  private static class HttpServletRequestExtractor implements TextMapGetter<HttpServletRequest> {
    @Override
    public Iterable<String> keys(HttpServletRequest carrier) {
      return () -> carrier.getHeaderNames().asIterator();
    }

    @Override
    public String get(HttpServletRequest carrier, String key) {
      return carrier.getHeader(key);
    }
  }
}
