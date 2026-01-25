package router;

import http.request.HttpMethod;
import http.request.Path;

import java.util.Objects;

public record RouteKey(
    Path path,
    HttpMethod method
){
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(method.name());
        sb.append(" : [");
        sb.append(path.toString());
        sb.append("]");
        return sb.toString();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RouteKey other)) return false;
        return path.equals(other.path) && method == other.method;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, method);
    }
}