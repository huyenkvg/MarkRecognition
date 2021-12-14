
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author HUYENKUTE
 */
public class BuoiThi {
    public static ArrayList<BuoiThi> danhsachBuoiThi = DBConection.layDanhSachBuoiThi();
    int mabuoithi;
    String phong;
    String ngay;
    String tenfileExcel;
    int tiet;

    public BuoiThi(int mabuoithi, String phong, String ngay, int tiet) {
        
        
        this.mabuoithi = mabuoithi;
        this.phong = phong.trim();
        this.ngay = ngay.trim();
        this.tiet = tiet;
        tenfileExcel =tiet +"-"+ ngay +"-"+ phong+".xlsx";
    }
    
    public BuoiThi() {
    }

    public boolean checkBuoiThi() // if true :khong bi trung buoi thi
    {
        System.out.println("---BuoiThi===" + this.phong+"-"+this.ngay+"-"+this.tiet);
        danhsachBuoiThi = DBConection.layDanhSachBuoiThi();
        for(BuoiThi x: danhsachBuoiThi)
        {
            
            System.out.println("--->......===" + x.phong+"-"+x.ngay+"-"+x.tiet);
            if(x.phong.equals(this.phong) && x.ngay.equals(this.ngay) && x.tiet == this.tiet)
            {
                this.mabuoithi = x.mabuoithi;
                return false;
            }
        }
        return true;
    }
   
    public int getMabuoithi() {
        return mabuoithi;
    }

    public void setMabuoithi(int mabuoithi) {
        this.mabuoithi = mabuoithi;
    }

    public String getPhong() {
        return phong;
    }

    public void setPhong(String phong) {
        this.phong = phong;
    }

    public String getNgay() {
        return ngay;
    }

    public void setNgay(String ngay) {
        this.ngay = ngay;
    }

    public int getTiet() {
        return tiet;
    }

    public void setTiet(int tiet) {
        this.tiet = tiet;
    }
    
    
}
