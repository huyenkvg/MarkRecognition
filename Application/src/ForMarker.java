

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.util.Pair;
import javax.swing.JOptionPane;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.isContourConvex;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author HUYENKUTE
 */
public class ForMarker {

    
    static double MIN_MARKER_AREA = 1200;
    static double MAX_MARKER_AREA = 6000;
    public static final int minNZcount = 2000;
    public static final double tiLeTimOMaTrang= 110/582;
    public static Mat anhNhiPhanCanDung;

    public static ArrayList<MatOfPoint> findTheFilledRect(ArrayList<MatOfPoint> contours) {

        ArrayList<MatOfPoint> vec = new ArrayList<>();
        Rect rect, rectCrop;
        for (int x = 0; x < contours.size(); x++) {
            rect = boundingRect(contours.get(x));
            double area = contourArea(contours.get(x));
            rectCrop = boundingRect(contours.get(x));
            Mat imageROI = anhNhiPhanCanDung.submat(rectCrop);
            int total = Core.countNonZero(imageROI);
        //    System.out.println("Count non zẻo: " + total);
            if (total < minNZcount) {
                vec.add(contours.get(x));
            }
        }

        return vec;
    }

    //==========================[ TÌM - 4 - MARKER ]==============================
    public static MatOfPoint2f findTheMarker(ArrayList<MatOfPoint2f> listSquare) {

        if (listSquare.size() < 4) {
            System.out.println("!- Can not find the Markers - khong Đủ hình vuông - line 137!!!");
//            JOptionPane.showMessageDialog(null, "Không nhận dạng đủ marker ", "Thông báo", JOptionPane.YES_NO_OPTION);
            return null;
        }

        Point[] fourMarkers = new Point[4];
        listSquare.sort(new Comparator<MatOfPoint2f>() {

            public int compare(MatOfPoint2f p1, MatOfPoint2f p2) {
                Rect r1 = boundingRect(p1), r2 = boundingRect(p2);

                if (r1.width < r2.width) {
                    return -1;
                } else if (r1.width > r2.width) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        Rect r = new Rect();
        boolean notfound = false;
        int dem = 0;
        for (int i = 0; i < listSquare.size(); i++) {
            r = boundingRect(listSquare.get(i));
        //    System.out.println("---->>>>> marker thu " + i + ": " + r.x + " " + r.y + "Wid:HEI:(" + r.width + ":" + r.height + ")  -- wid/hei: " + (abs(r.width * 1.0 / r.height)) + "--  Dientich: " + r.area());

            if (abs(r.width * 1.0 / r.height - 1) <= 0.5 && r.x >= 10 && r.y >= 10) {

                fourMarkers[dem] = new Point(r.x + r.width / 2, r.y + r.height / 2);
                dem++;
                if (dem == 4) {
                    break;
                }
            }
        }
        if (dem != 4) {
            System.out.println("Can not find the Markers - line 172!!!");
//            JOptionPane.showMessageDialog(null, "Không nhận dạng được marker ", "Thông báo", JOptionPane.YES_NO_OPTION);
            return null;
        }

        return new MatOfPoint2f(fourMarkers);
    }
    //==========================[ TÌM TẤT CẢ HÌNH VUÔNG ]==============================

    public static ArrayList<MatOfPoint2f> findSquares(Mat Img) {
        Mat gray = Img;
        ArrayList<MatOfPoint2f> squares = new ArrayList<>();
        // blur will enhance edge detection

        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchey = new Mat();
        MatOfPoint SrcMtx = null; // de ep kieu
        MatOfPoint2f approxCurve;
        Point[] approx;
        // Find contours and store them in a list
        // findContours​(gray, contours, RETR_TREE, CHAIN_APPROX_SIMPLE);
        Imgproc.findContours(gray, contours, hierarchey, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        // Test contours
//        System.out.println("DAY LA FIND SQUARES: line 194 --------------------------------------------------------------");

//        contours = ForMarker.findTheFilledRect(contours);
        for (MatOfPoint contour : contours) {
            // approximate contour with accuracy proportional
            // to the contour perimeter
            //  (MatOfPoint2f curve, MatOfPoint2f approxCurve, double epsilon, boolean closed)
            //MatOfPoint2f contourFloat = toMatOfPointFloat(contour);
            MatOfPoint2f contourFloat = new MatOfPoint2f(contour.toArray());
            double arcLen = Imgproc.arcLength(contourFloat, true) * 0.02;

            // Approximate polygonal curves.
            approxCurve = new MatOfPoint2f();
            Imgproc.approxPolyDP(contourFloat, approxCurve, arcLen, true);
            Rect rectRound = boundingRect(approxCurve);
            // Note: absolute value of an area is used because
            // area may be positive or negative - in accordance with the
            // contour orientation
            approx = approxCurve.toArray();

//                        squares.add(approxCurve);
            if ((abs(rectRound.width * 1.0 / rectRound.height - 1.0) < 0.3) && (abs(contourArea(approxCurve)) <= MAX_MARKER_AREA) && (abs(contourArea(approxCurve)) >= MIN_MARKER_AREA)) {
                System.out.println("Aproxx: (wid: hei) : " + "(" + rectRound.width + ":" + rectRound.height + ") | Area: " + contourArea(approxCurve) + " | length: " + approx.length);
                if ((approx.length == 4) && (isContourConvex(new MatOfPoint(approx)))) {
                    double maxCosine = 0;
                    System.out.print(approx.length + ":" + approxCurve.size());
                    System.out.print("  --  " + (abs(contourArea(approxCurve))));
                    System.out.println("  --  " + isContourConvex(new MatOfPoint(approx)));

                    for (int j = 2; j < 5; j++) {
                        double cosine = abs(Processing.angle(approx[j % 4], approx[j - 2], approx[j - 1]));
                        maxCosine = max(maxCosine, cosine);
                    }

//                System.out.println("maxCosine =" + maxCosine);
                    if (maxCosine <= 0.7) {
                        squares.add(approxCurve);
//                    System.out.println("OKE :)");
                        continue;
                    }
                }
//            else if((approx.length > 4 ) && (approx.length <= 20) && isContourConvex(new MatOfPoint(approx))){
//                // Xét 1 vòng chuỗi các điểm trong approx: lần lượt 3 điểm cạnh nhau
//                int demgocVuong = 0; //  đếmlấy số goc gần vuông: có cos bằng xấp xỉ 0,  ở đây em xét đến góc 60 độ là min nên COSINE có giá trị min cũng phải 0.5, đổi ý nên xét 0.6
//                for (int j = 0; j < approx.length-1; j++) {
//                    double cosine = angle(approx[(j+2)%approx.length], approx[j], approx[(j+1)%approx.length]);
//                    if(cosine > 0 && cosine <= 0.75 || cosine <=0 && cosine > -0.5)
//                        demgocVuong++;
//                }
//System.out.println(" Approx Length: " + approx.length);
//System.out.println(" DEM goc Vuong duoc bao nhieu: " + demgocVuong);
//                if (demgocVuong == 4) {
//                    squares.add(approxCurve);
//                    continue;
//                }
//            }
            }
//            System.out.println("NOOO");

        }
        //========================================= LOẠI BỎ NHŨNG HÌNH VUÔNG TRÙNG TÂM ====================================
        MatOfPoint2f q, Q;
        for (int i = 0; i < squares.size(); i++) {
            Q = new MatOfPoint2f(squares.get(i));
            for (int j = 0; j < squares.size(); j++) {

                q = new MatOfPoint2f(squares.get(j));
                Rect r1 = boundingRect(Q);
                Rect r2 = boundingRect(q);
                if (abs(r1.width - r2.width) < 12 && abs(sqrt((r1.x - r2.x) * (r1.x - r2.x) + (r1.y - r2.y) * (r1.y - r2.y))) < 15) {
                    if (r1.width > r2.width) {
                        squares.remove(i);
                    } else {
                        squares.remove(j);
                    }
                    break;
                }
            }
        }
     //   System.out.println("size òf líst square: " + squares.size());
        return squares;
    }
    
    // ====================== [Tìm Thứ tự XOAY 4 Marker - Hàm trợ giúp]===============================
    public static boolean CheckPTDT(Point A, Point B, Point P, Point Q) {
        double f1, f2;
        double x = P.x, y = P.y;
        f1 = (A.y - B.y) * (x - A.x) + (B.x - A.x) * (y - A.y);
        x = Q.x;
        y = Q.y;

        f2 = (A.y - B.y) * (x - A.x) + (B.x - A.x) * (y - A.y);
        if (f1 * f2 < 0) {
            return true;
        } else {
            return false;
        }

    }
    // ====================== [Tìm Thứ tự XOAY 4 Marker ]===============================

    public static MatOfPoint2f findTheRotatedsQuare(MatOfPoint2f shape) {
        // System.out.println("shape size"+ shape.size());
        List<Point> arr = new ArrayList<>(shape.toList());
        if (arr.size() < 4) {
            JOptionPane.showMessageDialog(null, "Không có đủ 4 marker ");
            return null;
        }
        Point tmp[] = new Point[4];
        Point forpage[] = new Point[4];
    //    System.out.println("Shape" + shape);
        MatOfPoint2f chosed = new MatOfPoint2f();
        int[] Idx = new int[]{0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3};
        int id = 0;
        double mindistance = 1000000000;
        for (int i = 0; i < 4; i++) {
            double distance = sqrt(arr.get(i).x * arr.get(i).x + arr.get(i).y * arr.get(i).y);
            if (distance < mindistance) {
                mindistance = distance;
                id = i;
            }
        }

        tmp[0] = new Point(arr.get(id).x, arr.get(id).y);

        if (CheckPTDT(tmp[0], arr.get(Idx[id + 1]), arr.get(Idx[id + 2]), arr.get(Idx[id + 3]))) // 2 doawn thang [A,B] va [P,Q]
        {
            tmp[2] = new Point(arr.get(Idx[id + 1]).x, arr.get(Idx[id + 1]).y);
            tmp[1] = new Point(arr.get(Idx[id + 2]).x, arr.get(Idx[id + 2]).y);
            tmp[3] = new Point(arr.get(Idx[id + 3]).x, arr.get(Idx[id + 3]).y);
        } else if (CheckPTDT(tmp[0], arr.get(Idx[id + 2]), arr.get(Idx[id + 3]), arr.get(Idx[id + 1]))) {

            tmp[2] = new Point(arr.get(Idx[id + 2]).x, arr.get(Idx[id + 2]).y);
            tmp[1] = new Point(arr.get(Idx[id + 3]).x, arr.get(Idx[id + 3]).y);
            tmp[3] = new Point(arr.get(Idx[id + 1]).x, arr.get(Idx[id + 1]).y);
        } else if (CheckPTDT(tmp[0], arr.get(Idx[id + 3]), arr.get(Idx[id + 1]), arr.get(Idx[id + 2]))) {

            tmp[2] = new Point(arr.get(Idx[id + 3]).x, arr.get(Idx[id + 3]).y);
            tmp[1] = new Point(arr.get(Idx[id + 1]).x, arr.get(Idx[id + 1]).y);
            tmp[3] = new Point(arr.get(Idx[id + 2]).x, arr.get(Idx[id + 2]).y);
        } else {
            System.out.println("Tat Ca Deu Khong khop");
        }
        try{
            if (tmp[1].y > tmp[3].y) {

                Point res = new Point(tmp[1].x, tmp[1].y);
                tmp[1] = tmp[3];
                tmp[3] = res;
            }
            
        }
        catch (NullPointerException e)
        {
            return null;
        }
        // ở đây temp[2] và temp[3] là 2 điểm nằm dưới cùng tờ giấy,ình phải lưu lại để cắt Ô mã trang :3
        // Lưu bằng cách gán cho thằng bên kia class ForPage
        // Cat ra ma xai
        Point n1 = new Point(tmp[3].x-tmp[0].x, tmp[3].y-tmp[0].y); // vector P0P3
        Point n2 = new Point(tmp[2].x-tmp[1].x, tmp[2].y-tmp[1].y); // vector p1p2
        n1.x*=110.0/582;
        n1.y*=110.0/582;
        n2.x*=110.0/582;
        n2.y*=110.0/582;
        
        forpage[0] =  new Point((int)tmp[3].x, (int)tmp[3].y);
        forpage[1] = new Point((int)tmp[2].x, (int)tmp[2].y);
        forpage[2] = new Point((int)tmp[2].x+(int)n2.x, (int)tmp[2].y+(int)n2.y);
        forpage[3] = new Point((int)tmp[3].x+(int)n1.x, (int)tmp[3].y+(int)n1.y);
//        System.out.println("Page corner Point: " + forpage[0].x +" : " +forpage[0].y);
//        System.out.println("Page corner Point: " + forpage[1].x +" : " +forpage[1].y);
//        System.out.println("Page corner Point: " + forpage[2].x +" : " +forpage[2].y);
//        System.out.println("Page corner Point: " + forpage[3].x +" : " +forpage[3].y);
        ForPage.pageCut = new MatOfPoint2f(forpage);
        
        shape = new MatOfPoint2f(tmp);
        return shape;
    }

}
