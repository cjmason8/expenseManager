package au.com.mason.expensemanager.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import au.com.mason.expensemanager.domain.Donation;
import au.com.mason.expensemanager.dto.DonationDto;

@Component
@Mapper(uses = {RefDataMapper.class})
public interface DonationMapper {
	
	DonationMapper INSTANCE = Mappers.getMapper( DonationMapper.class );
	 
    Donation donationDtoToDonation(DonationDto donationDto) throws Exception;
    
    Donation donationDtoToDonation(DonationDto donationDto, @MappingTarget Donation donation) throws Exception;
    
    DonationDto donationToDonationDto(Donation donation) throws Exception;

}
