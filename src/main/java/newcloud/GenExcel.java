package newcloud;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Utility class for exporting Q-value tables to Excel (.xls) format.
 * <p>
 * Uses Apache POI to write Q-table data for analysis and debugging.
 * </p>
 */
public class GenExcel {

    private HSSFSheet sheet;
    private HSSFWorkbook hwb;
    private HSSFRow row;

    private static final String NEW_LIST_FILE = "Q.csv";
    private static GenExcel instance = new GenExcel();

    /** Returns the singleton instance. */
    public static GenExcel getInstance() {
        if (instance == null) {
            instance = new GenExcel();
        }
        return instance;
    }

    /** Initialize the workbook and sheet. */
    public void init() {
        hwb = new HSSFWorkbook();
        sheet = hwb.createSheet();
    }

    /**
     * Write a single Q-value update to the Excel sheet.
     * Synchronized with Q-table updates.
     *
     * @param QList      the Q-value table
     * @param state_idx  the state key
     * @param action_idx the action (host) index
     * @param QValue     the updated Q-value
     */
    public void fillData(Map<String, Map<Integer, Double>> QList,
                         String state_idx, int action_idx, double QValue) {
        int rows = -1;
        for (Map.Entry<String, Map<Integer, Double>> me : QList.entrySet()) {
            rows++;
            if (state_idx.equals(me.getKey())) {
                break;
            }
        }
        row = sheet.createRow(rows);
        row.createCell(0).setCellValue(state_idx);
        row.createCell(action_idx + 1).setCellValue(QValue);
    }

    /** Write the workbook to file. */
    public void genExcel() {
        try {
            hwb.write(new FileOutputStream(new File(NEW_LIST_FILE), true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
