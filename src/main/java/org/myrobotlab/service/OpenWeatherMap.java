package org.myrobotlab.service;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.myrobotlab.framework.ServiceType;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.service.interfaces.KeyConsumer;
import org.slf4j.Logger;

/**
 * A service to query into OpenWeatherMap to get the current weather. For more
 * info check out http://openweathermap.org This service requires an API key
 * that is free to register for from Open Weather Map.
 * 
 */
public class OpenWeatherMap extends HttpClient implements KeyConsumer {

  private static final long serialVersionUID = 1L;
  public final static Logger log = LoggerFactory.getLogger(OpenWeatherMap.class);

  public final static String OPEN_WEATHER_MAP_KEY_NAME = "openweathermap.api.key";

  private String apiBase = "http://api.openweathermap.org/data/2.5/weather?q=";
  private String apiForecast = "http://api.openweathermap.org/data/2.5/forecast/?q=";
  private String units = "imperial"; // metric
  private String lang = "en";
  private String apiKey = null;
  
  public static class Weather {
    public Double temp;
    public Double tempMin;
    public Double tempMax;
    public Double pressure;
    public Double seaLevel;
    public Double grndLevel;
    public Double humidity;
    public Double tempKf;
    public String description;
    public String icon;
    public Double cloudsAll;
    public Double windSpeed;
    public Double windDeg;
    public Double rainVol;
  }

  public OpenWeatherMap(String reservedKey) {
    super(reservedKey);
  }

  /**
   * Return a array describing the forecast weather
   */
  private JSONObject fetch(String location, int hourPeriod) throws IOException, JSONException {

    if (apiKey == null) {
      Security security = Runtime.getSecurity();
      apiKey = security.getKey(OPEN_WEATHER_MAP_KEY_NAME);
      if (apiKey == null) {
        error("please use owm.setKey(XXXXXXXXXX) to set the api key - http://api.openweathermap.org");
        return null;
      }
    }

    String apiUrl = apiForecast + URLEncoder.encode(location, "utf-8") + "&appid=" + apiKey + "&mode=json&units=" + units + "&lang=" + lang + "&cnt=" + hourPeriod;
    String response = this.get(apiUrl);
    log.info("apiUrl: {}", apiUrl);
    log.info("Respnse: {}", response);
    JSONObject obj = new JSONObject(response);
    return obj;
  }

  /**
   * retrieve a string list of weather for the period indicated by hourPeriod 1
   * >= hourPeriod is 3 hours per index -> 24 hours is 8.
   * 
   * {"city":{"id":2802985,"name":"location","coord":{"lon":5.8581,"lat":50.7019},"country":"FR","population":0},
   * "cod":"200", "message":0.1309272, "cnt":3, "list":[ {"dt":1505386800,
   * "temp":{"day":11.62,"min":10.59,"max":12.39,"night":11.01,"eve":10.59,"morn":10.98},
   * "pressure":1006.58, "humidity":100,
   * "weather":[{"id":502,"main":"Rain","description":"heavy intensity
   * rain","icon":"10d"}], "speed":6.73, "deg":259, "clouds":92, "rain":17.33},
   * 
   * {"dt":1505473200,
   * "temp":{"day":14.96,"min":8.43,"max":14.96,"night":8.43,"eve":12.71,"morn":9.46},
   * "pressure":1014.87, "humidity":89,
   * "weather":[{"id":500,"main":"Rain","description":"light
   * rain","icon":"10d"}], "speed":4.8, "deg":249, "clouds":20, "rain":0.36},
   * 
   * {"dt":1505559600,
   * "temp":{"day":13.85,"min":8.09,"max":14.5,"night":8.44,"eve":12.5,"morn":8.09},
   * "pressure":1013.37, "humidity":95,
   * "weather":[{"id":501,"main":"Rain","description":"moderate
   * rain","icon":"10d"}], "speed":5.38, "deg":241, "clouds":44, "rain":5.55} ]}
   * 
   * @throws JSONException
   * @throws IOException
   * @throws ClientProtocolException
   */
  public String[] fetchForecast(String location) throws IOException, JSONException {
    return fetchForecast(location, 0);
  }

