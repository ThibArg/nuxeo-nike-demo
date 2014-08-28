package org.nuxeo.nike;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.runtime.datasource.ConnectionHelper;

public class AggregateValues {

    public static final String kLABEL_SEASONS = "Seasons";
    public static final String kLABEL_EVENTS = "Events";
    public static final String kLABEL_CATEGORIES = "Categories";
    public static final String kLABEL_KEYWORDS = "Nike-Keywords";
    public static final String kLABEL_WIDHTS = "Width Ranges";
    public static final String kLABEL_HEIGHTS = "Height Ranges";
    public static final String kLABEL_ALL = "All";

    public static final Log log = LogFactory.getLog(AggregateValues.class);

    protected static final String kSQL_FOR_SEASONS = "SELECT season AS label, COUNT(season) AS count"
                                                        + " FROM appcommon ac"
                                                        + " LEFT JOIN proxies p ON p.targetid = ac.id"
                                                        + " JOIN hierarchy h ON h.id = ac.id"
                                                        + " WHERE p.targetid IS NULL "
                                                          + " AND h.isversion IS NULL"
                                                          + " AND season IS NOT NULL AND season <> ''"
                                                        + " GROUP BY season ORDER BY season";

    protected static final String kSQL_FOR_EVENTS = "SELECT event AS label, COUNT(event) AS count"
                                                        + " FROM appcommon ac"
                                                        + " LEFT JOIN proxies p ON p.targetid = ac.id"
                                                        + " JOIN hierarchy h ON h.id = ac.id"
                                                        + " WHERE p.targetid IS NULL "
                                                          + " AND h.isversion IS NULL"
                                                          + " AND event IS NOT NULL AND event <> ''"
                                                        + " GROUP BY event ORDER BY event";


    protected static final String kSQL_FOR_CATEGORIES = "SELECT category AS label, COUNT(category) AS count"
                                                        + " FROM appcommon ac"
                                                        + " LEFT JOIN proxies p ON p.targetid = ac.id"
                                                        + " JOIN hierarchy h ON h.id = ac.id"
                                                        + " WHERE p.targetid IS NULL "
                                                          + " AND h.isversion IS NULL"
                                                          + " AND category IS NOT NULL AND category <> ''"
                                                        + " GROUP BY category ORDER BY category";

    protected static final String kSQL_FOR_KEYWORDS = "SELECT item AS label, count(item) AS count"
                                                        + " FROM ac_keywords as ack"
                                                        + " LEFT JOIN proxies p ON p.targetid = ack.id"
                                                        + " JOIN hierarchy h ON h.id = ack.id"
                                                        + " WHERE p.targetid IS NULL "
                                                        + " AND h.isversion IS NULL"
                                                          + " AND item IS NOT NULL AND item <> ''"
                                                        + " GROUP BY item ORDER BY item";

    protected static final String kSQL_FOR_WIDTH_RANGE = "SELECT width_range AS label, COUNT(width_range) AS count"
                                                        + " FROM appcommon ac"
                                                        + " LEFT JOIN proxies p ON p.targetid = ac.id"
                                                        + " JOIN hierarchy h ON h.id = ac.id"
                                                        + " WHERE p.targetid IS NULL "
                                                          + " AND h.isversion IS NULL"
                                                          + " AND width_range IS NOT NULL"
                                                        + " GROUP BY width_range ORDER BY width_range";

    protected static final String kSQL_FOR_HEIGHT_RANGE = "SELECT height_range AS label, COUNT(height_range) AS count"
                                                        + " FROM appcommon ac"
                                                        + " LEFT JOIN proxies p ON p.targetid = ac.id"
                                                        + " JOIN hierarchy h ON h.id = ac.id"
                                                        + " WHERE p.targetid IS NULL "
                                                          + " AND h.isversion IS NULL"
                                                          + " AND width_range IS NOT NULL"
                                                        + " GROUP BY height_range ORDER BY height_range";

    public String run(String inStatsOnWhat, CoreSession inSession) throws SQLException {
        String jsonResult = "";

        boolean doAll = inStatsOnWhat.equals( AggregateValues.kLABEL_ALL );

        jsonResult = "{";
        if(doAll || inStatsOnWhat.equals( AggregateValues.kLABEL_SEASONS )) {
            jsonResult += "\"seasons\":" + doTheSQLQuery(kSQL_FOR_SEASONS) + ",";
        }
        if(doAll || inStatsOnWhat.equals( AggregateValues.kLABEL_EVENTS )) {
            jsonResult += "\"events\":" + doTheSQLQuery(kSQL_FOR_EVENTS) + ",";
        }
        if(doAll || inStatsOnWhat.equals( AggregateValues.kLABEL_CATEGORIES )) {
            jsonResult += "\"categories\":" + doTheSQLQuery(kSQL_FOR_CATEGORIES) + ",";
        }
        if(doAll || inStatsOnWhat.equals( AggregateValues.kLABEL_KEYWORDS )) {
            jsonResult += "\"keywords\":" + doTheSQLQuery(kSQL_FOR_KEYWORDS) + ",";
        }
        if(doAll || inStatsOnWhat.equals( AggregateValues.kLABEL_WIDHTS )) {
            jsonResult += "\"widthRange\":" + doTheSQLQuery(kSQL_FOR_WIDTH_RANGE) + ",";
        }
        if(doAll || inStatsOnWhat.equals( AggregateValues.kLABEL_HEIGHTS )) {
            jsonResult += "\"heightRange\":" + doTheSQLQuery(kSQL_FOR_HEIGHT_RANGE) + ",";
        }
        jsonResult = jsonResult.substring(0, jsonResult.length() - 1);
        jsonResult += "}";

        return jsonResult;
    }

    protected String doTheSQLQuery(String inQueryStr) throws SQLException {
        String jsonResult = "[";

        Connection co = ConnectionHelper.getConnection(null);
        if(co != null) {
            try {
                Statement st = co.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                ResultSet rs = st.executeQuery(inQueryStr);
                while(rs.next()) {
                    jsonResult += "{\"label\":\"" + rs.getString("label") + "\", \"count\":" + rs.getInt("count") + "},";
                }
                rs.close();
                // Remove last comma.
                // If there was nothing, then reduce to empty
                if(jsonResult.equals("[")) {
                    jsonResult = "[]";
                } else {
                    jsonResult = jsonResult.substring(0, jsonResult.length() - 1) + "]";
                }

            } catch (SQLException e) {
                // . . .
                throw e;
            } finally {
                co.close();
            }
        }

        return jsonResult;
    }

}
