package utils;

import enums.FrameworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.DataProvider;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

public final class DataProviderUtils {

    private static final Logger log = LogManager.getLogger(DataProviderUtils.class);
    private static final String EXTENSION = ".xlsx";
    private static final String ITERATION = "iteration";
    private static final String INPUT_SHEET_NAME = "inputSheetName";

    private DataProviderUtils() {
    }

    // Setting Up Data To Execute
    @DataProvider(name = "dataForTest", parallel = true)
    public static Object[][] testData(Method m) throws IOException {

        // Clone Test Input File To Write Results & Return Test-Data
        cloneFile(FrameworkConstants.getInputExcelPath());
        return readDataFromExcel(FrameworkConstants.getInputExcelPath(), PropertyUtils.getValue(INPUT_SHEET_NAME));

    }

    public static synchronized Object[][] readDataFromExcel(String excelPath, String sheetName) {

        try (InputStream inp = new FileInputStream(excelPath);
             Workbook wb = WorkbookFactory.create(inp)) {

            // To check the sheet is null or exists
            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet with name " + sheetName + " not found in the Excel file.");
            }

            List<Map<String, String>> dataList = extractData(sheet);

            return convertTo2DArray(dataList);

        } catch (IOException e) {
            log.error("Unable to read data from excel --> {}.", e.getMessage());
            return new Object[0][0]; // Return empty array in case of failure
        }
    }

    private static synchronized List<Map<String, String>> extractData(Sheet sheet) {

        // Plus 1 for 0-based index
        int rowCount = sheet.getLastRowNum() + 1;
        int columnCount = sheet.getRow(0).getLastCellNum();

        List<Map<String, String>> dataList = new ArrayList<>();
        Set<String> uniqueIterations = new HashSet<>();

        // Start from 1 to skip header
        for (int i = 1; i < rowCount; i++) {
            Row row = sheet.getRow(i);
            if (row != null && !row.getZeroHeight()) {
                Map<String, String> mapData = extractRowData(sheet.getRow(0), row, columnCount);
                String iteration = mapData.get(ITERATION);
                if (iteration != null && !iteration.contains("_") && uniqueIterations.add(iteration)) {
                    dataList.add(mapData);
                }
            }
        }

        return dataList;
    }

    private static synchronized Map<String, String> extractRowData(Row headerRow, Row dataRow, int columnCount) {

        DataFormatter dft = new DataFormatter();
        dft.setUseCachedValuesForFormulaCells(true);

        Map<String, String> mapData = new HashMap<>();
        for (int j = 0; j < columnCount; j++) {
            Cell keyCell = headerRow.getCell(j);
            Cell valueCell = dataRow.getCell(j);
            if (keyCell != null && valueCell != null) {
                String tempKey = dft.formatCellValue(keyCell);
                String tempValue = dft.formatCellValue(valueCell);
                mapData.put(tempKey, tempValue);
            }
        }

        return mapData;
    }

    private static synchronized Object[][] convertTo2DArray(List<Map<String, String>> dataList) {
        return dataList.stream()
                .map(m -> new Object[]{m})
                .toArray(Object[][]::new);
    }

    public static synchronized void cloneFile(String originalFileLocation) throws IOException {

        int columnCount;

        // Create Clone File
        try (InputStream inp = new FileInputStream(originalFileLocation);
             Workbook wb = WorkbookFactory.create(inp)) {

            FileOutputStream fos = new FileOutputStream(FrameworkConstants.getResultDirectoryPath() + File.separator + FrameworkConstants.getClonedExcelName());
            wb.write(fos);

        } catch (Exception e) {
            log.error(" !!! Cloning File Failed !!! ---> {}", e.getMessage());
        }

        // Variables For Flagging
        String sheetName = PropertyUtils.getValue(INPUT_SHEET_NAME);

        // Create Headings For Common/TempRun
        try (InputStream inp = new FileInputStream(FrameworkConstants.getResultDirectoryPath() + File.separator + FrameworkConstants.getClonedExcelName());
             Workbook wb = WorkbookFactory.create(inp)) {

            Sheet sheet = wb.getSheet(sheetName);
            columnCount = sheet.getRow(0).getLastCellNum();

            // Enter Headings To Excel
            try (OutputStream os = new FileOutputStream(FrameworkConstants.getResultDirectoryPath() + File.separator + FrameworkConstants.getClonedExcelName())) {

                // Open Excel & Write
                Row row = sheet.getRow(0);

                // Create & Set Headings First
                List<String> columnHeadings = getColumnHeadings();
                for (int i = 0; i < columnHeadings.size(); i++) {
                    row.createCell(columnCount + i).setCellValue(columnHeadings.get(i));
                }

                CellStyle cellStyle = row.getCell(0).getCellStyle();
                row.setRowStyle(cellStyle);

                // Auto size the column widths
                for (int columnIndex = 0; columnIndex < sheet.getRow(0).getLastCellNum(); columnIndex++) {
                    sheet.autoSizeColumn(columnIndex);
                }

                // Hide Input Data Columns
                if (PropertyUtils.getValue("lob").equalsIgnoreCase("AU")) {
                    setColumnsHidden(sheet, 15, 31);
                } else {
                    setColumnsHidden(sheet, 3, 21);
                }

                // Write value to file
                wb.write(os);

            } catch (Exception e) {

                log.error(" !!!!! Excel WRITING Failed !!!!! ");

            }

        }

    }

    private static synchronized List<String> getColumnHeadings() {
        List<String> columnHeadings = new ArrayList<>();
        columnHeadings.add("Error/Warning Messages");
        columnHeadings.add("Execution Status");
        columnHeadings.add("Submission Number");
        columnHeadings.add("Policy Effective Date");
        columnHeadings.add("Policy Expiry Date");
        columnHeadings.add("Quote Number");
        columnHeadings.add("Quote Effective Date");
        columnHeadings.add("Quote Expiry Date");
        columnHeadings.add("Quote Status");
        columnHeadings.add("Total Premium");
        columnHeadings.add("Copy Submission Premium");
        columnHeadings.add("Total Coverage Premium");
        columnHeadings.add("Total Tax And Fees");
        columnHeadings.add("Policy Number");
        columnHeadings.add("New Business Rate Time");
        columnHeadings.add("New Business ISO ERC Version");
        columnHeadings.add("Endorsement Submission Number");
        columnHeadings.add("Endorsement Total Premium");
        columnHeadings.add("Endorsement Total Tax And Fees");
        columnHeadings.add("Endorsement Rate Time");
        columnHeadings.add("Renewal Submission Number");
        columnHeadings.add("Renewal Quote Number");
        columnHeadings.add("Renewal Quote Effective Date");
        columnHeadings.add("Renewal Quote Expiry Date");
        columnHeadings.add("Renewal Quote Status");
        columnHeadings.add("Renewal Total Coverage Premium");
        columnHeadings.add("Renewal Total Tax And Fees");
        columnHeadings.add("Renewal Policy Number");
        columnHeadings.add("Renewal Total Premium");
        columnHeadings.add("Renewal Rate Time");
        columnHeadings.add("Renewal ISO ERC Version");
        return columnHeadings;
    }

    public static synchronized void excelWriter(String columnName, String result, String... rowToWrite) {

        // Creating file object of existing Excel file
        File xlsxFile = new File(FrameworkConstants.getResultDirectoryPath() + File.separator + FrameworkConstants.getClonedExcelName());

        // Creating input stream
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(xlsxFile))) {

            // Creating workbook from input stream
            try (Workbook workbook = WorkbookFactory.create(inputStream)) {

                // Split For Cognitive Complexity
                excelWriterHelper(workbook, columnName, result, rowToWrite);

                // Crating output stream and writing the updated workbook
                try (FileOutputStream os = new FileOutputStream(xlsxFile)) {

                    workbook.write(os);
                    os.flush();
                    log.info("Excel file has been updated successfully.");

                }

            }

        } catch (IOException e) {
            log.info("Exception while updating an existing excel file. ---> {}", e.getMessage());
        }

    }

    private static synchronized void excelWriterHelper(Workbook workbook, String columnName, String result, String... rowToWrite) {

        // Variables For Flagging
        DataFormatter formatter = new DataFormatter();
        String sheetName = PropertyUtils.getValue(INPUT_SHEET_NAME);

        // Reading first sheet of Excel file
        Sheet sheet = workbook.getSheet(sheetName);

        // Iterating Excel to update
        for (Row row : sheet) {
            for (Cell cell : row) {

                // Get the text that appears in the cell by getting the cell value and applying any data formats (Date, 0.00, 1.23e9, $1.23, etc)
                String text = formatter.formatCellValue(cell);

                // Is it an exact match?
                if (columnName.equals(text)) {
                    Row row1 = rowToWrite.length > 0 ? sheet.getRow(Integer.parseInt(rowToWrite[0])) : sheet.getRow(Integer.parseInt(Thread.currentThread().getName()));
                    // Check if the cell has existing value
                    if (formatter.formatCellValue(row1.getCell(cell.getColumnIndex())).isEmpty()) {
                        // Enter Values
                        row1.createCell(cell.getColumnIndex()).setCellValue(result);
                        log.info("Updated {} to excel : {}.", columnName, result);
                    } else {
                        // Append Values
                        String existingValue = formatter.formatCellValue(row1.getCell(cell.getColumnIndex()));
                        row1.createCell(cell.getColumnIndex()).setCellValue(existingValue.concat("\n").concat(result));
                        log.info("Appended {} with {} to excel.", existingValue, result);
                    }
                    break;
                }

            }
        }

    }

    public static synchronized int[] getIndex(String searchText) {

        Workbook workbook;
        try (FileInputStream fis = new FileInputStream(FrameworkConstants.getResultDirectoryPath() + File.separator + FrameworkConstants.getClonedExcelName())) {
            workbook = new XSSFWorkbook(fis);

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheet(PropertyUtils.getValue(INPUT_SHEET_NAME));

                for (Row row : sheet) {
                    for (Cell cell : row) {

                        DataFormatter dft = new DataFormatter();
                        dft.setUseCachedValuesForFormulaCells(true);
                        String cellText = dft.formatCellValue(cell);
                        if (cellText.equals(searchText)) {
                            int rowIndex = row.getRowNum(); // Add 1 to convert to human-readable index
                            int columnIndex = cell.getColumnIndex(); // Add 1 to convert to human-readable index
                            return new int[]{rowIndex, columnIndex};
                        }

                    }
                }
            }

        } catch (IOException e) {
            log.error("Failed Find Operation");
        }

        return new int[0]; // Text not found in the Excel file
    }

    public static synchronized String getValueFromExcel(String columnName) {

        // Creating file object of existing Excel file
        File xlsxFile = new File(FrameworkConstants.getResultDirectoryPath() + File.separator + FrameworkConstants.getClonedExcelName());
        DataFormatter formatter = new DataFormatter();

        // Variables For Flagging
        String sheetName = PropertyUtils.getValue(INPUT_SHEET_NAME);

        // Creating input stream
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(xlsxFile))) {

            // Creating workbook from input stream
            try (Workbook workbook = WorkbookFactory.create(inputStream)) {

                // Reading first sheet of Excel file
                Sheet sheet = workbook.getSheet(sheetName);

                // Find the corresponding value in the same column in the current row
                Row valueRow = sheet.getRow(getIndex(Thread.currentThread().getName())[0]);
                return formatter.formatCellValue(valueRow.getCell(getIndex(columnName)[1]));

            }

        } catch (IOException e) {
            log.info("Exception while reading from the Excel file. ---> {}", e.getMessage());
        }

        // Return an empty string if the column is not found or there is an error
        return "FILE/VALUE_NOT_FOUND";
    }

    public static synchronized ConcurrentMap<String, Map<String, String>> excelReader(String sheetName, String iteration, String... optionalParamsTypeStateName) {
        Sheet sheet = null;
        int rowCount = 0;
        int columnCount = 0;
        ConcurrentMap<String, Map<String, String>> mainMap = new ConcurrentSkipListMap<>();

        // Open Excel Sheet
        try (InputStream inp = new FileInputStream(FrameworkConstants.getResultDirectoryPath() + File.separator + FrameworkConstants.getClonedExcelName());
             Workbook wb = WorkbookFactory.create(inp)) {

            sheet = wb.getSheet(sheetName);
            rowCount = sheet.getLastRowNum();
            columnCount = sheet.getRow(0).getLastCellNum();

        } catch (IOException e) {
            log.error(" !!!!!! Excel LOADING Failed !!!!!! {}.", e.getMessage());
        }

        // Read From Excel & Save In The resultMap
        DataFormatter dft = new DataFormatter();
        dft.setUseCachedValuesForFormulaCells(true);

        for (int i = 0; i < rowCount; i++) {
            Map<String, String> resultMap = new ConcurrentSkipListMap<>();
            for (int j = 0; j < columnCount; j++) {
                String key = dft.formatCellValue(sheet.getRow(0).getCell(j));
                String value = dft.formatCellValue(sheet.getRow(i + 1).getCell(j));
                resultMap.put(key, value);
                mainMap.put(resultMap.get(ITERATION), resultMap);
            }

        }

        // Update Result & Return based on the optional parameters
        if (optionalParamsTypeStateName.length == 1 && optionalParamsTypeStateName[0] != null) {
            String type = optionalParamsTypeStateName[0];
            mainMap.entrySet().removeIf(entry -> !entry.getKey().startsWith(iteration) || !(entry.getKey().length() == iteration.length() || entry.getKey().contains("_")) || !entry.getValue().get("type").equalsIgnoreCase(type));
        } else if (optionalParamsTypeStateName.length == 2 && optionalParamsTypeStateName[1] != null) {
            String type = optionalParamsTypeStateName[0];
            String stateName = optionalParamsTypeStateName[1];
            mainMap.entrySet().removeIf(entry -> !entry.getKey().startsWith(iteration) || !(entry.getKey().length() == iteration.length() || entry.getKey().contains("_")) || !entry.getValue().get("state").equalsIgnoreCase(stateName) || !entry.getValue().get("type").equalsIgnoreCase(type));
        } else {
            mainMap.entrySet().removeIf(entry -> !entry.getKey().startsWith(iteration) || !(entry.getKey().length() == iteration.length() || entry.getKey().contains("_")));
        }

        return mainMap;
    }

    private static synchronized void setColumnsHidden(Sheet sheet, int from, int to) {
        for (int i = from; i <= to; i++) {
            sheet.setColumnHidden(i, true);
        }
    }

}
