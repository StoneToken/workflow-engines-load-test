package ru.sbt.kogito.test.loadtests.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbt.ltf.annotation.Action;
import ru.sbt.ltf.annotation.Init;
import ru.sbt.ltf.annotation.Parameterized;
import ru.sbt.ltf.annotation.Provided;
import ru.sbt.ltf.annotation.Script;
import ru.sbt.ltf.core.Result;
import ru.sbt.ltf.gauge.Gauge;
import ru.sbt.ltf.gauge.StopWatch;
import ru.sbt.ltf.runtime.Environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.stream.Collectors;

@Script
public class PerformanceScriptKogitoAsync {

    private final static Logger LOG = LoggerFactory.getLogger(PerformanceScriptKogitoAsync.class);

    private final static String DEFAULT_URL = "http://localhost:8080";
    private final static String DEFAULT_URL_PATH = "/single";
    private final static String DEFAULT_STUB_URL = "http://localhost:8000";

    protected String url;
    protected String urlPath;
    protected String stubUrl;

    @Provided
    private Gauge gauge;

    @Provided
    private Environment env;

    HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10L))
            .build();

    private static final String METHOD_NAME = "LoadTestKogito_testSync";

    protected Result doAction() {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + urlPath))
                .setHeader("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            StopWatch stopWatch = gauge.start("Kogito_" + METHOD_NAME);
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (httpResponse.toString().equals("HTTP/1.1 200 ")) {
                String responseBody = httpResponse.body();
                stopWatch.stop(true);
                LOG.info("result: {}", responseBody);
                return Result.success();
            } else {
                stopWatch.stop(false);
                LOG.error("result: {}", httpResponse.body());
                return Result.error(httpResponse.body());
            }
        } catch (IOException | InterruptedException exception) {
            LOG.error("Error, HttpClient: ", exception);
            return Result.error(exception);
        }
    }

    protected void initialize(String ... params) {
        url =
                Optional.ofNullable(env.getArgument(params[0])).orElse(DEFAULT_URL);
        urlPath =
                Optional.ofNullable(env.getArgument(params[1])).orElse(DEFAULT_URL_PATH);
        stubUrl =
                Optional.ofNullable(env.getArgument(params[2])).orElse(DEFAULT_STUB_URL);

        LOG.debug("URL: {}", url);
        LOG.debug("subURL: {}", urlPath);
        LOG.debug("stubURL: {}", stubUrl);
    }

    @Init
    public void init(
            @Parameterized(name = "url", value = "http://10.31.0.5:8080") String url,
            @Parameterized(name = "url", value = "/single") String subUrl,
            @Parameterized(name = "stub_url", value = "http://10.31.0.5:8000") String stubUrl
    ) {
        initialize(url, subUrl, stubUrl);
    }

    @Action(invocationCount = 10)
    public Result action() {
        return doAction();
    }
}