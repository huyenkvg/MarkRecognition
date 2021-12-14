
import org.opencv.core.Point;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author HUYENKUTE
 */
public class SinhVien {
    int stt;
    private String masv;
    private String hoten;
    private String lop;
    private float diem;
    public static Point x;
    public static Point y;

    public SinhVien(int stt,String mssv, String hoten) {
        this.stt = stt;
        this.masv = mssv;
        this.hoten = hoten;
    }
    public SinhVien(int stt, String mssv, float diem) {
        this.stt = stt;
        this.masv = mssv;
        this.diem = diem;
    }

    public SinhVien() {
        this.masv = "";
        this.hoten = "";
        this.diem = 0;
    }

    public String getLop() {
        return lop;
    }

    public void setLop(String lop) {
        this.lop = lop;
    }

    public String getMasv() {
        return masv;
    }

    public void setMasv(String masv) {
        this.masv = masv;
    }

    public String getHoten() {
        return hoten;
    }

    public void setHoten(String hoten) {
        this.hoten = hoten;
    }


    public float getDiem() {
        return diem;
    }

    public void setDiem(float diem) {
        this.diem = diem;
    }

    public static Point getX() {
        return x;
    }

    public static void setX(Point x) {
        SinhVien.x = x;
    }

    public static Point getY() {
        return y;
    }

    public static void setY(Point y) {
        SinhVien.y = y;
    }
   



}
