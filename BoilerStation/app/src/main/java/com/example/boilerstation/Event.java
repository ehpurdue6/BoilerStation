package com.example.boilerstation;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;

public class Event {
    String name; public String getName() { return name; }
    int id; public int getId() { return id; }
    String organization; public String getOrganization() { return organization; }
    int organizationId; public int getOrganizationId() { return organizationId; }
    String theme; public String getTheme() { return theme; }
    String location; public String getLocation() { return location; }
    String building; // location: variable contains building and room
    String room; // location: will be split into this and prior
    double latitude; public double getLatitude() { return latitude; }
    double longitude; public double getLongitude() { return longitude; }
    Time time; public Time getTime() { return time; }
    Date date; public Date getDate() { return date; }
    Time endTime; public Time getEndTime() { return endTime; }
    Date endDate; public Date getEndDate() { return endDate; }
    public Event(String Json) {
        /*
         * Sorts through all data in the event's JSON entry.
         * Sifts variable values using relevant keywords.
         */
        if(Json.charAt(Json.length()-1) == '■') {
            String[] temp = Json.split("■");
            name = temp[0];
            id = Integer.parseInt(temp[1]);
            organization = temp[2];
            organizationId = Integer.parseInt(temp[3]);
            theme = temp[4];
            location = temp[5];
            latitude = Double.parseDouble(temp[6]);
            longitude = Double.parseDouble(temp[7]);
            date = Date.valueOf(temp[8]);
            time = Time.valueOf(temp[9]);
            endDate = Date.valueOf(temp[10]);
            endTime = Time.valueOf(temp[11]);
        }
        name = Json.split("\"name\":\"")[1].split("\"")[0];
        id = Integer.parseInt(Json.split("\"id\":\"")[1].split("\"")[0]);
        organization = Json.split("organizationName\":\"")[1].split("\"")[0];
        organizationId = Integer.parseInt(Json.split("organizationId\":")[1].split(",")[0]);
        theme = Json.split("theme\":\"")[1].split("\"")[0];
        location = Json.split("location\":\"")[1].split("\"")[0];
        try {
            String[] tempDate = Json.split("startsOn\":\"")[1].split("T")[0].split("-");
            date = new Date(Integer.parseInt(tempDate[0])-1900, Integer.parseInt(tempDate[1])-1, Integer.parseInt(tempDate[2]));
            tempDate = Json.split("endsOn\":\"")[1].split("T")[0].split("-");
            endDate = new Date(Integer.parseInt(tempDate[0])-1900, Integer.parseInt(tempDate[1])-1, Integer.parseInt(tempDate[2]));
            tempDate = Json.split("startsOn\":\"")[1].split("T")[1].split("\\+")[0].split(":");
            time = new Time(Integer.parseInt(tempDate[0]), Integer.parseInt(tempDate[1]), 0);
            tempDate = Json.split("endsOn\":\"")[1].split("T")[1].split("\\+")[0].split(":");
            endTime = new Time(Integer.parseInt(tempDate[0]), Integer.parseInt(tempDate[1]), 0);
        } catch (Exception e) {
            //name = "ERROR"; // TODO Make this an actual error throw
        }
        try {
            latitude = Double.parseDouble(Json.split("\"latitude\":\"")[1].split("\"")[0]);
            longitude = Double.parseDouble(Json.split("\"longitude\":\"")[1].split("\"")[0]);
        } catch (Exception e) {
            if (!setLatitudeAndLongitudeByBuilding(location.split(" ")[0].split(",")[0])) {
                if (!setLatitudeAndLongitudeByContained(location)) {
                    latitude = -1;
                    longitude = -1;
                } } }
    }
    public Event(String fileLine, String regex) {
        String[] data = fileLine.split("■");
        name = data[0];
        id = Integer.parseInt(data[1]);
        organization = data[2];
        organizationId = Integer.parseInt(data[3]);
        theme = data[4];
        location = data[5];
        latitude = Double.parseDouble(data[6]);
        longitude = Double.parseDouble(data[7]);
        String[] temp = data[8].split("-");
        date = new Date(Integer.parseInt(temp[0]),
                Integer.parseInt(temp[1]),
                Integer.parseInt(temp[2]));
        temp = data[9].split(":");
        time = new Time(Integer.parseInt(temp[0]),
                    Integer.parseInt(temp[1]),
                    Integer.parseInt(temp[2]) );
        temp = data[10].split("-");
        endDate = new Date(Integer.parseInt(temp[0]),
                Integer.parseInt(temp[1]),
                Integer.parseInt(temp[2]));
        temp = data[11].split(":");
        endTime = new Time(Integer.parseInt(temp[0]),
                Integer.parseInt(temp[1]),
                Integer.parseInt(temp[2]) );

    }
    String ordinalGenerator(int number) {
        String output = "" + number;
        if (output.length() > 1) {
            if (output.charAt(output.length()-2) == '1') {
                return output + "th";
            } }
        if (output.charAt(output.length()-1) == '1') {
            return output + "st";
        } else if (output.charAt(output.length()-1) == '2') {
            return output + "nd";
        } else if (output.charAt(output.length()-1) == '3') {
            return output + "rd";
        } else {
            return output + "th";
        }
    }
    String monthString(int month) {
        switch(month) {
            case (1): return "January";
            case (2): return "February";
            case (3): return "March";
            case (4): return "April";
            case (5): return "May";
            case (6): return "June";
            case (7): return "July";
            case (8): return "August";
            case (9): return "September";
            case (10): return "October";
            case (11): return "November";
            case (12): return "December";
        }
        return "Month Not Found";
    }
    public String getDateAsString() {
        return ordinalGenerator(Integer.parseInt(date.toString().split("-")[2])) +
                " " + monthString(Integer.parseInt(date.toString().split("-")[1])) +
                ", " + Integer.parseInt(date.toString().split("-")[0]);
    }
    public String getTimeAsString() {
        if (time == null) return "null";
        String[] temp = time.toString().split(":");
        if (Integer.parseInt(temp[0]) > 12) {
            return (Integer.parseInt(temp[0]) - 12) + ":" + temp[1] + " PM";
        } else {
            return Integer.parseInt(temp[0]) + ":" + temp[1] + " AM";
        }
    }
    @Override
    public String toString() {
        String placeStr;
        placeStr = location;
        if (latitude != -1) {
            placeStr += "\n      Coordinates: (" +
                    String.format("%.7f",latitude) + ", " +
                    String.format("%.7f",longitude) + ")";
        }
        return (name + " presented by " + organization
                + "\n      " + getDateAsString()) + " at " +
                getTimeAsString() + "\n      Location: " +
                placeStr;
    }
    public boolean stringContains(String container, String contained) {
        return container.toUpperCase().contains(contained.toUpperCase());
    }
    boolean setLatitudeAndLongitudeByContained(String location) {
        if (stringContains(location, "Pine Creek Camp")) {
            latitude = 40.41824987748497;
            longitude = -87.27795393565468;
        } else if (stringContains(location, "Tecumseh")) {
            latitude = 40.60071592482614;
            longitude = -86.76050510413539;
        } else if (stringContains(location, "Armory")) {
            latitude = 40.428065437421594;
            longitude = -86.91633073498924;
        } else if (stringContains(location, "1st Street Towers") ||
                stringContains(location, "First Street Towers")) {
            latitude = 40.4249022366566;
            longitude = -86.9245489043005;
        } else if (stringContains(location, "Grand Prix")) {
            latitude = 40.43829568322467;
            longitude = -86.9438504172622;
        } else if (stringContains(location, "Memorial Mall")) {
            latitude = 40.425045982369355;
            longitude = -86.91433494021761;
        } else if (stringContains(location, "Eagle Creek Sailing Club")) {
            latitude = 39.83428012799717;
            longitude = -86.32054639571143;
        } else if (stringContains(location, "Chipotle")) {
            latitude = 40.42364505376195;
            longitude = -86.90717587759993;
        } else if (stringContains(location, "Northwest Recreation Fields") ||
                stringContains(location, "Northwest Recreational Fields")) {
            latitude = 40.44076608291449;
            longitude = -86.93722575930005;
        } else if (stringContains(location, "Corec")
                || stringContains(location, "Co-rec")
                || stringContains(location, "France A. C")) {
            latitude = 40.42840501106635;
            longitude = -86.92232341779429;
        } else if (stringContains(location, "Natatorium")
                || stringContains(location, "IUI Gymnasium")) {
            latitude = 39.77077832710065;
            longitude = -86.17505430619111;
        } else if (stringContains(location, "Turkey Run")) {
            latitude = 39.89141671570858;
            longitude = -87.20116892524754;
        } else if (stringContains(location, "Rise On Chauncey")) {
            latitude = 40.422803353994155;
            longitude = -86.90685514663053;
        } else if (stringContains(location, "Studio B")) {
            latitude = 40.45224635897829;
            longitude = -86.91305881469948;
        } else if (stringContains(location, "Turning Point Academy")
                || stringContains(location, "Turning Pointe Academy")) {
            latitude = 40.45224635897829;
            longitude = -86.91305881469948;
        } else if (stringContains(location, "Dos Amigos")) {
            latitude = 40.423338229776554;
            longitude = -86.8991910589711;
        } else if (stringContains(location, "Baptist Student Foundation")) {
            latitude = 40.426486498143795;
            longitude = -86.91889979347519;
        } else if (stringContains(location, "Class of 1950")) {
            latitude = 40.4263671950073;
            longitude = -86.91505971053213;
        } else if (stringContains(location, "Wetherill")) {
            latitude = 40.426472875404926;
            longitude = -86.91305962657174;
        } else if (stringContains(location, "Centennial Mall")) {
            latitude = 40.42719345587852;
            longitude = -86.91375112574318;
        } else if (stringContains(location, "Hampton Hall")) {
            latitude = 40.43018032029219;
            longitude = -86.91484021806069;
        } else if (stringContains(location, "Forney")) {
            latitude = 40.429587439769826;
            longitude = -86.91392277731732;
        } else if (stringContains(location, "University Hall")) {
            latitude = 40.4255493124546;
            longitude = -86.9163150788823;
        } else if (stringContains(location, "Engineering Fountain")
                || stringContains(location, "Purdue's Campus")
                || stringContains(location, "Purdue Mall Fountain")
                || stringContains(location, "Purdue Mall- Fountain")) {
            latitude = 40.42867748798781;
            longitude = -86.91382497064222;
        } else if (stringContains(location, "Clapping Circle")) {
            latitude = 40.425813926322775;
            longitude = -86.91186137946326;
        } else if (stringContains(location, "Purdue Memorial Union")) {
            latitude = 40.424903554404445;
            longitude = -86.91051411779884;
        } else return false;
        return true;
    }
    boolean setLatitudeAndLongitudeByBuilding(String building) {
        building = building.toUpperCase();
        if (building.equals("CREC") ||building.equals("COREC") || building.equals("CO-REC")) {
            latitude = 40.42840501106635;
            longitude = -86.92232341779429;
        } else if (building.equals("WALC") || building.equals("WILMETH")) {
            latitude = 40.42747870732939;
            longitude = -86.91322774398164;
        } else if (building.equals("LILY")) {
            latitude = 40.42361481998694;
            longitude = -86.91773898340479;
        } else if (building.equals("STEW") || building.equals("STEWART")) {
            latitude = 40.42512071043663;
            longitude = -86.91294850430221;
        } else if (building.equals("RAWL") || building.equals("RAWLS")) {
            latitude = 40.42381830997918;
            longitude = -86.91000311741678;
        } else if (building.equals("BHEE")) {
            latitude = 40.42891767675046;
            longitude = -86.91193956012249;
        } else if (building.equals("PMU")) {
            latitude = 40.424903554404445;
            longitude = -86.91051411779884;
        } else if (building.equals("BEERING") || building.equals("Beering") ) { // What is the abbreviation for this?
            latitude = 40.42558185910778;
            longitude = -86.91630319527317;
        } else if (building.equals("HAMP")) {
            latitude = 40.430149855582606;
            longitude = -86.91483831450186;
        } else if (building.equals("LYLE")) {
            latitude = 40.420636770764354;
            longitude = -86.91634189331225;
        } else if (building.equals("NLSN")) {
            latitude = 40.421660046899675;
            longitude = -86.91555251337553;
        } else if (building.equals("LYNN")) {
            latitude = 40.42006399069703;
            longitude = -86.91534593630045;
        } else if (building.equals("KRANNERT") || building.equals("KRAN")) {
            latitude = 40.42352937590446;
            longitude = -86.91091510339406;
        } else if (building.equals("ARMS")) {
            latitude = 40.43102627697389;
            longitude = -86.91486090019797;
        } else if (building.equals("KRCH") || building.equals("KRACH")) {
            latitude = 40.427584936793394;
            longitude = -86.9212394535311;
        } else if (building.equals("LMBS")) {
            latitude = 40.42718833118501;
            longitude = -86.91187618389709;
        } else if (building.equals("BRNG")) {
            latitude = 40.42524290710142;
            longitude = -86.9151923328662;
        } else if (building.equals("UNIV")) {
            latitude = 40.4255493124546;
            longitude = -86.9163150788823;
        } else if (building.equals("MJIS")) {
            latitude = 40.42219403599676;
            longitude = -86.92108237546645;
        } else if (building.equals("LWSN") || building.equals("LAWSON")) {
            latitude = 40.427586871444475;
            longitude = -86.91698517731747;
        } else if (building.equals("BIDC") || building.equals("BECHTEL")) {
            latitude = 40.42753742897185;
            longitude = -86.91882565086496;
        } else if (building.equals("DUHME")) {
            latitude = 40.425466993501125;
            longitude = -86.92071742706285;
        } else if (building.equals("CL50")) {
            latitude = 40.4263671950073;
            longitude = -86.91505971053213;
        } else if (building.equals("TREC")) {
            latitude = 40.42856651476449;
            longitude = -86.92426712302975;
        } else if (building.equals("ISGL")) {
            latitude = 40.42547571031129;
            longitude = -86.91878702143657;
        } else if (building.equals("SMTH")) {
            latitude = 40.42350420657468;
            longitude = -86.91682367546412;
        } else if (building.equals("PE") // Also cant find PE
                || building.equals("ET") // Also cant find ET
                || building.equals("IO")) { // Might not be super correct but I can't
            latitude = 39.77399193433419; // Find IO and it's outside of Lafayette
            longitude = -86.17253015792069; // So it's out of the app's scope
        } else if (building.equals("FRNY") || building.equals("Forney")) {
            latitude = 40.429587439769826;
            longitude = -86.91392277731732;
        } else return false;
        return true;
    }
}
