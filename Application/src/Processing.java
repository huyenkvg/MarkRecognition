
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import static java.awt.image.ImageObserver.HEIGHT;
import static java.awt.image.ImageObserver.WIDTH;
import java.util.List;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import javafx.util.Pair;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.opencv.calib3d.Calib3d;
import static org.opencv.calib3d.Calib3d.RANSAC;
import org.opencv.core.Core;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;
import org.opencv.core.KeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import static org.opencv.highgui.HighGui.imshow;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.LINE_8;
import static org.opencv.imgproc.Imgproc.LINE_AA;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.isContourConvex;
import static org.opencv.imgproc.Imgproc.resize;
import static org.opencv.imgproc.Imgproc.threshold;
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
public class Processing {

    // Prepare:
    static int averageDefault = 65; // PHẦN TRĂM CHẤP NHẬN CỦA ĐIỂM PIXEL ĐEN TRÊN DIỆN TÍCH CONTOUR
    static double lineFrame = 16;
    static double MAX_DienTichOTron = 42000;
    static double MIN_DienTichOTron = 24000;
    
    static double MIN_FRAME_AREA = 130000;
    static double MAX_FRAME_AREA = 210000;
    public static int SOLUONGO = 10;
    // Nếu marker không đủ lớn mà ảnh cũng không đủ lớn, AproxPolyDP sẽ không nhận ra các cạnh trong hình vuông thực sự thẳng
    // dẫn đến 1 cạnh mà bị phân làm 3, 4 cạnh => Xác định hình vuông bị sai =>> phóng ảnh lên vài ngàn
    static final double CST_IMGWID = 1500; // paper width
    static final double CST_IMGHEI = 2000; // paper height
    
    static final double CST_PAPERWID = 1500; // paper width
    static final double CST_PAPERHEI = 1425; // paper height
    
    static  double CHECK_FRWID = 1352; // the frame have 12 circles 
    static  double CHECK_FRHEI = 135; // the frame have 12 circles
    
    static  double CST_FRWID = 3004; // the frame have 12 circles 
    static  double CST_FRHEI = 300; // the frame have 12 circles
    static final double CST_PAGEWID = 200; // ô mã trang wid
    static  double CST_PAGEHEI = 500; // ô mã trang height\
    
    //==========================[ LỌC NHIỄU VÀ PHÂN NGƯỠNG ẢNH ĐẦU VÀO ]==============================

    public static Mat prepareForMainImg(Mat img, int MPN) {
        Mat bw = new Mat();
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
        //erode(bw, bw, 3 )
        int elementType = Imgproc.CV_SHAPE_RECT;
        int kernelSize = 0;
        Mat element = Imgproc.getStructuringElement(elementType, new Size(2 * kernelSize + 1, 2 * kernelSize + 1),
                new Point(kernelSize, kernelSize));
        Imgproc.erode(img, img, Imgproc.getStructuringElement(MORPH_RECT, new Size(9, 9)));
        Imgproc.dilate(img, img, element);
        // PHÂN NGƯỠNG BẰNG HÀM THRESHOLD (ảnh đàu vào, ảnh đầu ra, mức phân ngưỡng, ..., kiểu phân ngưỡng)
//        threshold(img, bw, Main.mucPhanNguong, 255, THRESH_BINARY);

//------------- Phân ngưỡng bằng adaptive threshold----------------------------------------------
        bw = AdaptiveThreshold.MakeAdaptiveThreshold(img, 27);
        ForMarker.anhNhiPhanCanDung  = AdaptiveThreshold.MakeThreshold(img, MPN);
//        bw = AdaptiveThreshold.MakeThreshold(img);

//        resize(bw, bw, new Size(CST_PAPERWID, CST_PAPERWID),1, 1);
// ----------------RESIZE lại theo size giấy trước đã rồi tính, hiện tại tỉ lệ ảnh chụp là 3/4 nên làm vậy trước đã
        resize(bw, bw, new Size(CST_IMGWID, CST_IMGHEI), 1, 1);
//-------------------------------------------------------------------------------------------------------------

//---------------Toi muon luu lại image dau vao ------------------------------------------------
        resize(img, img, new Size(CST_IMGWID, CST_IMGHEI), 1, 1);
        Main.imageBanDau = img;
        return bw;
    }
    public static Mat prepare(Mat img, int MPN) { // mức phaan ngưỡng
        Mat bw = new Mat();
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
        //erode(bw, bw, 3 )
        int elementType = Imgproc.CV_SHAPE_RECT;
        int kernelSize = 0;
        Mat element = Imgproc.getStructuringElement(elementType, new Size(2 * kernelSize + 1, 2 * kernelSize + 1),
                new Point(kernelSize, kernelSize));
        Imgproc.erode(img, img, Imgproc.getStructuringElement(MORPH_RECT, new Size(9, 9)));
        Imgproc.dilate(img, img, element);
        // PHÂN NGƯỠNG BẰNG HÀM THRESHOLD (ảnh đàu vào, ảnh đầu ra, mức phân ngưỡng, ..., kiểu phân ngưỡng)
//        threshold(img, bw, Main.mucPhanNguong, 255, THRESH_BINARY);

//------------- Phân ngưỡng bằng adaptive threshold----------------------------------------------
        bw = AdaptiveThreshold.MakeAdaptiveThreshold(img, MPN);
//        ForMarker.anhNhiPhanCanDung  = AdaptiveThreshold.MakeThreshold(img);
//        bw = AdaptiveThreshold.MakeThreshold(img);

//        resize(bw, bw, new Size(CST_PAPERWID, CST_PAPERWID),1, 1);
// ----------------RESIZE lại theo size giấy trước đã rồi tính, hiện tại tỉ lệ ảnh chụp là 3/4 nên làm vậy trước đã
        resize(bw, bw, new Size(CST_IMGWID, CST_IMGHEI), 1, 1);
//-------------------------------------------------------------------------------------------------------------

//---------------Toi muon luu lại image dau vao ------------------------------------------------
        resize(img, img, new Size(CST_IMGWID, CST_IMGHEI), 1, 1);
        Main.imageREDBanDau = img;

        return img;
    }

