package btc.fsm.type.stub;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.sbt.jbpel.api.data.HttpStatus;
import ru.sbt.jbpel.api.data.RestErrorMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RestError implements RestErrorMessage {

    private final Map<String, String> headers;
    private final HttpStatus httpStatus;
    private final String replyValue;
    private final String parsedError;

    @NotNull
    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @NotNull
    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @NotNull
    @Override
    public String getReplyValue() {
        return replyValue;
    }

    @Nullable
    @Override
    public String getParsedError() {
        return parsedError;
    }

    public RestError(
      @NotNull Map<String, String> headers,
      @NotNull HttpStatus httpStatus,
      @NotNull String replyValue,
      @Nullable String parsedError
    ) {
        this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
        this.httpStatus = httpStatus;
        this.replyValue = replyValue;
        this.parsedError = parsedError;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RestError)) return false;
        RestError that = (RestError) o;
        return
          headers.equals(that.headers) &&
          httpStatus.equals(that.httpStatus) &&
          replyValue.equals(that.replyValue) &&
          Objects.equals(parsedError, that.parsedError);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers, httpStatus, replyValue, parsedError);
    }

    @Override
    public String toString() {
        return
          "{\"className\":\"RestError\""
            + ",\"headers\":\"" + headers + "\""
            + ",\"httpStatus\":\"" + httpStatus + "\""
            + ",\"replyValue\":\"" + replyValue + "\""
            + ",\"parsedError\":\"" + parsedError + "\""
            + "}";
    }
}
