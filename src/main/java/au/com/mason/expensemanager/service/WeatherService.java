package au.com.mason.expensemanager.service;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import au.com.mason.expensemanager.dto.WeatherForecastDayDto;
import au.com.mason.expensemanager.dto.WeatherForecastDto;

/**
 * Combines two public Bureau of Meteorology products: the Victorian town
 * forecast (IDV10753) for the location's temperatures and rain, and the
 * Melbourne forecast (IDV10450) for the metropolitan area's detailed text, UV
 * and fire danger.
 */
@Component
public class WeatherService {

	private static final Logger LOGGER = LogManager.getLogger(WeatherService.class);

	private static final Duration CACHE_DURATION = Duration.ofMinutes(30);

	private static final int FORECAST_DAYS = 7;

	private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

	@Value("${weather.forecast.url:https://www.bom.gov.au/fwo/IDV10753.xml}")
	private String forecastUrl;

	@Value("${weather.forecast.location:Moorabbin}")
	private String forecastLocation;

	@Value("${weather.forecast.detail-url:https://www.bom.gov.au/fwo/IDV10450.xml}")
	private String detailUrl;

	@Value("${weather.forecast.detail-area:VIC_ME001}")
	private String detailArea;

	private WeatherForecastDto cachedForecast;

	private Instant cachedAt;

	public synchronized WeatherForecastDto getForecast() throws Exception {
		if (cachedForecast != null && cachedAt.plus(CACHE_DURATION).isAfter(Instant.now())) {
			return cachedForecast;
		}

		try {
			cachedForecast = fetchForecast();
			cachedAt = Instant.now();
		} catch (Exception e) {
			if (cachedForecast == null) {
				throw e;
			}
			LOGGER.warn("Failed to refresh BOM forecast, serving cached copy", e);
		}
		return cachedForecast;
	}

	private WeatherForecastDto fetchForecast() throws Exception {
		WeatherForecastDto forecast;
		try (InputStream body = openXml(forecastUrl)) {
			forecast = parseForecast(body, forecastLocation, FORECAST_DAYS);
		}

		try (InputStream body = openXml(detailUrl)) {
			mergeDetail(forecast, body, detailArea);
		} catch (Exception e) {
			LOGGER.warn("Failed to load BOM detailed forecast, returning basic forecast only", e);
		}
		return forecast;
	}

	private InputStream openXml(String url) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(15))
			// BOM rejects requests without a descriptive User-Agent.
			.header("User-Agent", "expensemanager/1.0").header("Accept", "application/xml").GET().build();

		HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
		if (response.statusCode() != 200) {
			response.body().close();
			throw new IllegalStateException("BOM request to " + url + " failed with status " + response.statusCode());
		}
		return response.body();
	}

	static WeatherForecastDto parseForecast(InputStream xml, String location, int days) throws Exception {
		Document document = newDocumentBuilder().parse(xml);

		Element area = findArea(document, "description", location);
		if (area == null) {
			throw new IllegalStateException("Location " + location + " not found in BOM forecast");
		}

		List<WeatherForecastDayDto> forecastDays = new ArrayList<>();
		NodeList periods = area.getElementsByTagName("forecast-period");
		for (int i = 0; i < periods.getLength() && forecastDays.size() < days; i++) {
			forecastDays.add(toDay((Element) periods.item(i)));
		}

		WeatherForecastDto forecast = new WeatherForecastDto();
		forecast.setLocation(location);
		forecast.setIssuedAt(firstText(document, "issue-time-local"));
		forecast.setDays(forecastDays);
		return forecast;
	}

	static void mergeDetail(WeatherForecastDto forecast, InputStream xml, String areaAac) throws Exception {
		Document document = newDocumentBuilder().parse(xml);

		Element area = findArea(document, "aac", areaAac);
		if (area == null) {
			throw new IllegalStateException("Area " + areaAac + " not found in BOM detailed forecast");
		}

		Map<String, Element> periodsByDate = new HashMap<>();
		NodeList periods = area.getElementsByTagName("forecast-period");
		for (int i = 0; i < periods.getLength(); i++) {
			Element period = (Element) periods.item(i);
			periodsByDate.putIfAbsent(dateOf(period), period);
		}

		forecast.setDetailArea(area.getAttribute("description"));
		for (WeatherForecastDayDto day : forecast.getDays()) {
			Element period = periodsByDate.get(day.getDate());
			if (period == null) {
				continue;
			}
			forEachTypedChild(period, (type, value) -> {
				switch (type) {
					case "forecast" -> day.setForecastText(value);
					case "fire_danger" -> day.setFireDanger(value);
					case "uv_alert" -> day.setUvAlert(value);
					default -> {
					}
				}
			});
		}
	}

	private static DocumentBuilder newDocumentBuilder() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setExpandEntityReferences(false);
		return factory.newDocumentBuilder();
	}

	private static Element findArea(Document document, String attribute, String value) {
		NodeList areas = document.getElementsByTagName("area");
		for (int i = 0; i < areas.getLength(); i++) {
			Element area = (Element) areas.item(i);
			if (value.equalsIgnoreCase(area.getAttribute(attribute))) {
				return area;
			}
		}
		return null;
	}

	private static String dateOf(Element period) {
		String start = period.getAttribute("start-time-local");
		return start.length() >= 10 ? start.substring(0, 10) : start;
	}

	private static WeatherForecastDayDto toDay(Element period) {
		WeatherForecastDayDto day = new WeatherForecastDayDto();
		day.setDate(dateOf(period));

		forEachTypedChild(period, (type, value) -> {
			switch (type) {
				case "air_temperature_minimum" -> day.setMinTemp(parseInteger(value));
				case "air_temperature_maximum" -> day.setMaxTemp(parseInteger(value));
				case "forecast_icon_code" -> day.setIconCode(parseInteger(value));
				case "precis" -> day.setPrecis(value);
				case "probability_of_precipitation" -> day.setRainChance(value);
				case "precipitation_range" -> day.setRainRange(value);
				default -> {
				}
			}
		});
		return day;
	}

	private interface TypedChildConsumer {
		void accept(String type, String value);
	}

	private static void forEachTypedChild(Element period, TypedChildConsumer consumer) {
		NodeList children = period.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element child = (Element) node;
				consumer.accept(child.getAttribute("type"), child.getTextContent().trim());
			}
		}
	}

	private static Integer parseInteger(String value) {
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static String firstText(Document document, String tagName) {
		NodeList nodes = document.getElementsByTagName(tagName);
		return nodes.getLength() == 0 ? null : nodes.item(0).getTextContent().trim();
	}

}
