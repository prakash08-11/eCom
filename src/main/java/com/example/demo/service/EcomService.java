package com.example.demo.service;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.demo.entity.CredentialsEntity;
import com.example.demo.exception.APIException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;

import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
public class EcomService {
	
//	private static final Logger log = LoggerFactory.getLogger(EcomService.class);

// To validate the credentials
	public void validate(CredentialsEntity details) {
		
		if(details.getUserName().isEmpty()||details.getPassCode().isEmpty()) {
			throw new APIException("invalid userName/Password");
		}
		return;
		
	}

	public Boolean eComConnector(String userName, String passCode, String url) {
		
		String rawJson = fetchJsonFromAPI(userName, passCode,url);
//		log.info(rawJson);
		
		
		ArrayNode jsonArray = unWrap(rawJson);
//		log.info("unWrapped Json ---> \n"+jsonArray.toPrettyString());
		
		ArrayList<String> columnNames =   new ArrayList<String>();	
		jsonArray.forEach(element -> {			
			element.fieldNames().forEachRemaining(field ->{				
				if(!checkIfColumn(columnNames,field)) {					
//					log.info("--->"+field);
					columnNames.add(field);					
				}
			});	
		});	
		
		return convertJsonToCSV(columnNames,jsonArray);
		
	}

//	To insert the ArrayNode into the CSV file
	private Boolean convertJsonToCSV(ArrayList<String> columnNames, ArrayNode jsonArray) {
		
		try {
			DateTimeFormatter dateformatter = DateTimeFormatter.ofPattern("MM_dd_yyyy_HH_mm_ss");
			String timestamp = ZonedDateTime.now(ZoneId.of("UTC")).format(dateformatter);
			String fileName = "outputFiles/outPutFile_"+timestamp+".csv";
		Builder csvSchemaBuilder = CsvSchema.builder();
		
		columnNames.forEach(fieldName -> {
			csvSchemaBuilder.addColumn(fieldName);
		});
		
		CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();
		CsvMapper csvMapper = new CsvMapper();

			csvMapper.writerFor(JsonNode.class)
			  .with(csvSchema).writeValue(new File(fileName), jsonArray);
		
			return true;

		}
		
		catch (IOException e) {
			e.printStackTrace();
			throw new APIException("Issue while converting the JSON to CSV \n"+e.getMessage(),e);
			
		}	
		
	}
//  This to convert the Json string to Arraynode to make it simpler to insert it into CSV file
	private ArrayNode unWrap(String rawJson) {
		

		try {
			ObjectMapper mapper = new ObjectMapper();
			TreeMap <String, String> map = new TreeMap<String,String>();
			
		
			JsonNode jsonTree = mapper.readTree(rawJson);
			
			ArrayNode arrayJson = mapper.createArrayNode();
			
			if(jsonTree.isArray())
			{
				for(int i=0; i<jsonTree.size();i++) {
					
					arrayJson.add(mapper.valueToTree(flattenJson("", jsonTree.get(i))));
					
					
				}
				
			}else if (jsonTree.isObject()) {
				if (jsonTree.elements().next().isObject()||jsonTree.elements().next().isValueNode()) {
					arrayJson.add(mapper.valueToTree(flattenJson("", jsonTree)));
				}else {
					for(int i=0; i<jsonTree.elements().next().size();i++) {
						arrayJson.add(mapper.valueToTree(flattenJson("", jsonTree.elements().next().get(i))));
						
					}
					
				}
			}
			
//			log.info("this is the arrayMap --------> "+arrayJson.size()+"\n"+arrayJson.toPrettyString());
			
			return arrayJson;
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new APIException("Issue while processing the jsonResponse data /n"+e.getMessage(),e);
		}

	}
	
	
	
	// This is to flatten/unwarp the nested Json 
	private TreeMap<String, String> flattenJson (String prefix, JsonNode json)
	{
		
		TreeMap <String, String> map = new TreeMap<String,String>();
		
		Iterator<Map.Entry<String, JsonNode>> iterator = json.fields();
		
		while (iterator.hasNext()) {

			Map.Entry<String, JsonNode> entry = iterator.next();
//			log.info("inside the while loop" + entry.getValue().isObject() + " is an object  "
//					+ entry.getValue().isArray() + " is an array  " + entry.getValue().isValueNode()
//					+ " is value node  " + entry.getKey());
			
		
		if(entry.getValue().isObject()){
//			code for object
			
			String objectPrefix =(prefix.isBlank()?entry.getKey():prefix+"."+entry.getKey());
			
			map.putAll(flattenJson(objectPrefix,entry.getValue())); 
			
		}else if (entry.getValue().isArray()) {
			
//			code for Json Array
			String arrayPrefix;
			for(int i=0; i<entry.getValue().size();i++)
			{
				arrayPrefix = (prefix.isBlank()?entry.getKey()+".["+i+"]":prefix+"."+entry.getKey()+".["+i+"]");
				map.putAll(flattenJson(arrayPrefix,entry.getValue().get(i)));
			}
			
			
		}else if (entry.getValue().isValueNode()) {
			map.put((prefix.isBlank()?entry.getKey():prefix+"."+entry.getKey()), entry.getValue().toString());	
			
		}
		
		}
		return map;
	}

// To Fetch the json from the eCom API 
	private String fetchJsonFromAPI(String userName, String passCode, String url) {

		
		RestTemplate restTemplate= new RestTemplate();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(userName, passCode);
		
		HttpEntity<String> request = new HttpEntity<String>(headers);
		ResponseEntity<String> response= restTemplate.exchange(url,HttpMethod.GET, request, String.class);
		
		if(!(response.getStatusCodeValue() == 200)) {
			
			throw new APIException("Bad Request -> Uanble to fetch data from APi \n "+response.getBody());
			
		}
		if(response.getBody().isEmpty())
		{
			throw new APIException("Bad Request ->no content available \n ");
		}
		
		
		return response.getBody();
	}
	
	private boolean checkIfColumn(ArrayList<String> columnNames, String field) {
		// TODO Auto-generated method stub
		for(int i = 0; i<columnNames.size();i++) {
			if(columnNames.get(i).equalsIgnoreCase(field)) {
				return true;
			}
		}
		return false;
	}




}
