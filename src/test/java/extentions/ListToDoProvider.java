package extentions;

import client.ToDoClient;
import model.CreateToDo;
import model.ToDoItem;
import org.junit.jupiter.api.extension.*;
import ru.inno.todo.apache.ToDoClientApache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/*
* Провайдер позволяет:
* 1. За счёт имплементации интерфейса ParameterResolver выполнить какие-то действия перед тестом
* и передать в тест входные данные, к которым провайдер привязан
* 2. За счёт имплементации интерфейса AfterAllCallback выполнять действия по окончанию теста (например,
* удаление тестовых данных) */

public class ListToDoProvider implements ParameterResolver, AfterEachCallback {
    private static final String URL = "https://todo-app-sky.herokuapp.com";
    private final ToDoClient client = new ToDoClientApache(URL);
    private final static String TITLE = "Задача 1";
//    private int id;
    private List<ToDoItem> list = new ArrayList<>();
    private int listSizeToCreate;


    //Метод, который проверяет, надо ли выполнять этот провайдер
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        //Проверяем, что в связанном тесте есть переменная типа ToDoItem.class.
        // Если она есть, то возвращаем true и будет выполнен метод resolveParameter
        // для возврата нужного значения переменной для теста
//        boolean ret = parameterContext.getParameter().getType().equals(ToDoItem.class);
//        ret = parameterContext.getParameter().getType().isArray();
//        ret = parameterContext.getParameter().getType().arrayType().equals("java.util.List");
        boolean ret = true;
        if (ret) {
            String name = parameterContext.getParameter().getName();
            listSizeToCreate = Integer.parseInt(name.substring(name.indexOf('0'),3));
        }
        return ret;
    }


    //Метод, который выполняет подготовку данных для теста
    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        try {
            for (int i = 0; i < listSizeToCreate; i++) {
                CreateToDo createToDo = new CreateToDo();
                createToDo.setTitle("Задача " + i);
                list.add(i, client.create(createToDo));
            }
            return list;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        for (int i = 0; i < list.size(); i++) {
            client.deleteById(list.get(i).getId());
        }
    }
}
