package au.com.mason.expensemanager.mapper;

import au.com.mason.expensemanager.domain.Donation;
import au.com.mason.expensemanager.dto.DonationDto;
import au.com.mason.expensemanager.util.DateUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DonationMapper implements BaseMapper<Donation, DonationDto> {

    private static Gson gson = new GsonBuilder().serializeNulls().create();

    @Autowired
    private RefDataMapper refDataMapper;
    @Autowired
    private DocumentMapper documentMapper;

    public Donation dtoToEntity(DonationDto donationDto) {
        if ( donationDto == null ) {
            return null;
        }

        Donation donation = new Donation();

        if ( donationDto.getId() != null ) {
            donation.setId( donationDto.getId() );
        }
        donation.setCause( refDataMapper.dtoToEntity( donationDto.getCause() ) );
        donation.setDescription( donationDto.getDescription() );
        donation.setNotes( donationDto.getNotes() );
        donation.setDueDate(DateUtil.getFormattedDate(donationDto.getDueDateString()));
        donation.setMetaData((Map<String, String>) gson.fromJson(donationDto.getMetaDataChunk(), Map.class));
        if (donationDto.getDocumentDto() != null && donationDto.getDocumentDto().getFileName() != null) {
            donation.setDocument(documentMapper.dtoToEntity(donationDto.getDocumentDto()));
        }

        return donation;
    }

    public DonationDto entityToDto(Donation donation) {
        if ( donation == null ) {
            return null;
        }

        DonationDto donationDto = new DonationDto();

        donationDto.setId( donation.getId() );
        donationDto.setCause( refDataMapper.entityToDto( donation.getCause() ) );
        donationDto.setDescription( donation.getDescription() );
        donationDto.setNotes( donation.getNotes() );
        donationDto.setDueDateString(DateUtil.getFormattedDateString(donation.getDueDate()));
        donationDto.setDueDate(donation.getDueDate());
        donationDto.setMetaDataChunk(gson.toJson(donation.getMetaData(), Map.class));
        if (donation.getDocument() != null) {
            donationDto.setDocumentDto(documentMapper.entityToDto(donation.getDocument()));
        }

        return donationDto;
    }
}
