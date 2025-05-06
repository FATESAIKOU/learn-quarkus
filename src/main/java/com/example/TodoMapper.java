package com.example;

import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface TodoMapper {
    @Select("SELECT * FROM todo ORDER BY id")
    List<Todo> listAll();

    @Select("SELECT * FROM todo WHERE id = #{id}")
    Todo findById(Integer id);

    @Insert("INSERT INTO todo (title, description, completed) VALUES (#{title}, #{description}, #{completed})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Todo todo);

    @Update("UPDATE todo SET title=#{title}, description=#{description}, completed=#{completed}, updated_at=now() WHERE id=#{id}")
    int update(Todo todo);

    @Delete("DELETE FROM todo WHERE id=#{id}")
    int delete(Integer id);
}