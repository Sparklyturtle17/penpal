package com.penpals.common;

import java.time.ZoneId;

/**
 * US states (2-letter postal code) and Malawi districts (full name), each with a representative time zone.
 * Malawi is entirely Africa/Blantyre (CAT, +2, no DST). A few US states span zones (predominant one used).
 */
public enum State {

	// --- Malawi districts (all 28) — zone: Africa/Blantyre ---
	CHITIPA("Chitipa",       ZoneId.of("Africa/Blantyre")),
	KARONGA("Karonga",       ZoneId.of("Africa/Blantyre")),
	RUMPHI("Rumphi",         ZoneId.of("Africa/Blantyre")),
	NKHATA_BAY("Nkhata Bay", ZoneId.of("Africa/Blantyre")),
	MZIMBA("Mzimba",         ZoneId.of("Africa/Blantyre")),
	LIKOMA("Likoma",         ZoneId.of("Africa/Blantyre")),
	KASUNGU("Kasungu",       ZoneId.of("Africa/Blantyre")),
	NKHOTAKOTA("Nkhotakota", ZoneId.of("Africa/Blantyre")),
	NTCHISI("Ntchisi",       ZoneId.of("Africa/Blantyre")),
	DOWA("Dowa",             ZoneId.of("Africa/Blantyre")),
	SALIMA("Salima",         ZoneId.of("Africa/Blantyre")),
	LILONGWE("Lilongwe",     ZoneId.of("Africa/Blantyre")),
	MCHINJI("Mchinji",       ZoneId.of("Africa/Blantyre")),
	DEDZA("Dedza",           ZoneId.of("Africa/Blantyre")),
	NTCHEU("Ntcheu",         ZoneId.of("Africa/Blantyre")),
	MANGOCHI("Mangochi",     ZoneId.of("Africa/Blantyre")),
	MACHINGA("Machinga",     ZoneId.of("Africa/Blantyre")),
	ZOMBA("Zomba",           ZoneId.of("Africa/Blantyre")),
	CHIRADZULU("Chiradzulu", ZoneId.of("Africa/Blantyre")),
	BLANTYRE("Blantyre",     ZoneId.of("Africa/Blantyre")),
	MWANZA("Mwanza",         ZoneId.of("Africa/Blantyre")),
	THYOLO("Thyolo",         ZoneId.of("Africa/Blantyre")),
	MULANJE("Mulanje",       ZoneId.of("Africa/Blantyre")),
	PHALOMBE("Phalombe",     ZoneId.of("Africa/Blantyre")),
	CHIKWAWA("Chikwawa",     ZoneId.of("Africa/Blantyre")),
	NSANJE("Nsanje",         ZoneId.of("Africa/Blantyre")),
	BALAKA("Balaka",         ZoneId.of("Africa/Blantyre")),
	NENO("Neno",             ZoneId.of("Africa/Blantyre")),

	////////////////////////////////////////////////////////////////////////////////

	AL("Alabama",        ZoneId.of("America/Chicago")),
	AK("Alaska",         ZoneId.of("America/Anchorage")),
	AZ("Arizona",        ZoneId.of("America/Phoenix")),        // no DST
	AR("Arkansas",       ZoneId.of("America/Chicago")),
	CA("California",     ZoneId.of("America/Los_Angeles")),
	CO("Colorado",       ZoneId.of("America/Denver")),
	CT("Connecticut",    ZoneId.of("America/New_York")),
	DE("Delaware",       ZoneId.of("America/New_York")),
	FL("Florida",        ZoneId.of("America/New_York")),       // panhandle is Central
	GA("Georgia",        ZoneId.of("America/New_York")),
	HI("Hawaii",         ZoneId.of("Pacific/Honolulu")),       // no DST
	ID("Idaho",          ZoneId.of("America/Boise")),          // north is Pacific
	IL("Illinois",       ZoneId.of("America/Chicago")),
	IN("Indiana",        ZoneId.of("America/Indiana/Indianapolis")), // NW/SW corners Central
	IA("Iowa",           ZoneId.of("America/Chicago")),
	KS("Kansas",         ZoneId.of("America/Chicago")),        // far west is Mountain
	KY("Kentucky",       ZoneId.of("America/New_York")),       // west is Central
	LA("Louisiana",      ZoneId.of("America/Chicago")),
	ME("Maine",          ZoneId.of("America/New_York")),
	MD("Maryland",       ZoneId.of("America/New_York")),
	MA("Massachusetts",  ZoneId.of("America/New_York")),
	MI("Michigan",       ZoneId.of("America/Detroit")),        // a few western counties Central
	MN("Minnesota",      ZoneId.of("America/Chicago")),
	MS("Mississippi",    ZoneId.of("America/Chicago")),
	MO("Missouri",       ZoneId.of("America/Chicago")),
	MT("Montana",        ZoneId.of("America/Denver")),
	NE("Nebraska",       ZoneId.of("America/Chicago")),        // west is Mountain
	NV("Nevada",         ZoneId.of("America/Los_Angeles")),    // West Wendover is Mountain
	NH("New Hampshire",  ZoneId.of("America/New_York")),
	NJ("New Jersey",     ZoneId.of("America/New_York")),
	NM("New Mexico",     ZoneId.of("America/Denver")),
	NY("New York",       ZoneId.of("America/New_York")),
	NC("North Carolina", ZoneId.of("America/New_York")),
	ND("North Dakota",   ZoneId.of("America/Chicago")),        // southwest is Mountain
	OH("Ohio",           ZoneId.of("America/New_York")),
	OK("Oklahoma",       ZoneId.of("America/Chicago")),
	OR("Oregon",         ZoneId.of("America/Los_Angeles")),    // most of Malheur Co. is Mountain
	PA("Pennsylvania",   ZoneId.of("America/New_York")),
	RI("Rhode Island",   ZoneId.of("America/New_York")),
	SC("South Carolina", ZoneId.of("America/New_York")),
	SD("South Dakota",   ZoneId.of("America/Chicago")),        // west is Mountain
	TN("Tennessee",      ZoneId.of("America/Chicago")),        // east (Knoxville) is Eastern
	TX("Texas",          ZoneId.of("America/Chicago")),        // far west (El Paso) is Mountain
	UT("Utah",           ZoneId.of("America/Denver")),
	VT("Vermont",        ZoneId.of("America/New_York")),
	VA("Virginia",       ZoneId.of("America/New_York")),
	WA("Washington",     ZoneId.of("America/Los_Angeles")),
	WV("West Virginia",  ZoneId.of("America/New_York")),
	WI("Wisconsin",      ZoneId.of("America/Chicago")),
	WY("Wyoming",        ZoneId.of("America/Denver"));

	private final String displayName;
	private final ZoneId zone;

	State(String displayName, ZoneId zone) {
		this.displayName = displayName;
		this.zone = zone;
	}

	public String displayName() {
		return displayName;
	}

	public ZoneId zone() {
		return zone;
	}
}