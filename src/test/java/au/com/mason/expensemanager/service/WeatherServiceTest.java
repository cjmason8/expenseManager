package au.com.mason.expensemanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import au.com.mason.expensemanager.dto.WeatherForecastDayDto;
import au.com.mason.expensemanager.dto.WeatherForecastDto;

class WeatherServiceTest {

	private static final String XML = """
		<?xml version="1.0" encoding="UTF-8"?>
		<product version="1.7">
		  <amoc>
		    <issue-time-local tz="EST">2026-10-01T11:00:00+10:00</issue-time-local>
		  </amoc>
		  <forecast>
		    <area aac="VIC_PT001" description="Frankston" type="location">
		      <forecast-period index="0" start-time-local="2026-10-01T05:00:00+10:00">
		        <element type="air_temperature_maximum" units="Celsius">20</element>
		      </forecast-period>
		    </area>
		    <area aac="VIC_PT042" description="Moorabbin" type="location">
		      <forecast-period index="0" start-time-local="2026-10-01T05:00:00+10:00">
		        <element type="forecast_icon_code">16</element>
		        <element type="air_temperature_maximum" units="Celsius">22</element>
		        <text type="precis">Rain. Possible storm.</text>
		        <text type="probability_of_precipitation">100%</text>
		      </forecast-period>
		      <forecast-period index="1" start-time-local="2026-10-02T00:00:00+10:00">
		        <element type="forecast_icon_code">12</element>
		        <element type="precipitation_range">9 to 35 mm</element>
		        <element type="air_temperature_minimum" units="Celsius">11</element>
		        <element type="air_temperature_maximum" units="Celsius">17</element>
		        <text type="precis">Rain.</text>
		        <text type="probability_of_precipitation">95%</text>
		      </forecast-period>
		      <forecast-period index="2" start-time-local="2026-10-03T00:00:00+10:00">
		        <element type="air_temperature_minimum" units="Celsius">12</element>
		        <element type="air_temperature_maximum" units="Celsius">19</element>
		        <text type="precis">Showers.</text>
		      </forecast-period>
		      <forecast-period index="3" start-time-local="2026-10-04T00:00:00+10:00">
		        <text type="precis">Partly cloudy.</text>
		      </forecast-period>
		    </area>
		  </forecast>
		</product>
		""".strip();

	private static WeatherForecastDto parse(String location) throws Exception {
		return WeatherService.parseForecast(new ByteArrayInputStream(XML.getBytes(StandardCharsets.UTF_8)), location,
			3);
	}

	@Test
	void testParseForecast_ReturnsFirstThreeDaysForLocation() throws Exception {
		WeatherForecastDto forecast = parse("Moorabbin");

		assertEquals("Moorabbin", forecast.getLocation());
		assertEquals("2026-10-01T11:00:00+10:00", forecast.getIssuedAt());
		assertEquals(3, forecast.getDays().size());

		WeatherForecastDayDto today = forecast.getDays().get(0);
		assertEquals("2026-10-01", today.getDate());
		assertNull(today.getMinTemp());
		assertEquals(22, today.getMaxTemp());
		assertEquals(16, today.getIconCode());
		assertEquals("Rain. Possible storm.", today.getPrecis());
		assertEquals("100%", today.getRainChance());

		WeatherForecastDayDto tomorrow = forecast.getDays().get(1);
		assertEquals("2026-10-02", tomorrow.getDate());
		assertEquals(11, tomorrow.getMinTemp());
		assertEquals(17, tomorrow.getMaxTemp());
		assertEquals("9 to 35 mm", tomorrow.getRainRange());

		assertEquals("2026-10-03", forecast.getDays().get(2).getDate());
	}

	@Test
	void testParseForecast_UnknownLocation_Throws() {
		assertThrows(IllegalStateException.class, () -> parse("Dingley"));
	}

	private static final String DETAIL_XML = """
		<?xml version="1.0" encoding="UTF-8"?>
		<product version="1.7">
		  <forecast>
		    <area aac="VIC_FA001" description="Victoria" type="region">
		      <forecast-period start-time-local="2026-10-01T06:06:41+10:00">
		        <text type="product_footer">Footer</text>
		      </forecast-period>
		    </area>
		    <area aac="VIC_ME001" description="Melbourne metropolitan" type="metropolitan">
		      <forecast-period index="0" start-time-local="2026-10-01T00:00:00+10:00">
		        <text type="forecast">Rain. Winds southerly 15 to 25 km/h.</text>
		        <text type="fire_danger">No Rating</text>
		        <text type="uv_alert">Sun protection 9:10am to 3:00pm, UV Index predicted to reach 6 [High]</text>
		      </forecast-period>
		      <forecast-period index="1" start-time-local="2026-10-02T00:00:00+10:00">
		        <text type="forecast">Cloudy. Very high chance of showers.</text>
		      </forecast-period>
		    </area>
		  </forecast>
		</product>
		""".strip();

	@Test
	void testMergeDetail_AddsMetropolitanTextByDate() throws Exception {
		WeatherForecastDto forecast = parse("Moorabbin");

		WeatherService.mergeDetail(forecast, new ByteArrayInputStream(DETAIL_XML.getBytes(StandardCharsets.UTF_8)),
			"VIC_ME001");

		assertEquals("Melbourne metropolitan", forecast.getDetailArea());

		WeatherForecastDayDto today = forecast.getDays().get(0);
		assertEquals("Rain. Winds southerly 15 to 25 km/h.", today.getForecastText());
		assertEquals("No Rating", today.getFireDanger());
		assertEquals("Sun protection 9:10am to 3:00pm, UV Index predicted to reach 6 [High]", today.getUvAlert());

		WeatherForecastDayDto tomorrow = forecast.getDays().get(1);
		assertEquals("Cloudy. Very high chance of showers.", tomorrow.getForecastText());
		assertNull(tomorrow.getUvAlert());

		assertNull(forecast.getDays().get(2).getForecastText());
	}

}
