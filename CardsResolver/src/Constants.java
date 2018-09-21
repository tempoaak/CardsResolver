
public interface Constants {
    //по умолчанию, если не указана папка с картинками, используем текущую
	public static final String DEFAULT_INPUT_DIR = System.getProperty("user.dir");
	//Папка с шаблонами рангов карт (предварительлно обработаны toGray и binarize )
	public static final String RANK_TEMPL_DIR = "RankTemplates//";
	//Папка с шаблонами мастей карт (предварительлно обработаны toGray и binarize )
	public static final String SUITS_TEMPL_DIR = "SuitsTemplates//";
	public static final int[] ARRAY_RGB_OF_SHADOW_CARD_SURFACE = {120,120,120};
	public static final int[] ARRAY_RGB_OF_NORMAL_CARD_SURFACE  = {255,255,255};
	public static final int IMAGE_WIDTH = 636;
	public static final int IMAGE_HEIGHT = 1166;
	public static final int ALLOW_RANK_SCALE = 17;
	public static final int ALLOW_SUITS_SCALE = 7;
	//Это координаты начала каждой карты
	public static final int[] ARRAY_OF_CARD_POSITION_X = {142, 214, 285, 357, 428};
	public static final int OFFSET_OF_CARD_RANK_X = 11;
	public static final int OFFSET_OF_CARD_SUITS_X = 28;
	public static final int CARD_POSITION_RANK_Y = 592;
	public static final int CARD_POSITION_SUITS_Y = 634;
}
