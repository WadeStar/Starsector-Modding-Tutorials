package data.scripts;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.util.Misc;

public class MaMModPlugin extends BaseModPlugin {
	@Override
	public void onNewGame() {
		tutorialPart1();
		tutorialPart2();
	}
	
	/**
	 * Add a new market to a new star system.
	 * Let's add a minimal market from scratch to the game.
	 */
	private void tutorialPart1() {
		StarSystemAPI system = Global.getSector().createStarSystem("Wade's Star");
		
		PlanetAPI star = system.initStar("wades_star", "star_red_dwarf", 500, -400, -9400, 250);
		
		PlanetAPI planet = system.addPlanet("yoo-toob", star, "Yoo-Toob", "barren", 0, 73, 1777, 107);
		//Setting the planet faction will add the comm directory and tavern to its menu once a market is set up.
		//It's not strictly required.
		//Mixing the faction ownership of the planet with the faction ownership of the market 
		//will have... unusual results in the display, but market ownership is the more important factor.
		planet.setFaction(Factions.INDEPENDENT);

		//Create and initialize market:
		//Note that the size of the market determines how much demand there is for various goods.
		//A size 1 market's "population & infrastructure" industry (assuming there is one) 
		//will only demand supplies and produce nothing.
		//A smaller market is less likely to have a colony administrator assinged to it, 
		//despite the fact that a colony administrator will show up in its comms link.
		MarketAPI market = Global.getFactory().createMarket(
			"yoo-toob_market", //market id
			planet.getName(), //market display name, usually the planet's name
			1 //market size
		);
		
		planet.setMarket(market);

		//Market global property settings
		market.setPrimaryEntity(planet);

		//Setting survey level to fully surveyed will automatically reveal normally 
		//hidden resources on the planet overview as well as remove a silly "unexplored" description.
		//When a planet has a market, you won't be able to survey it (nor should you have to).
		market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
		//Set a tariff percentage of 30%.
		//This part of the code uses a mechanism called "MutableStat".
		//It is used in a variety of places throughout Starsector to accomplish a variety of things.
		//Here it is important to simply know that "generator" is the id of the tariff value
		//and that you set it using the modifyFlat() function with a decimal value.
		market.getTariff().modifyFlat("generator", 0.3f);

		//Planet surface/market conditions
		market.setPlanetConditionMarketOnly(false);
		//Some tags, like "thin atmosphere" will affect industry and installation upkeep costs.
		market.addCondition(Conditions.THIN_ATMOSPHERE);
		//market.addCondition(Conditions.ORE_SPARSE);
		//The population tag is purely decorative. You can set it to whatever you want or even omit it.
		//For player colonies, the population tag is adjusted to match the growth of the colony market size.
		market.addCondition(Conditions.POPULATION_1);
		
		//The following settings must be implemented in the correct order to function properly:
		//1) set the market faction id
		//2) add industries and sub-markets to the market
		//3) add the market to the global economy
		
		//The market's owning faction must be set before adding sub-markets and industries or the game will crash.
		market.setFactionId(Factions.INDEPENDENT);
		
		//Planet colony industries
		//If no industries are added, the game won't crash... 
		//Weapons and such will be available for purchase from black and open markets.
		//Your colony will have a constant -10 instability rating.
		//Mousing over "stability" in the colony info will give just a boilerplate explanation of 
		//stability but no additional information (you can find it however if you mouse over "growth").
		//There will be no supply or demand for goods under "commodities" though there will be procurement missions.

		//Once population is added, there will be a supply/demand for supplies. 
		//Stability will probably be around 5.
		//Stability information will be available upon mouse over of "stability".
		//There will be a severe accessibility penalty due to lack of spaceport.
		//Finally, population adds an "administrator" NPC to the comm directory.
		market.addIndustry(Industries.POPULATION);
		
		//A spaceport does not need to be added to access the open and black submarkets,
		//but colony access will have a -100% penalty.
		//Spaceport enables the repairs option in the main menu.
		//Spaceport adds "quartermaster" and "portmaster" NPCs to the comm directory.
		market.addIndustry(Industries.SPACEPORT);

		//Adding an orbital station (or bigger) to the industries on a planet will
		//automatically place a station in orbit.
		//Also adds a "station commander" NPC to the comm directory.
		market.addIndustry(Industries.ORBITALSTATION);

		//Note that there is much more to be said about industries and their effects.
		//An explanation of each is beyond the scope of this tutorial.

		//Planet sub-markets
		market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
		market.addSubmarket(Submarkets.SUBMARKET_BLACK);
		market.addSubmarket(Submarkets.SUBMARKET_OPEN);
		
		//The market needs to be added to the global economy after adding sub-markets and industries.
		//If you don't do this, at best all commodities will be $1 because the colony has no idea how to price them.
		//At worst, the game will crash.
		EconomyAPI globalEconomy = Global.getSector().getEconomy();		
		globalEconomy.addMarket(
			market, //The market to add obviously!
			false //The "withJunkAndChatter" flag. It will add space debris in orbit and radio chatter sound effects.*
		);
		//* Debris and chatter are added whenever you load a game, so this just tells the game not to add it *right now*.

		//SectorEntityToken relay = system.addCustomEntity("mam_relay", "Comm Relay", "comm_relay", Factions.INDEPENDENT);
		//relay.setCircularOrbit(star, 0, 1831, 23);

		system.autogenerateHyperspaceJumpPoints(true, true);
		
		//Final note: by adding a market to this system, 
		//any other planets added or generated will also be marked as "fully surveyed".
	}

