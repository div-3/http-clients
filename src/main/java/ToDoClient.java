

import model.ToDoItem;
import model.CreateToDO;

import java.io.IOException;
import java.util.List;

public interface ToDoClient {

    List<ToDoItem> getAll() throws IOException;

    ToDoItem getById(int id);

    ToDoItem create(CreateToDO createToDo) throws IOException;

    void deleteById(int id);

    ToDoItem renameById(int id, String newName);

    ToDoItem markCompleted(int id, boolean completed);

    void deleteAll() throws IOException;
}
