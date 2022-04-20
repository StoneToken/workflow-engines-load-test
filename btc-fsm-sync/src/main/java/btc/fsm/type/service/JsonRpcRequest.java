package btc.fsm.type.service;

import btc.fsm.dto.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.sbt.jbpel.api.data.RequestMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JsonRpcRequest implements RequestMessage {

    private final Map<String, String> headers;
    private final String username;

    public JsonRpcRequest(
      @NotNull Map<String, String> headers,
      @Nullable String username
    ) {
        this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
        this.username = username;
    }

    @NotNull
    @Override
    public Class<?>[] getFieldTypes() {
        return new Class[]{User.class};
    }

    @NotNull
    @Override
    public Object[] getFieldValues() {
        return new Object[]{username};
    }

    @NotNull
    @Override
    public String[] getFieldNames() {
        return new String[]{"username"};
    }

    @NotNull
    @Override
    public String getMethodName() {
        return "";
    }

    @NotNull
    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonRpcRequest)) return false;
        JsonRpcRequest that = (JsonRpcRequest) o;
        return
          headers.equals(that.headers) &&
          Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headers, username);
    }

    public String toString() {
        return
          "{\"className\":\"JsonRpcRequest\""
            + ",\"headers\":\"" + headers + "\""
            + ",\"username\":\"" + username + "\""
            + "}";
    }
}
