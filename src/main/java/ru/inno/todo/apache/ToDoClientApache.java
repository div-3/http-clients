package ru.inno.todo.apache;

import client.ToDoClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.CreateToDo;
import model.ToDoItem;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

public class ToDoClientApache implements ToDoClient {
    private final String URL;
    private final HttpClient httpClient;

    private final ObjectMapper mapper;

    public ToDoClientApache(String URL) {
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

        return mapper.readValue(EntityUtils.toString(response.getEntity()), new TypeReference<>() {
        });
    }

    @Override
    public ToDoItem getById(int id) {
        return null;
    }

    @Override
    public ToDoItem create(CreateToDo createToDo) throws IOException {
        HttpPost request = new HttpPost(URL);
        String body = mapper.writeValueAsString(createToDo);
        StringEntity entity = new StringEntity(body);
        request.setEntity(entity);
        HttpResponse response = httpClient.execute(request);

        return mapper.readValue(EntityUtils.toString(response.getEntity()), ToDoItem.class);
    }

    @Override
    public void deleteById(int id) {

    }

    @Override
    public ToDoItem renameById(int id, String newName) {
        return null;
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
