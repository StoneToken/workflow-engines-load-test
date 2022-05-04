package btc.fsm.type.stub;

import btc.fsm.dto.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.sbt.jbpel.api.data.HttpStatus;
import ru.sbt.jbpel.api.data.RestSuccessMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RestReply implements RestSuccessMessage {

    private final Map<String, String> headers;
    private final HttpStatus httpStatus;
    private final User replyValue;

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

    @Nullable
    @Override
    public User getReplyValue() {
        return replyValue;
    }

    public RestReply(
      @NotNull Map<String, String> headers,
      @NotNull HttpStatus httpStatus,
      @Nullable User replyValue
    ) {
        this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
        this.httpStatus = httpStatus;
        this.replyValue = replyValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RestReply)) return false;
        RestReply that = (RestReply) o;
        return
          headers.equals(that.headers) &&
          httpStatus.equals(that.httpStatus) &&
          Objects.equals(replyValue, that.replyValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers, httpStatus, replyValue);
    }

    @Override
    public String toString() {
        return
          "{\"className\":\"RestReply\""
            + ",\"headers\":\"" + headers + "\""
            + ",\"httpStatus\":\"" + httpStatus + "\""
            + ",\"replyValue\":\"" + replyValue + "\""
            + "}";
    }
}
