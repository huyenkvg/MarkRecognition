import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

class FileChooser {
    public static String dirpath="D:\\NKKH\\DOCDIEM\\ImagesForApp";
    public static String dsthipath="D:\\NKKH\\DOCDIEM\\SoreReadApplication\\DanhSachThi";
    public static String GetImgLink() {

        JFileChooser jfc = new JFileChooser(dirpath);
        jfc.setDialogTitle("Select an image");
        jfc.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG and jpeg images", "png", "jpg", "jpeg");
        jfc.addChoosableFileFilter(filter);

        int returnValue = jfc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
           return  jfc.getSelectedFile().getPath();
        }
        return null;
    }
    public static String GetFileLink() {

        JFileChooser jfc = new JFileChooser(dsthipath);
        jfc.setDialogTitle("Select an excel file");
        jfc.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("xlsx and xls", "xlsx", "xls");
        jfc.addChoosableFileFilter(filter);

        int returnValue = jfc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
           return  jfc.getSelectedFile().getPath();
        }
        return null;
    }
    public static void main(String[] args) {
        GetImgLink();
    }

}