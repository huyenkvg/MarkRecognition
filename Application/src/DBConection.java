
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class DBConection {

    public static int mode; // 1 is using mssv // 2 use so phach

    public static Connection layKetNoi() {
        Connection ketNoi = null;
        String uRL = "jdbc:sqlserver://;databaseName=QLNhapDiem";
        String userName = "sa";
        String password = "123";
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            ketNoi = DriverManager.getConnection(uRL, userName, password);
            System.out.println("Ket noi thanh cong!");
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("Thoi toang roi ong giao oi!");
        }
        return ketNoi;
    }

    public static String[] chuanHoa(String s) {
        String st = s;
        st = st.trim();
        st = st.substring(st.lastIndexOf("\\") + 1);
        st = st.replaceAll(".xlsx", "");
        st = st.replaceAll("\\s+", " ");
        st = st.replaceAll("-", " ");
        String[] temp = st.split(" ");
        temp[1] = temp[1].substring(4) + "-" + temp[1].substring(2, 4) + "-" + temp[1].substring(0, 2);
        System.out.println(temp[1]);

        return temp;
    }

    public static boolean ThemDanhSachThiVaoDB(ArrayList<SinhVien> listSinhViens, BuoiThi buoithi) {

        Connection conn = DBConection.layKetNoi();
        Statement st;
        try {
            System.out.println("insert into BUOITHI (PHONG, NGAY, TIET) values ('" + buoithi.phong + "', '" + buoithi.ngay + "', " + buoithi.tiet + ");");
            st = conn.createStatement();
            int row = (st.executeUpdate("insert into BUOITHI (PHONG, NGAY, TIET) values ('" + buoithi.phong + "', '" + buoithi.ngay + "', " + buoithi.tiet + ");", Statement.RETURN_GENERATED_KEYS));

            ResultSet generatedKeys = st.getGeneratedKeys();
            if (generatedKeys.next()) {
                row = generatedKeys.getInt(1);
                buoithi.setMabuoithi(row);
            }
            //====================== insert sinh vien =====================
            int index = 0;
            for (SinhVien sv : listSinhViens) {

                st.execute("insert into DANHSACHTHI (MASOBUOITHI, STTSV, SOPHACH_MSSV) values ('" + buoithi.mabuoithi + "', '" + (++index) + "', '" + sv.getMasv() + "');");
            }
            st.close();
            conn.close();
            return true;
        } catch (SQLException ex) {
            System.out.println("!!! --- Khong Update duoc: ADD DSSV Vao SQL");
            ex.printStackTrace();
        }
        return false;
    }

    public static ArrayList<BuoiThi> layDanhSachBuoiThi() {
        Connection conn = DBConection.layKetNoi();
        ResultSet rs;
        Statement st;
        ArrayList<BuoiThi> ds = new ArrayList<>();
        BuoiThi p;
        try {
            st = conn.createStatement();
            rs = st.executeQuery("select * from BuoiThi");
            while (rs.next()) {

                ds.add(new BuoiThi(rs.getInt("MASOBUOITHI"), rs.getString("PHONG"), rs.getString("NGAY"), rs.getInt("TIET")));
            }
            System.out.println("LayDSBuoiTHi : load ds thanh cong!");
            
            st.close();
            conn.close();
        } catch (SQLException ex) {
            System.out.println("LOI! load danh sach Buoi Thi khong duoc :((((");
            
        }
        
        return ds;
    }

    public static boolean updatePhieuDiem(int masobuoithi, int matrang, ArrayList<Double> diemthi) {
        System.out.println("Matrang:" +matrang);
        Connection conn = layKetNoi();
        ResultSet rs;
        Statement st;
        int minn = matrang*10;
        int maxx = minn+ 10;
        double soDiem = 0;
        try {
            st = conn.createStatement();
            for (int i = minn; i < maxx; i++) {
                soDiem = diemthi.get(i-minn);
                if(soDiem < 0)
                    soDiem = 0;
                st.executeUpdate("UPDATE DANHSACHTHI\n"
                        + "SET DIEM = '" + soDiem + "'\n"
                        + "WHERE MASOBUOITHI = " + masobuoithi
                        + " AND STTSV = " +(i) + ";");
                
                System.out.println("--- Diem Thi Cua Sinh vien " + (i) + " la: "+diemthi.get(i-minn) + masobuoithi);

            } 
            JOptionPane.showMessageDialog(null, "Đã lưu điểm thi vào CSDL", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
          
            System.out.println("LayDSSINHVIEN Cua Phieu Thi Nay : load ds thanh cong!");
            
            st.close();
            conn.close();
            return true;
        } catch (SQLException ex) {
            System.out.println("LOI! update phieu diem khong duoc :((((");
             JOptionPane.showMessageDialog(null, "Không Lưu được !!!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
          
            ex.printStackTrace();
        }

        return false;
    }

    public static ArrayList<SinhVien> layDanhSachSVCuaPhieuDiem(int masobuoithi, int matrang) {
        int maxx = matrang * 10 + 10;
        int minn = matrang * 10;
        Connection conn = DBConection.layKetNoi();
        ResultSet rs;
        Statement st;
        ArrayList<SinhVien> ds = new ArrayList<>();
        SinhVien p;
        try {
            st = conn.createStatement();
            rs = st.executeQuery("select * from DANHSACHTHI where (masobuoithi = " + masobuoithi + " and STTSV >= " + minn + " and STTSV < " + maxx + ");");
            while (rs.next()) {

                p = new SinhVien(rs.getInt("STTSV"), (rs.getString("SOPHACH_MSSV")), "Không có điểm!");

                ds.add(p);
            }
            System.out.println("LayDSSINHVIEN Cua Phieu Thi Nay : load ds thanh cong!");
        } catch (SQLException ex) {
            System.out.println("LOI! load danh sach SINH VIEN CUA PHIEU THI khong duoc :((((");
            ex.printStackTrace();
        }
        return ds;
    }
    public static ArrayList<Float> layDiem(int masobuoithi) {
        
        Connection conn = DBConection.layKetNoi();
        ResultSet rs;
        Statement st;
        ArrayList<Float> ds = new ArrayList<>();
        float p;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(" select DIEM from DANHSACHTHI where MASOBUOITHI = "+masobuoithi);
            while (rs.next()) {

                p = rs.getFloat("DIEM");

                ds.add(p);
            }
            System.out.println("LayDiem  : load ds thanh cong!");
        } catch (SQLException ex) {
            System.out.println("LOI! load danh sach DIEM khong duoc :((((");
            ex.printStackTrace();
        }
        return ds;
    }

    public static void main(String[] args) {
        ArrayList<SinhVien> dssvCoDiem = DBConection.layDanhSachSVCuaPhieuDiem(0, 18);
        int id = 0;
        for (SinhVien sv : dssvCoDiem) {

            System.out.println("---> " + sv.getMasv() + "  " + sv.stt + "  " + sv.getHoten());

        }
    }
}
