package au.com.mason.expensemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class WeatherForecastDayDto {
	private String date;
	private Integer minTemp;
	private Integer maxTemp;
	private String precis;
	private Integer iconCode;
	private String rainChance;
	private String rainRange;
}
