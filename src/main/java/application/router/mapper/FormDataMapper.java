package application.router.mapper;

import application.exception.client.IllegalContentTypeException;
import application.router.ReflectionMapUtil;
import http.body.FormBody;
import http.body.HttpBody;
import http.request.HttpRequest;

public class FormDataMapper<T> implements ArgumentMapper<T> {
    private final Class<T> clazz;

    public FormDataMapper(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T map(HttpRequest request) {
        HttpBody rawBody = request.httpBody();
        validateBodyType(rawBody);
        FormBody body = (FormBody) rawBody;
        return ReflectionMapUtil.mapByParameterName(clazz,body.formData());
    }

    private void validateBodyType(HttpBody body) {
        if (!(body instanceof FormBody)) {
            throw new IllegalContentTypeException("지원하지 않는 타입입니다!!");
        }
    }

}
