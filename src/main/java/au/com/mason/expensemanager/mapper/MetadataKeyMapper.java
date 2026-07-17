package au.com.mason.expensemanager.mapper;

import org.mapstruct.Mapper;

import au.com.mason.expensemanager.domain.MetadataKey;
import au.com.mason.expensemanager.dto.MetadataKeyDto;

@Mapper(componentModel = "spring")
public interface MetadataKeyMapper extends BaseMapper<MetadataKey, MetadataKeyDto> {

}
