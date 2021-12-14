import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.threshold;
public class AdaptiveThreshold extends Application {
   public void start(Stage stage) throws IOException {
      //Loading the OpenCV core library
      System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
      String file ="D:\\Images\\win2.jpg";
      Mat src = Imgcodecs.imread(file, Imgcodecs.IMREAD_GRAYSCALE);
      //Creating an empty matrices to store the destination image.
      Mat dst = new Mat(src.rows(), src.cols(), src.type());
      //Applying simple threshold
      Imgproc.adaptiveThreshold(src, dst, 125,
      Imgproc.ADAPTIVE_THRESH_MEAN_C,
      Imgproc.THRESH_BINARY, 15, 12);
      //Converting matrix to JavaFX writable image
      Image img = HighGui.toBufferedImage(dst);
      WritableImage writableImage= SwingFXUtils.toFXImage((BufferedImage) img, null);
      //Setting the image view
      ImageView imageView = new ImageView(writableImage);
      imageView.setX(10);
      imageView.setY(10);
      imageView.setFitWidth(575);
      imageView.setPreserveRatio(true);
      //Setting the Scene object
      Group root = new Group(imageView);
      Scene scene = new Scene(root, 595, 400);
      stage.setTitle("Adaptive Threshold");
      stage.setScene(scene);
      stage.show();
   }
   
   public static Mat MakeThreshold(Mat src, int mucPhanNguong)
   {
       
      //Creating an empty matrices to store the destination image.
      Mat dst = new Mat(src.rows(), src.cols(), src.type());
      //Applying simple threshold
//      Imgproc.adaptiveThreshold(src, dst, 255,
//      Imgproc.ADAPTIVE_THRESH_MEAN_C,
//      Imgproc.THRESH_BINARY, 95, 12);

        threshold(src, dst, mucPhanNguong, 255, THRESH_BINARY);
      //Converting matrix to JavaFX writable image
      Image img = HighGui.toBufferedImage(dst);
      return dst;
       
   }
   public static Mat MakeAdaptiveThreshold(Mat src, int mucPhanNguong)
   {
       
      //Creating an empty matrices to store the destination image.
      Mat dst = new Mat(src.rows(), src.cols(), src.type());
      //Applying simple threshold
      Imgproc.adaptiveThreshold(src, dst, 255,
      Imgproc.ADAPTIVE_THRESH_MEAN_C,
      Imgproc.THRESH_BINARY, mucPhanNguong, 12);
      //Converting matrix to JavaFX writable image
      Image img = HighGui.toBufferedImage(dst);
      return dst;
       
   }
   public static Mat ThresHold_INV(Mat src)
   {
       
      //Creating an empty matrices to store the destination image.
      Mat dst = new Mat(src.rows(), src.cols(), src.type());
      //Applying simple threshold
      Imgproc.adaptiveThreshold(src, dst, 255,
      Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
      Imgproc.THRESH_BINARY_INV, 13, 12);
      //Converting matrix to JavaFX writable image
      Image img = HighGui.toBufferedImage(dst);
      return dst;
       
   }
   // TEST 
   public static void main(String args[]) {
      launch(args);
   }
}