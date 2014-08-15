/*
ImageBank.java
James Porter

An object to contain all the images drawn in the Jned program, providing index-based access to them
*/

import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class ImageBank {
	private BufferedImage[] images;
								//OFFSETS FOR ITEM GRAPHICS from coordinate origin of item
								//	Gau	Hom	Min	Flo	Thw	-	-	-	Zap	-	-	-	See	-	-	-	Las	-	-	-	Cha	-	-	-
								//	Pla	Gol	Bou	Edr	Esw	Owy	-	-	-	ndr	-	-	-	ldr	-	-	-	sw	tdr	-	-	-	sw	lch	-	-	-	-	-	-	-	
	private int[]			xs = {	-6,	-6,	-4,	-6,	-10,-10,-10,-10,-9,	-9,	-9,	-9,	-9,	-9,	-9,	-9,	-9,	-9,	-9,	-9,	-9,	-9,	-9,	-9,
									-4,	-3,	-10,-13,-6,	7,	-12,-12,-12,-2,	-12,0,	-12,22,	0,	0,	0,	-4,	19,	0,	0,	0,	-3,	0,	-9,	-7,	-9,	-5,	-9,	-7,	-9},								
							ys = {	-6,	-6,	-4,	-6,	-10,-10,-10,-10,-9,	-9,	-9,	-9,	-15,-15,-15,-15,-9,	-9,	-9,	-9,	-9,	-9,	-9,	-9,
									-11,-3,	-10,-12,-4,	-12,7,	-12,-12,-12,-2,	-12,0,	0,	22,	0,	0,	-4,	0,	19,	0,	0,	-3,	-7,	-9,	0,	-9,	-7,	-9,	-5,	-9};
	//FIELDS FOR INDICES
							//Level area
								//Enemies
	public static final int		GAUSS = 0,
								HOMING = 1,
								MINE = 2,
								FLOOR = 3,
								THWUMP = 4, 	//4 directions
								ZAP = 8,		//4 directions
								SEEKER = 12,	//4 directions
								LASER = 16,		//4 directions
								CHAINGUN = 20,	//4 directions
								
								//Items
								PLAYER = 24,
								GOLD = 25,
								BOUNCE = 26,
								EXIT = 27,		//Door/switch
								ONEWAY = 29,	//4 directions
								NDOOR = 33,		//4 directions
								LDOOR = 37,		//4 directions, switch
								TDOOR = 42,		//4 directions, switch
								LAUNCH = 47,	//8 directions
								
								//Tiles
								TILE45 = 55,	//4 directions
								THIN63 = 59,	//4 directions
								THIN27 = 63,	//4 directions
								CONCAVE = 67,	//4 directions
								HALF = 71,		//4 directions
								THICK63 = 75,	//4 directions
								THICK27 = 79,	//4 directions
								CONVEX = 83,	//4 directions
								FILL = 87,
								ERASE = 88,
								
							//Buttons			//Every button has a pushed and unpushed state
								//Enemies
								BT_GAUSS = 89,
								BT_HOMING = 91,
								BT_MINE = 93,
								BT_FLOOR = 95,
								BT_THWUMP = 97, 	//4 directions
								BT_ZAP = 105,		//6 behaviors, 4 directions
								BT_SEEKER = 125,	//6 behaviors, 4 directions
								BT_LASER = 145,		//6 behaviors, 4 directions
								BT_CHAINGUN = 165,	//6 behaviors, 4 directions
								
								//Items
								BT_PLAYER = 185,
								BT_GOLD = 187,
								BT_BOUNCE = 189,
								BT_EXIT = 191,		
								BT_ONEWAY = 193,	//4 directions
								BT_NDOOR = 201,		//4 directions
								BT_LDOOR = 209,		//4 directions, super
								BT_TDOOR = 219,		//4 directions, super
								BT_LAUNCH = 229,	//8 directions, teleporter
								
								//Tiles
								BT_TILE45 = 247,	//4 directions
								BT_THIN63 = 255,	//4 directions
								BT_THIN27 = 263,	//4 directions
								BT_CONCAVE = 271,	//4 directions
								BT_HALF = 279,		//4 directions
								BT_THICK63 = 287,	//4 directions
								BT_THICK27 = 295,	//4 directions
								BT_CONVEX = 303,	//4 directions
								BT_FILL = 311,
								BT_ERASE = 313,
								
							//Other
								BT_DOWN_ARROW = 315;
	public ImageBank () {
		String	itemEnemies = "images/enemies/",
				itemItems = "images/items/",
				itemTiles = "images/tiles/",
				button = "images/buttons/",
				buttonEnemies = "images/buttons/enemies/",
				buttonItems = "images/buttons/items/",
				buttonTiles = "images/buttons/tiles/";
				
		images = new BufferedImage[317];
	//Level area
		//Enemies
		try {images[ImageBank.GAUSS] = ImageIO.read(new File(itemEnemies + "Gaussturret.gif"));} catch (IOException e) {}
		try {images[ImageBank.HOMING] = ImageIO.read(new File(itemEnemies + "Hominglauncher.gif"));} catch (IOException e) {}
		try {images[ImageBank.MINE] = ImageIO.read(new File(itemEnemies + "Mine.gif"));} catch (IOException e) {}
		try {images[ImageBank.FLOOR] = ImageIO.read(new File(itemEnemies + "Floorguard.gif"));} catch (IOException e) {}
		try {images[ImageBank.THWUMP] = ImageIO.read(new File(itemEnemies + "Thwump/ThwumpD.gif"));} catch (IOException e) {}
		try {images[ImageBank.THWUMP+1] = ImageIO.read(new File(itemEnemies + "Thwump/ThwumpS.gif"));} catch (IOException e) {}
		try {images[ImageBank.THWUMP+2] = ImageIO.read(new File(itemEnemies + "Thwump/ThwumpA.gif"));} catch (IOException e) {}
		try {images[ImageBank.THWUMP+3] = ImageIO.read(new File(itemEnemies + "Thwump/ThwumpW.gif"));} catch (IOException e) {}
		try {images[ImageBank.ZAP] = ImageIO.read(new File(itemEnemies + "Zapdrone/ZapdroneD.gif"));} catch (IOException e) {}
		try {images[ImageBank.ZAP+1] = ImageIO.read(new File(itemEnemies + "Zapdrone/ZapdroneS.gif"));} catch (IOException e) {}
		try {images[ImageBank.ZAP+2] = ImageIO.read(new File(itemEnemies + "Zapdrone/ZapdroneA.gif"));} catch (IOException e) {}
		try {images[ImageBank.ZAP+3] = ImageIO.read(new File(itemEnemies + "Zapdrone/ZapdroneW.gif"));} catch (IOException e) {}
		try {images[ImageBank.SEEKER] = ImageIO.read(new File(itemEnemies + "Seekerdrone/SeekerdroneD.gif"));} catch (IOException e) {}
		try {images[ImageBank.SEEKER+1] = ImageIO.read(new File(itemEnemies + "Seekerdrone/SeekerdroneS.gif"));} catch (IOException e) {}
		try {images[ImageBank.SEEKER+2] = ImageIO.read(new File(itemEnemies + "Seekerdrone/SeekerdroneA.gif"));} catch (IOException e) {}
		try {images[ImageBank.SEEKER+3] = ImageIO.read(new File(itemEnemies + "Seekerdrone/SeekerdroneW.gif"));} catch (IOException e) {}
		try {images[ImageBank.LASER] = ImageIO.read(new File(itemEnemies + "Laserdrone/LaserdroneD.gif"));} catch (IOException e) {}
		try {images[ImageBank.LASER+1] = ImageIO.read(new File(itemEnemies + "Laserdrone/LaserdroneS.gif"));} catch (IOException e) {}
		try {images[ImageBank.LASER+2] = ImageIO.read(new File(itemEnemies + "Laserdrone/LaserdroneA.gif"));} catch (IOException e) {}
		try {images[ImageBank.LASER+3] = ImageIO.read(new File(itemEnemies + "Laserdrone/LaserdroneW.gif"));} catch (IOException e) {}
		try {images[ImageBank.CHAINGUN] = ImageIO.read(new File(itemEnemies + "Chaingundrone/ChaingundroneD.gif"));} catch (IOException e) {}
		try {images[ImageBank.CHAINGUN+1] = ImageIO.read(new File(itemEnemies + "Chaingundrone/ChaingundroneS.gif"));} catch (IOException e) {}
		try {images[ImageBank.CHAINGUN+2] = ImageIO.read(new File(itemEnemies + "Chaingundrone/ChaingundroneA.gif"));} catch (IOException e) {}
		try {images[ImageBank.CHAINGUN+3] = ImageIO.read(new File(itemEnemies + "Chaingundrone/ChaingundroneW.gif"));} catch (IOException e) {}
		
		//Items
		try {images[ImageBank.PLAYER] = ImageIO.read(new File(itemItems + "Player.gif"));} catch (IOException e) {}
		try {images[ImageBank.GOLD] = ImageIO.read(new File(itemItems + "Gold.gif"));} catch (IOException e) {}
		try {images[ImageBank.BOUNCE] = ImageIO.read(new File(itemItems + "Bounceblock.gif"));} catch (IOException e) {}
		try {images[ImageBank.EXIT] = ImageIO.read(new File(itemItems + "Exit/Exitdoor.gif"));} catch (IOException e) {}
		try {images[ImageBank.EXIT+1] = ImageIO.read(new File(itemItems + "Exit/Exitswitch.gif"));} catch (IOException e) {}
		try {images[ImageBank.ONEWAY] = ImageIO.read(new File(itemItems + "Oneway/OnewayD.gif"));} catch (IOException e) {}
		try {images[ImageBank.ONEWAY+1] = ImageIO.read(new File(itemItems + "Oneway/OnewayS.gif"));} catch (IOException e) {}
		try {images[ImageBank.ONEWAY+2] = ImageIO.read(new File(itemItems + "Oneway/OnewayA.gif"));} catch (IOException e) {}
		try {images[ImageBank.ONEWAY+3] = ImageIO.read(new File(itemItems + "Oneway/OnewayW.gif"));} catch (IOException e) {}
		try {images[ImageBank.NDOOR] = ImageIO.read(new File(itemItems + "Normaldoor/NormaldoorD.gif"));} catch (IOException e) {}
		try {images[ImageBank.NDOOR+1] = ImageIO.read(new File(itemItems + "Normaldoor/NormaldoorS.gif"));} catch (IOException e) {}
		try {images[ImageBank.NDOOR+2] = ImageIO.read(new File(itemItems + "Normaldoor/NormaldoorA.gif"));} catch (IOException e) {}
		try {images[ImageBank.NDOOR+3] = ImageIO.read(new File(itemItems + "Normaldoor/NormaldoorW.gif"));} catch (IOException e) {}
		try {images[ImageBank.LDOOR] = ImageIO.read(new File(itemItems + "Lockeddoor/LockeddoorD.gif"));} catch (IOException e) {}
		try {images[ImageBank.LDOOR+1] = ImageIO.read(new File(itemItems + "Lockeddoor/LockeddoorS.gif"));} catch (IOException e) {}
		try {images[ImageBank.LDOOR+2] = ImageIO.read(new File(itemItems + "Lockeddoor/LockeddoorA.gif"));} catch (IOException e) {}
		try {images[ImageBank.LDOOR+3] = ImageIO.read(new File(itemItems + "Lockeddoor/LockeddoorW.gif"));} catch (IOException e) {}
		try {images[ImageBank.LDOOR+4] = ImageIO.read(new File(itemItems + "Lockeddoor/Lockeddoorswitch.gif"));} catch (IOException e) {}
		try {images[ImageBank.TDOOR] = ImageIO.read(new File(itemItems + "Trapdoor/TrapdoorD.gif"));} catch (IOException e) {}
		try {images[ImageBank.TDOOR+1] = ImageIO.read(new File(itemItems + "Trapdoor/TrapdoorS.gif"));} catch (IOException e) {}
		try {images[ImageBank.TDOOR+2] = ImageIO.read(new File(itemItems + "Trapdoor/TrapdoorA.gif"));} catch (IOException e) {}
		try {images[ImageBank.TDOOR+3] = ImageIO.read(new File(itemItems + "Trapdoor/TrapdoorW.gif"));} catch (IOException e) {}
		try {images[ImageBank.TDOOR+4] = ImageIO.read(new File(itemItems + "Trapdoor/Trapdoorswitch.gif"));} catch (IOException e) {}
		try {images[ImageBank.LAUNCH] = ImageIO.read(new File(itemItems + "Launchpad/LaunchpadD.gif"));} catch (IOException e) {}
		try {images[ImageBank.LAUNCH+1] = ImageIO.read(new File(itemItems + "Launchpad/LaunchpadDS.gif"));} catch (IOException e) {}
		try {images[ImageBank.LAUNCH+2] = ImageIO.read(new File(itemItems + "Launchpad/LaunchpadS.gif"));} catch (IOException e) {}
		try {images[ImageBank.LAUNCH+3] = ImageIO.read(new File(itemItems + "Launchpad/LaunchpadAS.gif"));} catch (IOException e) {}
		try {images[ImageBank.LAUNCH+4] = ImageIO.read(new File(itemItems + "Launchpad/LaunchpadA.gif"));} catch (IOException e) {}
		try {images[ImageBank.LAUNCH+5] = ImageIO.read(new File(itemItems + "Launchpad/LaunchpadAW.gif"));} catch (IOException e) {}
		try {images[ImageBank.LAUNCH+6] = ImageIO.read(new File(itemItems + "Launchpad/LaunchpadW.gif"));} catch (IOException e) {}
		try {images[ImageBank.LAUNCH+7] = ImageIO.read(new File(itemItems + "Launchpad/LaunchpadDW.gif"));} catch (IOException e) {}
		
		//Tiles
		try {images[ImageBank.TILE45] = ImageIO.read(new File(itemTiles + "45tile/45tileW.gif"));} catch (IOException e) {}
		try {images[ImageBank.TILE45+1] = ImageIO.read(new File(itemTiles + "45tile/45tileQ.gif"));} catch (IOException e) {}
		try {images[ImageBank.TILE45+2] = ImageIO.read(new File(itemTiles + "45tile/45tileA.gif"));} catch (IOException e) {}
		try {images[ImageBank.TILE45+3] = ImageIO.read(new File(itemTiles + "45tile/45tileS.gif"));} catch (IOException e) {}
		try {images[ImageBank.THIN63] = ImageIO.read(new File(itemTiles + "63thintile/63thintileW.gif"));} catch (IOException e) {}
		try {images[ImageBank.THIN63+1] = ImageIO.read(new File(itemTiles + "63thintile/63thintileQ.gif"));} catch (IOException e) {}
		try {images[ImageBank.THIN63+2] = ImageIO.read(new File(itemTiles + "63thintile/63thintileA.gif"));} catch (IOException e) {}
		try {images[ImageBank.THIN63+3] = ImageIO.read(new File(itemTiles + "63thintile/63thintileS.gif"));} catch (IOException e) {}
		try {images[ImageBank.THIN27] = ImageIO.read(new File(itemTiles + "27thintile/27thintileW.gif"));} catch (IOException e) {}
		try {images[ImageBank.THIN27+1] = ImageIO.read(new File(itemTiles + "27thintile/27thintileQ.gif"));} catch (IOException e) {}
		try {images[ImageBank.THIN27+2] = ImageIO.read(new File(itemTiles + "27thintile/27thintileA.gif"));} catch (IOException e) {}
		try {images[ImageBank.THIN27+3] = ImageIO.read(new File(itemTiles + "27thintile/27thintileS.gif"));} catch (IOException e) {}
		try {images[ImageBank.CONCAVE] = ImageIO.read(new File(itemTiles + "Concavetile/ConcavetileW.gif"));} catch (IOException e) {}
		try {images[ImageBank.CONCAVE+1] = ImageIO.read(new File(itemTiles + "Concavetile/ConcavetileQ.gif"));} catch (IOException e) {}
		try {images[ImageBank.CONCAVE+2] = ImageIO.read(new File(itemTiles + "Concavetile/ConcavetileA.gif"));} catch (IOException e) {}
		try {images[ImageBank.CONCAVE+3] = ImageIO.read(new File(itemTiles + "Concavetile/ConcavetileS.gif"));} catch (IOException e) {}
		try {images[ImageBank.HALF] = ImageIO.read(new File(itemTiles + "Halftile/HalftileW.gif"));} catch (IOException e) {}
		try {images[ImageBank.HALF+1] = ImageIO.read(new File(itemTiles + "Halftile/HalftileQ.gif"));} catch (IOException e) {}
		try {images[ImageBank.HALF+2] = ImageIO.read(new File(itemTiles + "Halftile/HalftileA.gif"));} catch (IOException e) {}
		try {images[ImageBank.HALF+3] = ImageIO.read(new File(itemTiles + "Halftile/HalftileS.gif"));} catch (IOException e) {}
		try {images[ImageBank.THICK63] = ImageIO.read(new File(itemTiles + "63thicktile/63thicktileW.gif"));} catch (IOException e) {}
		try {images[ImageBank.THICK63+1] = ImageIO.read(new File(itemTiles + "63thicktile/63thicktileQ.gif"));} catch (IOException e) {}
		try {images[ImageBank.THICK63+2] = ImageIO.read(new File(itemTiles + "63thicktile/63thicktileA.gif"));} catch (IOException e) {}
		try {images[ImageBank.THICK63+3] = ImageIO.read(new File(itemTiles + "63thicktile/63thicktileS.gif"));} catch (IOException e) {}
		try {images[ImageBank.THICK27] = ImageIO.read(new File(itemTiles + "27thicktile/27thicktileW.gif"));} catch (IOException e) {}
		try {images[ImageBank.THICK27+1] = ImageIO.read(new File(itemTiles + "27thicktile/27thicktileQ.gif"));} catch (IOException e) {}
		try {images[ImageBank.THICK27+2] = ImageIO.read(new File(itemTiles + "27thicktile/27thicktileA.gif"));} catch (IOException e) {}
		try {images[ImageBank.THICK27+3] = ImageIO.read(new File(itemTiles + "27thicktile/27thicktileS.gif"));} catch (IOException e) {}
		try {images[ImageBank.CONVEX] = ImageIO.read(new File(itemTiles + "Convextile/ConvextileW.gif"));} catch (IOException e) {}
		try {images[ImageBank.CONVEX+1] = ImageIO.read(new File(itemTiles + "Convextile/ConvextileQ.gif"));} catch (IOException e) {}
		try {images[ImageBank.CONVEX+2] = ImageIO.read(new File(itemTiles + "Convextile/ConvextileA.gif"));} catch (IOException e) {}
		try {images[ImageBank.CONVEX+3] = ImageIO.read(new File(itemTiles + "Convextile/ConvextileS.gif"));} catch (IOException e) {}
		try {images[ImageBank.FILL] = ImageIO.read(new File(itemTiles + "Fill.gif"));} catch (IOException e) {}
		try {images[ImageBank.ERASE] = ImageIO.read(new File(itemTiles + "Erase.gif"));} catch (IOException e) {}

		
	//Buttons
		//Enemies
		try {images[ImageBank.BT_GAUSS] = ImageIO.read(new File(buttonEnemies + "Gaussturret.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_GAUSS+1] = ImageIO.read(new File(buttonEnemies + "Gaussturretpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_HOMING] = ImageIO.read(new File(buttonEnemies + "Hominglauncher.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_HOMING+1] = ImageIO.read(new File(buttonEnemies + "Hominglauncherpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_MINE] = ImageIO.read(new File(buttonEnemies + "Mine.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_MINE+1] = ImageIO.read(new File(buttonEnemies + "Minepushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_FLOOR] = ImageIO.read(new File(buttonEnemies + "Floorguard.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_FLOOR+1] = ImageIO.read(new File(buttonEnemies + "Floorguardpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THWUMP] = ImageIO.read(new File(buttonEnemies + "Thwump/ThwumpD.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THWUMP+1] = ImageIO.read(new File(buttonEnemies + "Thwump/ThwumpDpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THWUMP+2] = ImageIO.read(new File(buttonEnemies + "Thwump/ThwumpS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THWUMP+3] = ImageIO.read(new File(buttonEnemies + "Thwump/ThwumpSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THWUMP+4] = ImageIO.read(new File(buttonEnemies + "Thwump/ThwumpA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THWUMP+5] = ImageIO.read(new File(buttonEnemies + "Thwump/ThwumpApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THWUMP+6] = ImageIO.read(new File(buttonEnemies + "Thwump/ThwumpW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THWUMP+7] = ImageIO.read(new File(buttonEnemies + "Thwump/ThwumpWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP] = ImageIO.read(new File(buttonEnemies + "Zapdrone/ZapdroneD.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+1] = ImageIO.read(new File(buttonEnemies + "Zapdrone/ZapdroneDpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+2] = ImageIO.read(new File(buttonEnemies + "Zapdrone/ZapdroneS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+3] = ImageIO.read(new File(buttonEnemies + "Zapdrone/ZapdroneSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+4] = ImageIO.read(new File(buttonEnemies + "Zapdrone/ZapdroneA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+5] = ImageIO.read(new File(buttonEnemies + "Zapdrone/ZapdroneApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+6] = ImageIO.read(new File(buttonEnemies + "Zapdrone/ZapdroneW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+7] = ImageIO.read(new File(buttonEnemies + "Zapdrone/ZapdroneWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+8] = ImageIO.read(new File(buttonEnemies + "Zapdrone/Zapdronesurfacecw.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+9] = ImageIO.read(new File(buttonEnemies + "Zapdrone/Zapdronesurfacecwpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+10] = ImageIO.read(new File(buttonEnemies + "Zapdrone/Zapdronesurfaceccw.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+11] = ImageIO.read(new File(buttonEnemies + "Zapdrone/Zapdronesurfaceccwpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+12] = ImageIO.read(new File(buttonEnemies + "Zapdrone/Zapdronedumbcw.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+13] = ImageIO.read(new File(buttonEnemies + "Zapdrone/Zapdronedumbcwpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+14] = ImageIO.read(new File(buttonEnemies + "Zapdrone/Zapdronedumbccw.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+15] = ImageIO.read(new File(buttonEnemies + "Zapdrone/Zapdronedumbccwpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+16] = ImageIO.read(new File(buttonEnemies + "Zapdrone/Zapdronealt.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+17] = ImageIO.read(new File(buttonEnemies + "Zapdrone/Zapdronealtpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+18] = ImageIO.read(new File(buttonEnemies + "Zapdrone/Zapdronerand.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ZAP+19] = ImageIO.read(new File(buttonEnemies + "Zapdrone/Zapdronerandpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/SeekerdroneD.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+1] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/SeekerdroneDpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+2] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/SeekerdroneS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+3] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/SeekerdroneSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+4] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/SeekerdroneA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+5] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/SeekerdroneApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+6] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/SeekerdroneW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+7] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/SeekerdroneWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+8] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/Seekerdronesurfacecw.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+9] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/Seekerdronesurfacecwpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+10] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/Seekerdronesurfaceccw.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+11] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/Seekerdronesurfaceccwpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+12] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/Seekerdronedumbcw.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+13] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/Seekerdronedumbcwpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+14] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/Seekerdronedumbccw.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+15] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/Seekerdronedumbccwpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+16] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/Seekerdronealt.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+17] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/Seekerdronealtpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+18] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/Seekerdronerand.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_SEEKER+19] = ImageIO.read(new File(buttonEnemies + "Seekerdrone/Seekerdronerandpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER] = ImageIO.read(new File(buttonEnemies + "Laserdrone/LaserdroneD.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+1] = ImageIO.read(new File(buttonEnemies + "Laserdrone/LaserdroneDpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+2] = ImageIO.read(new File(buttonEnemies + "Laserdrone/LaserdroneS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+3] = ImageIO.read(new File(buttonEnemies + "Laserdrone/LaserdroneSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+4] = ImageIO.read(new File(buttonEnemies + "Laserdrone/LaserdroneA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+5] = ImageIO.read(new File(buttonEnemies + "Laserdrone/LaserdroneApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+6] = ImageIO.read(new File(buttonEnemies + "Laserdrone/LaserdroneW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+7] = ImageIO.read(new File(buttonEnemies + "Laserdrone/LaserdroneWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+8] = ImageIO.read(new File(buttonEnemies + "Laserdrone/Laserdronesurfacecw.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+9] = ImageIO.read(new File(buttonEnemies + "Laserdrone/Laserdronesurfacecwpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+10] = ImageIO.read(new File(buttonEnemies + "Laserdrone/Laserdronesurfaceccw.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+11] = ImageIO.read(new File(buttonEnemies + "Laserdrone/Laserdronesurfaceccwpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+12] = ImageIO.read(new File(buttonEnemies + "Laserdrone/Laserdronedumbcw.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+13] = ImageIO.read(new File(buttonEnemies + "Laserdrone/Laserdronedumbcwpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+14] = ImageIO.read(new File(buttonEnemies + "Laserdrone/Laserdronedumbccw.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+15] = ImageIO.read(new File(buttonEnemies + "Laserdrone/Laserdronedumbccwpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+16] = ImageIO.read(new File(buttonEnemies + "Laserdrone/Laserdronealt.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+17] = ImageIO.read(new File(buttonEnemies + "Laserdrone/Laserdronealtpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+18] = ImageIO.read(new File(buttonEnemies + "Laserdrone/Laserdronerand.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LASER+19] = ImageIO.read(new File(buttonEnemies + "Laserdrone/Laserdronerandpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/ChaingundroneD.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+1] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/ChaingundroneDpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+2] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/ChaingundroneS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+3] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/ChaingundroneSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+4] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/ChaingundroneA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+5] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/ChaingundroneApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+6] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/ChaingundroneW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+7] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/ChaingundroneWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+8] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/Chaingundronesurfacecw.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+9] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/Chaingundronesurfacecwpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+10] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/Chaingundronesurfaceccw.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+11] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/Chaingundronesurfaceccwpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+12] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/Chaingundronedumbcw.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+13] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/Chaingundronedumbcwpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+14] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/Chaingundronedumbccw.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+15] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/Chaingundronedumbccwpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+16] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/Chaingundronealt.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+17] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/Chaingundronealtpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+18] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/Chaingundronerand.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CHAINGUN+19] = ImageIO.read(new File(buttonEnemies + "Chaingundrone/Chaingundronerandpushed.gif"));} catch (IOException e) {}
		
		//Items
		try {images[ImageBank.BT_PLAYER] = ImageIO.read(new File(buttonItems + "Player.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_PLAYER+1] = ImageIO.read(new File(buttonItems + "Playerpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_GOLD] = ImageIO.read(new File(buttonItems + "Gold.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_GOLD+1] = ImageIO.read(new File(buttonItems + "Goldpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_BOUNCE] = ImageIO.read(new File(buttonItems + "Bounceblock.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_BOUNCE+1] = ImageIO.read(new File(buttonItems + "Bounceblockpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_EXIT] = ImageIO.read(new File(buttonItems + "Exit/Exitdoor.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_EXIT+1] = ImageIO.read(new File(buttonItems + "Exit/Exitdoorpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ONEWAY] = ImageIO.read(new File(buttonItems + "Oneway/OnewayD.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ONEWAY+1] = ImageIO.read(new File(buttonItems + "Oneway/OnewayDpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ONEWAY+2] = ImageIO.read(new File(buttonItems + "Oneway/OnewayS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ONEWAY+3] = ImageIO.read(new File(buttonItems + "Oneway/OnewaySpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ONEWAY+4] = ImageIO.read(new File(buttonItems + "Oneway/OnewayA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ONEWAY+5] = ImageIO.read(new File(buttonItems + "Oneway/OnewayApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ONEWAY+6] = ImageIO.read(new File(buttonItems + "Oneway/OnewayW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ONEWAY+7] = ImageIO.read(new File(buttonItems + "Oneway/OnewayWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_NDOOR] = ImageIO.read(new File(buttonItems + "Normaldoor/NormaldoorD.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_NDOOR+1] = ImageIO.read(new File(buttonItems + "Normaldoor/NormaldoorDpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_NDOOR+2] = ImageIO.read(new File(buttonItems + "Normaldoor/NormaldoorS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_NDOOR+3] = ImageIO.read(new File(buttonItems + "Normaldoor/NormaldoorSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_NDOOR+4] = ImageIO.read(new File(buttonItems + "Normaldoor/NormaldoorA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_NDOOR+5] = ImageIO.read(new File(buttonItems + "Normaldoor/NormaldoorApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_NDOOR+6] = ImageIO.read(new File(buttonItems + "Normaldoor/NormaldoorW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_NDOOR+7] = ImageIO.read(new File(buttonItems + "Normaldoor/NormaldoorWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LDOOR] = ImageIO.read(new File(buttonItems + "Lockeddoor/LockeddoorD.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LDOOR+1] = ImageIO.read(new File(buttonItems + "Lockeddoor/LockeddoorDpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LDOOR+2] = ImageIO.read(new File(buttonItems + "Lockeddoor/LockeddoorS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LDOOR+3] = ImageIO.read(new File(buttonItems + "Lockeddoor/LockeddoorSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LDOOR+4] = ImageIO.read(new File(buttonItems + "Lockeddoor/LockeddoorA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LDOOR+5] = ImageIO.read(new File(buttonItems + "Lockeddoor/LockeddoorApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LDOOR+6] = ImageIO.read(new File(buttonItems + "Lockeddoor/LockeddoorW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LDOOR+7] = ImageIO.read(new File(buttonItems + "Lockeddoor/LockeddoorWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LDOOR+8] = ImageIO.read(new File(buttonItems + "Lockeddoor/Lockeddoor.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LDOOR+9] = ImageIO.read(new File(buttonItems + "Lockeddoor/Lockeddoorpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_TDOOR] = ImageIO.read(new File(buttonItems + "Trapdoor/TrapdoorD.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_TDOOR+1] = ImageIO.read(new File(buttonItems + "Trapdoor/TrapdoorDpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_TDOOR+2] = ImageIO.read(new File(buttonItems + "Trapdoor/TrapdoorS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_TDOOR+3] = ImageIO.read(new File(buttonItems + "Trapdoor/TrapdoorSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_TDOOR+4] = ImageIO.read(new File(buttonItems + "Trapdoor/TrapdoorA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_TDOOR+5] = ImageIO.read(new File(buttonItems + "Trapdoor/TrapdoorApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_TDOOR+6] = ImageIO.read(new File(buttonItems + "Trapdoor/TrapdoorW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_TDOOR+7] = ImageIO.read(new File(buttonItems + "Trapdoor/TrapdoorWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_TDOOR+8] = ImageIO.read(new File(buttonItems + "Trapdoor/Trapdoor.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_TDOOR+9] = ImageIO.read(new File(buttonItems + "Trapdoor/Trapdoorpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH] = ImageIO.read(new File(buttonItems + "Launchpad/LaunchpadD.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH+1] = ImageIO.read(new File(buttonItems + "Launchpad/LaunchpadDpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH+2] = ImageIO.read(new File(buttonItems + "Launchpad/LaunchpadDS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH+3] = ImageIO.read(new File(buttonItems + "Launchpad/LaunchpadDSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH+4] = ImageIO.read(new File(buttonItems + "Launchpad/LaunchpadS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH+5] = ImageIO.read(new File(buttonItems + "Launchpad/LaunchpadSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH+6] = ImageIO.read(new File(buttonItems + "Launchpad/LaunchpadAS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH+7] = ImageIO.read(new File(buttonItems + "Launchpad/LaunchpadASpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH+8] = ImageIO.read(new File(buttonItems + "Launchpad/LaunchpadA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH+9] = ImageIO.read(new File(buttonItems + "Launchpad/LaunchpadApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH+10] = ImageIO.read(new File(buttonItems + "Launchpad/LaunchpadAW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH+11] = ImageIO.read(new File(buttonItems + "Launchpad/LaunchpadAWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH+12] = ImageIO.read(new File(buttonItems + "Launchpad/LaunchpadW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH+13] = ImageIO.read(new File(buttonItems + "Launchpad/LaunchpadWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH+14] = ImageIO.read(new File(buttonItems + "Launchpad/LaunchpadDW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH+15] = ImageIO.read(new File(buttonItems + "Launchpad/LaunchpadDWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH+16] = ImageIO.read(new File(buttonItems + "Launchpad/Teleporter.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_LAUNCH+17] = ImageIO.read(new File(buttonItems + "Launchpad/Teleporterpushed.gif"));} catch (IOException e) {}
		
		//Tiles
		try {images[ImageBank.BT_TILE45] = ImageIO.read(new File(buttonTiles + "45tile/45tileW.gif"));} catch (IOException e) {System.out.print("Nope. ");}
		try {images[ImageBank.BT_TILE45+1] = ImageIO.read(new File(buttonTiles + "45tile/45tileWpushed.gif"));} catch (IOException e) {System.out.print("Nope. ");}
		try {images[ImageBank.BT_TILE45+2] = ImageIO.read(new File(buttonTiles + "45tile/45tileS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_TILE45+3] = ImageIO.read(new File(buttonTiles + "45tile/45tileSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_TILE45+4] = ImageIO.read(new File(buttonTiles + "45tile/45tileA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_TILE45+5] = ImageIO.read(new File(buttonTiles + "45tile/45tileApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_TILE45+6] = ImageIO.read(new File(buttonTiles + "45tile/45tileQ.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_TILE45+7] = ImageIO.read(new File(buttonTiles + "45tile/45tileQpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THIN63] = ImageIO.read(new File(buttonTiles + "63thintile/63thintileW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THIN63+1] = ImageIO.read(new File(buttonTiles + "63thintile/63thintileWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THIN63+2] = ImageIO.read(new File(buttonTiles + "63thintile/63thintileS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THIN63+3] = ImageIO.read(new File(buttonTiles + "63thintile/63thintileSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THIN63+4] = ImageIO.read(new File(buttonTiles + "63thintile/63thintileA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THIN63+5] = ImageIO.read(new File(buttonTiles + "63thintile/63thintileApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THIN63+6] = ImageIO.read(new File(buttonTiles + "63thintile/63thintileQ.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THIN63+7] = ImageIO.read(new File(buttonTiles + "63thintile/63thintileQpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THIN27] = ImageIO.read(new File(buttonTiles + "27thintile/27thintileW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THIN27+1] = ImageIO.read(new File(buttonTiles + "27thintile/27thintileWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THIN27+2] = ImageIO.read(new File(buttonTiles + "27thintile/27thintileS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THIN27+3] = ImageIO.read(new File(buttonTiles + "27thintile/27thintileSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THIN27+4] = ImageIO.read(new File(buttonTiles + "27thintile/27thintileA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THIN27+5] = ImageIO.read(new File(buttonTiles + "27thintile/27thintileApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THIN27+6] = ImageIO.read(new File(buttonTiles + "27thintile/27thintileQ.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THIN27+7] = ImageIO.read(new File(buttonTiles + "27thintile/27thintileQpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CONCAVE] = ImageIO.read(new File(buttonTiles + "Concavetile/ConcavetileW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CONCAVE+1] = ImageIO.read(new File(buttonTiles + "Concavetile/ConcavetileWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CONCAVE+2] = ImageIO.read(new File(buttonTiles + "Concavetile/ConcavetileS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CONCAVE+3] = ImageIO.read(new File(buttonTiles + "Concavetile/ConcavetileSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CONCAVE+4] = ImageIO.read(new File(buttonTiles + "Concavetile/ConcavetileA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CONCAVE+5] = ImageIO.read(new File(buttonTiles + "Concavetile/ConcavetileApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CONCAVE+6] = ImageIO.read(new File(buttonTiles + "Concavetile/ConcavetileQ.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CONCAVE+7] = ImageIO.read(new File(buttonTiles + "Concavetile/ConcavetileQpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_HALF] = ImageIO.read(new File(buttonTiles + "Halftile/HalftileW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_HALF+1] = ImageIO.read(new File(buttonTiles + "Halftile/HalftileWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_HALF+2] = ImageIO.read(new File(buttonTiles + "Halftile/HalftileS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_HALF+3] = ImageIO.read(new File(buttonTiles + "Halftile/HalftileSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_HALF+4] = ImageIO.read(new File(buttonTiles + "Halftile/HalftileA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_HALF+5] = ImageIO.read(new File(buttonTiles + "Halftile/HalftileApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_HALF+6] = ImageIO.read(new File(buttonTiles + "Halftile/HalftileQ.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_HALF+7] = ImageIO.read(new File(buttonTiles + "Halftile/HalftileQpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THICK63] = ImageIO.read(new File(buttonTiles + "63thicktile/63thicktileW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THICK63+1] = ImageIO.read(new File(buttonTiles + "63thicktile/63thicktileWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THICK63+2] = ImageIO.read(new File(buttonTiles + "63thicktile/63thicktileS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THICK63+3] = ImageIO.read(new File(buttonTiles + "63thicktile/63thicktileSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THICK63+4] = ImageIO.read(new File(buttonTiles + "63thicktile/63thicktileA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THICK63+5] = ImageIO.read(new File(buttonTiles + "63thicktile/63thicktileApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THICK63+6] = ImageIO.read(new File(buttonTiles + "63thicktile/63thicktileQ.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THICK63+7] = ImageIO.read(new File(buttonTiles + "63thicktile/63thicktileQpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THICK27] = ImageIO.read(new File(buttonTiles + "27thicktile/27thicktileW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THICK27+1] = ImageIO.read(new File(buttonTiles + "27thicktile/27thicktileWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THICK27+2] = ImageIO.read(new File(buttonTiles + "27thicktile/27thicktileS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THICK27+3] = ImageIO.read(new File(buttonTiles + "27thicktile/27thicktileSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THICK27+4] = ImageIO.read(new File(buttonTiles + "27thicktile/27thicktileA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THICK27+5] = ImageIO.read(new File(buttonTiles + "27thicktile/27thicktileApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THICK27+6] = ImageIO.read(new File(buttonTiles + "27thicktile/27thicktileQ.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_THICK27+7] = ImageIO.read(new File(buttonTiles + "27thicktile/27thicktileQpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CONVEX] = ImageIO.read(new File(buttonTiles + "Convextile/ConvextileW.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CONVEX+1] = ImageIO.read(new File(buttonTiles + "Convextile/ConvextileWpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CONVEX+2] = ImageIO.read(new File(buttonTiles + "Convextile/ConvextileS.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CONVEX+3] = ImageIO.read(new File(buttonTiles + "Convextile/ConvextileSpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CONVEX+4] = ImageIO.read(new File(buttonTiles + "Convextile/ConvextileA.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CONVEX+5] = ImageIO.read(new File(buttonTiles + "Convextile/ConvextileApushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CONVEX+6] = ImageIO.read(new File(buttonTiles + "Convextile/ConvextileQ.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_CONVEX+7] = ImageIO.read(new File(buttonTiles + "Convextile/ConvextileQpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_FILL] = ImageIO.read(new File(buttonTiles + "Fill.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_FILL+1] = ImageIO.read(new File(buttonTiles + "Fillpushed.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ERASE] = ImageIO.read(new File(buttonTiles + "Erase.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_ERASE+1] = ImageIO.read(new File(buttonTiles + "Erasepushed.gif"));} catch (IOException e) {}
		
	//Other
		try {images[ImageBank.BT_DOWN_ARROW] = ImageIO.read(new File(button + "Downarrow.gif"));} catch (IOException e) {}
		try {images[ImageBank.BT_DOWN_ARROW+1] = ImageIO.read(new File(button + "Downarrowpushed.gif"));} catch (IOException e) {}
	}
	
	public BufferedImage get(int index) {
		if(index >= 0 && index < images.length) {
			return images[index];
		}
		return null;
	}
	public int getXoff(int index) {
		if(index >= 0 && index < xs.length) {
			return xs[index];
		}
		return 0;
	}
	public int getYoff(int index) {
		if(index >= 0 && index < ys.length) {
			return ys[index];
		}
		return 0;
	}
}