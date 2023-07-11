import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.inno.todo.apache.MyRequestInterceptor;
import ru.inno.todo.apache.MyResponseInterceptor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ToDoContractTest {
    private static final String URL = "https://todo-app-sky.herokuapp.com";

    private HttpClient client;

    @BeforeEach
    public void setUp() {
        client = HttpClientBuilder
                .create()
                .addInterceptorLast(new MyRequestInterceptor())
                .addInterceptorFirst(new MyResponseInterceptor())
                .build();
    }

    @Test
    @DisplayName("Получение списка задач. Проверяем статус-код и заголовок Content-Type")
    public void shouldReceive200OnGetRequest() throws IOException {
        // Запрос
        HttpGet getListReq = new HttpGet(URL);
        // Получить ответ
        HttpResponse response = client.execute(getListReq);
        String body = EntityUtils.toString(response.getEntity());

        // Проверить поля ответа
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(1, response.getHeaders("Content-Type").length);
        assertEquals("application/json; charset=utf-8", response.getHeaders("Content-Type")[0].getValue());
        assertTrue(body.startsWith("["));
        assertTrue(body.endsWith("]"));
    }

    @Test
    @DisplayName("Создание задачи. Проверяем статус-код, заголовок Content-Type и тело ответа содержит json")
    public void shouldReceive201OnPostRequest() throws IOException {
        HttpResponse response = createNewTask();
        String body = EntityUtils.toString(response.getEntity());

        // Проверить поля ответа
        assertEquals(201, response.getStatusLine().getStatusCode());
        assertEquals(1, response.getHeaders("Content-Type").length);
        assertEquals("application/json; charset=utf-8", response.getHeaders("Content-Type")[0].getValue());
        assertTrue(body.startsWith("{"));
        assertTrue(body.endsWith("}"));
    }

    @Test
    @DisplayName("Создание задачи с пустым телом запроса. Статус-код = 400, в теле ответа есть сообщение об ошибке")
    public void shouldReceive400OnEmptyPost() throws IOException {
        // Запрос
        HttpPost createItemReq = new HttpPost(URL);

        // Получить ответ
        HttpResponse response = client.execute(createItemReq);
        String bodyAsIs = EntityUtils.toString(response.getEntity());
        String bodyToBe = "{\"status\":400,\"message\":\"Invalid JSON\"}";

        // Проверить поля ответа
        assertEquals(400, response.getStatusLine().getStatusCode());
        assertEquals(1, response.getHeaders("Content-Type").length);
        assertEquals("application/json; charset=utf-8", response.getHeaders("Content-Type")[0].getValue());
        assertEquals(bodyToBe, bodyAsIs);
    }

    @Test
    @DisplayName("Удаляет существующую задачу. Статус 204, Content-Length=0")
    public void shouldReceive204OnDelete() throws IOException {
        // создать задачу, которую будем удалять
        HttpResponse newTask = createNewTask();
        String body = EntityUtils.toString(newTask.getEntity());
        String id = "/" + body.substring(6, 11);

        HttpDelete deleteTaskReq = new HttpDelete(URL + id);
        HttpResponse response = client.execute(deleteTaskReq);
        assertEquals(204, response.getStatusLine().getStatusCode());
        assertEquals(1, response.getHeaders("Content-Length").length);
        assertEquals("0", response.getHeaders("Content-Length")[0].getValue());
    }

    private HttpResponse createNewTask() throws IOException {
        // Запрос
        HttpPost createItemReq = new HttpPost(URL);

        String myContent = "{\"title\" : \"test\"}";
        StringEntity entity = new StringEntity(myContent, ContentType.APPLICATION_JSON);
        createItemReq.setEntity(entity);

        // Получить ответ
        return client.execute(createItemReq);
    }
}
