import client.ToDoClient;
import extentions.*;
import model.CreateToDo;
import model.ToDoItem;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.inno.todo.apache.ToDoClientApache;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты бизнес-логики:")
public class ToDoBusinessTest {

    /*Провёл небольшое исследование по поводу параллельного запуска тестов.
    * 1. Включил параллельное выполнение следующим образом:
    * 1.1. создал файл настроек JUnit  */

    /*Тесты на бизнес-логику:
    Позитивные:
    1. Создание задачи +
    2. Переименование задачи +
    3. Отметка задачи выполненной и невыполненной +
    4. Удаление одной задачи по id +
    5. Удаление всех задач +
    6. Получение всего списка задач +
    7. Получение одной задачи по id +
    8. Создание дубликата задачи +
    9. Добавление 50 задач +
    10. Создание задачи пустой задачи ("", " ", null)

    Негативные:
    1н. Повторное удаление задачи по id

    */
    private ToDoClient client;
    private final String TEST_TASK_1_TITLE = "Задача 0";

    @BeforeEach
    public void setUp() throws IOException {
        client = new ToDoClientApache("https://todo-app-sky.herokuapp.com");
    }

    @Test
    @Tag("Positive")
    @ExtendWith({ListToDoItemsBeforeProvider.class, ListToDoProvider.class})
    @DisplayName("1. Создание задачи")
    public void shouldCreateTask(List<ToDoItem> listBefore, @ToDoItemNumber List<ToDoItem> items) throws IOException {
        // получить список задач
        ToDoItem item = items.get(0);

        // проверить, что задача отображается в списке
        assertFalse(item.getUrl().isBlank());
        assertFalse(item.isCompleted());
        assertTrue(item.getId() > 0);
        assertEquals(TEST_TASK_1_TITLE, item.getTitle());
        // TODO: bug report. Oreder is null
        assertEquals(0, item.getOrder());
        // задач стало на 1 больше
        List<ToDoItem> listAfter = client.getAll();
//        assertEquals(1, listAfter.size() - listBefore.size());

        // проверить еще и по id
        ToDoItem single = client.getById(item.getId());
        assertEquals(TEST_TASK_1_TITLE, single.getTitle());

        //Очистка от тестовых данных
//        deleteTestTask1();    //Очищается автоматически через AfterEachCallback в ListToDoProvider
    }

    @Test
    @Tag("Positive")
    @ExtendWith(ListToDoProvider.class)
    @DisplayName("2. Переименование задачи")
    public void shouldRenameItemById(@ToDoItemNumber List<ToDoItem> items) throws IOException {
        ToDoItem item = items.get(0);        //Теперь передаётся от SingleToDoProvider при запуске теста

        //Проверить, что задача с нужным именем создана
        assertEquals(item.getTitle(), client.getById(item.getId()).getTitle());

        //Проверить, что задача с нужным именем переименована
        String titleExpected = "Задача 2";
        client.renameById(item.getId(), titleExpected);
        assertEquals(titleExpected, client.getById(item.getId()).getTitle());
    }

    @Test
    @Tag("Positive")
    @ExtendWith(ListToDoProvider.class)
    @DisplayName("3. Отметка задачи выполненной и невыполненной")
    public void shouldMarkItemDoneById(@ToDoItemNumber List<ToDoItem> items) throws IOException {
        ToDoItem item = items.get(0);
        String title = item.getTitle();

        //Проверить, что задача с нужным именем создана и у неё completed = false
        assertEquals(title, item.getTitle());
        assertFalse(item.isCompleted());

        //Проверить, что задача с нужным именем помечена выполненной completed = true
        client.markCompleted(item.getId(), true);

        item = client.getById(item.getId());
        assertEquals(title, item.getTitle());
        assertTrue(item.isCompleted());

        //Проверить, что задача с нужным именем помечена невыполненной completed = false
        client.markCompleted(item.getId(), false);

        item = client.getById(item.getId());
        assertEquals(title, item.getTitle());
        assertFalse(item.isCompleted());
    }

    @Test
    @Tag("Positive")
    @ExtendWith(ListToDoProvider.class)
    @DisplayName("4. Удаление одной задачи по id")
    public void shouldDeleteItemByID(@ToDoItemNumber(needToDeleteCreatedAfter = false) List<ToDoItem> listCreated)
            throws IOException {
        // получить список задач
        List<ToDoItem> listBefore = client.getAll();

        deleteTestTask(listCreated.get(0).getId());

        List<ToDoItem> listAfter = client.getAll();

        //Проверяем, что список сократился на 1 задачу
//        assertEquals(1, listBefore.size() - listAfter.size());

        //Проверяем, что задачу нельзя найти по ID
        assertNull(client.getById(listCreated.get(0).getId()));
    }

