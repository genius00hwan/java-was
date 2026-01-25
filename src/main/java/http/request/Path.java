package http.request;

public record Path(String value) {

    private static final String PATH_SPLIT = "/";
    public Path {
        value = normalize(value);
    }

    private static String normalize(String path) {
        if (path == null || path.isBlank()) return PATH_SPLIT;
        path = path.replaceAll("/{2,}", PATH_SPLIT);
        if (path.endsWith("/") && path.length() > 1) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Path other)) return false;
        return value.equals(normalize(other.value));
    }


    @Override
    public String toString() {
        return value;
    }
}

