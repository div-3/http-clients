package extentions;

import client.ToDoClient;
import model.CreateToDo;
import model.ToDoItem;
import org.junit.jupiter.api.extension.*;
import ru.inno.todo.apache.ToDoClientApache;

import java.io.IOException;
/*
* Провайдер позволяет:
* 1. За счёт имплементации интерфейса ParameterResolver выполнить какие-то действия перед тестом
* и передать в тест входные данные, к которым провайдер привязан
* 2. За счёт имплементации интерфейса AfterAllCallback выполнять действия по окончанию теста (например,
* удаление тестовых данных) */

public class SingleToDoProvider implements ParameterResolver, AfterAllCallback {
    private static final String URL = "https://todo-app-sky.herokuapp.com";
    private final ToDoClient client = new ToDoClientApache(URL);
    private final static String TITLE = "Задача 1";
    private int id;


    //Метод который проверяет, надо ли выполнять этот провайдер
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        //Проверяем, что в связанном тесте есть переменная типа ToDoItem.class.
        // Если она есть, то возвращаем true и будет выполнен метод resolveParameter
        // для возврата нужного значения перемнной для теста
        return parameterContext.getParameter().getType().equals(ToDoItem.class);
    }


    //Метод который выполняет подготовку данных для теста
    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        CreateToDo createToDo = new CreateToDo();
        createToDo.setTitle(TITLE);
        try {
            ToDoItem item = client.create(createToDo);
            id = item.getId();      //Сохраняем id созданной задачи для её удаления после теста.
            return item;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        client.deleteById(id);
    }
}
