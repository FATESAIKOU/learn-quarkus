package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class TodoService {
    @Inject
    TodoMapper todoMapper;

    public List<Todo> listAll() {
        return todoMapper.listAll();
    }

    public Todo findById(Integer id) {
        return todoMapper.findById(id);
    }

    public Todo create(Todo todo) {
        todoMapper.insert(todo);
        return todo;
    }

    public boolean update(Integer id, Todo todo) {
        todo.setId(id);
        return todoMapper.update(todo) > 0;
    }

    public boolean delete(Integer id) {
        return todoMapper.delete(id) > 0;
    }
}
