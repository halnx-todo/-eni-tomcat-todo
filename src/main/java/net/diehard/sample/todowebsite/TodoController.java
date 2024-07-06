package net.diehard.sample.todowebsite;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import net.diehard.sample.todowebsite.todo.SessionUnsavedList;
import net.diehard.sample.todowebsite.todo.TodoItem;
import net.diehard.sample.todowebsite.todo.TodoItemRepository;
import net.diehard.sample.todowebsite.todo.TodoListViewModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@Controller
@ComponentScan
public class TodoController implements Serializable{


    private static final Logger LOG = Logger.getLogger(TodoController.class.getCanonicalName());
    @Serial
    private static final long serialVersionUID = 7646230512286143109L;

    @Value("${spring.servlet.multipart.location}")
    private String storageLocation;

    private final TodoItemRepository repository;

    private final SessionUnsavedList todoUnsavedList;

    private long sessionIndex = -1;

    public TodoController(TodoItemRepository repository, SessionUnsavedList todoUnsavedList) {
        this.repository = repository;
        this.todoUnsavedList = todoUnsavedList;
    }

    @RequestMapping("/")
    public String index(HttpSession session, Model model) {
        LOG.info("/ called ");
        List<TodoItem> todoList = repository.findAll();
        LOG.info("requestItems Persisted : " + todoList);
        LOG.info("requestItems Memory : " + todoUnsavedList);
        todoList.addAll(todoUnsavedList);
        model.addAttribute("newitem", new TodoItem());
        model.addAttribute("items", new TodoListViewModel(todoList));
        model.addAttribute("myHostName", getHostname());
        return "index";
    }

    @RequestMapping("/add")
    public String addTodo(HttpSession session, @RequestParam(name="file", required = false) MultipartFile file, @ModelAttribute TodoItem requestItem) {
        LOG.info("/add called ");
        TodoItem item = new TodoItem(requestItem.getCategory(), requestItem.getName());
        item.setOnlyinsession(requestItem.isOnlyinsession());

        LOG.finer("requestItem : " + requestItem);
        //Check that the file is uploaded
        if (Objects.nonNull(file) && !file.isEmpty()) {
            String tmpFileName = FileService.storeFile(file, storageLocation);
            item.setFilename(tmpFileName);
        }else{
            LOG.finer("file : " + file);
            item.setFilename("");
        }

        if (requestItem.isOnlyinsession()) {
            item.setId(sessionIndex--);
            LOG.finer("requestItem : " + item);
            todoUnsavedList.add(item);
        } else {
            LOG.finer("requestItem : " + item);
            repository.save(item);
        }
        return "redirect:/";
    }

    @RequestMapping("/update")
    public String updateTodo(HttpSession session, @ModelAttribute TodoListViewModel requestItems) {
        LOG.info("/update called ");
        for (TodoItem requestItem : requestItems.getTodoList()) {
            LOG.info("update requestItem : " + requestItem);
            Long index = requestItem.getId();
            if(Objects.isNull(requestItem.getId())) {
                continue;
            }
            // index < 1 means todoitems in sessions (not in database)
            if (index < 1) {
                // todo is draft
                if (requestItem.isDelete()) {
                    LOG.info("delete requestItem : " + requestItem);
                    todoUnsavedList.remove(requestItem);
                    session.setAttribute("SessionUnsavedList",
                            todoUnsavedList);
                }else if (requestItem.isComplete()) {
                    todoUnsavedList.remove(requestItem);
                    session.setAttribute("SessionUnsavedList",
                            todoUnsavedList);
                    saveTodo(requestItem);
                }
            }else if (requestItem.isDelete()) {
                LOG.info("delete requestItem : " + requestItem);
                TodoItem item = new TodoItem(requestItem.getCategory(), requestItem.getName());
                item.setId(requestItem.getId());
                repository.delete(item);
            } else {
                LOG.info("save requestItem : " + requestItem);
                saveTodo(requestItem);
            }
        }
        return "redirect:/";
    }

    private void saveTodo(TodoItem requestItem) {
        TodoItem item = new TodoItem(requestItem.getCategory(), requestItem.getName());
        item.setComplete(requestItem.isComplete());
        item.setFilename(requestItem.getFilename());
        item.setOnlyinsession(false);
        item.setId(requestItem.getId());
        repository.save(item);
    }

    //this is a very dirty trick
    //It is used into index.html in order to display hostname (aka name of the pod)
    private String getHostname() {
        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
            return hostName;
        } catch (UnknownHostException e) {
            LOG.warning("UnknownHostException : " + e.getLocalizedMessage());
            return "no-hostname";
        }

    }

    @RequestMapping(value = "/files/{filename}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getFile(
            @PathVariable("filename") String fileName,
            HttpServletResponse response) throws IOException {

            Resource resource = FileService.loadFileAsResource(storageLocation,fileName);
            if (resource != null) {
                FileCopyUtils.copy(resource.getInputStream(), response.getOutputStream());
                response.flushBuffer();
            }

    }

}
