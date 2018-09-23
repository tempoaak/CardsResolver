package com.akartkam.cardsresolver;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
 
public class ImageService {
 
  
	public static int getCompareIndexOfSubImages(BufferedImage img1,
			BufferedImage img2, int offsetX, int positionY) {
		BufferedImage img11 = img1.getSubimage(offsetX, positionY,
				img2.getWidth(), img2.getHeight());
		img11 = binarize(img11);
		// Это для повышения точности распознавания, т.к. некоторые карты имею
		// затененную поверхность
		changeColor(img11, Constants.ARRAY_RGB_OF_SHADOW_CARD_SURFACE,
				Constants.ARRAY_RGB_OF_NORMAL_CARD_SURFACE);
		return Double.valueOf(getDifferencePercent(img11, img2)).intValue();

	}

	private static double getDifferencePercent(BufferedImage img1, BufferedImage img2) {
		int w1 = img1.getWidth();
		int h1 = img1.getHeight();
		int w2 = img2.getWidth();
		int h2 = img2.getHeight();

		long diff = 0;
		for (int y = 0; y < h1; y++) {
			for (int x = 0; x < w1; x++) {
				diff += getPixelDifferenc(img1.getRGB(x, y), img2.getRGB(x, y));
			}
		}
		long totalPixels = 3L * 255 * w1 * h1;
		return 100.0 * diff / totalPixels;
	}

	private static int getPixelDifferenc(int rgb1, int rgb2) {
		int r1 = (rgb1 >> 16) & 0xff;
		int g1 = (rgb1 >> 8) & 0xff;
		int b1 = rgb1 & 0xff;
		int r2 = (rgb2 >> 16) & 0xff;
		int g2 = (rgb2 >> 8) & 0xff;
		int b2 = rgb2 & 0xff;
		return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
	}

	private static void changeColor(BufferedImage img, int[] rgb1, int[] rgb2) {
		int MASK = 0x00ffffff;
		int sourceRgb = rgb1[0] << 16 | rgb1[1] << 8 | rgb1[2];
		int destRgb = sourceRgb ^ (rgb1[0] << 16 | rgb1[1] << 8 | rgb1[2]);
		int w = img.getWidth();
		int h = img.getHeight();
		int[] arrRgb = img.getRGB(0, 0, w, h, null, 0, w);
		for (int i = 0; i < arrRgb.length; i++) {
			if ((arrRgb[i] & MASK) == sourceRgb) {
				arrRgb[i] ^= destRgb;
			}
		}
		img.setRGB(0, 0, w, h, arrRgb, 0, w);
	}    
 
 
    //Функция преобразует изображения в оттенки серого
    private static BufferedImage grayscale(BufferedImage img) {
        int alpha, red, green, blue;
        int newPixel;
 
        BufferedImage lum = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
 
        for(int i=0; i<img.getWidth(); i++) {
            for(int j=0; j<img.getHeight(); j++) { 
                //Разбор пикселей на цвета и прозрачность
                alpha = new Color(img.getRGB(i, j)).getAlpha();
                red = new Color(img.getRGB(i, j)).getRed();
                green = new Color(img.getRGB(i, j)).getGreen();
                blue = new Color(img.getRGB(i, j)).getBlue();
 
                red = (int) (0.21 * red + 0.71 * green + 0.07 * blue);
                //Обратно к оригиналу
                newPixel = colorToRGB(alpha, red, red, red);
                lum.setRGB(i, j, newPixel); 
            }
        } 
        return lum; 
    }
 
    //Гистограмма 
    private static int[] getHistogram(BufferedImage img) {
        int[] hist = IntStream.range(0, 256).map(i->0).toArray();
        for(int i=0; i<img.getWidth(); i++) {
            for(int j=0; j<img.getHeight(); j++) {
                int red = new Color(img.getRGB (i, j)).getRed();
                hist[red]++;
            }
        }
        return hist;
    }
    
    
    //Получить пороговое значения для бинеаризиции
    private static int treshold(BufferedImage img) {
 
        int[] histogram = getHistogram(img);
        int total = img.getHeight() * img.getWidth();
 
        float sum = 0;
        for(int i=0; i<256; i++) sum += i * histogram[i];
 
        float sumB = 0;
        int wB = 0;
        int wF = 0;
 
        float varMax = 0;
        int threshold = 0;
 
        for(int i=0 ; i<256 ; i++) {
            wB += histogram[i];
            if(wB == 0) continue;
            wF = total - wB;
 
            if(wF == 0) break;
 
            sumB += (float) (i * histogram[i]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;
 
            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);
 
            if(varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }
 
        return threshold;
 
    }
 
    public static BufferedImage binarize(BufferedImage img) {
 
        int red;
        int newPixel;
        
        img = grayscale(img);
 
        int threshold = treshold(img);
 
        BufferedImage binarized = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
 
        for(int i=0; i<img.getWidth(); i++) {
            for(int j=0; j<img.getHeight(); j++) {

                red = new Color(img.getRGB(i, j)).getRed();
                int alpha = new Color(img.getRGB(i, j)).getAlpha();
                if(red > threshold) {
                    newPixel = 255;
                }
                else {
                    newPixel = 0;
                }
                newPixel = colorToRGB(alpha, newPixel, newPixel, newPixel);
                binarized.setRGB(i, j, newPixel); 
 
            }
        }
 
        return binarized;
 
    }
 
    private static int colorToRGB(int alpha, int red, int green, int blue) {
 
        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red; newPixel = newPixel << 8;
        newPixel += green; newPixel = newPixel << 8;
        newPixel += blue;
 
        return newPixel;
 
    }
 
}