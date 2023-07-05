package ru.inno.todo.apache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ReferenceType;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

public class ToDoClient {

    public static String URL = "https://todo-app-sky.herokuapp.com";

    public static void main(String[] args) throws IOException {
        // Client to send requests
        HttpClient client = HttpClientBuilder.create().build();
        // JSON to Object and back
        ObjectMapper mapper = new ObjectMapper();

        // GET list
        HttpGet request1 = new HttpGet(URL);
        // Status, Headers, Body
        HttpResponse response1 = client.execute(request1);
        System.out.println(response1.getStatusLine());
        for (Header header : response1.getAllHeaders()) {
            System.out.println(header);
        }
        String body1 = EntityUtils.toString(response1.getEntity());
        System.out.println(body1);

        // POST create new todo
        HttpPost createItemRequest = new HttpPost(URL);
        createItemRequest.addHeader("Content-Type", "application/json");
        createItemRequest.addHeader("Username", "Dima");

        String myContent = "{\"title\" : \"My java task\"}";
        StringEntity entity = new StringEntity(myContent);
        createItemRequest.setEntity(entity);
        HttpResponse newItem = client.execute(createItemRequest);

        System.out.println(newItem.getStatusLine());
        System.out.println();

        // Parse response body
//      Object o =  mapper.readValue(EntityUtils.toString(newItem.getEntity()), Object.class );
        ToDoItem item =  mapper.readValue(EntityUtils.toString(newItem.getEntity()), ToDoItem.class);

        // GET item
        HttpGet request2 = new HttpGet(URL + item.getUrl());
        HttpResponse response2 = client.execute(request2);
        if (response2.getStatusLine().getStatusCode() == 200) {
            String body2 = EntityUtils.toString(response2.getEntity());
            System.out.println(body2);
        } else {
            System.err.println("Error");
            System.out.println(response2.getStatusLine());
        }

        // GET list (typed)
        HttpResponse listAsString = client.execute(request1);
        List<ToDoItem> list = mapper.readValue(EntityUtils.toString(listAsString.getEntity()), List.class);
        System.out.println(list.size());

        // POST create item (typed)
        // Step 1. Create object
        CreateToDO itemToCreate = new CreateToDO();
        itemToCreate.setTitle("my new task");
        // Step 2. Convert to string
        String s = mapper.writeValueAsString(itemToCreate);
        // Step 3. Set entity
        entity = new StringEntity(s);
        createItemRequest.setEntity(entity);
        HttpResponse newItemTyped = client.execute(createItemRequest);
        System.out.println(newItemTyped.getStatusLine());


        // PATCH mark task as completed

        // DELETE task by id

    }
}
