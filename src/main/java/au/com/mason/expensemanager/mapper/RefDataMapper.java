package au.com.mason.expensemanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.dto.RefDataDto;

@Component
@Mapper
public interface RefDataMapper {
	
	RefDataMapper INSTANCE = Mappers.getMapper( RefDataMapper.class );
	 
    RefData refDataDtoToRefData(RefDataDto refDataDto) throws Exception;
    
    RefData refDataDtoToRefData(RefDataDto refDataDto, @MappingTarget RefData refData) throws Exception;
    
    RefDataDto refDataToRefDataDto(RefData refData) throws Exception;

}
