package com.thinkgem.jeesite.common.utils.excel.handlers;






import java.io.IOException;

import java.io.InputStream;

import java.util.ArrayList;

import java.util.List;



import javax.xml.parsers.ParserConfigurationException;


import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;

import org.apache.poi.openxml4j.opc.OPCPackage;

import org.apache.poi.ss.usermodel.DataFormatter;

import org.apache.poi.ss.util.CellAddress;

import org.apache.poi.ss.util.CellReference;


import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;

import org.apache.poi.xssf.eventusermodel.XSSFReader;

import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;

import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;

import org.apache.poi.xssf.model.StylesTable;

import org.apache.poi.xssf.usermodel.XSSFComment;

import org.xml.sax.ContentHandler;

import org.xml.sax.InputSource;

import org.xml.sax.SAXException;

import org.xml.sax.XMLReader;



/**

 * A rudimentary XLSX -> CSV processor modeled on the

 * POI sample program XLS2CSVmra from the package

 * org.apache.poi.hssf.eventusermodel.examples.

 * As with the HSSF version, this tries to spot missing

 * rows and cells, and output empty entries for them.

 * <p/>

 * Data sheets are read using a SAX parser to keep the

 * memory footprint relatively small, so this should be

 * able to read enormous workbooks.  The styles table and

 * the shared-string table must be kept in memory.  The

 * standard POI styles table class is used, but a custom

 * (read-only) class is used for the shared string table

 * because the standard POI SharedStringsTable grows very

 * quickly with the number of unique strings.

 * <p/>

 * For a more advanced implementation of SAX event parsing

 * of XLSX files, see {@link XSSFEventBasedExcelExtractor}

 * and {@link XSSFSheetXMLHandler}. Note that for many cases,

 * it may be possible to simply use those with a custom

 * {@link SheetContentsHandler} and no SAX code needed of

 * your own!

 */

/**

 * 用sax解析xlsx 格式文档 转成csv格式

 *

 *

 */

public class XLSX2CSV {

    /**

     * Uses the XSSF Event SAX helpers to do most of the work

     * of parsing the Sheet XML, and outputs the contents

     * as a (basic) CSV.

     */

    private class SheetToCSV implements SheetContentsHandler {

        private boolean firstCellOfRow = false;

        private int currentRow = -1;

        private int currentCol = -1;



        private void outputMissingRows(int number) {

            for (int i = 0; i < number; i++) {

                curstr = new ArrayList<String>();

                for (int j = 0; j < minColumns; j++) {

                    curstr.add("");

                }

                output.add(curstr);

            }

        }



        @Override

        public void startRow(int rowNum) {

            curstr = new ArrayList<String>();

            // If there were gaps, output the missing rows

            outputMissingRows(rowNum - currentRow - 1);

            // Prepare for this row

            firstCellOfRow = true;

            currentRow = rowNum;

            currentCol = -1;

        }



        @Override

        public void endRow(int rowNum) {

            // Ensure the minimum number of columns

            for (int i = currentCol; i < minColumns ; i++) {

                curstr.add("");

            }

            output.add(curstr);

        }



        @Override

        public void cell(String cellReference, String formattedValue,

                         XSSFComment comment) {

//            if (firstCellOfRow) {

//                firstCellOfRow = false;

//            } else {

//                curstr.append(',');

//            }



            // gracefully handle missing CellRef here in a similar way as XSSFCell does

            if (cellReference == null) {

                cellReference = new CellAddress(currentRow, currentCol).formatAsString();

            }



            // Did we miss any cells?

            int thisCol = (new CellReference(cellReference)).getCol();

            int missedCols = thisCol - currentCol - 1;

            for (int i = 0; i < missedCols; i++) {

                curstr.add("");

            }

            currentCol = thisCol;



            // Number or string?

            try {

                Double.parseDouble(formattedValue);

                curstr.add(formattedValue);

            } catch (NumberFormatException e) {

                // output.append('"');

                curstr.add(formattedValue);

                //   output.append('"');

            }

        }



        @Override

        public void headerFooter(String text, boolean isHeader, String tagName) {

            // Skip, no headers or footers in CSV

        }

    }





    ///



    private final OPCPackage xlsxPackage;



    /**

     * Number of columns to read starting with leftmost

     */

    private final int minColumns;



    /**

     * Destination for data

     */



    private List<ArrayList<String>> output;

    private ArrayList<String> curstr;



    public  List<ArrayList<String>> get_output(){

        return output;

    }



    /**

     * Creates a new XLSX -> CSV converter

     *

     * @param pkg        The XLSX package to process

     * @param output     The PrintStream to output the CSV to

     * @param minColumns The minimum number of columns to output, or -1 for no minimum

     */

    public XLSX2CSV(OPCPackage pkg, int minColumns) {

        this.xlsxPackage = pkg;

        this.minColumns = minColumns;

    }





    /**

     * Parses and shows the content of one sheet

     * using the specified styles and shared-strings tables.

     *

     * @param styles

     * @param strings

     * @param sheetInputStream

     */

    public void processSheet(

            StylesTable styles,

            ReadOnlySharedStringsTable strings,

            SheetContentsHandler sheetHandler,

            InputStream sheetInputStream)

            throws IOException, ParserConfigurationException, SAXException {

        DataFormatter formatter = new DataFormatter();

        InputSource sheetSource = new InputSource(sheetInputStream);

        try {

            XMLReader sheetParser = SAXHelper.newXMLReader();

            ContentHandler handler = new XSSFSheetXMLHandler(

                    styles, null, strings, sheetHandler, formatter, false);

            sheetParser.setContentHandler(handler);

            sheetParser.parse(sheetSource);

        } catch (ParserConfigurationException e) {

            throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());

        }

    }



    /**

     * Initiates the processing of the XLS workbook file to CSV.

     *

     * @throws IOException

     * @throws OpenXML4JException

     * @throws ParserConfigurationException

     * @throws SAXException

     */

    public void process()

            throws IOException, OpenXML4JException, ParserConfigurationException, SAXException {

        ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(this.xlsxPackage);

        XSSFReader xssfReader = new XSSFReader(this.xlsxPackage);

        StylesTable styles = xssfReader.getStylesTable();

        XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();

//        int index = 0;

        while (iter.hasNext()) {

            output = new ArrayList<ArrayList<String>> ();

            InputStream stream = iter.next();

//            String sheetName = iter.getSheetName();

//            System.out.println("正在读取sheet： "+sheetName + " [index=" + index + "]:");

            processSheet(styles, strings, new SheetToCSV(), stream);

//            System.out.println("sheet 读取完成!");

            stream.close();

//            ++index;

        }

    }





//    public static void main(String[] args) throws Exception {

//      /*  if (args.length < 1) {

//            System.err.println("Use:");

//            System.err.println("  XLSX2CSV <xlsx file> [min columns]");

//            return;

//        }*/

//

//        File xlsxFile = new File("F:\\8月数据.xlsx");

//        if (!xlsxFile.exists()) {

//            System.err.println("Not found or not a file: " + xlsxFile.getPath());

//            return;

//        }

//

//        int minColumns = -1;

//        if (args.length >= 2)

//            minColumns = Integer.parseInt(args[1]);

//

//        // The package open is instantaneous, as it should be.

//        OPCPackage p = OPCPackage.open(xlsxFile.getPath(), PackageAccess.READ);

//        XLSX2CSV xlsx2csv = new XLSX2CSV(p, System.out, minColumns);

//        xlsx2csv.process();

//        p.close();

//    }

}
