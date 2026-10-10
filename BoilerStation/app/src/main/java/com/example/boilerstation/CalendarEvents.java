package com.example.boilerstation;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.Date;
import java.util.Scanner;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// TODO Make a function that removes null latitudes and longitudes
// TODO Make a function that removes latitudes and longitudes from
// outside Lafayette metro area
public class CalendarEvents extends AppCompatActivity {
    // The functions that will need to be called are at the top of the document
    // Returns the events as an array of event class.
    Event[] events; public Event[] getEvents() { return events; }
    MainActivity.DownloadFeedback downloadFeedback;
    OkHttpClient internet = new OkHttpClient();
    String rawJson;
    // The constructor loads the BoilerLink data
    public CalendarEvents(MainActivity.DownloadFeedback feedback) {
        threadSplitter(feedback, new Date());
        downloadFeedback = feedback;
    }
    // TODO I will finish this later
    // The constructor loads the save file and only
    // loads the BoilerLink data that doesn't match
    public CalendarEvents(MainActivity.DownloadFeedback feedback, String fileName) {
        threadSplitter(feedback, new Date());
        downloadFeedback = feedback;
    }
    public void WriteEvents() throws IOException
    { WriteEvents("purdueEvents"); }
    public void WriteEvents(String fileName) throws IOException {
        FileWriter eventSave = new FileWriter(
                new File(downloadFeedback.getContext().getFilesDir(),
                (fileName + ".txt")));
        for(int i = 0; i < events.length; i++) {
            String timeStr; String endTimeStr;
            if (events[i].getTime() == null) {
                timeStr = "12:00:00";
            } else { timeStr = events[i].getTime().toString(); }
            if (events[i].getEndTime() == null) {
                endTimeStr = "12:00:00";
            } else { endTimeStr = events[i].getEndTime().toString(); }
            eventSave.write(
                    events[i].getName() + "■" +
                            events[i].getId() + "■" +
                            events[i].getOrganization() + "■" +
                            events[i].getOrganizationId() + "■" +
                            events[i].getTheme() + "■" +
                            events[i].getLocation() + "■" +
                            events[i].getLatitude() + "■" +
                            events[i].getLongitude() + "■" +
                            events[i].getDate().toString()
                            + "■" + timeStr + "■" +
                            events[i].getEndDate().toString()
                            + "■" + endTimeStr + "■\n"
            );
        }
        eventSave.close();
    }
    public Event[] ReadEvents(String fileName) throws IOException {
        Scanner eventSave = new Scanner(new File(
                downloadFeedback.getContext().getFilesDir(),
                (fileName + ".txt")));
        StringBuilder fileData = new StringBuilder();
        while (eventSave.hasNextLine()) {
            fileData.append(eventSave.nextLine()).append("\n");
        }
        String[] input = fileData.toString().split("\n");
        Event[] output = new Event[input.length];
        for (int i = 0; i < output.length; i++) {
            output[i] = new Event(input[i],"■");
        } return output; }
    public void ReadAndSetEvents() throws IOException {
        events = ReadEvents("purdueEvents");
    }
    public interface JsonCallback {
        void DataGreenFlag();
        void DataRedFlag(Exception e);
    }
    String monthToInt (String month) {
        switch(month) {
            case "Jan": return "01";
            case "Feb": return "02";
            case "Mar": return "03";
            case "Apr": return "04";
            case "May": return "05";
            case "Jun": return "06";
            case "Jul": return "07";
            case "Aug": return "08";
            case "Sep": return "09";
            case "Oct": return "10";
            case "Nov": return "11";
            case "Dec": return "12";
            default: return "ERROR";
        }
    }
    int findNumberOfCases() {
        return 1300; // TODO finish this
    }
    public void threadSplitter(MainActivity.DownloadFeedback feedback, Date date) {
        downloadFeedback = feedback;
        String[] dateData = date.toString().split(" ");
        Log.d("Date Used In Search",dateData[5] + "-" + monthToInt(dateData[1]) + "-" + dateData[2]);
        collectJson("https://www.boilerlink.purdue.edu/api/discovery/event/search?endsAfter="
                + dateData[5] + "-" + monthToInt(dateData[1]) + "-" + dateData[2] //Format: 2026-09-28
                + "T14%3A28%3A12-04%3A00&orderByField=endsOn&orderByDirection=ascending&status=Approved&take="
                + findNumberOfCases() + "&query=",
                new JsonCallback() {
                    @Override
                    public void DataGreenFlag() {
                        runOnUiThread(() -> {
                            try {
                                WriteEvents();
                                downloadFeedback.DataGreenFlag();
                            } catch (Exception e) {
                                downloadFeedback.DataRedFlag(e);
                            }
                        });
                    }

                    @Override
                    public void DataRedFlag(Exception e) {
                        runOnUiThread(() -> {
                            downloadFeedback.DataRedFlag(e);
                        });
                    }
                }
        );
    }
    public static int GetValueIndex(String rawJson) throws JSONException {
        // This is the beginning of the array of all events.
        String arrayMarker = "\"value\": ["; // TODO Make this an entry into Strings.xml
        for (int i = 0; i < rawJson.length(); i++) {
            if (rawJson.charAt(i) == arrayMarker.charAt(0)) {
                boolean hasBegun = true;
                for (int j = 1; j < arrayMarker.length(); j++) {
                    if (arrayMarker.charAt(j) == rawJson.charAt(i+j)) hasBegun = false;
                }
                if (hasBegun) return i;
            }
        }
        throw new JSONException("Value tag not found in data.");
    }
    // Collects the raw JSON data from the BoilerLink API.
    void collectJson(String url, JsonCallback callback) {
        Request request = new Request.Builder().url(url)
                .get().build();
        internet.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.DataRedFlag(e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.DataRedFlag(
                            new IOException("HTTP error: " + response.code())
                    );
                    return;
                }

                rawJson = response.body().string();
                GenerateEventArray(GetArrayValues());
                callback.DataGreenFlag();
            }
        });
    }
    String getTestString () {
        int l = 0;
        String writeTxt = "";
        for (int i = 0; i < events.length; i++) {
            if(events[i].latitude != -1) {
                writeTxt += " - " + events[i].toString() + "\n";
                l++;
            }
        }
        writeTxt = "Array length: " + l + "\n" + writeTxt;
        return writeTxt;
    }
    // Returns all of the JSON output according to the search.
    public String GetRawJson() { return rawJson; }
    // Returns the array of event objects
    public void GenerateEventArray(String[] inputArr) {
        Event[] returnVal = new Event[inputArr.length];
        for (int i = 0; i < inputArr.length; i++) {
            returnVal[i] = new Event(inputArr[i]);
        }
        events = returnVal;
    }
    // Returns only the raw array of events in the search.
    public String GetValueArray() { return rawJson.split("value\":\\[")[1]; }
    // Returns the JSON strings of all of the events.
    public String[] GetArrayValues() { return GetValueArray().split("(?=\\{\"id\")"); }
}
