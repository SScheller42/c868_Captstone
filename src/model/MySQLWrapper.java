package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps and handles most SQL syntax.
 */
public class MySQLWrapper {
    public enum TableNames {
        appointments,
        contacts,
        countries,
        customers,
        firstLevelDivisions,
        users
    }

    private Connection databaseConnection = null;
    private String errorMessage = "";

    /**
     * Creates the database connection to the MySQL database that should be used.
     * @param connectionString The connection string that should be utilized to connect. Should be in the format: jdbc:mysql://<host>/<database>
     * @param userName The username that should be used to connect to the database the program should use.
     * @param password The corresponding password for the username to be used to connect to the database.
     * @return True if connection is successful, false if not.
     */
    public boolean createConnection(String connectionString, String userName, String password) {
        try {
            this.databaseConnection = DriverManager.getConnection(connectionString, userName, password);


            return true;

        } catch (SQLException ex) {
            // We had an error so go ahead and return false and store our message
            this.errorMessage = ex.getMessage();
            return false;
        }
    }

    /**
     * Converts the table enumeration into a string literal for MySQL queries. Should be used instead of hard-coding tables in query strings in case a table name changes.
     * @param table The table enum value for the table that should be queried.
     * @return The string literal value of the table that should be accessed.
     */
    public String getTableNameFromEnum(TableNames table) {
        switch (table) {
            case appointments:
                return "appointments";

            case contacts:
                return "contacts";

            case countries:
                return "countries";

            case customers:
                return "customers";

            case firstLevelDivisions:
                return "first_level_divisions";

            case users:
                return "users";
        }
        return "";
    }

    /**
     * Reads data from a database.
     * @param table The table that the data should be read from.
     * @param tableHeaders The specific columns that should be read from the table.
     * @param whereClause (Optional) The where clause that specifies what rows should be read.
     * @param joinClause (Optional) The join clause that specifies what tables should be joined and how.
     * @param orderBy (Optional) The order by statement that specifies in what order the data should be returned.
     * @param limit (Optional) The maximum number of results that should be returned from the read. If a value less than 1 is specified all results will be returned.
     * @return An array of map objects that contain all values from each row of the specified table columns.
     */
    public ArrayList<Map<String, String>> selectFromTable(TableNames table, String tableHeaders, String whereClause, String joinClause, String orderBy, long limit) {
        StringBuilder rawSQLStatement = new StringBuilder("SELECT ").append(tableHeaders);

        rawSQLStatement.append(" FROM ");
        rawSQLStatement.append(getTableNameFromEnum(table));

        // Check if we have a join clause
        if (joinClause != "") {
            rawSQLStatement.append(" " + joinClause);
        }

        // Check to see if we have a where clause and if so go ahead and add it to the string builder
        if (whereClause != "") {
            rawSQLStatement.append(" WHERE ");
            rawSQLStatement.append(whereClause);
        }

        // Check if we have an order request
        if (orderBy != "") {
            rawSQLStatement.append(" ORDER BY ");
            rawSQLStatement.append(orderBy);
        }

        // Check if we have a limit request
        if (limit > 0) {
            rawSQLStatement.append(" LIMIT ");
            rawSQLStatement.append(limit);
        }

        return selectFromTable(rawSQLStatement.toString());
    }

    /**
     * Reads data from a database.
     * @param rawSQLCommand The raw MySQL that should be executed for the read. Used for advanced operations.
     * @return An array of map objects that contain all values from each row of the specified table columns.
     */
    public ArrayList<Map<String, String>> selectFromTable(String rawSQLCommand) {
        try {
            // Create the prepared statement from our string builder
            PreparedStatement statement = this.databaseConnection.prepareStatement(rawSQLCommand);

            // Actually execute the statement and store the results for processing
            ResultSet results = statement.executeQuery();

            // Grab our meta-data
            ResultSetMetaData metaData = results.getMetaData();
            int columnCount = metaData.getColumnCount();
            String[] columnNames = new String[columnCount];

            // Do a quick loop and grab all of our column names
            for (int i = 0; i < columnCount; i++) {
                columnNames[i] = metaData.getColumnLabel(i + 1);
            }

            // Create a dictionary list to hold all of our rows plus their headers
            ArrayList<Map<String, String>> parsedResults = new ArrayList<Map<String, String>>();

            // Loop through and build a string dictionary that can be returned and parsed by calling method
            while (results.next()) {
                // Create the next entry
                Map<String, String> nextEntry = new HashMap<String, String>();

                // Grab all the entries for that row
                for (int i = 0; i < columnNames.length; i++) {
                    nextEntry.put(columnNames[i], results.getString(columnNames[i]));
                }

                // Push the result into our list
                parsedResults.add(nextEntry);
            }

            // Close our statement
            statement.close();

            // Return our results
            return parsedResults;

        } catch (SQLException ex) {
            this.errorMessage = ex.getMessage();
        }

        // If we made it here toss out a null value
        return null;
    }

    /**
     * Inserts a row into a table. Columns and fields should have a 1:1 relationship to ensure data is inserted correctly.
     * @param tableName The table where the data should be inserted.
     * @param columns The columns that should have data inserted.
     * @param fields The data that should be inserted into the columns.
     * @return True if operations was successful, false if it was not.
     */
    public boolean insertEntry(TableNames tableName, String[] columns, String[] fields) {
        try {
            // Create our string builder and begin creation of the SQL query
            StringBuilder builder = new StringBuilder("INSERT INTO ");

            // Add in our table name
            builder.append(getTableNameFromEnum(tableName));

            // Append our actual insert string
            builder.append(buildInsertString(columns, fields));

            // Create our statement
            PreparedStatement statement = this.databaseConnection.prepareStatement(builder.toString());

            // Perform our action
            int results = statement.executeUpdate();

            // Close our connection
            statement.close();

            // And then return whether we were successful
            return results > 0;
        } catch (SQLException ex) {
            this.errorMessage = ex.getMessage();
            return false;
        }
    }

