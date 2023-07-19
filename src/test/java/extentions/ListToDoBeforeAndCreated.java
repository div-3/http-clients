package extentions;

import model.ToDoItem;

import java.util.List;

public record ListToDoBeforeAndCreated(List<ToDoItem> listBeforeCreation, List<ToDoItem> listCreatedItems) {
}
