package ru.inno.todo.apache;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class MyRequestInterceptor implements HttpRequestInterceptor {
    @Override
    public void process(HttpRequest httpRequest, HttpContext httpContext) throws IOException {
        System.out.println(httpRequest.getRequestLine());
        for (Header header : httpRequest.getAllHeaders()) {
            System.out.println(header);
        }

        if (httpRequest.getRequestLine().getMethod().equals("POST")
                || httpRequest.getRequestLine().getMethod().equals("PATCH")) {
            HttpEntityEnclosingRequest req = (HttpEntityEnclosingRequest) httpContext.getAttribute("http.request");
            System.out.println("BODY: ===>");
            if (req.getEntity() == null) {
                System.out.println("NO BODY");
            } else {
                System.out.println(EntityUtils.toString(req.getEntity()));
            }
        }
    }
}
