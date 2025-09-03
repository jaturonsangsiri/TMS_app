package com.siamatic.tms.services.excel;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.zip.*;

public class CreateExcel {
  // Static Exel (XLSX) file
  static String content_types_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default ContentType=\"application/vnd.openxmlformats-package.relationships+xml\" Extension=\"rels\"/><Default ContentType=\"application/xml\" Extension=\"xml\"/><Override ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\" PartName=\"/docProps/app.xml\"/><Override ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\" PartName=\"/docProps/core.xml\"/><Override ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml\" PartName=\"/xl/sharedStrings.xml\"/><Override ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\" PartName=\"/xl/styles.xml\"/><Override ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\" PartName=\"/xl/workbook.xml\"/><Override ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\" PartName=\"/xl/worksheets/sheet1.xml\"/></Types>";
  static String docProps_app_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\"><Application>" + "Created Low level From Scratch" + "</Application></Properties>";
  static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  static String docProps_core_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><dcterms:created xsi:type=\"dcterms:W3CDTF\">" + formatter.format(Calendar.getInstance().getTime()) /*java.time.Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString()*/ + "</dcterms:created><dc:creator>" + "Axel Richter from scratch" + "</dc:creator></cp:coreProperties>";
  static String _rels_rels_xml  = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Target=\"xl/workbook.xml\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\"/><Relationship Id=\"rId2\" Target=\"docProps/app.xml\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties\"/><Relationship Id=\"rId3\" Target=\"docProps/core.xml\" Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\"/></Relationships>";
  static String xl_rels_workbook_xml_rels_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Target=\"sharedStrings.xml\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings\"/><Relationship Id=\"rId2\" Target=\"styles.xml\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\"/><Relationship Id=\"rId3\" Target=\"worksheets/sheet1.xml\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\"/></Relationships>";
  static String xl_sharedstrings_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><sst count=\"0\" uniqueCount=\"0\" xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"/>";
  static String xl_styles_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><numFmts count=\"0\"/><fonts count=\"1\"><font><sz val=\"11.0\"/><color indexed=\"8\"/><name val=\"Calibri\"/><family val=\"2\"/><scheme val=\"minor\"/></font></fonts><fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"darkGray\"/></fill></fills><borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders><cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs><cellXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/></cellXfs></styleSheet>";
  static String xl_workbook_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><workbookPr date1904=\"false\"/><bookViews><workbookView activeTab=\"0\"/></bookViews><sheets><sheet name=\"" + "Sheet1" + "\" r:id=\"rId3\" sheetId=\"1\"/></sheets></workbook>";
  static String xl_worksheets_sheet1_xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><dimension ref=\"A1\"/><sheetViews><sheetView workbookViewId=\"0\" tabSelected=\"true\"/></sheetViews><sheetFormatPr defaultRowHeight=\"15.0\"/><sheetData/><pageMargins bottom=\"0.75\" footer=\"0.3\" header=\"0.3\" left=\"0.7\" right=\"0.7\" top=\"0.75\"/></worksheet>";
  ArrayList<ArrayList<Object>> sheetDat = new ArrayList<>();

  public void CreatePlainXLSX() {
    sheetDat.add(0, new ArrayList<>());
  }

  public void addCell(int rowNum, int cellNum, Object value) {
    if (sheetDat.size() <= rowNum){
      for (int a = sheetDat.size(); a <= rowNum; a++) {
        sheetDat.add(new ArrayList<>());
      }
    }

    if (sheetDat.get(rowNum).size() <= cellNum) {
      for (int a = sheetDat.get(rowNum).size(); a <= cellNum; a++) {
        sheetDat.get(rowNum).add("");
      }
    }
    sheetDat.get(rowNum).set(cellNum, value);
  }

  private static int toNumber(String name) {
    int number = 0;
    for (int i = 0; i < name.length(); i++) {
      number = number * 26 + (name.charAt(i) - ('A' - 1));
    }
    return number;
  }

  private static String toName(int number) {
    StringBuilder sb = new StringBuilder();
    while (number-- > 0) {
      sb.append((char)('A' + (number % 26)));
      number /= 26;
    }
    return sb.reverse().toString();
  }

  private String incrementColumnR(String a) {
    return toName(toNumber(a) + 1);
  }

