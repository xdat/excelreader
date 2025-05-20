package com.orakoglu.excelreader.common;

public class CsvRequest {
	private String dirName;
	private String schemaName;
	private String charSet;
	private boolean firstLineHeader;
	private char delimiter;
	private char textDelimiter;

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getCharSet() {
		return charSet;
	}

	public void setCharSet(String charSet) {
		this.charSet = charSet;
	}

	public boolean isFirstLineHeader() {
		return firstLineHeader;
	}

	public void setFirstLineHeader(boolean firstLineHeader) {
		this.firstLineHeader = firstLineHeader;
	}

	public char getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}

	public char getTextDelimiter() {
		return textDelimiter;
	}

	public void setTextDelimiter(char textDelimiter) {
		this.textDelimiter = textDelimiter;
	}

	public String getDirName() {
		return dirName;
	}

	public void setDirName(String dirName) {
		this.dirName = dirName;
	}

}
