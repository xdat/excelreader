package com.orakoglu.excelreader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExcelReader {

	@Autowired
	Controller controller;

	@RequestMapping("/")
	@ResponseBody
	public ResponseEntity<String> home(@RequestParam("filename") String filename,
			@RequestParam("schemaname") String schemaName, @RequestParam("outputdir") String outputDir) {
		String _SQL = "";
		try {
			_SQL = controller.readExcel(filename, schemaName, outputDir);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new ResponseEntity<String>(_SQL, HttpStatus.OK);
	}

}
