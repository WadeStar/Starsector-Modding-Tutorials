package data.scripts;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.util.Misc;

import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;

public class MaSModPlugin extends BaseModPlugin {
    @Override
    public void onNewGame() {	
		SectorAPI sector = Global.getSector();
		StarSystemAPI system = sector.createStarSystem("Wade's Star");
		
		//It's by no means necessary to set a background to your star system.
		//However, let's see how to do it.
		//If you prefer a non-descript star field, simply disable this line.
		system.setBackgroundTextureFilename("graphics/mas/backgrounds/test_background.jpg");

		//We'll use this particular API to instantiate the star.
		//It allows us to set the position without having to also maintain an 
		//external file to set the hyperspace position.
		//This is convenient for this example.
		//But if you add lots of stars with your mod and want to keep track of their positions
		//relative to each other, the external settings file can also be convenient.
		//PlanetAPI initStar(
		//    id, //unique id for this star. This is how we retrieve it!
		//    type, //You can refer to com.fs.starfarer.api.impl.campaign.ids.StarTypes or 
		//		use the actual values found in
		//		\Starsector\starsector-core\data\config\planets.json
		//    float radius, //The rest are pretty obvious!
		//    float hyperspaceLocationX, 
		//    float hyperspaceLocationY, 
		//    float coronaSize) // corona radius, from star edge
		
		//We append the "f" to the end of these numbers to help tell Java that the numbers
		//are "floating point numbers" (ie numbers that can have a decimal point).
		//It has no special meaning other than that.
		PlanetAPI star = system.initStar(
			"wadestar", 
			"star_orange", 
			500f, 
			-400f, 
			-9400f, 
			250); 
		
		//This sets an ambient light color in entire system, affects all entities (planets, stars, etc).
		//It is not required but can be used to make a spooky effect.
		//Other times it makes things look horrible.
		//Let's see how this color 0xCC0080 looks. It's my favorite color!
		system.setLightColor(new Color(0xCC, 0x00, 0x80)); 

		//Let's add a hot Jupiter to the system. Simple as can be now.
		PlanetAPI testPlanet2 = system.addPlanet("test2", star, "Aaarg", "gas_giant", 0, 150, 1200, 25);
		Misc.initConditionMarket(testPlanet2);

		//Now let's add some random "entities" to the system.
		//The StarSystemGenerator can do this for us and it will add anything you can imagine:
		//accretion disks, more planets, moons, asteroids, etc. You never know!
		StarSystemGenerator.addOrbitingEntities(
			system, 
			star, 
			StarAge.AVERAGE, //This setting determines what kind of potential entities are added.
			2, 4, // min/max entities to add
			2400, // radius to start adding at 
			1, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
			false); // whether to use custom or system-name based names

		//Finally, to make the star appear correctly in the game it is necessary to add hyperspace points.
		//This is the easiest way to handle it.
		//In this case we want to make sure we add a fringe jump point or else no one will be able to leave 
		//our star system without a transverse jump! So the second "true" is kind of important.
		//autogenerateHyperspaceJumpPoints(
		//	boolean generateEntrancesAtGasGiants, //Create jump point at our gas giants?
		//	boolean generateFringeJumpPoint) //Create a jump point at the edge of the system?
		system.autogenerateHyperspaceJumpPoints(true, true);
    }
}
