package au.com.mason.expensemanager.mapper;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import au.com.mason.expensemanager.domain.Donation;
import au.com.mason.expensemanager.dto.DonationDto;
import au.com.mason.expensemanager.util.DateUtil;

@Component
public class DonationMapperWrapper {
	
	@Autowired
	private DocumentMapperWrapper documentMapperWrapper;	
	
	private DonationMapper donationMapper = DonationMapper.INSTANCE;
	private Gson gson = new GsonBuilder().serializeNulls().create();
	
	public Donation donationDtoToDonation(DonationDto donationDto) throws Exception {
		Donation donation = donationMapper.donationDtoToDonation(donationDto);
		donation.setDueDate(DateUtil.getFormattedDate(donationDto.getDueDateString()));
		donation.setMetaData((Map<String, String>) gson.fromJson(donationDto.getMetaDataChunk(), Map.class));
		if (donationDto.getDocumentDto() != null) {
			donation.setDocument(documentMapperWrapper.documentDtoToDocument(donationDto.getDocumentDto()));
		}
		
		return donation;
	}

    public Donation donationDtoToDonation(DonationDto donationDto, Donation donationParam) throws Exception {
    	Donation donation = donationMapper.donationDtoToDonation(donationDto, donationParam);
    	donation.setDueDate(DateUtil.getFormattedDate(donationDto.getDueDateString()));
    	donation.setMetaData((Map<String, String>) gson.fromJson(donationDto.getMetaDataChunk(), Map.class));
		if (donationDto.getDocumentDto() != null) {
			donation.setDocument(documentMapperWrapper.documentDtoToDocument(donationDto.getDocumentDto()));
		}
    	
		return donation;
    }
    
    public DonationDto donationToDonationDto(Donation donation) throws Exception {
    	DonationDto donationDto = donationMapper.donationToDonationDto(donation);
    	donationDto.setDueDateString(DateUtil.getFormattedDateString(donation.getDueDate()));
    	donationDto.setMetaDataChunk(gson.toJson(donation.getMetaData(), Map.class));
		if (donation.getDocument() != null) {
			donationDto.setDocumentDto(documentMapperWrapper.documentToDocumentDto(donation.getDocument()));
		}

		return donationDto;
    }

}