	/**
	 * Add a new market to an existing system.
	 * Let's inject conflict into the Corvus system by putting a Tritachyon mining operation in their territory.
	 */
	private void tutorialPart2() {
		//Let's return to "Testulon" from the first tutorial and make it a new planet in Hegemony space.
		StarSystemAPI system = Global.getSector().getStarSystem("Corvus");
		SectorEntityToken star = system.getStar();
		PlanetAPI planet = system.addPlanet("testulon", star, "Testulon", "lava_minor", 240, 120, 2000, 120);
		
		//This is a useful method for automatically adding random but appropriate surface conditions to a planet.
		//It will ignore any planet that has already had conditions added to it.
		//This means vanilla systems like Corvus won't have existing planets modified.
		//Only our new planet will have (random) resources applied to it.
		Misc.generatePlanetConditions(system, StarAge.OLD);
		
		//Since we'll add a Tritachyon market/colony, 
		//set ownership of the planet to Tritachyon for in-game visual consistency.
		planet.setFaction(Factions.TRITACHYON);

		//Testulon Station Alpha
		//We'll mostly interact with this object's SystemEntityToken features,
		//but as a point of note, it's actually a CustomCampaignEntityAPI.
		CustomCampaignEntityAPI station1 = system.addCustomEntity(
			"testulon_station1", //id
			"Testulon Station Alpha", //display name
			"station_side03", //types are found in data/config/custom_entities.json
			Factions.TRITACHYON
		);
		station1.setCircularOrbitPointingDown(
			planet, 
			0, //Angle
			223, //orbit radius
			23 //orbit period
		);
		//Since we have the CustomCampaignEnityAPI available, let's use it.
		//We know from looking in the custom_entities.json that the orbital's sprite size is 30, 
		//so add 5 to that to make the encounter radius a little more snug.
		station1.setRadius(35);

		//Testulon Station Beta
		//Because the only thing better than *one* orbital station is...
		CustomCampaignEntityAPI station2 = system.addCustomEntity(
			"testulon_station2",
			"Testulon Station Beta", 
			"station_side03", //types are found in data/config/custom_entities.json
			Factions.TRITACHYON
		);
		station2.setCircularOrbitPointingDown(
			planet, 
			180, //Angle
			223, //orbit radius
			23 //orbit period
		);
		station2.setRadius(35);

		//Because we already technically setup market conditions above (using Misc.generatePlanetConditions())
		//a market is already on this planet.
		//We're going to grab its reference object instead of trying to create a new market.
		MarketAPI market = planet.getMarket();
		String marketId = market.getId(); //We don't need the id really, but take note that it has been auto-generated.

		market.setPlanetConditionMarketOnly(false); //We are going to turn this into a proper colony and not just a "surface only".
		market.setFactionId(Factions.TRITACHYON);
		market.addCondition(Conditions.POPULATION_3);
		market.setSize(3);
		//At this point in the code we can actually "dock" with the planet! 
		//But there's no sub-markets to interact with... let's fix that now.
		market.addSubmarket(Submarkets.SUBMARKET_BLACK);
		market.addSubmarket(Submarkets.SUBMARKET_OPEN);
		
		//Population is basically essential.
		market.addIndustry(Industries.POPULATION);
		market.addIndustry(Industries.SPACEPORT);
		//Put a defensive station in orbit.
		market.addIndustry(Industries.ORBITALSTATION_HIGH);
		//Make it so fleets will spawn here.
		market.addIndustry(Industries.PATROLHQ);
		//Make it so that ores will be mined here.
		market.addIndustry(Industries.MINING);
		
		//Those rascally Tritachyon have set their tariffs to 15%!
		market.getTariff().modifyFlat("generator", 0.15f);

		//Unless you want all the commodities to buy/sell for $1, add to the local economy!
		EconomyAPI globalEconomy = Global.getSector().getEconomy();		
		globalEconomy.addMarket(market, false);

		//Now let's link the orbiting stations to the colony market.
		//Do this by adding it to the market's connected entities.
		//When a colony has an orbital station industry *and* you connect an orbital entity to its market,
		//that orbital entity is used to represent the orbital station visually 
		//instead of spawning an additional generic one.
		market.getConnectedEntities().add(station1);
		station1.setMarket(market);
		//Since we set the faction when we created the station entity above, 
		//we don't need to explicitly set its faction here.
		
		//We can add the second orbital, too. However, it will not be used to represent the orbital station.
		market.getConnectedEntities().add(station2);
		station2.setMarket(market);
		//Again, this entity was initialized as Tritachyon, so we don't need to set its faction.

		//This will update the order of planets as they appear in the planet list display.
		//As of 0.9 it does not appear to be strictly necessary.
		system.updateAllOrbits();
	}
}
