package extentions;

import client.ToDoClient;
import model.CreateToDo;
import model.ToDoItem;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.provider.MethodSource;
import ru.inno.todo.apache.ToDoClientApache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/*
 * Провайдер позволяет:
 * 1. За счёт имплементации интерфейса ParameterResolver выполнить какие-то действия перед тестом
 * и передать в тест входные данные, к которым провайдер привязан
 * 2. За счёт имплементации интерфейса AfterAllCallback выполнять действия по окончанию теста (например,
 * удаление тестовых данных)
 * 3. В аннотации @ToDoItemNumber передаётся:
 *  - количество необходимых ToDoItem
 *  - признак необходимости их удаления после выполнения теста */

public class ListToDoProvider implements ParameterResolver, AfterEachCallback {
    private static final String URL = "https://todo-app-sky.herokuapp.com";
    private final ToDoClient client = new ToDoClientApache(URL);
    private final static String TITLE = "Задача 1";
    //    private int id;
    private List<ToDoItem> listCreated = new ArrayList<>();
    private List<ToDoItem> listBefore;
    private int listSizeToCreate;
    boolean needToDeleteCreatedAfter;


    //Метод, который проверяет, надо ли выполнять этот провайдер
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        //Проверяем, что в связанном тесте есть переменная типа ToDoItem.class.
        // Если она есть, то возвращаем true и будет выполнен метод resolveParameter
        // для возврата нужного значения переменной для теста
        ToDoItemNumber count = parameterContext.getParameter().getAnnotation(ToDoItemNumber.class); //Получение аннотации с @ToDoItemNumber  с количеством необходимых Item
        boolean ret = parameterContext.getParameter().getType().equals(List.class) && count != null;
        if (ret) {
            listSizeToCreate = count.count();
            needToDeleteCreatedAfter = count.needToDeleteCreatedAfter();
        }
        return ret;
    }


    //Метод, который выполняет подготовку данных для теста
    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        try {
            listBefore = client.getAll();
            for (int i = 0; i < listSizeToCreate; i++) {
                CreateToDo createToDo = new CreateToDo();
                createToDo.setTitle("Задача " + i);
                listCreated.add(i, client.create(createToDo));
            }
//            return new ListToDoBeforeAndCreated(listBefore, listCreated);
            return listCreated;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        if (needToDeleteCreatedAfter) {
            for (ToDoItem toDoItem : listCreated) {
                client.deleteById(toDoItem.getId());
            }
        }
    }
}
