import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
 
public class CardsResolver {
    
	private static final String DEFAULT_INPUT_DIR = System.getProperty("user.dir");
    private static Map<String, BufferedImage> mapTempls = new HashMap<>();

    private static void loadTemplates()  {
		File dirTempls = new File("Templates//");
		File[] filesTempls = dirTempls.listFiles((d, n) -> n.endsWith(".png"));
		mapTempls = Arrays.asList(filesTempls).
				                      stream().
				                      collect(Collectors.
				                    		  toMap(f -> f.getName().replaceFirst("[.][^.]+$", "")
				                    				  ,
													f -> {
														  try 
															 { 
															  return ImageIO.read(f);
															 } catch (IOException e) {
																 throw new RuntimeException(e);  
															 }
														  }
													)
					                    	  );
    } 
 
    public static void main(String[] args) throws IOException {
        // https://rosettacode.org/mw/images/3/3c/Lenna50.jpg
        // https://rosettacode.org/mw/images/b/b6/Lenna100.jpg
	    // 147, 219, 291, 363, 435
        loadTemplates();
        if (mapTempls.isEmpty()) {
        	System.out.println("Отсутствуют шаблоны");
        	System.exit(0);
        }
        String dirScan = args.length == 0? DEFAULT_INPUT_DIR: Optional.ofNullable(args[0]).orElse(DEFAULT_INPUT_DIR);
        File[] arrlf = new File(dirScan).listFiles((d, n) -> n.endsWith(".png")); 
        for (File inFile : arrlf) {
    		BufferedImage img1 = ImageIO.read(inFile);	
    		
    		//changeColor(img1, 16,16,18,35,35,38);
    		//changeColor(img1, 96,34,34,205,73,73);
    		for(Map.Entry<String, BufferedImage> entry : mapTempls.entrySet()) {
                BufferedImage img2 = entry.getValue(); 
                img2 = OtsuBinarize.toGray(img2);
                img2 = OtsuBinarize.binarize(img2);
                BufferedImage img11 = img1.getSubimage(153, 592, img2.getWidth(), img2.getHeight());
                for (int i = 153; i < 168; i++) {
                    int px = img1.getRGB(i,595) ;
                    System.out.println(Integer.toHexString(px));                	
                }
                changeColor(img11, 120,120,120,255,255,255);
        		changeColor(img11, 77,77,78,255,255,255);
        		changeColor(img11, 76,76,77,255,255,255);
        		File outputfile = new File(inFile.getName()+".gray.png");
        		ImageIO.write(img11, "png", outputfile);       		
        		img11 = OtsuBinarize.toGray(img11);
        		img11 = OtsuBinarize.binarize(img11);
        		//File outputfile = new File(inFile.getName()+".gray.png");
        		//ImageIO.write(img11, "png", outputfile);

                
                //File outputfile1 = new File(entry.getKey()+".gray.png");
        		//ImageIO.write(img2, "png", outputfile1);
                Double p = getDifferencePercent(img11, img2);
                System.out.println("diff percent: " + inFile.getName()+ " - " + entry.getKey()+ " - " + p.intValue());
                
    		}
		}

		

		

		
		//File outputfile = new File("img1.png");
		//ImageIO.write(img1, "png", outputfile);
    }
 
    private static double getDifferencePercent(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();
        int width2 = img2.getWidth();
        int height2 = img2.getHeight();
        if (width != width2 || height != height2) {
            throw new IllegalArgumentException(String.format("Images must have the same dimensions: (%d,%d) vs. (%d,%d)", width, height, width2, height2));
        }
 
        long diff = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                diff += pixelDiff(img1.getRGB(x, y), img2.getRGB(x, y));
            }
        }
        long maxDiff = 3L * 255 * width * height;
 
        return 100.0 * diff / maxDiff;
    }
 
    private static int pixelDiff(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xff;
        int g1 = (rgb1 >>  8) & 0xff;
        int b1 =  rgb1        & 0xff;
        int r2 = (rgb2 >> 16) & 0xff;
        int g2 = (rgb2 >>  8) & 0xff;
        int b2 =  rgb2        & 0xff;
        return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
    }
	
    private static void changeColor(
        BufferedImage imgBuf,
        int oldRed, int oldGreen, int oldBlue,
        int newRed, int newGreen, int newBlue) {

	    int RGB_MASK = 0x00ffffff;
	    int ALPHA_MASK = 0xff000000;

	    int oldRGB = oldRed << 16 | oldGreen << 8 | oldBlue;
	    int toggleRGB = oldRGB ^ (newRed << 16 | newGreen << 8 | newBlue);

	    int w = imgBuf.getWidth();
	    int h = imgBuf.getHeight();

	    int[] rgb = imgBuf.getRGB(0, 0, w, h, null, 0, w);
	    for (int i = 0; i < rgb.length; i++) {
		if ((rgb[i] & RGB_MASK) == oldRGB) {
		    rgb[i] ^= toggleRGB;
		}
	    }
	    imgBuf.setRGB(0, 0, w, h, rgb, 0, w);
    }
    
    private static void grayscale(BufferedImage img){
        //get image width and height
        int width = img.getWidth();
        int height = img.getHeight();
        //convert to grayscale
        for(int y = 0; y < height; y++){
          for(int x = 0; x < width; x++){
            int p = img.getRGB(x,y);

            int a = (p>>24)&0xff;
            int r = (p>>16)&0xff;
            int g = (p>>8)&0xff;
            int b = p&0xff;

            //calculate average
            int avg = (r+g+b)/3;

            //replace RGB value with avg
            p = (a<<24) | (avg<<16) | (avg<<8) | avg;

            img.setRGB(x, y, p);
          }
        }
     }
    
       

}
