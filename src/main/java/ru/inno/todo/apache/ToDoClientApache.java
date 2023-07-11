package ru.inno.todo.apache;

import client.ToDoClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import model.CreateToDo;
import model.ToDoItem;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.*;

public class ToDoClientApache implements ToDoClient {
    private final String URL;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public ToDoClientApache(String URL) throws IOException {
        this.URL = URL;
        this.httpClient = HttpClientBuilder
                .create()
                .addInterceptorLast(new MyRequestInterceptor())
                .addInterceptorFirst(new MyResponseInterceptor())
                .build();
        this.mapper = new ObjectMapper();
    }

    @Override
    public List<ToDoItem> getAll() throws IOException {
        HttpGet request = new HttpGet(URL);
        HttpResponse response = httpClient.execute(request);

        return mapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<>() {});
    }

    @Override
    public ToDoItem getById(int id) throws IOException {
        HttpGet request = new HttpGet(URL + "/" + id);
        request.addHeader("Content-Type", "application/json; charset=utf-8");    //Почему-то не сработала кодировка.
        HttpResponse response = httpClient.execute(request);

        return mapper.readValue(EntityUtils.toString(response.getEntity()), ToDoItem.class);
    }

    @Override
    public ToDoItem create(CreateToDo createToDo) throws IOException {
        HttpPost request = new HttpPost(URL);
        String body = mapper.writeValueAsString(createToDo);
        StringEntity entity = new StringEntity(body, ContentType.APPLICATION_JSON);
        request.setEntity(entity);
        HttpResponse response = httpClient.execute(request);

        return mapper.readValue(EntityUtils.toString(response.getEntity()), ToDoItem.class);
    }

    @Override
    public void deleteById(int id) {
        HttpDelete request = new HttpDelete(URL + "/" + id);
    }

    @Override
    public ToDoItem renameById(int id, String newName) throws IOException {
        HttpPatch request = new HttpPatch(URL + "/" + id);
        ToDoItem item = getById(id);
        item.setTitle(newName);
        String body = mapper.writeValueAsString(item);
        System.out.println("PATCH body: " + body);

        StringEntity patchBody = new StringEntity(body, ContentType.APPLICATION_JSON);        //Установка типа запроса для Entity. Иначе получается неправильная кодировка.
        request.setEntity(patchBody);

        //Отправка запроса
        HttpResponse response = httpClient.execute(request);
        return mapper.readValue(EntityUtils.toString(response.getEntity()), ToDoItem.class);
    }

    @Override
    public ToDoItem markCompleted(int id, boolean completed) {
        return null;
    }

    @Override
    public void deleteAll() throws IOException {
        HttpDelete request = new HttpDelete(URL);
        httpClient.execute(request);
    }
}
