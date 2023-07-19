package extentions;

import client.ToDoClient;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import ru.inno.todo.apache.ToDoClientApache;

import java.io.IOException;
import java.util.List;

public class ListToDoItemsBeforeProvider implements ParameterResolver {
    private static final String URL = "https://todo-app-sky.herokuapp.com";
    private final ToDoClient client = new ToDoClientApache(URL);

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        if (parameterContext.getParameter().getName().equals("arg0")    //Проверка, что данный параметр стоитна первом месте
                && parameterContext.getParameter().getType().equals(List.class)) {
            return true;
        }
        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        try {
            return client.getAll();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
