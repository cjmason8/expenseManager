package au.com.mason.expensemanager.mapper;

public interface BaseMapper<T, V> {
    T dtoToEntity(V dto);
    V entityToDto(T entity);
}
