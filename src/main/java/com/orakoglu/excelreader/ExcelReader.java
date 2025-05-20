package com.orakoglu.excelreader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.orakoglu.excelreader.common.CsvRequest;
import com.orakoglu.excelreader.common.ExcelRequest;

@RestController
public class ExcelReader {

	@Autowired
	Controller controller;

	@PostMapping(value = "/readexcel", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> readexcel(@RequestBody ExcelRequest request) {
		String _SQL = "";
		try {
			_SQL = controller.readExcel(request.getFilename(), request.getSchemaName(), request.getOutputDir());
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.OK);
		}

		return new ResponseEntity<String>(_SQL, HttpStatus.OK);
	}

	@PostMapping(value = "/readcsv", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> readCsv(@RequestBody CsvRequest request) {
		try {
			controller.doReadDirectoryForDbf(request);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.OK);
		}
		return new ResponseEntity<String>("DONE", HttpStatus.OK);
	}

	@PostMapping(value = "/runsqls", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<String> runSqls(@RequestBody ExcelRequest request) {
		String _SQL = "";
		try {
			_SQL = controller.readExcel(request.getFilename(), request.getSchemaName(), request.getOutputDir());
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.OK);
		}
		return new ResponseEntity<String>(_SQL, HttpStatus.OK);
	}

}
