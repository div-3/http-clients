package ru.inno.todo.apache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.ArrayList;
import java.util.List;

public class ToDoClientMy {
    public static String URL = "https://todo-app-sky.herokuapp.com";
    record ResponseBodyRecord(HttpResponse response, String body){}
    public static ObjectMapper mapper = new ObjectMapper();                   //Parse JSON bodies from responses


    public static void main(String[] args) {

        HttpClient client = HttpClientBuilder.create().build();     //Http Client to send requests


        //API testing:

        //1. GET запрос для получения списка задач
        System.out.println("------------------------\nПолучение списка задач.\n------------------------");
        ResponseBodyRecord rbr = getRequest(client);
        System.out.println("GET response body: "
                + getResponseEntityAsListOfToDoItems(rbr.body).toString());

        //2. DELETE запрос на URL = "https://todo-app-sky.herokuapp.com" очищает список задач
        System.out.println("------------------------\nОчистка списка задач.\n------------------------");

        deleteRequest(client, null);    //При выполнении с задачей null очищается весь список задач
        getRequest(client);

        //3. POST запрос (добавляет задачу в список)
        System.out.println("------------------------\nДобавление задачи.\n------------------------");

        ToDoItem newItem1 = new ToDoItem();
        newItem1.setTitle("Изучить HTTP");
        rbr = postRequest(client, newItem1);
        newItem1 = getToDoItemFromResponse(rbr.body);
        System.out.println(newItem1);

        ToDoItem newItem2 = new ToDoItem();
        newItem2.setTitle("Изучить SOAP");
        rbr = postRequest(client, newItem2);
        newItem2 = getToDoItemFromResponse(rbr.body);
        System.out.println(newItem2);

        ToDoItem newItem3 = new ToDoItem();
        newItem3.setTitle("Изучить REST");
        rbr = postRequest(client, newItem3);
        newItem3 = getToDoItemFromResponse(rbr.body);
        System.out.println(newItem3);

        //4. GET запрос всего списка задач
        System.out.println("------------------------\nПолучение списка задач.\n------------------------");
        rbr = getRequest(client);
        System.out.println(rbr.body);

        //5. PATCH запрос (помечает задачу выполненной)
        System.out.println("------------------------\nЗадача выполнена.\n------------------------");
        HttpPatch patchRequest;
        rbr = patchRequest(client, newItem1);
        newItem1 = getToDoItemFromResponse(rbr.body);
        System.out.println(newItem1);

        //6. DELETE запрос одной задачи
        System.out.println("------------------------\nУдаление одной задачи.\n------------------------");

        deleteRequest(client, newItem1);    //При выполнении с задачей null очищается весь список задач
        rbr = getRequest(client);
        System.out.println("GET response body: "
                + getResponseEntityAsListOfToDoItems(rbr.body).toString());
    }

    private static ToDoItem getToDoItemFromResponse(String body) {
        try {
            return mapper.readValue(body, ToDoItem.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ResponseBodyRecord postRequest(HttpClient client, ToDoItem newItem) {
        HttpPost postRequest = new HttpPost(URL);
        HttpResponse postResponse;
        String body;
        try {
            body = getJsonAsStringWithSelectedToDoItem(newItem, new String[]{"title"});  //получение JSON с нужными полями класса

            System.out.println("POST body: " + body);

            StringEntity postBody = new StringEntity(body, ContentType.APPLICATION_JSON);        //Установка типа запроса для Entity. Иначе получается неправильная кодировка.
            postRequest.setEntity(postBody);
//            postRequest.addHeader("Content-Type", "application/json");    //Почему-то не сработала кодировка.

            //Настройка Header'ов
            postRequest.addHeader("Username", "Ivan");
            postRequest.addHeader("Accept-Encoding", "gzip, deflate, br");

            //Отправка запроса
            postResponse = client.execute(postRequest);

            System.out.println("POST response status: " + postResponse.getStatusLine());
            return new ResponseBodyRecord(postResponse, EntityUtils.toString(postResponse.getEntity()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static ResponseBodyRecord patchRequest(HttpClient client, ToDoItem item) {
        HttpPatch request = new HttpPatch(URL + item.getUrl());
        HttpResponse response;
        String body;
        try {
            item.setCompleted(true);
            body = getJsonAsStringWithSelectedToDoItem(item, new String[]{"completed"});  //получение JSON с нужными полями класса

            System.out.println("PATCH body: " + body);

            StringEntity patchBody = new StringEntity(body, ContentType.APPLICATION_JSON);        //Установка типа запроса для Entity. Иначе получается неправильная кодировка.
            request.setEntity(patchBody);
//            request.addHeader("Content-Type", "application/json");    //Почему-то не сработала кодировка.

            //Настройка Header'ов
            request.addHeader("Username", "Ivan");
            request.addHeader("Accept-Encoding", "gzip, deflate, br");

            //Отправка запроса
            response = client.execute(request);

            System.out.println("PATCH response status: " + response.getStatusLine());
            return new ResponseBodyRecord(response, EntityUtils.toString(response.getEntity()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static String getJsonAsStringWithSelectedToDoItem(ToDoItem item, String[] selectedItems){
        ObjectNode rootNode = mapper.valueToTree(item);         //Получение всего дерева JSON из item
        ObjectNode selectedNode = mapper.createObjectNode();    //Создание пустого нода для вывода
        for (String s : selectedItems) {
            selectedNode.put(s, rootNode.get(s));               //Заполнение нода для вывода только нужными полями класса
        }
        try {
            return mapper.writeValueAsString(selectedNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpResponse deleteRequest(HttpClient client, ToDoItem item) {
        HttpDelete request;
        if (item == null){
            request = new HttpDelete(URL);
        }else {
            request = new HttpDelete(URL + item.getUrl());
        }

        HttpResponse response;
        try {
            response = client.execute(request);
            System.out.println("DELETE response status: " + response.getStatusLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private static ResponseBodyRecord getRequest(HttpClient client) {
        HttpGet getRequest = new HttpGet(URL);
        HttpResponse getResponse;
        String body;
        try {
            getResponse = client.execute(getRequest);
            System.out.println("GET response status: " + getResponse.getStatusLine());
            body = EntityUtils.toString(getResponse.getEntity());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ResponseBodyRecord(getResponse, body);
    }

    private static List<ToDoItem> getResponseEntityAsListOfToDoItems(String body) {
        List<ToDoItem> listToDo = new ArrayList<>();
        try {
            listToDo = mapper.readValue(body, List.class);
        } catch (Exception e) {
            System.err.println("Ошибка при разборе body. " + e.toString());
        }
        return listToDo;
    }
}

