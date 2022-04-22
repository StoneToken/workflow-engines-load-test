package btc.fsm.type.service;

import btc.fsm.dto.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.sbt.jbpel.api.data.HttpStatus;
import ru.sbt.jbpel.api.data.JsonRpcSuccessMessage;

import java.util.*;

public class JsonRpcReply implements JsonRpcSuccessMessage {

    private final Map<String,String> headers;
    private final HttpStatus httpStatus;
    private final User[] reply;

    public JsonRpcReply(Map<String, String> headers, HttpStatus httpStatus, User[] reply) {
        this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
        this.httpStatus = httpStatus;
        this.reply = reply;
    }

    public JsonRpcReply(HttpStatus httpStatus, User[] reply) {
        this.headers = Collections.emptyMap();
        this.httpStatus = httpStatus;
        this.reply = reply;
    }

    @Nullable
    @Override
    public User[] getReplyValue() {
        return reply;
    }

    @NotNull
    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @NotNull
    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JsonRpcReply message = (JsonRpcReply) o;
        return Objects.equals(headers, message.headers) &&
                Arrays.equals(reply, message.reply);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers, reply);
    }

    @Override
    public String toString() {
        return "{\"className\":\"JsonRpcReply\""
               + ",\"headers\":\"" + headers + "\""
               + ",\"httpStatus\":\"" + httpStatus + "\""
               + ",\"reply\":\"" + Arrays.toString(reply) + "\""
               + "}";
    }
}