  public void exportToFile(FileOutputStream fileout) throws Exception {
    // result goes into a ByteArrayOutputStream
    ByteArrayOutputStream resultout = new ByteArrayOutputStream();

    // needed objects
    ZipEntry zipentry;
    byte[] data;

    // create ZipOutputStream
    ZipOutputStream zipout = new ZipOutputStream(resultout);

    // create the static parts of the XLSX ZIP file:
    zipentry = new ZipEntry("[Content_Types].xml");
    zipout.putNextEntry(zipentry);
    data = content_types_xml.getBytes();
    zipout.write(data, 0, data.length);
    zipout.closeEntry();

    zipentry = new ZipEntry("docProps/app.xml");
    zipout.putNextEntry(zipentry);
    data = docProps_app_xml.getBytes();
    zipout.write(data, 0, data.length);
    zipout.closeEntry();

    zipentry = new ZipEntry("docProps/core.xml");
    zipout.putNextEntry(zipentry);
    data = docProps_core_xml.getBytes();
    zipout.write(data, 0, data.length);
    zipout.closeEntry();

    zipentry = new ZipEntry("_rels/.rels");
    zipout.putNextEntry(zipentry);
    data = _rels_rels_xml.getBytes();
    zipout.write(data, 0, data.length);
    zipout.closeEntry();

    zipentry = new ZipEntry("xl/_rels/workbook.xml.rels");
    zipout.putNextEntry(zipentry);
    data = xl_rels_workbook_xml_rels_xml.getBytes();
    zipout.write(data, 0, data.length);
    zipout.closeEntry();

    zipentry = new ZipEntry("xl/sharedStrings.xml");
    zipout.putNextEntry(zipentry);
    data = xl_sharedstrings_xml.getBytes();
    zipout.write(data, 0, data.length);
    zipout.closeEntry();

    zipentry = new ZipEntry("xl/styles.xml");
    zipout.putNextEntry(zipentry);
    data = xl_styles_xml.getBytes();
    zipout.write(data, 0, data.length);
    zipout.closeEntry();

    zipentry = new ZipEntry("xl/workbook.xml");
    zipout.putNextEntry(zipentry);
    data = xl_workbook_xml.getBytes();
    zipout.write(data, 0, data.length);
    zipout.closeEntry();

    // preparing the sheet data:
    String sheetdata = "<sheetData>";
    int r = 0;
    String c = toName(0);
    ArrayList<ArrayList<Object>> sheet = sheetDat;
    for (ArrayList<Object> rowData : sheet) {
      sheetdata += "<row r=\"" + ++r + "\">";
      c = toName(0);
      for (Object cellData : rowData) {
        c = incrementColumnR(c);
        sheetdata += "<c r=\"" + c + r + "\"";
        if (cellData instanceof String && ((String) cellData).startsWith("=")) {
          sheetdata += "><f>" + ((String) cellData).replace("=", "") + "</f></c>";
        } else if (cellData instanceof String) {
          sheetdata += " t=\"inlineStr\"><is><t>" + cellData + "</t></is></c>";
        } else if (cellData instanceof Double || cellData instanceof Integer) {
          sheetdata += "><v>" + cellData + "</v></c>";
        }
      }
      sheetdata += "</row>";
    }
    sheetdata += "</sheetData>";

    // get the static sheet xml into a buffer for further processing
    StringBuffer xl_worksheets_sheet1_xml_buffer = new StringBuffer(xl_worksheets_sheet1_xml);

    // get position of the <dimension ref=\"A1\"/> in the static xl_worksheets_sheet1_xml
    int dimensionstart = xl_worksheets_sheet1_xml_buffer.indexOf("<dimension ref=\"A1\"/>");
    // replace the <dimension ref=\"A1\"/> with the new dimension
    xl_worksheets_sheet1_xml_buffer = xl_worksheets_sheet1_xml_buffer.replace(
            dimensionstart,
            dimensionstart + "<dimension ref=\"A1\"/>".length(),
            "<dimension ref=\"A1:" + c + r + "\"/>");

    // get position of the <sheetData/> in the static xl_worksheets_sheet1_xml
    int sheetdatastart = xl_worksheets_sheet1_xml_buffer.indexOf("<sheetData/>");
    // replace the <sheetData/> with the prepared sheet date string
    xl_worksheets_sheet1_xml_buffer = xl_worksheets_sheet1_xml_buffer.replace(
            sheetdatastart,
            sheetdatastart + "<sheetData/>".length(),
            sheetdata);

    // create the xl/worksheets/sheet1.xml
    zipentry = new ZipEntry("xl/worksheets/sheet1.xml");
    zipout.putNextEntry(zipentry);
    data = xl_worksheets_sheet1_xml_buffer.toString().getBytes();
    zipout.write(data, 0, data.length);
    zipout.closeEntry();

    zipout.finish();

    // now ByteArrayOutputStream resultout contains the XLSX file data

    // writing this data into a file
    if(fileout != null) {
      resultout.writeTo(fileout);
      resultout.close();
    }
  }
}