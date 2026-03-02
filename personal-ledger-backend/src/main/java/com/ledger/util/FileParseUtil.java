package com.ledger.util;

import com.ledger.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件解析工具类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Slf4j
public class FileParseUtil {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FORMATTER_COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    /**
     * 解析Excel文件
     */
    public static List<Map<String, Object>> parseExcel(MultipartFile file) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // 读取表头
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException("Excel文件表头为空");
            }
            
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValue(cell));
            }
            
            // 读取数据行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                Map<String, Object> rowData = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    rowData.put(headers.get(j), getCellValue(cell));
                }
                
                dataList.add(rowData);
            }
            
        } catch (IOException e) {
            log.error("解析Excel文件失败", e);
            throw new BusinessException("解析Excel文件失败: " + e.getMessage());
        }
        
        return dataList;
    }
    
    /**
     * 解析CSV文件
     */
    public static List<Map<String, Object>> parseCsv(MultipartFile file) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : csvParser) {
                Map<String, Object> rowData = new HashMap<>();
                for (String header : csvParser.getHeaderNames()) {
                    rowData.put(header, record.get(header));
                }
                dataList.add(rowData);
            }
            
        } catch (IOException e) {
            log.error("解析CSV文件失败", e);
            throw new BusinessException("解析CSV文件失败: " + e.getMessage());
        }
        
        return dataList;
    }
    
    /**
     * 获取单元格值
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DATE_TIME_FORMATTER.format(cell.getLocalDateTimeCellValue());
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    /**
     * 转换为BigDecimal
     */
    public static BigDecimal toBigDecimal(Object value) {
        if (value == null || "".equals(value.toString().trim())) {
            return null;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            throw new BusinessException("金额格式错误: " + value);
        }
    }
    
    /**
     * 转换为LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Object value) {
        if (value == null || "".equals(value.toString().trim())) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.toString(), DATE_TIME_FORMATTER);
        } catch (Exception e) {
            throw new BusinessException("时间格式错误: " + value + "，正确格式为: yyyy-MM-dd HH:mm:ss");
        }
    }
    
    /**
     * 转换为LocalDate
     */
    public static LocalDate toLocalDate(Object value) {
        if (value == null || "".equals(value.toString().trim())) {
            return null;
        }
        String dateStr = value.toString().trim();
        try {
            // 先尝试 yyyyMMdd 格式
            if (dateStr.length() == 8 && dateStr.matches("\\d{8}")) {
                return LocalDate.parse(dateStr, DATE_FORMATTER_COMPACT);
            }
            // 再尝试 yyyy-MM-dd 格式
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            throw new BusinessException("日期格式错误: " + value + "，支持格式: yyyy-MM-dd 或 yyyyMMdd");
        }
    }
    
    /**
     * 转换为LocalTime
     */
    public static LocalTime toLocalTime(Object value) {
        if (value == null || "".equals(value.toString().trim())) {
            return null;
        }
        try {
            return LocalTime.parse(value.toString(), TIME_FORMATTER);
        } catch (Exception e) {
            throw new BusinessException("时间格式错误: " + value + "，正确格式为: HH:mm:ss");
        }
    }
}
