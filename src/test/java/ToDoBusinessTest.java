import client.ToDoClient;
import model.CreateToDo;
import model.ToDoItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.inno.todo.apache.ToDoClientApache;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ToDoBusinessTest {

    /*Тесты на бизнес-логику:
    Позитивные:
    1. Создание задачи
    2. Переименование задачи
    3. Отметка задачи выполненной
    4. Снятие отметки выполнено
    5. Удаление одной задачи по id
    6. Удаление всех задач
    7. Получение всего списка задач
    8. Получение одной задачи по id
    9. Создание дубликата задачи

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

    @BeforeEach
    public void setUp() throws IOException {
        client = new ToDoClientApache("https://todo-app-sky.herokuapp.com");
    }

    @Test
    @Tag("Positive")
    @DisplayName("Проверяем, что задача создается")
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
        client.deleteById(single.getId());
    }

    @Test
    @Tag("Positive")
    @DisplayName("Проверка, что задача переименовывается")
    //    2. Переименование задачи
    public void shouldRenameItemById() throws IOException {
        ToDoItem item = createTestTask1();

        //Проверить, что задача с нужным именем создана
        assertEquals(TEST_TASK_1_TITLE, client.getById(item.getId()).getTitle());

        //Проверить, что задача с нужным именем переименована
        String titleExpected = "Задача 2";
        client.renameById(item.getId(), titleExpected);
        assertEquals(titleExpected, client.getById(item.getId()).getTitle());

        //Очистка от тестовых данных
        client.deleteById(item.getId());
    }

    @Test
    @Tag("Positive")
    @DisplayName("Проверка, что задача отмечается выполненной")
    //    3. Отметка задачи выполненной
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

        //Очистка от тестовых данных
        client.deleteById(item.getId());
    }

    private ToDoItem createTestTask1() throws IOException {
        // создать задачу
        CreateToDo todo = new CreateToDo();
        todo.setTitle(TEST_TASK_1_TITLE);
        return client.create(todo);
    }

}
