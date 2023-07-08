package ru.inno.todo.apache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.ArrayList;
import java.util.List;

public class ToDoClientMy {
    public static String URL = "https://todo-app-sky.herokuapp.com";
    public static ObjectMapper mapper = new ObjectMapper();                   //Parse JSON bodies from responses
    record ResponseBodyRecord(HttpResponse response, String body) {}


    public static void main(String[] args) {
        HttpClient client = HttpClientBuilder.create().build();     //Http Client to send requests

        //1. GET запрос для получения списка задач
        System.out.println("\n------------------------\nGET. Получение списка задач.\n------------------------");
        ResponseBodyRecord rbr = getRequest(client);
        System.out.println("GET response body: " + getBodyString(rbr));

        //2. DELETE запрос на URL = "https://todo-app-sky.herokuapp.com" очищает список задач
        System.out.println("\n------------------------\nDELETE. Очистка списка задач.\n------------------------");
        deleteRequest(client, null);    //При выполнении с задачей null очищается весь список задач
        System.out.println("\n------------------------\nGET. Получение списка задач.\n------------------------");
        getRequest(client);

        //3. POST запрос (добавляет задачу в список)
        System.out.println("\n------------------------\nPOST. Добавление задач:\n------------------------");

        System.out.println("\n------------------------\nPOST. Задача 1:\n------------------------");
        ToDoItem newItem1 = new ToDoItem();
        newItem1.setTitle("Изучить HTTP");
        rbr = postRequest(client, newItem1);
        newItem1 = getToDoItemFromResponse(rbr.body);
        System.out.println(newItem1);

        System.out.println("\n------------------------\nPOST. Задача 2:\n------------------------");
        ToDoItem newItem2 = new ToDoItem();
        newItem2.setTitle("Изучить SOAP");
        rbr = postRequest(client, newItem2);
        newItem2 = getToDoItemFromResponse(rbr.body);
        System.out.println(newItem2);

        System.out.println("\n------------------------\nPOST. Задача 3:\n------------------------");
        ToDoItem newItem3 = new ToDoItem();
        newItem3.setTitle("Изучить REST");
        rbr = postRequest(client, newItem3);
        newItem3 = getToDoItemFromResponse(rbr.body);
        System.out.println(newItem3);

        //4. GET запрос всего списка задач
        System.out.println("\n------------------------\nGET. Получение списка задач.\n------------------------");
        rbr = getRequest(client);
        System.out.println(getBodyString(rbr));

        //5. PATCH запрос (помечает задачу выполненной)
        System.out.println("\n------------------------\nPATCH. Установка признака, что задача выполнена.\n------------------------");
        HttpPatch patchRequest;
        rbr = patchRequest(client, newItem1);
        newItem1 = getToDoItemFromResponse(rbr.body);
        System.out.println("Задача 1: " + newItem1);

        //6. DELETE запрос одной задачи
        System.out.println("\n------------------------\nDELETE. Удаление одной задачи (Задачи 1).\n------------------------");

        rbr = deleteRequest(client, newItem1);      //Удаление задачи 1
        System.out.println("DELETE response body: " + getBodyString(rbr));

        System.out.println("\n------------------------\nGET. Получение списка задач.\n------------------------");
        rbr = getRequest(client);
        System.out.println("GET response body: " + getBodyString(rbr));
    }

    private static String getBodyString(ResponseBodyRecord rbr) {
        return (getResponseEntityAsListOfToDoItems(rbr.body) != null) ?
                    getResponseEntityAsListOfToDoItems(rbr.body).toString() : "отсутствует";
    }

    private static ToDoItem getToDoItemFromResponse(String body) {
        try {
            return mapper.readValue(body, ToDoItem.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static ResponseBodyRecord postRequest(HttpClient client, ToDoItem newItem) {
        HttpPost request = new HttpPost(URL);
        HttpResponse response;
        String body;
        return runRequest(client, newItem, request);
    }

    private static <R> ResponseBodyRecord runRequest(HttpClient client, ToDoItem newItem, R request) {
        String body;
        HttpResponse response;
        try {
            body = getJsonAsStringWithSelectedToDoItem(newItem, new String[]{"title"});  //получение JSON с нужными полями класса
            System.out.println("POST body: " + body);
            StringEntity postBody = new StringEntity(body, ContentType.APPLICATION_JSON);        //Установка типа запроса для Entity. Иначе получается неправильная кодировка.
            request.setEntity(postBody);
//            request.addHeader("Content-Type", "application/json");    //Почему-то не сработала кодировка.

            //Настройка Header'ов
            request.addHeader("Username", "Ivan");
            request.addHeader("Accept-Encoding", "gzip, deflate, br");

            //Отправка запроса
            response = client.execute(request);
            System.out.println("POST response status: " + response.getStatusLine());

            return new ResponseBodyRecord(response, EntityUtils.toString(response.getEntity()));
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

    private static String getJsonAsStringWithSelectedToDoItem(ToDoItem item, String[] selectedItems) {
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

    private static ResponseBodyRecord deleteRequest(HttpClient client, ToDoItem item) {
        HttpDelete request;
        if (item == null) {
            request = new HttpDelete(URL);
        } else {
            request = new HttpDelete(URL + item.getUrl());
        }

        String body = null;
        HttpResponse response;
        try {
            response = client.execute(request);
            System.out.println("DELETE response status: " + response.getStatusLine());
            HttpEntity entity = response.getEntity();
            if (entity != null) body = EntityUtils.toString(entity);
            return new ResponseBodyRecord(response, body);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ResponseBodyRecord getRequest(HttpClient client) {
        HttpGet request = new HttpGet(URL);
        HttpResponse response;
        String body;
        try {
            response = client.execute(request);
            System.out.println("GET response status: " + response.getStatusLine());
            body = EntityUtils.toString(response.getEntity());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ResponseBodyRecord(response, body);
    }

    private static List<ToDoItem> getResponseEntityAsListOfToDoItems(String body) {
        if (body == null) return null;
        List<ToDoItem> listToDo = new ArrayList<>();
        try {
            listToDo = mapper.readValue(body, List.class);
        } catch (Exception e) {
            System.err.println("Ошибка при разборе body. " + e.toString());
        }
        return listToDo;
    }
}