    /**
     * Updates an entry in a table. Columns and fields should have a 1:1 relationship to ensure data is updated correctly.
     * @param tableName The table where the data should be updated.
     * @param columns The columns that should have data updated.
     * @param fields The data that should be updated into the columns.
     * @param whereClause The where clause that specifies what row(s) should be affected.
     * @return True if operation was successful, false if it was not.
     */
    public boolean updateEntry(TableNames tableName, String[] columns, String[] fields, String whereClause) {
        try {
            // Create our string builder and grab the basic info
            StringBuilder builder = new StringBuilder("UPDATE ");

            // Get the table name
            builder.append(getTableNameFromEnum(tableName));

            // Add our set...
            builder.append(" SET ");

            // Then go ahead and actually create the bulk of our update string
            builder.append(buildUpdateString(columns, fields));

            // Finally add where we're updating
            builder.append(" WHERE ");
            builder.append(whereClause);

            // Create our statement
            PreparedStatement statement = this.databaseConnection.prepareStatement(builder.toString());

            // Perform our action
            int results = statement.executeUpdate();

            // Close our statement
            statement.close();

            // Return the result
            return results > 0;
        } catch (SQLException ex) {
            this.errorMessage = ex.getMessage();
            return false;
        }
    }

    /**
     * Deletes one or more rows from a table.
     * @param tableName The table where the data should be removed.
     * @param whereClause The where clause that specifies what rows should be affected.
     * @return True if the operation was successful, false if not.
     */
    public boolean deleteEntry(TableNames tableName, String whereClause) {
        try {
            // Create our string builder
            StringBuilder builder = new StringBuilder("DELETE FROM ");

            // Grab our table name
            builder.append(getTableNameFromEnum(tableName));

            // If we have a where clause let's append that as well
            if (whereClause.length() > 0) {
                builder.append(" WHERE ");
                builder.append(whereClause);
            }

            // Create our statement
            PreparedStatement statement = this.databaseConnection.prepareStatement(builder.toString());

            // Execute our query and return the result
            int results = statement.executeUpdate();

            // Close
            statement.close();

            // Return the result
            return results > 0 ? true : false;
        } catch (SQLException ex) {
            this.errorMessage = ex.getMessage();
            return false;
        }
    }

    /**
     * Builds an insert string from two arrays. The arrays should have a 1:1 relationship to ensure that the string is created correctly.
     * @param columns The array that holds the columns that should be inserted into the table.
     * @param fields The array that holds the fields that should be inserted into the table.
     * @return A formatted string generated from the two arguments. Will look something like (column1, column2) VALUES (field1, field2)
     */
    private String buildInsertString(String[] columns, String[] fields) {
        // Create our string builders
        StringBuilder columnBuilder = new StringBuilder("(");
        StringBuilder fieldBuilder = new StringBuilder("(");

        // Loop through all the columns and values
        for (int i = 0; i < columns.length; i++) {
            columnBuilder.append(columns[i]);

            fieldBuilder.append("'");
            fieldBuilder.append(fields[i]);

            columnBuilder.append(", ");
            fieldBuilder.append("', ");
        }

        // Replace our trailing characters with a ), as well as VALUES for the columnbuilder
        columnBuilder.replace(columnBuilder.length() - 2, columnBuilder.length(), ") VALUES ");
        fieldBuilder.replace(fieldBuilder.length() -2, fieldBuilder.length(), ")");

        // Add the fieldbuilder to the column builder, so it should now look something like (column1, column2) VALUES (field1, field2)
        columnBuilder.append(fieldBuilder);

        // Return our string
        return columnBuilder.toString();
    }

    /**
     * Builds an update string from two arrays. The arrays should have a 1:1 relationship to ensure that the string is created correctly.
     * @param columns The array that holds the columns that should be updated into the table.
     * @param fields The array that holds the fields that should be updated into the table.
     * @return A formatted string generated from the two arguments. Will look something like (column1='field1', column2='field2')
     */
    private String buildUpdateString(String[] columns, String[] fields) {
        // Create a stringbuilder
        StringBuilder builder = new StringBuilder();

        // Loop through and append all the values
        for (int i = 0; i < columns.length; i++) {
            builder.append(columns[i]);
            builder.append("='");
            builder.append(fields[i]);
            builder.append("', ");
        }

        // Remove the trailing comma
        builder.delete(builder.length() -2, builder.length());

        // Return our string
        return builder.toString();
    }

    /**
     * Closes the database connection and frees up resources. Should be called whenever there is a successful connection to a database.
     */
    public void closeConnection() {
        try {
            // Make sure that we're not null or already closed
            if (this.databaseConnection != null && !this.databaseConnection.isClosed()) {
                this.databaseConnection.close();
                this.databaseConnection = null;
            }
        } catch (SQLException ex) {
            // Grab whatever error was thrown
            this.errorMessage = ex.getMessage();
        }
    }

    /**
     * Returns the last encountered MySQL exception error. Used for debugging.
     * @return A string of the last encountered MySQL generated error.
     */
    public String getDatabaseErrorMessage() {
        return this.errorMessage;
    }
}
