package au.com.mason.expensemanager.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class WeatherForecastDto {
	private String location;
	private String issuedAt;
	private List<WeatherForecastDayDto> days;
}
