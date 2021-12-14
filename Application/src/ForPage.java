
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Comparator;
import javafx.util.Pair;
import javax.swing.JOptionPane;
import org.opencv.calib3d.Calib3d;
import static org.opencv.calib3d.Calib3d.RANSAC;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.isContourConvex;
import static org.opencv.imgproc.Imgproc.warpPerspective;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author HUYENKUTE
 */
public class ForPage {

    static final int PageFrame_WID = 0;
    static final int PageFrame_HEI = 0;
    static final int PageFrame_MAX_AREA = 0;
    static final int PageFrame_MIN_AREA = 0;
    static final int countNZMin = 0;
    
    static double MAX_TRIANGLE_AREA = 50000;
    static double MIN_TRIANGLE_AREA = 2000;
    

    static int CHECK_FRHEI = 194; // khung ma trang dung de tim kiem o ma trang tren to giay sau khi cat
    static int CHECK_FRWID = 1507; // khung ma trang
    static double MAX_FRAME_AREA = 320000; // dien ti o ma trang
    static double MIN_FRAME_AREA = 260000; // dien tic o ma trang

    public static Mat imgOMaTrangBina;
    public static Mat imgOMaTrangGray;
    public static Mat anhNhiPhanCanDung;
    public static MatOfPoint2f pageCut;
    
    
    //==========================[ XOAY VÀ CẮT Phần CHỨA Ô MÃ TRANG ]==============================
    public static Mat CropPageArea(Mat src) {
        MatOfPoint2f shape = pageCut;
        Size n = shape.size();
        Mat draw = src.clone();
        Point pointArr[] = new Point[4];

        Rect box = Imgproc.boundingRect(shape);
        
        MatOfPoint2f src_vertices = shape;

        pointArr[0] = new Point(0, 0);
        pointArr[1] = new Point(1750, 0);
        pointArr[2] = new Point(1750, 300);
        pointArr[3] = new Point(0,300);
        MatOfPoint2f dst_vertices = new MatOfPoint2f(pointArr);

        Mat h = Calib3d.findHomography(src_vertices, dst_vertices, RANSAC, 5);//(pts_src, pts_dst, tam, RANSAC, 5);
        Size size = (box.size());
        size = new Size(1750, 300);
//            
        Mat im_out = new Mat();
        // Warp source image to destination based on homography
        warpPerspective(src, im_out, h, size);
//        resize( im_out, im_out, new Size(450, 600), 1, 1);
//        imshow("Homography", im_out);
        return im_out;
    }
    //==========================[  CẮT THEO Ô MÃ TRANG ]==============================
    public static Mat CropPageNumberFrame(MatOfPoint2f shape, Mat src) {
        Size n = shape.size();
        Mat draw = src.clone();
        Point pointArr[] = new Point[4];

        Rect box = Imgproc.boundingRect(shape);

        pointArr[0] = new Point(0, 0);
        pointArr[1] = new Point(3120, 0);
        pointArr[2] = new Point(3120, 400);
        pointArr[3] = new Point(0, 400);

        MatOfPoint2f pts;
        MatOfPoint2f src_vertices = shape;

        MatOfPoint2f dst_vertices = new MatOfPoint2f(pointArr);

        Mat tam;
        Mat h = Calib3d.findHomography(src_vertices, dst_vertices, RANSAC, 5);//(pts_src, pts_dst, tam, RANSAC, 5);
        Mat rotated;
        Size size = (box.size());
        size = new Size(3120, 385);
//            
        Mat im_out = new Mat();
        // Warp source image to destination based on homography
        warpPerspective(src, im_out, h, size);
        return im_out;
    }
    public static MatOfPoint2f findPageFrame(Mat Img) {
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
    //    System.out.println("DAY LA FIND RECT: contour size(): " + contours.size());
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
            approx = approxCurve.toArray();
//                        squares.add(approxCurve)
            if ((abs(rectRound.width - CHECK_FRWID) <= 70 && abs(rectRound.height - CHECK_FRHEI) <= 70) && ((abs(rectRound.width * 1.0 / rectRound.height) <= 9.0) && (abs(rectRound.width * 1.0 / rectRound.height) >= 7.0)) && (abs(contourArea(approxCurve)) <= MAX_FRAME_AREA) && (abs(contourArea(approxCurve)) >= MIN_FRAME_AREA)) {
                if ((approx.length == 4) && (isContourConvex(new MatOfPoint(approx)))) {
//                    double maxCosine = 0;
//
//                    for (int j = 2; j < 5; j++) {
//                        double cosine = abs(angle(approx[j % 4], approx[j - 2], approx[j - 1]));
//                        maxCosine = max(maxCosine, cosine);
//                    }

//                System.out.println("maxCosine =" + maxCosine);
//                    if (maxCosine <= 0.75) {
           //         System.out.println("Aproxx -- HINH CHU NHAT: (wid: hei) : " + "(" + rectRound.width + ":" + rectRound.height + ") | Area: " + contourArea(approxCurve) + " | length: " + approx.length);

                    squares.add(approxCurve);
//                    System.out.println("OKE :)");
                    continue;
//                    }
                }
            }
        }

