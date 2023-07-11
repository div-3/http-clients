import client.ToDoClient;
import model.CreateToDo;
import model.ToDoItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.inno.todo.apache.ToDoClientApache;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ToDoBusinessTest {
    private ToDoClient client;

    @BeforeEach
    public void setUp() throws IOException {
        client = new ToDoClientApache("https://todo-app-sky.herokuapp.com");
    }

    @Test
    @DisplayName("Проверяем, что задача создается")
    public void shouldCreateTask() throws IOException {
        // получить список задач
        List<ToDoItem> listBefore = client.getAll();
        // создать задачу
        CreateToDo todo = new CreateToDo();
        String title = "My super task";
        todo.setTitle(title);
        ToDoItem newItem = client.create(todo);

        // проверить, что задача отображается в списке
        assertFalse(newItem.getUrl().isBlank());
        assertFalse(newItem.isCompleted());
        assertTrue(newItem.getId() > 0);
        assertEquals(title, newItem.getTitle());
        // TODO: bug report. Oreder is null
        assertEquals(0, newItem.getOrder());
        // задач стало на 1 больше
        List<ToDoItem> listAfter = client.getAll();
        assertEquals(1, listAfter.size() - listBefore.size());

        // проверить еще и по id
        ToDoItem single = client.getById(newItem.getId());
        assertEquals(title, single.getTitle());
    }

    @Test
    @DisplayName("Проверка, что задача переименовывается")
    public void shouldRenameItemById() throws IOException {
        String title1 = "Задача 1";
        String titleExpected = "Задача 2";
        CreateToDo createToDo = new CreateToDo();
        createToDo.setTitle(title1);
        ToDoItem item = client.create(createToDo);
        assertEquals(title1, client.getById(item.getId()).getTitle());
        client.renameById(item.getId(), titleExpected);
        assertEquals(titleExpected, client.getById(item.getId()).getTitle());
    }

}
