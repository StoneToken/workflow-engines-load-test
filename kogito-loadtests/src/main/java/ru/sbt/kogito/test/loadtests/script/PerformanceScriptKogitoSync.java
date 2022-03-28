package ru.sbt.kogito.test.loadtests.script;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sbt.ltf.annotation.Action;
import ru.sbt.ltf.annotation.End;
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
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

@Script
public class PerformanceScriptKogitoSync implements AutoCloseable {

    protected final static Logger LOG = LoggerFactory.getLogger(PerformanceScriptKogitoSync.class);

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

    protected Result doAction(CloseableHttpClient closeableHttpClient, String methodName) {

        HttpPost httpPost = new HttpPost(url + urlPath);
        httpPost.setHeader("Content-Type", "application/json");

        StopWatch stopWatch = gauge.start("Kogito_" + methodName);
        try {
            HttpResponse httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().toString().equals("HTTP/1.1 200 ")) {
                String responseBody =
                        new BufferedReader(
                                new InputStreamReader(
                                        httpResponse
                                                .getEntity()
                                                .getContent(), StandardCharsets.UTF_8)
                        )
                        .lines()
                        .collect(Collectors.joining(System.lineSeparator()));
                stopWatch.stop(true);
                LOG.info("result: {}", responseBody);
                return Result.success();
            } else {
                stopWatch.stop(false);
                LOG.error("result: {}", httpResponse.getStatusLine().toString());
                return Result.error(httpResponse.getStatusLine().toString());
            }
        } catch (IOException exception) {
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


    CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    private static final String METHOD_NAME = "LoadTestKogito_testSync";

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
        return doAction(httpClient, METHOD_NAME);
    }

    @End
    @Override
    public void close() throws Exception {
        httpClient.close();
    }
}