    @Test
    @Disabled
    @Tag("Positive")
    @Tag("Destructive")
    @ExtendWith({ListToDoItemsBeforeProvider.class, ListToDoProvider.class})
    @DisplayName("5. Удаление всех задач")
    public void shouldDeleteAll(List<ToDoItem> listBefore,
                                @ToDoItemNumber(count = 5, needToDeleteCreatedAfter = false) List<ToDoItem> listCreated)
            throws IOException {

        //Проверка, что было создано именно 5 элементов
        assertEquals(5, listCreated.size());

        List<ToDoItem> listAsIs = client.getAll();

        //Проверка, что размеры массивов различаются на 5 элементов
        assertEquals(5, listAsIs.size() - listBefore.size());

        //Проверка, что все элементы массива созданных задач находятся в массиве полученных задач
        assertTrue(listAsIs.containsAll(listCreated));

        //Удаление всех задач
        client.deleteAll();

        //Проверка, что все задачи удалены
        assertEquals(0, client.getAll().size());

        //Проверка, что каждая задача удалена
        for (int i = 0; i < 5; i++) {
            assertNull(client.getById(listCreated.get(i).getId()));
        }
    }

    @Test
    @Tag("Positive")
    @ExtendWith(ListToDoItemsBeforeProvider.class)
    @ExtendWith(ListToDoProvider.class)
    @DisplayName("6. Получение всего списка задач")
    public void shouldGetAll(List<ToDoItem> listBefore,
                             @ToDoItemNumber(count = 5) List<ToDoItem> listCreated) throws IOException {

        //Получение списка задач
        List<ToDoItem> listAsIs = client.getAll();

        //Проверка, что размеры массивов одинаковые
//        assertEquals(5, listAsIs.size() - listBefore.size());

        //Проверка, что все элементы массива созданных задач находятся в массиве полученных задач
        assertTrue(listAsIs.containsAll(listCreated));
    }

    @Test
    @Tag("Positive")
    @ExtendWith(ListToDoProvider.class)
    @DisplayName("7. Получение одной задачи по id ")
    public void shouldGetItemByID(@ToDoItemNumber List<ToDoItem> items) throws IOException {
        //Получение задачи по ID
        ToDoItem item = client.getById(items.get(0).getId());

        // проверить, что задача отображается в списке
        assertFalse(item.getUrl().isBlank());
        assertFalse(item.isCompleted());
        assertTrue(item.getId() > 0);
        assertEquals(TEST_TASK_1_TITLE, item.getTitle());
        // TODO: bug report. Oreder is null
        assertEquals(0, item.getOrder());
    }

    @Test
    @Tag("Positive")
    @ExtendWith(ListToDoProvider.class)
    @DisplayName("8. Создание дубликата задачи")
    public void shouldCreateDuplicateItem(@ToDoItemNumber List<ToDoItem> items1,
                                          @ToDoItemNumber List<ToDoItem> items2) {
        ToDoItem item1 = items1.get(0);
        ToDoItem item2 = items1.get(1);

        // проверить, что у задачи 1 и задачи 2 совпадают: title, order, completed
        assertNotEquals(item1.getId(), item2.getId());
        assertNotEquals(item1.getUrl(), item2.getUrl());
        assertEquals(item1.getTitle(), item2.getTitle());
        assertEquals(item1.getOrder(), item2.getOrder());
        assertEquals(item1.isCompleted(), item2.isCompleted());
    }

    @Test
    @Tag("Positive")
    @ExtendWith({ListToDoItemsBeforeProvider.class, ListToDoProvider.class})
    @DisplayName("9. Добавление 50 задач")
    public void shouldCreate50Item(List<ToDoItem> listStart,
                                   @ToDoItemNumber(count = 50) List<ToDoItem> listCreated) throws IOException {

        //Получение списка задач
        List<ToDoItem> listAsIs = client.getAll();

        //Проверка, что размеры массивов отличаются на 50 элементов
//        assertEquals(50, listAsIs.size() - listStart.size());

        //Проверка, что все элементы массива созданных задач находятся в массиве полученных задач
        assertTrue(listAsIs.containsAll(listCreated));
    }

    @ParameterizedTest(name = "Строка названия = {0}")
    @MethodSource("getEmptyTitle")
    @Tag("Positive")
    @DisplayName("10. Создание задачи пустой задачи (\"\", \" \", \"null\")")
    public void shouldCreateEmptyTask(String emptyTitle) throws IOException {
        // получить список задач
        List<ToDoItem> listBefore = client.getAll();
        CreateToDo createToDo = new CreateToDo();
        createToDo.setTitle(emptyTitle);
        ToDoItem item = client.create(createToDo);

        // проверить, что задача отображается в списке
        assertFalse(item.getUrl().isBlank());
        assertFalse(item.isCompleted());
        assertTrue(item.getId() > 0);
        assertEquals(emptyTitle, item.getTitle());
        // TODO: bug report. Oreder is null
        assertEquals(0, item.getOrder());
        // задач стало на 1 больше
        List<ToDoItem> listAfter = client.getAll();
//        assertEquals(1, listAfter.size() - listBefore.size());

        // проверить еще и по id
        ToDoItem single = client.getById(item.getId());
        assertEquals(emptyTitle, single.getTitle());

        //Очистка от тестовых данных
        client.deleteById(item.getId());
    }

    private static String[] getEmptyTitle() {
        return new String[]{"", " ", "null"};
    }

    //-----------------------------------------------------------------------------------------------------------------
    //Негативные тесты
    //-----------------------------------------------------------------------------------------------------------------

    private void deleteTestTask(int testTaskId) throws IOException {
        client.deleteById(testTaskId);
    }

}
