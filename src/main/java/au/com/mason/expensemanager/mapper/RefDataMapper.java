package au.com.mason.expensemanager.mapper;

import au.com.mason.expensemanager.domain.RefData;
import au.com.mason.expensemanager.domain.RefDataType;
import au.com.mason.expensemanager.dto.RefDataDto;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class RefDataMapper {

    private Gson gson = new GsonBuilder().serializeNulls().create();

    public RefDataDto refDataToRefDataDto(RefData refData) {
        if ( refData == null ) {
            return null;
        }

        RefDataDto refDataDto = new RefDataDto();
        refDataDto.setId(refData.getId());
        refDataDto.setValue(String.valueOf(refData.getId()));
        refDataDto.setTypeDescription(refData.getType().getDescription());
        refDataDto.setDescription(refData.getDescription());
        refDataDto.setType(refData.getType().name());
        refDataDto.setMetaDataChunk(gson.toJson(refData.getMetaData(), Map.class));

        return refDataDto;
    }

    public RefData refDataDtoToRefData(RefDataDto refDataDto) {
        if ( refDataDto == null ) {
            return null;
        }

        RefData refData = new RefData();
        refData.setId(refDataDto.getId());
        refData.setType(RefDataType.valueOf(refDataDto.getType()));
        refData.setDescription(refDataDto.getDescription());
        refData.setMetaData((Map<String, Object>) gson.fromJson(refDataDto.getMetaDataChunk(), Map.class));

        return refData;
    }

    public RefData refDataDtoToRefData(RefDataDto refDataDto, RefData refData) {
        if ( refDataDto == null ) {
            return null;
        }

        if ( refDataDto.getId() != null ) {
            refData.setId( refDataDto.getId() );
        }
        refData.setDescription( refDataDto.getDescription() );
        if ( refDataDto.getType() != null ) {
            refData.setType( Enum.valueOf( RefDataType.class, refDataDto.getType() ) );
        }
        else {
            refData.setType( null );
        }

        return refData;
    }
}