  public String[] fetchForecast(String location, int hourPeriod) throws IOException, JSONException {
    String[] result = new String[11];
    String localUnits = "fahrenheit";
    if (units.equals("metric")) {
      // for metric, celsius
      localUnits = "celsius";
    }
    if ((hourPeriod >= 0) && (hourPeriod <= 40)) {
      JSONObject jsonObj = null;
      try {
        jsonObj = fetch(location, (hourPeriod));
      } catch (IOException | JSONException e) {
        error("OpenWeatherMap : fetch error", e);
        return null;
      }
      // log.info(jsonObj.toString());
      // Getting the list node
      JSONArray list;
      try {
        list = jsonObj.getJSONArray("list");
      } catch (JSONException e) {
        error("OpenWeatherMap : API key or the city is not recognized", e);
        return null;
      }
      // Getting the required element from list by dayIndex
      JSONObject item = list.getJSONObject(hourPeriod - 1);

      result[0] = item.getJSONArray("weather").getJSONObject(0).get("description").toString();
      JSONObject temp = item.getJSONObject("main");
      result[1] = temp.get("temp").toString();
      result[2] = location;
      result[3] = item.getJSONArray("weather").getJSONObject(0).get("id").toString();
      result[4] = temp.get("pressure").toString();
      result[5] = temp.get("humidity").toString();
      result[6] = temp.get("temp_min").toString();
      result[7] = temp.get("temp_max").toString();
      result[8] = item.getJSONObject("wind").getString("speed");
      result[9] = item.getJSONObject("wind").getString("deg");
      result[10] = localUnits;
    } else {
      error("OpenWeatherMap : Index is out of range");
      return null;
    }
    return result;
  }

  @Deprecated
  public String fetchWeather(String location) throws IOException, JSONException {
    return fetchForecast(location, 0)[0];
  }

  public String getApiBase() {
    return apiBase;
  }

  /**
   * @param apiBase
   *          The base url that is assocaited with the open weather map api.
   */
  public void setApiBase(String apiBase) {
    this.apiBase = apiBase;
  }

  public String getUnits() {
    return units;
  }

  /**
   * @param units
   *          The units, can be either imperial or metric.
   */
  public void setUnits(String units) {
    this.units = units;
  }

  public String getApiKey() {
    return apiKey;
  }

  /**
   * @param apiKey
   *          REQUIRED: specify your API key with this method.
   */
  public void setApiKey(String apiKey) {
    setKey(OPEN_WEATHER_MAP_KEY_NAME, apiKey);
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  /**
   * This static method returns all the details of the class without it having
   * to be constructed. It has description, categories, dependencies, and peer
   * definitions.
   * 
   * @return ServiceType - returns all the data
   * 
   */
  static public ServiceType getMetaData() {

    ServiceType meta = new ServiceType(OpenWeatherMap.class.getCanonicalName());
    meta.addDescription("This service will query OpenWeatherMap for the current weather.  Get an API key at http://openweathermap.org/");
    meta.addCategory("data", "weather");
    meta.setCloudService(true);
    meta.addDependency("org.json", "json", "20090211");
    meta.addPeer("httpClient", "HttpClient", "httpClient");
    return meta;
  }

  /**
   * useful for the user to query which keys are required
   */
  @Override
  public String[] getKeyNames() {
    return new String[] { OPEN_WEATHER_MAP_KEY_NAME };
  }

  /**
   * sets any keyname / value pair safely into the Security service
   */
  @Override
  public void setKey(String keyName, String keyValue) {
    Security security = Runtime.getSecurity();
    security.setKey(keyName, keyValue);
    broadcastState();
  }

  public static void main(String[] args) {
    try {
      OpenWeatherMap owm = (OpenWeatherMap) Runtime.start("weather", "OpenWeatherMap");
      // owm.setApiKey("xxxxxxxxxxxxxxxxxxxxxxxxxxx");
      String[] fetchForecast = owm.fetchForecast("Portland,US");

      // tomorrow is 8 ( 3 * 8 )
      fetchForecast = owm.fetchForecast("Portland,US", 2);
      String sentence = "(" + fetchForecast[3] + ") In " + fetchForecast[2] + " the weather is " + fetchForecast[0] + ".  " + fetchForecast[1] + " degrees " + fetchForecast[10];
      log.info(sentence);
    } catch (Exception e) {
      log.error("main threw", e);
    }
  }

}