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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.inno.todo.apache.MyRequestInterceptor;
import ru.inno.todo.apache.MyResponseInterceptor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*  Негативные тесты:
    1н. Отказ в создание задачи (скрипт, без тела)
    2н. Переименование задачи на недопустимое значение (скрипт, без тела)
    3н. Переименование задачи с неправильным id (добавочный URL: 0, -1, maxInteger, double, boolean, String, "") +
    4н. Отметка задачи выполненной с недопустимым телом ("", " ", null, скрипт, без тела)
    5н. Удаление задачи с неправильным id (добавочный URL: 0, -1, maxInteger, double, boolean, String, "", " ")
    6н. Получение всего списка задач с неправильными параметрами (присутствует тело в запросе GET)
    7н. Получение одной записи с неправильным id (добавочный URL: 0, -1, maxInteger, double, boolean, String, "", " ")
    8н. Получение одной задачи по id с неправильными параметрами (присутствует тело в запросе GET)
    */

@DisplayName("Тесты контракта: ")
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

    @DisplayName("3.1. Переименование задачи с неправильным id (SC 400)")
    @Tag("Negative")
    @ParameterizedTest(name = "Добавочный URL = {0}")
    @MethodSource("getWrongIdSc400")
    public void shouldNotPatchItemWithWrongIdStatus400(String id) throws IOException {
        HttpPatch httpPatch = new HttpPatch(URL + "/" + id);
        HttpResponse response = client.execute(httpPatch);
        assertEquals(400, response.getStatusLine().getStatusCode());
    }

    @DisplayName("3.2. Переименование задачи с неправильным id (SC 404)")
    @Tag("Negative")
    @ParameterizedTest(name = "Добавочный URL = {0}")
    @MethodSource("getWrongIdSc404")
    public void shouldNotPatchItemWithWrongIdStatus404(String id) throws IOException {
        HttpPatch httpPatch = new HttpPatch(URL + "/" + id);
        HttpResponse response = client.execute(httpPatch);
        assertEquals(404, response.getStatusLine().getStatusCode());
    }

    @ParameterizedTest(name = "Номер заказа = {0}")
    @MethodSource("getWrongOrders")
    @Tag("Negative")
    @DisplayName("1н. Отказ в создание задачи (скрипт, без тела)")
    public void tes(int i) {

    }

    private static String[] getWrongIdSc400() {
        Integer max = Integer.MAX_VALUE;
        return new String[]{"0", "-1", max.toString(), "3.14", "true", "false", "test", "-", "_", ".", "~", "'", ":"};
    }

    private static String[] getWrongIdSc404() {
        return new String[]{"", "?", "*", "(", ")", ";", "@", "&", "=", "+", "$", ",", "/", "?", "#"};
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
