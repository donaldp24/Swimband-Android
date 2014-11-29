package com.aquaticsafetyconceptsllc.iswimband.CoreData;

public class SwimbandData {
	public int warningTime;
	public int alarmTime;
	public int alarmType;
	public String authKey;
	public String bandId;
	public String name;
	public Boolean firstTime;
	public Double disconnectTime;

	public static SwimbandData createSwimbandData() {

		SwimbandData swimbandData = new SwimbandData();
		swimbandData.warningTime = 0;
		swimbandData.alarmTime = 0;
		swimbandData.alarmType = 0;
		swimbandData.authKey = "";
		swimbandData.name = "";
		swimbandData.bandId = "";
		swimbandData.firstTime = false;
		swimbandData.disconnectTime = 0.0d;

		return swimbandData;
	}

	public SwimbandData() {
		warningTime = 0;
		alarmTime = 0;
		alarmType = 0;
		authKey = "";
		bandId = "";
		firstTime = false;
		disconnectTime = 0.0d;
	}
}
