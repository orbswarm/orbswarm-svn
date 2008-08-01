package com.orbswarm.swarmcon;

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.Map.Entry;

/** The orb message class. */

public class Message extends Properties
{
    /** Known message types. */

    enum Type
    {
      POSITION_REPORT("p"), INFO_REPORT("i"), UNKNOWN("");

      private String token = null;

      Type(String token)
      {
        this.token = token;
      }

      public static Type getType(String token)
      {
        for (Type type: Type.values())
          if (type.token.equalsIgnoreCase(token))
            return type;

        return UNKNOWN;
      }

      public String toString()
      {
        return token;
      }
    };

    /** Known message fields. */

    enum Field
    {
      EASTING("e"), NORTHING("n"), UNKNOWN("");

      private String token = null;

      Field(String token)
      {
        this.token = token;
      }

      public static Field getField(String token)
      {
        for (Field field: Field.values())
          if (field.token.equalsIgnoreCase(token))
            return field;

        return UNKNOWN;
      }

      public String toString()
      {
        return token;
      }
    };

    /** The message type. */

    private Type type;

    /** The idenifier of the sender of the messsage. */

    private int senderId;

    /** Construct a message from given message text. 
     * 
     * @param message the message text
     */

    public Message(String message)
    {
      parseMessage(message);
    }
        
    /** Get the message type.
     *
     * @return the message type.
     */

    public Type getType()
    {
      return type;
    }

    /** Get the id of message sender.  For orbs that currently an number
     * between 60 and 65.
     *
     * @return the message senderId.
     */

    public int getSenderId()
    {
      return senderId;
    }

    // parser in a message 

    private void parseMessage(String message) 
    {
      StringTokenizer t = new StringTokenizer(message, " @\t\n\r\f={}");

      // get the sender id

      if (!t.hasMoreTokens())
        throw new Error("No sender id specified in message: " + message);
      senderId = Integer.valueOf(t.nextToken());

      // get the message type

      if (!t.hasMoreTokens())
        throw new Error("No type specified in message: " + message);
      type = Type.getType(t.nextToken());

      // loop through the remaining tokens and parse them in as
      // properties

      while (t.hasMoreTokens())
      {
        // get the property

        String key = t.nextToken();

        // get the value
            
        if (!t.hasMoreTokens())
          throw new Error(
            "Property \"" + key + 
            "\" has no value in message: " + message);
        String value = t.nextToken();
            
        // set property value
            
        setProperty(key, value);
      }
    }

    /** {@inheritDoc} */
        
    @Override
    public String toString()
    {
      StringBuffer buf = new StringBuffer("@" + senderId + " " + type);
          
      for (Entry<Object,Object> entry: entrySet())
        buf.append(" " + entry.getKey() + "=" + entry.getValue());
          
      return buf.toString();
    }

    /** Get string property.
     *
     * @param name the name of the property
     *
     * @return the string value of the property
     */ 

    public String getStringProperty(String name)
    {
      return (String)getProperty(name);
    }

    /** Get double property.
     *
     * @param name the name of the property
     *
     * @return the double value of the property
     */ 

    public Double getDoubleProperty(String name)
    {
      return Double.valueOf((String)getProperty(name));
    }

    /** Get double property.
     *
     * @param field the name of the property
     *
     * @return the double value of the property
     */ 

    public Double getDoubleProperty(Field field)
    {
      return getDoubleProperty(field.toString());
    }
}
