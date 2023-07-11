package model;

import java.util.Objects;

public class CreateToDO {
    private String title;

    public CreateToDO() {
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
        if (!(o instanceof CreateToDO that)) return false;
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
