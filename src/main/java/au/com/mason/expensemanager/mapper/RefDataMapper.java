package au.com.mason.expensemanager.mapper;

import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.domain.RefDataType;
import au.com.mason.expensemanager.dto.RefDataDto;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class RefDataMapper implements BaseMapper<RefData, RefDataDto> {
    private static Gson gson = new GsonBuilder().serializeNulls().create();

    public RefData dtoToEntity(RefDataDto refDataDto) {
        if ( refDataDto == null ) {
            return null;
        }

        RefData refData = new RefData();

        if ( refDataDto.getId() != null ) {
            refData.setId( refDataDto.getId() );
        }
        refData.setDescription( refDataDto.getDescription() );
        if ( refDataDto.getType() != null ) {
            refData.setType( Enum.valueOf( RefDataType.class, refDataDto.getType() ) );
        }
        refData.setMetaData((Map<String, String>) gson.fromJson(refDataDto.getMetaDataChunk(), Map.class));

        return refData;
    }

    public RefDataDto entityToDto(RefData refData) {
        if ( refData == null ) {
            return null;
        }

        RefDataDto refDataDto = new RefDataDto();

        refDataDto.setId( refData.getId() );
        refDataDto.setDescription( refData.getDescription() );
        if ( refData.getType() != null ) {
            refDataDto.setType( refData.getType().name() );
            refDataDto.setTypeDescription(refData.getType().getDescription());
        }
        refDataDto.setMetaDataChunk(gson.toJson(refData.getMetaData(), Map.class));
        refDataDto.setDeleted(refData.isDeleted());

        return refDataDto;
    }
}