        //========================================= LOẠI BỎ NHŨNG HÌNH Chu Nhat TRÙNG TÂM ====================================
        MatOfPoint2f q, Q;
        for (int i = 0; i < squares.size(); i++) {
            Q = new MatOfPoint2f(squares.get(i));
            for (int j = 0; j < squares.size(); j++) {

                q = new MatOfPoint2f(squares.get(j));
                Rect r1 = boundingRect(Q);
                Rect r2 = boundingRect(q);
                if (abs(r1.width - r2.width) < 30 && abs(sqrt((r1.x - r2.x) * (r1.x - r2.x) + (r1.y - r2.y) * (r1.y - r2.y))) < 12) {
//                    if (r1.width > r2.width) {
//                        squares.remove(i);
//                    } else {
//                        squares.remove(j);
//                    }

                    break;
                }
            }
        }
        
        if(squares.size() != 1)
        {
//            JOptionPane.showMessageDialog(null, "Tìm dư khung mã trang rồi");
            return null;
        }
        return squares.get(0);
    }
    static int getDistance(Point A, Point B)
    {
        return (int) abs(sqrt((A.x-B.x)*(A.x-B.x)+(A.y-B.y)*(A.y-B.y)));
    }
    public static int getTheDirectOfTriangle(MatOfPoint2f approxCurve) // 0 1 2 3
    {
        Rect rectRound = boundingRect(approxCurve);
        int direct = 0;
        
        Point[] approx = approxCurve.toArray();
        Point[] vitri  = new Point[4];
        vitri[0] = new Point(rectRound.x, rectRound.y); // huong 11h
        vitri[1] = new Point(rectRound.x+rectRound.width, rectRound.y); // huong 1h
        vitri[2] = new Point(rectRound.x+rectRound.width, rectRound.y+rectRound.height); // huong 5h
        vitri[3] = new Point(rectRound.x, rectRound.y+rectRound.height); // huog 7h
        
        int distance, maxDistance  = -1;
        
     //   System.out.println("DSTANCE:");
        for (int i = 0; i < 4; i++) {
            distance = 0;
            for(int j =0; j< approx.length; j++)
            {
                distance += getDistance(approx[j], vitri[i]);
            }
            if(distance>maxDistance)
            {
                maxDistance = distance;
                direct = i;
            }
                System.out.print("  |  "+distance);
        }
        
        System.out.println("  --->DIRECT: "+ (direct+2)%4);
        return (direct+2)%4;
    }
    public static String detectingTriangle(Mat image) { // return the Page Number
        int masoBuoithi  = 0;
        int pageNumber = 0;
        Mat gray = image;
        ArrayList<MatOfPoint2f> triangles = new ArrayList<>();
        ArrayList<MatOfPoint2f> debugTris = new ArrayList<>();
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
        for (MatOfPoint contour : contours) {
            
            
            MatOfPoint2f contourFloat = new MatOfPoint2f(contour.toArray());
            double arcLen = Imgproc.arcLength(contourFloat, true) * 0.02;

            approxCurve = new MatOfPoint2f();
            Imgproc.approxPolyDP(contourFloat, approxCurve, arcLen, true);
            Rect rectRound = boundingRect(approxCurve);
            approx = approxCurve.toArray();

//                        triangles.add(approxCurve);
            if ((abs(rectRound.width * 1.0 / rectRound.height - 1.0) < 0.5) && (abs(contourArea(approxCurve)) <= MAX_TRIANGLE_AREA) && (abs(contourArea(approxCurve)) >= MIN_TRIANGLE_AREA)) {
        //        System.out.println("Aproxx of Triangle: (wid: hei) : " + "(" + rectRound.width + ":" + rectRound.height + ") | Area: " + contourArea(approxCurve) + " | length: " + approx.length);
                if ((approx.length >= 3 && approx.length <= 8)) {
                        triangles.add(approxCurve);
                        continue;
                }
            }

        }
        //========================================= LOẠI BỎ NHŨNG HÌNH TAM GIÁC TRÙNG NHAU ====================================
        MatOfPoint2f q, Q;
        for (int i = 0; i < triangles.size(); i++) {
            Q = new MatOfPoint2f(triangles.get(i));
            for (int j = 0; j < triangles.size(); j++) {

                q = new MatOfPoint2f(triangles.get(j));
                Rect r1 = boundingRect(Q);
                Rect r2 = boundingRect(q);
                if (abs(r1.width - r2.width) < 30 && abs(sqrt((r1.x - r2.x) * (r1.x - r2.x) + (r1.y - r2.y) * (r1.y - r2.y))) < 45) {
                    if (r1.width > r2.width) {
                        triangles.remove(i);
                    } else {
                       triangles.remove(j);
                    }
                    break;
                }
            }
        }
        triangles.sort(new Comparator<MatOfPoint2f>() {

            public int compare(MatOfPoint2f p1, MatOfPoint2f p2) {
                Rect r1 = boundingRect(p1), r2 = boundingRect(p2);

                if (r1.x > r2.x) {
                        return -1;
                    } else if (r1.x == r2.x) {
                        return 0;
                    } else {
                        return 1;
                    }
            }
        });
//        if(triangles.size() != 10)
//        {
//            return null;
//        }
        if(triangles.size() < 7)
        {
            return null;
        }
        
        for(int i = 0; i < 3; i++)
        {
          //     System.out.println("--------- Hình tam giác thứ " + i +": ");
               int theDirectOfTriangle = getTheDirectOfTriangle(triangles.get(i));
               pageNumber += theDirectOfTriangle*(Math.pow(4, i));
               
               debugTris.add(triangles.get(i));
        }
        for(int i = 3; i < triangles.size(); i++)
        {
               
            //   System.out.println("--------- Hình tam giác thứ " + i +": ");
               int theDirectOfTriangle = getTheDirectOfTriangle(triangles.get(i));
               masoBuoithi += theDirectOfTriangle*(Math.pow(4, i-3));
               
               debugTris.add(triangles.get(i));
        }
   //     System.out.println("size òf líst Triangle: " + triangles.size());
//        Main.displayImage(Main.Mat2BufferedImage(Processing.debugSquares(debugTris, gray)),"TRIANGLES");
        
        return masoBuoithi+" "+ pageNumber;
    }
    public static ArrayList<MatOfPoint> findTheFilledTriangle(ArrayList<MatOfPoint> contours) {

        ArrayList<MatOfPoint> vec = new ArrayList<>();
        Rect rect, rectCrop;
        for (int x = 0; x < contours.size(); x++) {
            rect = boundingRect(contours.get(x));
            double area = contourArea(contours.get(x));
            rectCrop = boundingRect(contours.get(x));
            Mat imageROI = ForMarker.anhNhiPhanCanDung.submat(rectCrop);
            int total = Core.countNonZero(imageROI);
     //       System.out.println("Count non zẻo tam giac: " + total);
            if (total < countNZMin) {
                vec.add(contours.get(x));
            }
        }

        return vec;
    }

}
