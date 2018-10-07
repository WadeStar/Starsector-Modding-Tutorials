package data.scripts;

//Requried for Example 1:
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.util.Misc;

//Additionally required for Example 2:
import com.fs.starfarer.api.campaign.PlanetSpecAPI;

/**
 * This mod demonstrates the bare-minimum necessary code/scripting required to add a planet to an existing star system.
 * It is intended for those with minimal programming experience, let alone Java experience.
 * It tries to get the reader started with the bare basics that they can use to make simple, though impactful, 
 * changes to their StarSector game.
 * 
 * At times, some programming jargon will be defined and then used. Even if it doesn't make sense, the reader should
 * still be able to duplicate the code herein and modify it to obtain their own desired results.
 * 
 * TPModPlugin.java is a "class" file. It will be compiled by the game (ie Java) into byte-code (an actual .class file).
 */
public class TPModPlugin extends BaseModPlugin {
	@Override
	/**
	 * onNewGame() is the standard place to put planet and star creation logic.
	 * However, you may implement planet creation in other parts of the BaseModPlugin for interesting results.
	 * What if a series of events by the player "revealed" a new planet? That's theoretically possible!
	 * ...but beyond the scope of this example.
	 */
	public void onNewGame() {		
		//--------------------------------------------------
		// Example 1
		//--------------------------------------------------
		//There's only one StarSector, so it's accessed this way:
		SectorAPI sector = Global.getSector();
		//There are many star systems, though, and they are retrieved using their id, which happens to often be their name.
		StarSystemAPI system = sector.getStarSystem("Corvus");
		//Each star system has only one central star (other stars would be satellites just like the planets).
		//So this is how you get the singular star at the center of the star system.
		//...nebulae and the like are another matter! Let's stay focused.
		SectorEntityToken star = system.getStar();
		
		//This is the most straightforward way to add a planet to an existing star system.
		//Note that we tell the planet what to orbit in the second argument.
		//In this case it's the star we retrieved above... but it could be another planet or any other SectorEntityToken in the star system.
		//For now, let's keep it simple.
		PlanetAPI testPlanet = system.addPlanet("testPlanetId", star, "Testulon", "lava_minor", 240, 120, 2000, 120);
		
		//This is necessary to address an NPE (Null Pointer Exception) that occurs in the game.
		//It should not be necessary for unremarkable planets such as the one in this tutorial starting from version 0.9.
		Misc.initConditionMarket(testPlanet);
		
		//This should update the order of planets as they appear in the planet list display starting from version 0.9.
		system.updateAllOrbits();
		
		//--------------------------------------------------
		// Example 2
		//--------------------------------------------------
		//It may be helpful to compartmentalize your logic into different methods/functions.
		//Many mods already do this to an... unnecessary degree of complexity.
		//Moderation in all things is the key to sanity.
		//For the next example, let's compartmentalize our second planet in a sub-routine:

		createSecondTestPlanet();
	}

	private void createSecondTestPlanet() {
		//Below is a slightly more succinct way to write the initial lines from example 1.
		//This can sometimes cut down the number of import statements as well as the number of lines in your class file.
		//It is known as method chaining and it is common in functional programming.
		StarSystemAPI system = Global.getSector().getStarSystem("Corvus");
		Misc.initConditionMarket(
			system.addPlanet("testSecondPlanetId", system.getStar(), "Testia", "barren", 200, 120, 2500, 120)
		);
		//What types of planets are there to choose from? That can be found in the 
		// \starsector-core\data\config\planets.json

		//You'll notice stars and everything else are also defined there.
		
		//It is possible to define your own classes of planets as well! 
		//Like a Gaia world, or a Machine world... but that's for the next version of this tutorial.

		//Next let's try doing something weird like changing Asharu into the terran world it always wanted to be!
		
		//First, we must retreive Asharu from all the entities available in this star system.
		SectorEntityToken asharuTheEntity = system.getEntityById("asharu");
		//Let's change the name.
		asharuTheEntity.setName("New Asharu");

		//So far, we only know of Asharu as a SectorEntityToken.
		//That's good enough to change the name, 
		//but we need to further categorize it as a planet if we want to change its planet attributes.
		//This is done by casting it as a PlanetAPI.
		//Another way to describe "casting" is that it's like converting the SectorEntityToken to a PlanetAPI.
		//(Programmers, spare me your pedantics. This isn't for you.)
		//If Asharu weren't a planet in the first place, this conversion would fail.
		//Further discussion would require diving into the fundamentals of object oriented programming.
		//If the concept interests you, Google "polymorphism".
		//If it annoys you, don't.
		PlanetAPI asharuThePlanet = (PlanetAPI) asharuTheEntity;

		//Now that we have Asharu the planet, we need to get its specs.
		PlanetSpecAPI newAsharuSpec = asharuThePlanet.getSpec();
		//The PlanetSpecAPI let's us change the planet's texture.
		newAsharuSpec.setTexture("graphics/planets/terran.jpg");
		//There are many other attributes we can change...
		//Experiement with the other methods available through PlanetSpecAPI.
		//Once we're done changing the specs, we need to update Asharu with these changes.
		asharuThePlanet.applySpecChanges();

		//Load up the game and, wait, what's this?
		//It says Asharu is *still* a desert planet?!
		//That's because we didn't tell it otherwise.
		//We just literally changed the texture of the planet.
		//Unfortunately, changing the planet type is not possible using the PlanetSpecAPI...

		//To truly turn Asharu into a Terran world would require that we remove it and replace it 
		//with a brand new planet initially created using the "terran" type.
		//But since Asharu has a market and an orbital station, this is easier said than done.
		//Once you know how to create markets, you might revisit this exercise.
		//But more likely you'll be wanting to make your own new planets and markets instead
		//and will leave poor Asharu behind... the forgotten desert planet.
	}
}
