package au.com.mason.expensemanager.mapper;

import org.mapstruct.Mapper;

import au.com.mason.expensemanager.domain.MetadataValue;
import au.com.mason.expensemanager.dto.MetadataValueDto;

@Mapper(componentModel = "spring", uses = MetadataKeyMapper.class)
public interface MetadataValueMapper extends BaseMapper<MetadataValue, MetadataValueDto> {

}
