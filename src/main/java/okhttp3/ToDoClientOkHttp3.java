package okhttp3;



import client.ToDoClient;
import model.CreateToDo;
import model.ToDoItem;
import java.io.IOException;
import java.util.List;

public class ToDoClientOkHttp3 implements ToDoClient {
    @Override
    public List<ToDoItem> getAll() throws IOException {
        return null;
    }

    @Override
    public ToDoItem getById(int id) {
        return null;
    }

    @Override
    public ToDoItem create(CreateToDo createToDo) throws IOException {
        return null;
    }

    @Override
    public void deleteById(int id) {

    }

    @Override
    public ToDoItem renameById(int id, String newName) {
        return null;
    }

    @Override
    public ToDoItem markCompleted(int id, boolean completed) {
        return null;
    }

    @Override
    public void deleteAll() {

    }
}
