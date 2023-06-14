package com.orakoglu.excelreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
public class Controller {

	Logger logger = Logger.getLogger(Controller.class.getName());

	public String readExcel(String fileName, String schemaName, String outputDir) throws IOException {
		StringBuilder sb = new StringBuilder();
		StopWatch watch = new StopWatch();
		try (InputStream is = new FileInputStream(fileName); ReadableWorkbook wb = new ReadableWorkbook(is)) {
			
			watch.start();
			wb.getSheets().forEach(sheet -> {
				String tableName = schemaName.trim().replaceAll("[\\W]|_", "_").toLowerCase().concat(".")
						.concat(convertTo(sheet.getName().trim()).replaceAll("[\\W]|_", "_")).toLowerCase();
				List<String> columnNames = new ArrayList<>();
				try (Stream<Row> rows = sheet.openStream()) {

					Row row = rows.findFirst().get();
					row.forEach(c -> {
						String columnName = convertTo(c.getText().trim()).replaceAll("[\\W]|_", "_").toLowerCase();
						while (columnNames.contains(columnName))
							columnName += "_";
						columnNames.add(columnName);
					});

				} catch (Exception e) {
					e.printStackTrace();
				}

				sb.append("drop schema if exists ");
				sb.append(schemaName.trim().replaceAll("[\\W]|_", "_").toLowerCase());
				sb.append(" cascade;\n");
				sb.append("create schema ");
				sb.append(schemaName.trim().replaceAll("[\\W]|_", "_").toLowerCase());
				sb.append(";\n");
				sb.append("create table ");
				sb.append(tableName);
				sb.append(" (\n\tID bigserial primary key");
				for (String columnName : columnNames) {
					sb.append(",\n\t");
					sb.append(columnName);
					sb.append(" varchar(1000)");
				}
				sb.append(");\n");

				sb.append("insert into ");
				sb.append(tableName);
				sb.append("(");
				for (String columnName : columnNames) {
					sb.append(columnName);
					if (columnNames.indexOf(columnName) != columnNames.size() - 1)
						sb.append(",");
				}
				sb.append(")\n\tvalues\n");

				try (Stream<Row> rows = sheet.openStream()) {
					boolean[] isFirst = { true };
					rows.skip(1).forEach(r -> {
						if (!isFirst[0])
							sb.append(",\n");
						else
							isFirst[0] = !isFirst[0];
						sb.append("\t\t(");
						for (String columnName : columnNames) {
							String value = r.getCellAsString(columnNames.indexOf(columnName)).orElse(null);
							sb.append(value == null ? "NULL" : "'".concat(value).concat("'"));
							if (columnNames.indexOf(columnName) != columnNames.size() - 1)
								sb.append(",");

						}
						sb.append(")");
					});
					sb.append(";");

				} catch (Exception e) {
					e.printStackTrace();
				}

				watch.stop();

				File file = new File(outputDir.concat(tableName).concat(".SQL"));
				if (file.exists())
					file.delete();
				try (FileOutputStream outputStream = new FileOutputStream(file)) {
					outputStream.write(sb.toString().getBytes());
					outputStream.flush();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
		return "done in ".concat(Long.toString(watch.getTotalTimeMillis()));
	}

	private String convertTo(String value) {
		String convertedValue = "";
		if (value != null && !"".equals(value)) {
			convertedValue = value.replace("Ç", "C");
			convertedValue = convertedValue.replace("Ğ", "G");
			convertedValue = convertedValue.replace("İ", "I");
			convertedValue = convertedValue.replace("Ö", "O");
			convertedValue = convertedValue.replace("Ş", "S");
			convertedValue = convertedValue.replace("Ü", "U");
			convertedValue = convertedValue.replace("ç", "c");
			convertedValue = convertedValue.replace("ğ", "g");
			convertedValue = convertedValue.replace("ı", "i");
			convertedValue = convertedValue.replace("ö", "o");
			convertedValue = convertedValue.replace("ş", "s");
			convertedValue = convertedValue.replace("ü", "u");
		}
		return convertedValue;
	}

	public static void __main(String[] args) throws Exception {
		Controller controller = new Controller();
		controller.readExcel("/home/xdat/Downloads/Secim-Sonuclari_2023_TURKIYE_MILLETVEKILI SECIMI_2023-05-31.xlsx",
				"MV20230531", "/home/xdat/Desktop/");
	}
}
