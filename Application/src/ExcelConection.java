
import com.gembox.spreadsheet.ExcelFile;
import com.gembox.spreadsheet.ExcelRow;
import com.gembox.spreadsheet.ExcelWorksheet;
import com.gembox.spreadsheet.RowColumn;
import com.gembox.spreadsheet.SpreadsheetInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelConection {

    static ArrayList<SinhVien> listStudent;//     
    static String fileName;
    
    public static String tenKyThi = "Cuối Kì I";
    public static String ngayThi = "17/09/2021";
    public static String tenHocPhan = "Tin Học Cơ Sở 2";
    static char[] kituTrang = {'z', '{', 'y', 'x'}; // => 0 , 1, 2, 3
    public static boolean cellType = false; // true: MSSV, false: SOPHACH

    public static ArrayList<SinhVien> layDSSinhVien(String filename) {
        fileName = filename;
        listStudent = new ArrayList<>();
        try {
            FileInputStream excelFile = new FileInputStream(new File(filename));
            excelFile.toString();
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();
            Iterator<Row> iterator = datatypeSheet.iterator();
//      Row firstRow = iterator.next();
//      Cell firstCell = firstRow.getCell(0);
//      System.out.println(firstCell.getStringCellValue());
            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                SinhVien sv = new SinhVien();
//                System.out.println("CELLSTYLE: "+currentRow.getCell(0).getCellType());
                if (currentRow.getCell(0).getStringCellValue().charAt(0) == 'N') // MSSV + HOTEN
                {
                    cellType = true;
                    sv.setMasv(((currentRow.getCell(0).getStringCellValue())));
                    sv.setHoten(currentRow.getCell(1).getStringCellValue());
                    sv.setLop(currentRow.getCell(2).getStringCellValue());
                    System.out.println(" Lay Tu Excel: MSSV " + sv.getMasv());
                } else //if(currentRow.getCell(0).getCellType().toString().equals("NUMERIC")){ // SOPHACH
                {
                    cellType = false;
                    sv.setMasv((("" + currentRow.getCell(0).getStringCellValue())));
                    System.out.println(" Lay DSSV Tu Excel SO PHACH: " + sv.getMasv());
                }
//        sv.setSophach(currentRow.getCell(2).getStringCellValue());

                listStudent.add(sv);
            }

            workbook.close();
        } catch (FileNotFoundException | NoClassDefFoundError e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listStudent;
    }

    static String chuyenHe10sanghe4(int he10, int dodaiform) {
        int he4 = 0;
        int count = 0;
        while (he10 > 0) {
            he4 = (int) ((he10 % 4) * Math.pow(10, count++)) + he4;
            he10 /= 4;
        }
        String kq = "" + he4;
        while (kq.length() < dodaiform) {
            kq = "0" + kq;
        }
//        System.out.println("hiihi: "+ he4);
        return kq;
    }

    public static boolean GhiDSThiRaTemplateNgang(ArrayList<SinhVien> dssv, int vitri, int masobuoithi, int masotrang) throws java.io.IOException {
        // If using Professional version, put your serial key below.
        SpreadsheetInfo.setLicense("FREE-LIMITED-KEY");
        ExcelFile workbook;
        try{
        if (cellType) {
            workbook = ExcelFile.load("D:\\NKKH\\DOCDIEM\\SoreReadApplication\\Danh Sach Thi Se In Ra\\TemplatePhieuTheoMSSV.xlsx");

        } else {
            workbook = ExcelFile.load("D:\\NKKH\\DOCDIEM\\SoreReadApplication\\Danh Sach Thi Se In Ra\\TemplatePhieuTheoSoPhach.xlsx");
        }
            
        }
        catch(FileNotFoundException e){
//            e.printStackTrace();
            JOptionPane.showMessageDialog(null,"Không Tìm Thấy file xlsx Template Kiểm Tra Lại folder D:\\NKKH\\DOCDIEM\\SoreReadApplication\\Danh Sach Thi Se In Ra", "Thông báo!", INFORMATION_MESSAGE);
            return false;
        }
        int workingDays = 8;

        LocalDateTime startDate = LocalDateTime.now().plusDays(-workingDays);
        LocalDateTime endDate = LocalDateTime.now();

        ExcelWorksheet worksheet = workbook.getWorksheet(0);

        // Find cells with placeholder text and set their values.
        RowColumn rowColumnPosition;
        String oMaTrang = "";
        String res = chuyenHe10sanghe4(masobuoithi, 7) + chuyenHe10sanghe4(masotrang+vitri/10-1, 3); //  số trang chhuws ko phải Mã Trang
        for (int i = 0; i < res.length(); i++) {
            if (res.charAt(i) == '0') {
                oMaTrang += kituTrang[0];
            } else if (res.charAt(i) == '1') {

                oMaTrang += kituTrang[1];
            } else if (res.charAt(i) == '2') {

                oMaTrang += kituTrang[2];
            } else if (res.charAt(i) == '3') {

                oMaTrang += kituTrang[3];
            }
            if (i != res.length() - 1) {
                oMaTrang += " ";
            }
        }

        if ((rowColumnPosition = worksheet.getCells().findText("~PHONGTHI", true, true)) != null) {
            worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue("Phòng Thi: " + Main.buoithiDangXet.phong);
        }
        if ((rowColumnPosition = worksheet.getCells().findText("~KITHI", true, true)) != null) {
            worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue("Kỳ Thi: " + tenKyThi);
        }
        if ((rowColumnPosition = worksheet.getCells().findText("~NGAYTHI", true, true)) != null) {
            worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue("Ngày Thi: " + Main.buoithiDangXet.ngay);
        }
        if ((rowColumnPosition = worksheet.getCells().findText("~HOCPHAN", true, true)) != null) {
            worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue("Học Phần: " + tenHocPhan);
        }
        if ((rowColumnPosition = worksheet.getCells().findText("~OMATRANG", true, true)) != null) {
            worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue(oMaTrang);
        }
        if ((rowColumnPosition = worksheet.getCells().findText("~MATRANGBANGSO", true, true)) != null) {
            worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue(masobuoithi+" "+(masotrang +vitri/10));
        }
        //NẾU ĐÂY LÀ DANH SÁCH THEO TÊN + MSSV
        if(cellType)
        while ((rowColumnPosition = worksheet.getCells().findText("~TENCUASINHVIEN", true, true)) != null) {
            if (vitri < listStudent.size()) {
                worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue(dssv.get(vitri).getHoten());
                if ((rowColumnPosition = worksheet.getCells().findText("~MSSV", true, true)) != null) {
                    worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue(dssv.get(vitri).getMasv());

                    if ((rowColumnPosition = worksheet.getCells().findText("~LOPSV", true, true)) != null) {
                        worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue(dssv.get(vitri).getLop());
                    }
                }
                if ((rowColumnPosition = worksheet.getCells().findText("~STT", true, true)) != null) {
                    worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue(vitri);

                }

            } else {
                worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue(" ");
                if ((rowColumnPosition = worksheet.getCells().findText("~MSSV", true, true)) != null) {
                    worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue(" ");

                    if ((rowColumnPosition = worksheet.getCells().findText("~LOPSV", true, true)) != null) {
                        worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue(" ");

                    }
                }
                if ((rowColumnPosition = worksheet.getCells().findText("~STT", true, true)) != null) {
                    worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue(vitri);

                }
            }
            vitri++;
        }
        //NẾU ĐÂY LÀ DANH SÁCH THEO SỐ PHÁCH
        if(!cellType)
        while ((rowColumnPosition = worksheet.getCells().findText("~SOPHACH", true, true)) != null) {
            if (vitri < listStudent.size()) {
                worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue(dssv.get(vitri).getMasv());
                if ((rowColumnPosition = worksheet.getCells().findText("~STT", true, true)) != null) {
                    worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue(vitri);

                }

            } else {
                worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue(" ");
                if ((rowColumnPosition = worksheet.getCells().findText("~STT", true, true)) != null) {
                    worksheet.getCell(rowColumnPosition.getRow(), rowColumnPosition.getColumn()).setValue(" ");

                }
            }
            vitri++;
        }

        worksheet.calculate();
        String[] temp = DBConection.chuanHoa(Main.fileExcelName);
        workbook.save("D:\\NKKH\\DOCDIEM\\SoreReadApplication\\Danh Sach Thi Se In Ra\\DSThi "+ temp[2]+"-"+temp[1]+"-"+temp[0] + " trang-" + (masotrang + (vitri / 10-1)) + ".xlsx");
        return true;
    }

    public static void main(String[] args) throws IOException {
        GhiDSThiRaTemplateNgang(layDSSinhVien(FileChooser.GetFileLink()), 0, 18, 0);
//        chuyenHe10sanghe4(101);
    }
}
