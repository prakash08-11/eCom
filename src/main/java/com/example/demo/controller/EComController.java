package com.example.demo.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.CredentialsEntity;
import com.example.demo.service.EcomService;

@RestController
@RequestMapping("/eCom")
public class EComController {
	
	
	@Autowired
	private EcomService eComService ;
	
	
	@GetMapping("/health")
	public String health() {
		return "Service Available";
	}
	@PostMapping(value = "/eComConnector")
	@ResponseStatus(code = HttpStatus.CREATED)
	public ResponseEntity<String> eComConnector(@RequestBody CredentialsEntity details) {
		
		eComService.validate(details);
		
		String userName= details.getUserName();
		String passCode =details.getPassCode();
		String url="https://virtina-store.myshopify.com/admin/api/2020-10/products.json";
		
		if(eComService.eComConnector(userName,passCode,url))
		{
			return new ResponseEntity<>("Saved The CSV file in the local", HttpStatus.CREATED);

		}
		
		return new ResponseEntity<>("Bad Request unable to process the request ", HttpStatus.BAD_REQUEST);
		
	}
}
