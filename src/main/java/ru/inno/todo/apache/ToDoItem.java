package ru.inno.todo.apache;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class ToDoItem {
    //{"id":84794,"title":"abracadabra","completed":false,"order":null,"url":"/84794"}
    private int id;
    private String title;
    private boolean completed;
    private int order;
    private String url;

    public ToDoItem() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof ToDoItem toDoItem)) return false;
        return getId() == toDoItem.getId() && isCompleted() == toDoItem.isCompleted() && getOrder() == toDoItem.getOrder() && Objects.equals(getTitle(), toDoItem.getTitle()) && Objects.equals(getUrl(), toDoItem.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), isCompleted(), getOrder(), getUrl());
    }

    @Override
    public String toString() {
        return "ToDoItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", completed=" + completed +
                ", order=" + order +
                ", url='" + url + '\'' +
                '}';
    }
}
