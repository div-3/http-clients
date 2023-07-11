package model;

import java.util.Objects;

public class CreateToDo {
    private String title;

    public CreateToDo() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CreateToDo that)) return false;
        return Objects.equals(getTitle(), that.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle());
    }

    @Override
    public String toString() {
        return "CreateToDO{" +
                "title='" + title + '\'' +
                '}';
    }
}
