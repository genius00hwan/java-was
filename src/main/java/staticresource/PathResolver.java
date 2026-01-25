package staticresource;

import http.request.Path;

public class PathResolver {
    private static final String DEFAULT_SUFFIX = "/index.html";
    private static final String DIRECTORY_DELIMITER = "/";
    public static Path resolveForStatic(Path input) {
        String rawPath = input.value();
        if (rawPath.endsWith(DIRECTORY_DELIMITER)) {
            return appendIndex(input);
        }

        String[] parts = rawPath.split(DIRECTORY_DELIMITER);
        String lastSegment = parts[parts.length - 1];
        boolean isDirectoryLike = !lastSegment.contains(".");

        if (isDirectoryLike) {
            return appendIndex(input);
        }

        return input;
    }

    public static Path resolveForApp(Path input) {
        String raw = input.value();
        if (raw.endsWith(DEFAULT_SUFFIX)) {
            return new Path(raw.substring(0, raw.length() - DEFAULT_SUFFIX.length()) + DIRECTORY_DELIMITER);
        }
        return input;
    }

    private static Path appendIndex(Path input) {
        return new Path(input.value() + DEFAULT_SUFFIX);
    }
}
