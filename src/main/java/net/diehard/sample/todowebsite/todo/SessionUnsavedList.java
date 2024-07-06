package net.diehard.sample.todowebsite.todo;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;


@Component
public class SessionUnsavedList extends ArrayList<TodoItem> implements Serializable, BeanPostProcessor {

}
