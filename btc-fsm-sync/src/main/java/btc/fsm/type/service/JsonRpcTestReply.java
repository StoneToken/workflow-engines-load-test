package btc.fsm.type.service;

import btc.fsm.dto.User;
import org.jetbrains.annotations.NotNull;
import ru.sbt.jbpel.api.data.HttpStatus;
import ru.sbt.jbpel.api.data.JsonRpcSuccessMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JsonRpcTestReply implements JsonRpcSuccessMessage {

    private final Map<String, String> headers;
    private final HttpStatus httpStatus;
    private final User replyValue;

    public JsonRpcTestReply(Map<String, String> headers, HttpStatus httpStatus, User replyValue) {
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

    public User getReplyValue() {
        return this.replyValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonRpcTestReply)) return false;
        JsonRpcTestReply that = (JsonRpcTestReply) o;
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
          "{\"className\":\"JsonRpcTestReply\""
            + ",\"headers\":\"" + headers + "\""
            + ",\"httpStatus\":\"" + httpStatus + "\""
            + ",\"replyValue\":\"" + replyValue + "\""
            + "}";
    }
}
