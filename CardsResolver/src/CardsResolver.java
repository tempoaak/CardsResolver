import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

public class CardsResolver {
    private static Map<String, BufferedImage> mapRankTempls = new HashMap<>();
    private static Map<String, BufferedImage> mapSuitsTempls = new HashMap<>();
    //Всего обнаружено карт во всех найденных файлах
    private static int totalFoundCards = 0;
    //Всего обнаружено карт во всех найденных файлах
    private static int totalNotFoundCards = 0;    
    //Функция получения Map с соответствующими шаблонами
    private static Map<String, BufferedImage> loadTemplates(String dir)  {
		File dirTempls = new File(dir);
		File[] filesTempls = dirTempls.listFiles((d, n) -> n.endsWith(".png"));
		Map<String, BufferedImage> mapTempls = 
				    Stream.of(filesTempls).
		                      collect(Collectors.
		                    		  toMap(f -> f.getName().replaceFirst("[.][^.]+$", ""),
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
		return mapTempls;
    } 
 
    public static void main(String[] args) throws IOException {
    	mapRankTempls = loadTemplates(Constants.RANK_TEMPL_DIR);
        if (mapRankTempls.isEmpty()) {
        	System.out.println("Отсутствуют шаблоны рангов карт ");
        	System.exit(0);
        };
        mapSuitsTempls = loadTemplates(Constants.SUITS_TEMPL_DIR);
        if (mapRankTempls.isEmpty()) {
        	System.out.println("Отсутствуют шаблоны мастей карт");
        	System.exit(0);
        }        
        String dirScan = args.length == 0? Constants.DEFAULT_INPUT_DIR
        		                         : Optional.ofNullable(args[0]).orElse(Constants.DEFAULT_INPUT_DIR);
        File[] arrlf = new File(dirScan).listFiles((d, n) -> n.endsWith(".png"));
        for (File inFile : arrlf) {
    		BufferedImage img1 = ImageIO.read(inFile);
            if (img1.getWidth()!= Constants.IMAGE_WIDTH || img1.getHeight() != Constants.IMAGE_HEIGHT){
            	System.out.println("Неверный размерисходного изображения в файле "+inFile.getName());
            	continue;
            }
            //массивы для хранения рангов и мастей
    		String[] foundRank = {"?","?","?","?","?"};
    		String[] foundSuits = {"?","?","?","?","?"};
                for(int cardNum = 0; cardNum < Constants.ARRAY_OF_CARD_POSITION_X.length; cardNum++) {
                	//обработка ранга карты
            		for(Map.Entry<String, BufferedImage> entry : mapRankTempls.entrySet()) {
                        BufferedImage img2 = entry.getValue(); 
                        int pd = ImageService.getCompareIndexOfSubImages(img1, img2, 
                        		Constants.ARRAY_OF_CARD_POSITION_X[cardNum]+Constants.OFFSET_OF_CARD_RANK_X,
                        		Constants.CARD_POSITION_RANK_Y, entry.getKey());
		                if (pd <= Constants.ALLOW_RANK_SCALE){
		                	foundRank[cardNum] = entry.getKey(); 
		                }
            		}
            		//обработка масти карты
            		for(Map.Entry<String, BufferedImage> entry : mapSuitsTempls.entrySet()) {
                        BufferedImage img2 = entry.getValue(); 
                        int pd = ImageService.getCompareIndexOfSubImages(img1, img2, 
                        		Constants.ARRAY_OF_CARD_POSITION_X[cardNum]+Constants.OFFSET_OF_CARD_SUITS_X,
                        		Constants.CARD_POSITION_SUITS_Y, entry.getKey());
                        //File outputfile = new File(inFile.getName()+cardNum+".gray.png");
		        		//ImageIO.write(img11, "png", outputfile);
		                if (pd <= Constants.ALLOW_SUITS_SCALE){
		                	foundSuits[cardNum] = entry.getKey();
		                	//Так масти распознаются точнее, то количество карт считаем по дайденным мастям
		                	totalFoundCards++;
		                }
            		}
                }
            //Собираем строку из массивов найденных рангов и мастей
            //Если распознать ранг карты не удалось в выходной строке будет знак вопроса
            String res = IntStream.range(0, foundRank.length)
            		              .mapToObj(i->foundRank[i]+foundSuits[i])
            		              .collect(Collectors.joining())
            		              .replace("??", "");
            //Считаем нераспознанные карты
            totalNotFoundCards += res.chars().filter(ch -> ch == '?').count(); 
            //Выводим информацию о распознанных картах по данному файлу
            System.out.println(inFile.getName()+ " - " + res);

		}
        System.out.println();
        System.out.printf("Общее количество распознанных карт - %d \n", totalFoundCards);
        System.out.printf("Общее количество нераспознанных карт - %d \n", totalNotFoundCards);
        System.out.println("Завершение работы.");
    }

}
