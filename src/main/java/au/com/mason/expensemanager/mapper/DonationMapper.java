package au.com.mason.expensemanager.mapper;

import au.com.mason.expensemanager.domain.Donation;
import au.com.mason.expensemanager.dto.DonationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DonationMapper {

    @Autowired
    private RefDataMapper refDataMapper;

    public Donation donationDtoToDonation(DonationDto donationDto) {
        if ( donationDto == null ) {
            return null;
        }

        Donation donation = new Donation();

        if ( donationDto.getId() != null ) {
            donation.setId( donationDto.getId() );
        }
        donation.setCause( refDataMapper.refDataDtoToRefData( donationDto.getCause() ) );
        donation.setDescription( donationDto.getDescription() );
        donation.setNotes( donationDto.getNotes() );

        return donation;
    }

    public DonationDto donationToDonationDto(Donation donation) {
        if ( donation == null ) {
            return null;
        }

        DonationDto donationDto = new DonationDto();

        donationDto.setId( donation.getId() );
        donationDto.setCause( refDataMapper.refDataToRefDataDto( donation.getCause() ) );
        donationDto.setDescription( donation.getDescription() );
        donationDto.setNotes( donation.getNotes() );

        return donationDto;
    }
}
