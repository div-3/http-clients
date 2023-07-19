package extentions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ToDoItemNumber {
    int count() default 1;  //Количество ToDoItem для ParameterResolver

    boolean needToDeleteCreatedAfter() default true;    //Надо ли удалять созданные данные после теста
}
