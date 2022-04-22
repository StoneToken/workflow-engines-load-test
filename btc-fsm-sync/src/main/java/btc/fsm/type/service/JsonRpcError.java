package btc.fsm.type.service;

import org.jetbrains.annotations.NotNull;
import ru.sbt.jbpel.api.data.HttpStatus;
import ru.sbt.jbpel.api.data.JsonRpcErrorMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JsonRpcError implements JsonRpcErrorMessage {
    private final Map<String,String> headers;
    private final HttpStatus httpStatus;
    private final ru.sbt.jbpel.api.data.JsonRpcError<String> error;

    public JsonRpcError(Map<String, String> headers, HttpStatus status, ru.sbt.jbpel.api.data.JsonRpcError<String> error) {
        this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
        httpStatus = status;
        this.error = error;
    }

    public JsonRpcError(HttpStatus status, ru.sbt.jbpel.api.data.JsonRpcError<String> error) {
        this.headers = Collections.emptyMap();
        httpStatus = status;
        this.error = error;
    }

    @NotNull
    @Override
    public ru.sbt.jbpel.api.data.JsonRpcError<String> getReplyValue() {
        return error;
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
        JsonRpcError error1 = (JsonRpcError)o;
        return Objects.equals(headers, error1.headers) &&
               Objects.equals(httpStatus, error1.httpStatus) &&
               Objects.equals(error, error1.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers, httpStatus, error);
    }

    @Override
    public String toString() {
        return "{\"className\":\"JsonRpcError\""
               + ",\"headers\":\"" + headers + "\""
               + ",\"httpStatus\":\"" + httpStatus + "\""
               + ",\"error\":\"" + error + "\""
               + "}";
    }
}
