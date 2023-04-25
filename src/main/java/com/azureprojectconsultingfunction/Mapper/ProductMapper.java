package com.azureprojectconsultingfunction.Mapper;

import org.apache.ibatis.annotations.*;

import com.azureprojectconsultingfunction.Model.Product;

@Mapper
public interface ProductMapper {
    @Select("SELECT * FROM Product WHERE id = #{id}")
    Product findById(@Param("id") Long id);

    @Insert("INSERT INTO Product (name) VALUES (#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Product product);

    @Update("UPDATE Product SET name = #{name} WHERE id = #{id}")
    void update(Product product);

    @Delete("DELETE FROM Product WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
}
