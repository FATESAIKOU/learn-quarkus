package com.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/todos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TodoResource {
    @Inject
    TodoService todoService;

    @GET
    public List<Todo> list() {
        return todoService.listAll();
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Integer id) {
        Todo todo = todoService.findById(id);
        if (todo == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(todo).build();
    }

    @POST
    public Response create(Todo todo) {
        Todo created = todoService.create(todo);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, Todo todo) {
        boolean updated = todoService.update(id, todo);
        if (!updated) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(todoService.findById(id)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        boolean deleted = todoService.delete(id);
        if (!deleted) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.noContent().build();
    }
}