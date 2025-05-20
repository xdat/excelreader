package com.orakoglu.excelreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.orakoglu.excelreader.common.CsvRequest;

@Component
public class Controller {

	@PersistenceContext
	EntityManager entityManager;

	Logger logger = Logger.getLogger(Controller.class.getName());

	public String readExcel(String fileName, String schemaName, String outputDir) throws IOException {
		StringBuilder sb = new StringBuilder();
		StopWatch watch = new StopWatch();
		try (InputStream is = new FileInputStream(fileName); ReadableWorkbook wb = new ReadableWorkbook(is)) {

			watch.start();

			wb.getSheets().forEach(sheet -> {
				String tableName = schemaName.trim().replaceAll("[\\W]|_", "_").toLowerCase().concat(".").concat(convertTo(sheet.getName().trim()).replaceAll("[\\W]|_", "_")).toLowerCase();
				List<String> columnNames = new ArrayList<>();
				try (Stream<Row> rows = sheet.openStream()) {

					Row row = rows.findFirst().get();
					row.forEach(c -> {
						String columnName = convertTo(c.getText().trim()).replaceAll("[\\W]|_", "_").toLowerCase();
						if (columnName.trim().compareTo("") == 0)
							columnName = "__EMPTY";
						while (columnNames.contains(columnName))
							columnName += "_";
						columnNames.add(columnName);
					});

				} catch (Exception e) {
					e.printStackTrace();
				}

				// sb.append("drop schema if exists ");
				// sb.append(schemaName.trim().replaceAll("[\\W]|_", "_").toLowerCase());
				// sb.append(" cascade;\n");
				sb.append("create schema if not exists ");
				sb.append(schemaName.trim().replaceAll("[\\W]|_", "_").toLowerCase());
				sb.append(";\n");
				sb.append("create table ");
				sb.append(tableName);
				sb.append(" (\n\t___ID bigserial primary key");
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

							String value = r.getCellRawValue(columnNames.indexOf(columnName)).orElse(null);
							sb.append(value == null ? "NULL" : "'".concat(value.replace("'", "''")).concat("'"));
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

	private String readCsv(String fileName, CsvRequest request) throws IOException {
		StringBuilder sb = new StringBuilder();
		StopWatch watch = new StopWatch();
		try {
			watch.start();
			File __file = new File(fileName);
			String outputDir = __file.getParent();
			Scanner scanner = new Scanner(__file, request.getCharSet());
			String tableName = "__".concat(__file.getName().toLowerCase(Application.en).replace(".csv", ""));

			sb.append("set statement_timeout = 0;\n");
			sb.append("set lock_timeout = 0;\n");
			sb.append("set idle_in_transaction_session_timeout = 0;\n");
			sb.append("set client_encoding = 'UTF8';\n");
			sb.append("set standard_conforming_strings = on;\n");
			sb.append("select pg_catalog.set_config('search_path', '', false);\n");
			sb.append("set check_function_bodies = false;\n");
			sb.append("set xmloption = content;\n");
			sb.append("set client_min_messages = warning;\n");
			sb.append("set row_security = off;\n");

//			sb.append("drop schema if exists ");
//			sb.append(schemaName.trim().replaceAll("[\\W]|_", "_").toLowerCase(Application.en));
//			sb.append(" cascade;\n");
			sb.append("create schema if not exists ");
			sb.append(request.getSchemaName().trim().replaceAll("[\\W]|_", "_").toLowerCase(Application.en));
			sb.append(";\n");
			StringBuilder copy = new StringBuilder();
			copy.append("copy ");
			copy.append(request.getSchemaName());
			copy.append(".");
			copy.append(tableName);
			copy.append(" ( ");

			sb.append("create table ");
			sb.append(request.getSchemaName());
			sb.append(".");
			sb.append(tableName);
			sb.append(" (");
//			sb.append("\n\t___id bigserial primary key");

			if (request.isFirstLineHeader() && scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] values = line.split(String.valueOf(request.getDelimiter()));
				Arrays.asList(values).forEach(value -> {
					String columnName = value.toLowerCase(Application.en).replace(String.valueOf(request.getTextDelimiter()), "");
					sb.append("\n\t");
					sb.append(columnName);
					sb.append(" varchar(1000),");

					copy.append(columnName);
					copy.append(", ");
				});
				sb.reverse().deleteCharAt(0).reverse().append(");\n");
			}

			copy.reverse().deleteCharAt(1).reverse().append(" )  from stdin;");

			sb.append("\n");
			sb.append(copy.toString());
			sb.append("\n");

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
//				System.out.println(line);
				String[] values = line.split(String.valueOf(request.getDelimiter()));

				Arrays.asList(values).forEach(value -> {
					String __value = "\\N";
					if (!(value == null || value.replace(String.valueOf(request.getTextDelimiter()).trim(), "").equals("")))
						__value = value.replace(String.valueOf(request.getTextDelimiter()).trim(), "");

					__value = __value.replace("'", "\\'").replace("\"", "\\\"")//
							.replace("\\", "\\\\").replace("\n", "\\n")//
							.replace("\r", "\\r").replace("\t", "\\t")//
							.replace("\b", "\\b").replace("\f", "\\f");

					sb.append(__value).append("\t");
				});
				sb.append("\n");
			}

			sb.append(".");

			scanner.close();

			File file = new File(outputDir.concat("/").concat(tableName).concat(".sql"));
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
			System.out.println(file.getAbsolutePath());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		watch.stop();
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

	public void doReadDirectoryForDbf(CsvRequest request) {
		try {
			IOFileFilter fileFilter = new SuffixFileFilter("DBF");
			IOFileFilter dirFilter = DirectoryFileFilter.DIRECTORY;
			if (!new File(request.getDirName().concat("/csvexport/")).exists())
				new File(request.getDirName().concat("/csvexport/")).mkdirs();

			Iterator<File> it = FileUtils.iterateFilesAndDirs(new File(request.getDirName()), fileFilter, dirFilter);
			while (it.hasNext()) {
				File file = it.next();
				if (!file.isDirectory()) {

					if (!file.getName().toLowerCase(Application.en).contains("sudokmn"))
						continue;

					String sourceDbf = file.getPath();
					String targetCsvName = file.getParent().replace(request.getDirName(), "").replace("/", "_").concat("_").concat(file.getName().replace(".DBF", ".CSV")).replaceFirst("_", "");
					String targetCsv = request.getDirName().concat("/csvexport/").concat(targetCsvName);

					System.out.println(sourceDbf);
					// System.out.println(sourceDbf.replace(dirName, ""));
					// System.out.println(dirName);
					// System.out.println(targetCsvName);
					System.out.println(targetCsv);
					String cmd = String.format("dbf-rb -c %s > %s", sourceDbf, targetCsv);
//					System.out.println(cmd);
					Process p = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", cmd });
					InputStream stdout = p.getInputStream();
					IOUtils.toString(stdout, Charset.defaultCharset());
					readCsv(targetCsv, request);

//					InputStream stderr = p.getErrorStream();
//					System.out.println(IOUtils.toString(stderr, Charset.defaultCharset()));
//					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static void main(String[] args) throws Exception {
		Controller controller = new Controller();
//		controller.readExcel("/home/xdat/Downloads/Secim-Sonuclari_2023_TURKIYE_MILLETVEKILI SECIMI_2023-05-31.xlsx", "MV20230531", "/home/xdat/Desktop/");

		CsvRequest request = new CsvRequest();
		request.setDirName("/home/xdat/Desktop/ahili/AHILI/ES2/SU");
		request.setSchemaName("eskomdos");
		request.setCharSet("IBM857");
		request.setFirstLineHeader(true);
		request.setDelimiter(',');
		request.setTextDelimiter('"');
		controller.doReadDirectoryForDbf(request);

	}
}
