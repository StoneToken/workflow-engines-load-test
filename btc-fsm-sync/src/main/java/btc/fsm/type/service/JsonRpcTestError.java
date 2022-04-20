package btc.fsm.type.service;

import btc.fsm.dto.User;
import org.jetbrains.annotations.NotNull;
import ru.sbt.jbpel.api.data.HttpStatus;
import ru.sbt.jbpel.api.data.JsonRpcError;
import ru.sbt.jbpel.api.data.JsonRpcErrorMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JsonRpcTestError implements JsonRpcErrorMessage {

    private final Map<String, String> headers;
    private final HttpStatus httpStatus;
    private final JsonRpcError<User> replyValue;

    public JsonRpcTestError(
      @NotNull Map<String, String> headers,
      @NotNull HttpStatus httpStatus,
      @NotNull JsonRpcError<User> replyValue
    ) {
        this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
        this.httpStatus = httpStatus;
        this.replyValue = replyValue;
    }

    @NotNull
    public Map<String, String> getHeaders() {
        return this.headers;
    }

    @NotNull
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    @NotNull
    public JsonRpcError<User> getReplyValue() {
        return this.replyValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonRpcTestError)) return false;
        JsonRpcTestError that = (JsonRpcTestError) o;
        return
          headers.equals(that.headers) &&
          httpStatus.equals(that.httpStatus) &&
          Objects.equals(replyValue, that.replyValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers, httpStatus, replyValue);
    }

    public String toString() {
        return
          "{\"className\":\"JsonRpcTestError\""
            + ",\"headers\":\"" + headers + "\""
            + ",\"httpStatus\":\"" + httpStatus + "\""
            + ",\"error\":\"" + replyValue + "\""
            + "}";
    }
}
