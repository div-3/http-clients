import client.ToDoClient;
import model.CreateToDo;
import model.ToDoItem;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.inno.todo.apache.ToDoClientApache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ToDoBusinessTest {

    /*Тесты на бизнес-логику:
    Позитивные:
    1. Создание задачи +
    2. Переименование задачи +
    3. Отметка задачи выполненной и невыполненной +
    4. Удаление одной задачи по id -
    5. Удаление всех задач
    6. Получение всего списка задач
    7. Получение одной задачи по id
    8. Создание дубликата задачи

    Негативные:
    1. Создание пустой задачи ("", " ", null, скрипт, без тела)
    2. Переименование задачи на недопустимое значение ("", " ", null, скрипт, без тела)
    3. Переименование задачи с неправильным id (добавочный URL: 0, -1, maxInteger, double, boolean, String, "", " ")
    3. Отметка задачи выполненной с недопустимым телом ("", " ", null, скрипт, без тела)
    4. Удаление задачи с неправильным id (добавочный URL: 0, -1, maxInteger, double, boolean, String, "", " ")
    5. Повторное удаление задачи по id
    6. Получение всего списка задач с неправильными параметрами (присутствует тело в запросе GET)
    7. Получение одной записи с неправильным id (добавочный URL: 0, -1, maxInteger, double, boolean, String, "", " ")
    8. Получение одной задачи по id с неправильными параметрами (присутствует тело в запросе GET)
    */
    private ToDoClient client;
    private final String TEST_TASK_1_TITLE = "Задача 1";
    private int testTask1Id;

    @BeforeEach
    public void setUp() throws IOException {
        client = new ToDoClientApache("https://todo-app-sky.herokuapp.com");
    }

    @Test
    @Tag("Positive")
    @DisplayName("1. Создание задачи")
    //    1. Создание задачи
    public void shouldCreateTask() throws IOException {
        // получить список задач
        List<ToDoItem> listBefore = client.getAll();
        ToDoItem item = createTestTask1();

        // проверить, что задача отображается в списке
        assertFalse(item.getUrl().isBlank());
        assertFalse(item.isCompleted());
        assertTrue(item.getId() > 0);
        assertEquals(TEST_TASK_1_TITLE, item.getTitle());
        // TODO: bug report. Oreder is null
        assertEquals(0, item.getOrder());
        // задач стало на 1 больше
        List<ToDoItem> listAfter = client.getAll();
        assertEquals(1, listAfter.size() - listBefore.size());

        // проверить еще и по id
        ToDoItem single = client.getById(item.getId());
        assertEquals(TEST_TASK_1_TITLE, single.getTitle());

        //Очистка от тестовых данных
        deleteTestTask1();
    }

    @Test
    @Tag("Positive")
    @DisplayName("2. Переименование задачи")
    public void shouldRenameItemById() throws IOException {
        ToDoItem item = createTestTask1();

        //Проверить, что задача с нужным именем создана
        assertEquals(TEST_TASK_1_TITLE, client.getById(item.getId()).getTitle());

        //Проверить, что задача с нужным именем переименована
        String titleExpected = "Задача 2";
        client.renameById(item.getId(), titleExpected);
        assertEquals(titleExpected, client.getById(item.getId()).getTitle());

        //Очистка от тестовых данных
        deleteTestTask1();
    }

    @Test
    @Tag("Positive")
    @DisplayName("3. Отметка задачи выполненной и невыполненной")
    public void shouldMarkItemDoneById() throws IOException {
        ToDoItem item = createTestTask1();

        //Проверить, что задача с нужным именем создана и у неё completed = false
        assertEquals(TEST_TASK_1_TITLE, item.getTitle());
        assertFalse(item.isCompleted());

        //Проверить, что задача с нужным именем помечена выполненной completed = true
        client.markCompleted(item.getId(),true);

        item = client.getById(item.getId());
        assertEquals(TEST_TASK_1_TITLE, item.getTitle());
        assertTrue(item.isCompleted());

        //Проверить, что задача с нужным именем помечена выполненной completed = false
        client.markCompleted(item.getId(),false);

        item = client.getById(item.getId());
        assertEquals(TEST_TASK_1_TITLE, item.getTitle());
        assertFalse(item.isCompleted());

        //Очистка от тестовых данных
        deleteTestTask1();
    }
    @Test
    @Tag("Positive")
    @DisplayName("4. Удаление одной задачи по id")
    public void shouldDeleteItemByID() throws IOException {
        // получить список задач
        ToDoItem item = createTestTask1();
        List<ToDoItem> listBefore = client.getAll();
        for (ToDoItem i : listBefore) {
            System.out.println(i.getTitle());
        }
        deleteTestTask1();
        List<ToDoItem> listAfter = client.getAll();
        for (ToDoItem i : listAfter) {
            System.out.println(i.getTitle());
        }

        assertEquals(1, listBefore.size() - listAfter.size());
    }

    @Test
    @Tag("Positive")
    @DisplayName("5. Удаление всех задач")
    public void shouldDeleteAll() throws IOException {
        //Создаём 5 задач
        ArrayList<ToDoItem> listBefore = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            CreateToDo createToDo = new CreateToDo();
            createToDo.setTitle("Задача " + i);
            listBefore.add(i, client.create(createToDo));
            assertEquals(listBefore.get(i).getTitle(), client.getById(listBefore.get(i).getId()).getTitle());
        }

        //Удаление всех задач
        client.deleteAll();

        //Проверка, что все задачи удалены
        assertEquals(0, client.getAll().size());

        //Проверка, что каждая задача удалена
        for (int i = 0; i < 5; i++) {
            assertNull(client.getById(listBefore.get(i).getId()));
        }
    }

    private ToDoItem createTestTask1() throws IOException {
        // создать задачу
        CreateToDo todo = new CreateToDo();
        todo.setTitle(TEST_TASK_1_TITLE);
        ToDoItem item = client.create(todo);
        testTask1Id = item.getId();
        return item;
    }

    private void deleteTestTask1() throws IOException {
        client.deleteById(testTask1Id);
    }

}
