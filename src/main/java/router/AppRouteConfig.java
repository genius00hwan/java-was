package router;


import application.facade.ApiModule;
import application.router.MethodHandler;
import http.request.Path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppRouteConfig {

    private final List<ApiModule> apiModules;

    public AppRouteConfig(List<ApiModule> apiModules) {
        this.apiModules = apiModules;
    }

    public Map<RouteKey, MethodHandler<?>> buildRouteMap() {
        Map<RouteKey, MethodHandler<?>> fullMap = new HashMap<>();

        for (ApiModule facade : apiModules) {
            String prefix = facade.basePath();
            for (Map.Entry<RouteKey, MethodHandler<?>> entry : facade.routes().entrySet()) {
                RouteKey key = entry.getKey();
                Path fullPath = new Path(prefix + key.path().value());
                fullMap.put(new RouteKey(fullPath, key.method()), entry.getValue());
            }
        }

        return fullMap;
    }

}
