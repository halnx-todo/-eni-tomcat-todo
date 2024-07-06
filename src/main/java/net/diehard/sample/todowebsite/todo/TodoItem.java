package net.diehard.sample.todowebsite.todo;

import jakarta.persistence.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.Objects;

@Entity
public class TodoItem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String category;
    private String name;
    private boolean onlyinsession;
    private boolean complete;
    //This annonation is just there
    // to be an example off such configuration
    //@Basic(fetch = FetchType.EAGER)
    private String filename;
    @Transient
    private MultipartFile file;
    @Transient
    private boolean delete;

    public TodoItem() {
    }

    public TodoItem(String category, String name) {
        this.category = category;
        this.name = name;
        this.onlyinsession = false;
        this.complete = false;
        this.delete = false;
    }

    @Override
    public String toString() {
        return String.format(
                "TodoItem[id=%d, category='%s', name='%s', filename=%s, onlyinsession='%b', complete='%b']",
                id, category, name, filename, onlyinsession, complete);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TodoItem) {
            return Objects.equals(this.getId(), ((TodoItem) o).getId());
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isOnlyinsession() {
        return onlyinsession;
    }

    public void setOnlyinsession(boolean onlyinsession) {
        this.onlyinsession = onlyinsession;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

}
