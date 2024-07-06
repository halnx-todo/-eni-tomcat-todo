package net.diehard.sample.todowebsite.todo;

import jakarta.validation.Valid;

import java.util.List;

public class TodoListViewModel {

    @Valid
    private List<TodoItem> todoList;

    public TodoListViewModel(List<TodoItem> todoList) {
        this.todoList = todoList;
    }

    public List<TodoItem> getTodoList() {
        return todoList;
    }

    public void setTodoList(List<TodoItem> todoList) {
        this.todoList = todoList;
    }

}