    //===================================================================================================
    //==========================[ TÍNH GÓC TỪ 3 ĐIỂM ]==============================
    static double angle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

    //==========================[ CHUYỂN TỪ MAT2BUFERED SANG MBUFFERED IMAGE ]==============================
    static BufferedImage Mat2BufferedImage(Mat xm) {
        // Fastest code
        // output can be assigned either to a BufferedImage or to an Image
        Mat m = xm.clone();
        
//        Imgproc.resize(m, m, new Size(m.size(WIDTH), m.size(HEIGHT)), 0.25, 0.25);
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }
// =========================[ HIỂN THỊ ẢNH ]==================

    static void displayImage(Image img2) {

        //BufferedImage img=ImageIO.read(new File("/HelloOpenCV/lena.png"));
        ImageIcon icon = new ImageIcon(img2);
        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(img2.getWidth(null) + 50, img2.getHeight(null) + 50);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    

    //==========================[ TÌM TẤT CẢ  RECT ]==============================
    public static ArrayList<MatOfPoint2f> findRect(Mat Img) {
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
   //     System.out.println("DAY LA FIND RECT: contour size(): " +contours.size());
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
            if ((abs(rectRound.width - CHECK_FRWID) <= 200  && abs(rectRound.height - CHECK_FRHEI) <= 100)&&((abs(rectRound.width * 1.0 / rectRound.height) <= 12.0)&&(abs(rectRound.width * 1.0 / rectRound.height )>=8.0))  && (abs(contourArea(approxCurve)) <= MAX_FRAME_AREA) && (abs(contourArea(approxCurve)) >= MIN_FRAME_AREA)) {
                if ((approx.length == 4) && (isContourConvex(new MatOfPoint(approx)))) {
//                    double maxCosine = 0;
//
//                    for (int j = 2; j < 5; j++) {
//                        double cosine = abs(angle(approx[j % 4], approx[j - 2], approx[j - 1]));
//                        maxCosine = max(maxCosine, cosine);
//                    }

//                System.out.println("maxCosine =" + maxCosine);
//                    if (maxCosine <= 0.75) {
           //             System.out.println("Aproxx -- HINH CHU NHAT: (wid: hei) : " + "("+rectRound.width +":"+rectRound.height+ ") | Area: " + contourArea(approxCurve) + " | length: " +approx.length);
        
                        squares.add(approxCurve);
//                    System.out.println("OKE :)");
                        continue;
//                    }
                }
            }
        }

        //========================================= LOẠI BỎ NHŨNG HÌNH CHỮ NHẬT TRÙNG TÂM ====================================
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
      //  System.out.println("size òf líst Rectangle: " + squares.size());
        squares.sort(new Comparator<MatOfPoint2f>() {

            public int compare(MatOfPoint2f p1, MatOfPoint2f p2) {
                Rect r1 = boundingRect(p1), r2 = boundingRect(p2);

                if (r1.y < r2.y) {
                    return -1;
                } else if (r1.x > r2.x) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        return squares;
    }
   

    //=================ơ KHOANH VÙNG HÌNH VUÔNG ĐỂ KIAAMR TRA ]=================================
    public static Mat debugSquares(ArrayList<MatOfPoint2f> squares, Mat src) {
        Mat image = new Mat(src.rows(), src.cols(), CV_8UC3);
        ArrayList<MatOfPoint> arrMat = new ArrayList<>(); //= new MatOfPoint[squares.size()];
        for (MatOfPoint2f square : squares) {
            arrMat.add(new MatOfPoint(square.toArray()));
        }
        // List<MatOfPoint> contours = Arrays.asList(arrMat);
        for (int i = 0; i < squares.size(); i++) {
            // draw contour
            if (i < 4) {

                Imgproc.drawContours(image, arrMat, i, new Scalar(255, 255, 0), 3, 1);
            } else {
                Imgproc.drawContours(image, arrMat, i, new Scalar(0, 255, 255), 3, 1);
            }
        }
//        if(squares.size() == 1)
            resize(image, image, new Size(1000,127));
//        else
//            resize(image, image, new Size(450,600));
        return image;
    }

   
    //==========================[TÍNH VỊ TRÍ CỦA KHUNG ĐIỂM THI CHỨA 12 Ô TRÒN]==================================
//    public static MatOfPoint2f CaculateMarkFrame(MatOfPoint2f frame)
//    {
//        CST_FRHEI = CST_PAPERHEI/SOLUONGO;
//        Point[] points = new Point[4];
//        
//        
//        return new MatOfPoint2f(points);
//    }

    //==========================[ XOAY VÀ CẮT THEO Ô ĐIỂM THI]==============================
    public static Mat RotateAndCropMarkFrame(MatOfPoint2f shape, Mat src) {
        Size n = shape.size();
        Mat draw = src.clone();
        Point pointArr[] = new Point[4];

        Rect box = Imgproc.boundingRect(shape);

        pointArr[0] = new Point(0, 0);
        pointArr[1] = new Point(CST_FRWID, 0);
        pointArr[2] = new Point(CST_FRWID, CST_FRHEI);
        pointArr[3] = new Point(0, CST_FRHEI);

        MatOfPoint2f pts;
        MatOfPoint2f src_vertices = shape;

        MatOfPoint2f dst_vertices = new MatOfPoint2f(pointArr);

        Mat tam;
        Mat h = Calib3d.findHomography(src_vertices, dst_vertices, RANSAC, 5);//(pts_src, pts_dst, tam, RANSAC, 5);
        Mat rotated;
        Size size = (box.size());
        size = new Size(CST_FRWID, CST_FRHEI);
//            
        Mat im_out = new Mat();
        // Warp source image to destination based on homography
        warpPerspective(src, im_out, h, size);
        imshow("Homography", im_out);
        src = im_out;
        return im_out;
    }
    //==========================[ XOAY VÀ CẮT THEO MARKER ]==============================
    public static Mat RotateAndCropMarker(MatOfPoint2f shape, Mat src) {
        Size n = shape.size();
        Mat draw = src.clone();
        Point pointArr[] = new Point[4];

        Rect box = Imgproc.boundingRect(shape);

        pointArr[0] = new Point(0, 0);
        pointArr[1] = new Point(CST_PAPERWID, 0);
        pointArr[2] = new Point(CST_PAPERWID, CST_PAPERHEI);
        pointArr[3] = new Point(0, CST_PAPERHEI);
        MatOfPoint2f src_vertices = shape;

        MatOfPoint2f dst_vertices = new MatOfPoint2f(pointArr);
        Mat h = Calib3d.findHomography(src_vertices, dst_vertices, RANSAC, 5);//(pts_src, pts_dst, tam, RANSAC, 5);
        Size size = (box.size());
        size = new Size(CST_PAPERWID, CST_PAPERHEI);
//            
        Mat im_out = new Mat();
        // Warp source image to destination based on homography
        warpPerspective(src, im_out, h, size);
//        
        return im_out;
    }

    // ====================[ sắp xếp theo vị trí ]=============================
    public static ArrayList<Point> sortedListByPosition(ArrayList<Point> vec) {

        Collections.sort(vec, new Comparator<Point>() {
            public int compare(Point p1, Point p2) {
                if (p1.y == p2.y) {
                    if (p1.x < p2.x) {
                        return 1;
                    }
                    return 0;
                }
                if (p1.y < p2.y) {
                    return 1;
                }
                return 0;
            }
        });

        return vec;
    }
    
    // ========================[ XÁC ĐỊNH VÒNG TRÒN ]=========================================================
    public static Double detectingCircle(Mat image, Mat testCaiThien) //trả về vị trí theo trục tung của ô điểm thi và điểm thi của vị trí này
    {
//        displayImage(Mat2BufferedImage(image));
        double ans = -1;
        int averageArea = 0;
        int average = averageDefault;
        ArrayList<Point> vans = new ArrayList<>();
        Point pt = new Point();
        ArrayList<MatOfPoint> repareToFindCir = new ArrayList<>();
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        int count = 0;
        Mat hierarchy = new Mat();
        Point[] approx;
        // Find contours and store them in a list
        Imgproc.findContours(image, repareToFindCir, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);
        int  id = 1;
        Rect rect;
        Mat test = new Mat(image.rows(), image.cols(), CV_8UC3);

        //System.out.println(res.size() + " : ");
        for (int i = 0; i < repareToFindCir.size(); i++) {
            Rect r1 = boundingRect(repareToFindCir.get(i));
            double area1 = contourArea(repareToFindCir.get(i));
         //   System.out.println("DETECTING CiRCLE: \n" + area1 + "  --  " + r1.width + " : " + r1.height);

            if (area1 >= MIN_DienTichOTron && area1 <= MAX_DienTichOTron && abs(1 - ((double) r1.width / (double) r1.height)) <= 0.3) {

                boolean yes = true;
                for (MatOfPoint contour : contours) {
                    Rect r2 = boundingRect(contour);
                    double area2 = contourArea(contour);
//                    System.out.println("---------->  "+(sqrt((r1.x - r2.x) * (r1.x - r2.x) + (r1.y - r2.y) * (r1.y - r2.y)))+" ---- "+ min(r1.width, r2.width));
                    if (min(r1.width, r2.width) < 200 && (sqrt((r1.x - r2.x) * (r1.x - r2.x) + (r1.y - r2.y) * (r1.y - r2.y))) < min(r1.width, r2.width)) {
                        yes = false;
                        //System.out.println("------------------------------------------");
                        if (area1 > area2) {
                            contour = (repareToFindCir.get(i));
                        }
                    }
                }
                if (yes) {
                    contours.add(repareToFindCir.get(i));
                }
            }
        }
        if(contours.size()!=12)
            return -2.0;
        // Mảng chứa ô tròn được tô và vị trí của ô tròn
        ArrayList<Pair<Point, Integer>> vec = new ArrayList<>();
        for (int x = 0; x < contours.size(); x++) {
            rect = boundingRect(contours.get(x));
            double area = contourArea(contours.get(x));
            averageArea = (int) area;
            Rect rectCrop = boundingRect(contours.get(x));
//            Mat imageROI = image.submat(rectCrop);            
            Mat imageROI = testCaiThien.submat(rectCrop);// Thay thế image bằng image cải thiện đã lọc màu đỏ khỏi cuộc đời tôi :)
            int total = Core.countNonZero(imageROI);
            
            
            pt.x = rect.x + rect.width / 2;
            pt.y = rect.y + rect.height / 2;
//            vec.add(new Pair(new Point(pt.x, pt.y), pixel)); lấy tỉ lệ
                total = tinhTrungBinh(total, averageArea);
                if(total>=100)
                    average = averageDefault+(total-100);
    
            vec.add(new Pair(new Point(pt.x, pt.y), total));
            //System.out.println("pt: (" + pt.x + "  :: " + pt.y + ")");
            Imgproc.drawContours(test, contours, x, new Scalar(0, 255, 0), RETR_TREE);
        }
        // SORT các ô tròn trong mảng theo vị trí, các ô tròn đã được đánh dấu kèm giá trị tô hay không Pair Point và Double :  Point là vị trí, Double là điểm;
        vec.sort(new Comparator<Pair<Point, Integer>>() {

            public int compare(Pair<Point, Integer> p1, Pair<Point, Integer> p2) {

                    if (p1.getKey().x < p2.getKey().x) {
                        return -1;
                    } else if (p1.getKey().x == p2.getKey().x) {
                        return 0;
                    } else {
                        return 1;
                    }
            }
        });
//        averageArea /= 12;
        for (int i = 0; i < min(11, vec.size()); i++) {
            vans.add(vec.get(i).getKey());
           // System.out.println("-------------- " +vec.get(i).getValue());
            if (vec.get(i).getValue() < average) {
                if (count > 0) {
                    return  -1 * 1.0; // It's False // Không hợp lệ
                }
                count++;
                ans = i;
            }
        }
        if(vec.size()==12)
        {
            if (vec.get(11).getValue() < average+5) {
                count++;
                ans += 0.5;
            }
        }
       // System.out.println("ĐÂY LÀ DS VEC CỦA <Pair<Point, Double>>\n" + vec);
        Mat drawing = new Mat(image.size(), CV_8UC3);
        for (int i = 1; i < contours.size(); i++) {
            Scalar color = new Scalar(125, 250, 124);
            Imgproc.drawContours(drawing, contours, (int) i, color, 2, LINE_8, hierarchy, 0);

        }

       // displayImage(Mat2BufferedImage(test));
        //cout << "aver:" << aver << " contours size: "<< contours.size()<<  "\n";

//        System.out.println(vec.size());
//        System.out.println(count + " : ");
//        System.out.println(ans);
        if (vans.size() == 0) {
            return -1.0;
        }
        return ans;
    }
    public static int tinhTrungBinh(int total, int area)
    {
        total = total*100/area;
       // System.out.println("TRUNGBINH = "+total );
      return total;
    }

    
}
